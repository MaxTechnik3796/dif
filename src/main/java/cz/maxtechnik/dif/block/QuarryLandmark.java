package cz.maxtechnik.dif.block;

import com.mojang.serialization.MapCodec;
import cz.maxtechnik.dif.block.entity.QuarryLandmarkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
@SuppressWarnings("deprecation")
public class QuarryLandmark extends BaseEntityBlock{
	public static final MapCodec<QuarryLandmark> CODEC=simpleCodec(properties->new QuarryLandmark());
	@Override
	protected @NotNull MapCodec<? extends BaseEntityBlock> codec(){
		return CODEC;
	}
	public static final DirectionProperty FACING=BlockStateProperties.FACING;
	protected static final VoxelShape UP_SHAPE=Block.box(6D,0D,6D,10D,10D,10D);
	protected static final VoxelShape DOWN_SHAPE=Block.box(6D,6D,6D,10D,16D,10D);
	protected static final VoxelShape NORTH_SHAPE=Block.box(6D,6D,6D,10D,10D,16D);
	protected static final VoxelShape SOUTH_SHAPE=Block.box(6D,6D,0D,10D,10D,10D);
	protected static final VoxelShape WEST_SHAPE=Block.box(6D,6D,6D,16D,10D,10D);
	protected static final VoxelShape EAST_SHAPE=Block.box(0D,6D,6D,10D,10D,10D);
	public QuarryLandmark(){
		super(Properties.of().strength(0.5F,0.5F).sound(SoundType.WOOD).noCollission().noOcclusion().pushReaction(PushReaction.BLOCK).lightLevel(state->14));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.UP));
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return this.defaultBlockState().setValue(FACING,context.getClickedFace());
	}
	@Override
	public @NotNull VoxelShape getShape(BlockState blockState,@NotNull BlockGetter level,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return switch(blockState.getValue(FACING)){
			case DOWN -> DOWN_SHAPE;
			case NORTH -> NORTH_SHAPE;
			case SOUTH -> SOUTH_SHAPE;
			case WEST -> WEST_SHAPE;
			case EAST -> EAST_SHAPE;
			default -> UP_SHAPE;
		};
	}
	@Override
	public @NotNull BlockState rotate(BlockState blockState,Rotation rotation){
		return blockState.setValue(FACING,rotation.rotate(blockState.getValue(FACING)));
	}
	@Override
	public @NotNull BlockState mirror(BlockState blockState,Mirror mirror){
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}
	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState blockState){
		return RenderShape.MODEL;
	}
	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return new QuarryLandmarkBlockEntity(pos,blockState);
	}
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState blockState,@NotNull BlockEntityType<T> type){
		return null;
	}
	@Override
	public @NotNull InteractionResult useWithoutItem(@NotNull BlockState blockState,@NotNull Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull BlockHitResult hit){
		if(!level.isClientSide&&level.getBlockEntity(pos) instanceof QuarryLandmarkBlockEntity blockEntity)
			blockEntity.onRightClick(player);
		return InteractionResult.sidedSuccess(level.isClientSide);
	}
	@Override
	public void onRemove(@NotNull BlockState blockState,@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState newState,boolean moving){
		if(!blockState.is(newState.getBlock())&&!level.isClientSide)
			if(level.getBlockEntity(pos) instanceof QuarryLandmarkBlockEntity lmEntity) lmEntity.onRemoved();
		super.onRemove(blockState,level,pos,newState,moving);
	}
}