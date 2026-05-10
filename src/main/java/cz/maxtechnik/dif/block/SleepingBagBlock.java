package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.SleepingBagBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SleepingBagBlock extends BaseEntityBlock{
	public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<BedPart> PART=BlockStateProperties.BED_PART;
	public static final BooleanProperty OCCUPIED=BlockStateProperties.OCCUPIED;
	protected static final VoxelShape SHAPE=Block.box(0D,0D,0D,16D,2D,16D);

	public SleepingBagBlock(){
		super(BlockBehaviour.Properties.of().sound(SoundType.WOOL).strength(0.2F).pushReaction(PushReaction.BLOCK).noOcclusion());
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING,Direction.NORTH)
				.setValue(PART,BedPart.FOOT)
				.setValue(OCCUPIED,false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING,PART,OCCUPIED);
	}

	@Override
	protected @NotNull com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec(){
		return net.minecraft.world.level.block.state.BlockBehaviour.simpleCodec(p->new SleepingBagBlock());
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx){
		Direction dir=ctx.getHorizontalDirection();
		BlockPos pos=ctx.getClickedPos();
		BlockPos headPos=pos.relative(dir);
		Level level=ctx.getLevel();
		if(level.getBlockState(headPos).canBeReplaced(ctx)&&level.getWorldBorder().isWithinBounds(headPos))
			return this.defaultBlockState().setValue(FACING,dir);
		return null;
	}

	@Override
	public void onPlace(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState oldState,boolean moving){
		if(state.getValue(PART)==BedPart.FOOT){
			BlockPos headPos=pos.relative(state.getValue(FACING));
			level.setBlock(headPos,state.setValue(PART,BedPart.HEAD),3);
		}
	}

	@Override
	public void onRemove(BlockState state,@NotNull Level level,@NotNull BlockPos pos,BlockState newState,boolean moving){
		if(!state.is(newState.getBlock())){
			BlockPos otherPos;
			if(state.getValue(PART)==BedPart.FOOT)
				otherPos=pos.relative(state.getValue(FACING));
			else
				otherPos=pos.relative(state.getValue(FACING).getOpposite());
			BlockState otherState=level.getBlockState(otherPos);
			if(otherState.is(this))
				level.removeBlock(otherPos,false);
		}
		super.onRemove(state,level,pos,newState,moving);
	}

	@Override
	public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull BlockHitResult hit){
		if(level.isClientSide) return InteractionResult.CONSUME;

		// Přejdi na HEAD
		if(state.getValue(PART)!=BedPart.HEAD){
			pos=pos.relative(state.getValue(FACING));
			state=level.getBlockState(pos);
			if(!state.is(this)) return InteractionResult.FAIL;
		}

		// Exploduje v Netheru / Endu
		if(!BedBlock.canSetSpawn(level)){
			level.explode(null,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,5.0F,Level.ExplosionInteraction.BLOCK);
			return InteractionResult.SUCCESS;
		}

		if(state.getValue(OCCUPIED)){
			player.displayClientMessage(net.minecraft.network.chat.Component.translatable("block.minecraft.bed.occupied"),true);
			return InteractionResult.SUCCESS;
		}

		player.startSleepInBed(pos).ifLeft(problem->{
			if(problem!=null&&problem.getMessage()!=null)
				player.displayClientMessage(problem.getMessage(),true);
		});
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean isBed(@NotNull BlockState state,@NotNull BlockGetter level,@NotNull BlockPos pos,@Nullable LivingEntity sleeper){
		return true;
	}

	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState state){
		return RenderShape.MODEL;
	}

	@Override
	public @NotNull VoxelShape getShape(@NotNull BlockState state,@NotNull BlockGetter level,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return SHAPE;
	}

	@Override
	public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state,@NotNull BlockGetter level,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return SHAPE;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return state.getValue(PART)==BedPart.HEAD?new SleepingBagBlockEntity(pos,state):null;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState state,@NotNull BlockEntityType<T> type){
		return null;
	}

	@Override
	protected @NotNull BlockState rotate(@NotNull BlockState state,@NotNull Rotation rot){
		return state.setValue(FACING,rot.rotate(state.getValue(FACING)));
	}

	@Override
	protected @NotNull BlockState mirror(@NotNull BlockState state,@NotNull Mirror mirror){
		return state.setValue(FACING,mirror.mirror(state.getValue(FACING)));
	}
}