package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.QuarryLandmarkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class QuarryLandmark extends BaseEntityBlock {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	
	protected static final VoxelShape UP_SHAPE = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 10.0D, 10.0D);
	protected static final VoxelShape DOWN_SHAPE = Block.box(6.0D, 6.0D, 6.0D, 10.0D, 16.0D, 10.0D);
	protected static final VoxelShape NORTH_SHAPE = Block.box(6.0D, 6.0D, 6.0D, 10.0D, 10.0D, 16.0D);
	protected static final VoxelShape SOUTH_SHAPE = Block.box(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 10.0D);
	protected static final VoxelShape WEST_SHAPE = Block.box(6.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D);
	protected static final VoxelShape EAST_SHAPE = Block.box(0.0D, 6.0D, 6.0D, 10.0D, 10.0D, 10.0D);

	public QuarryLandmark() {
		super(Properties.of().strength(0.5F, 0.5F).sound(SoundType.WOOD).noCollission().noOcclusion().lightLevel(state -> 14));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getClickedFace());
	}

	@Override
	public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case DOWN -> DOWN_SHAPE;
			case NORTH -> NORTH_SHAPE;
			case SOUTH -> SOUTH_SHAPE;
			case WEST -> WEST_SHAPE;
			case EAST -> EAST_SHAPE;
			default -> UP_SHAPE;
		};
	}

	@Override
	public @NotNull BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public @NotNull BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new QuarryLandmarkBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
		return null;
	}

	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		if (!level.isClientSide && level.getBlockEntity(pos) instanceof QuarryLandmarkBlockEntity lmEntity)
			lmEntity.onRightClick(player);
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean moving) {
		if (!state.is(newState.getBlock()) && !level.isClientSide)
			if (level.getBlockEntity(pos) instanceof QuarryLandmarkBlockEntity lmEntity) lmEntity.onRemoved();
		super.onRemove(state, level, pos, newState, moving);
	}
}