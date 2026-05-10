package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.PortalBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PortalBlock extends BaseEntityBlock{
	public static final EnumProperty<DoubleBlockHalf> HALF=BlockStateProperties.DOUBLE_BLOCK_HALF;
	public static final DirectionProperty FACING=BlockStateProperties.FACING;
	public static final DirectionProperty EXTENSION_DIR=DirectionProperty.create("extension_dir",Direction.values());
	public static final BooleanProperty IS_BLUE=BooleanProperty.create("is_blue");
	public static final BooleanProperty IS_LINKED=BooleanProperty.create("is_linked");

	public PortalBlock(Properties p){
		super(p);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(HALF,DoubleBlockHalf.LOWER)
				.setValue(FACING,Direction.NORTH)
				.setValue(EXTENSION_DIR,Direction.UP)
				.setValue(IS_BLUE,true)
				.setValue(IS_LINKED,false));
	}

	@Override
	public void neighborChanged(@NotNull BlockState state,Level level,@NotNull BlockPos pos,@NotNull Block block,@NotNull BlockPos fromPos,boolean isMoving){
		if(!level.isClientSide){
			Direction face=state.getValue(FACING);
			if(pos.relative(face.getOpposite()).equals(fromPos)&&level.isEmptyBlock(fromPos))
				level.destroyBlock(pos,false);
		}
	}

	@Override
	public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos){
		return true;
	}

	@Override
	public int getLightBlock(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos){
		return 0;
	}

	@Override
	public @NotNull com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec(){
		return net.minecraft.world.level.block.state.BlockBehaviour.simpleCodec(PortalBlock::new);
	}

	@Override
	public @NotNull VoxelShape getShape(BlockState state,@NotNull BlockGetter level,@NotNull BlockPos pos,@NotNull CollisionContext ctx){
		return switch(state.getValue(FACING)){
			case NORTH -> Block.box(0,0,15.9,16,16,16.1);
			case SOUTH -> Block.box(0,0,-0.1,16,16,0.1);
			case WEST -> Block.box(15.9,0,0,16.1,16,16);
			case EAST -> Block.box(-0.1,0,0,0.1,16,16);
			case UP -> Block.box(0,-0.1,0,16,0.1,16);
			case DOWN -> Block.box(0,15.9,0,16,16.1,16);
		};
	}
	@Override
	public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean moving){
		if(!state.is(newState.getBlock())&&!level.isClientSide){
			if(state.getValue(HALF)==DoubleBlockHalf.LOWER){
				if(level.getBlockEntity(pos) instanceof PortalBlockEntity be&&level instanceof ServerLevel sl){
					PortalBlockEntity.removeOldPortal(sl, be.getOwner(), be.isBlue());
				}
			}
		}
		super.onRemove(state, level, pos, newState, moving);
	}

	@Override
	public @NotNull BlockState updateShape(BlockState state,@NotNull Direction dir,@NotNull BlockState adj,@NotNull LevelAccessor world,@NotNull BlockPos pos,@NotNull BlockPos adjPos){
		DoubleBlockHalf half=state.getValue(HALF);
		Direction ext=state.getValue(EXTENSION_DIR);
		if((dir==ext&&half==DoubleBlockHalf.LOWER)||(dir==ext.getOpposite()&&half==DoubleBlockHalf.UPPER)){
			return adj.is(this)?state:Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state,dir,adj,world,pos,adjPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){
		b.add(HALF,FACING,EXTENSION_DIR,IS_BLUE,IS_LINKED);
	}

	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState state){
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos p,BlockState s){
		return s.getValue(HALF)==DoubleBlockHalf.LOWER?new PortalBlockEntity(p,s):null;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,@NotNull BlockState s,@NotNull BlockEntityType<T> t){
		return !l.isClientSide&&s.getValue(HALF)==DoubleBlockHalf.LOWER
				?createTickerHelper(t,DifModBlockEntities.PORTAL.get(),PortalBlockEntity::tick)
				:null;
	}
}