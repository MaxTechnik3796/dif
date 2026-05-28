package cz.maxtechnik.dif.block;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import cz.maxtechnik.dif.block.entity.EngineBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Engine extends KineticBlock implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public Engine(Properties properties) {
		super(properties.noOcclusion());
		registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState blockState) {
		return new EngineBlockEntity(pos, blockState);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
			@NotNull Level level, @NotNull BlockState blockState, @NotNull BlockEntityType<T> type) {
		if (type != DifModBlockEntities.ENGINE.get()) return null;
		return (lvl, pos, state, be) -> {
			if (be instanceof EngineBlockEntity engine) engine.tick();
		};
	}

	@Override
	public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public Direction.Axis getRotationAxis(BlockState blockState) {
		return blockState.getValue(FACING).getAxis();
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState blockState, Direction face) {
		return face.getAxis() == getRotationAxis(blockState);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
	}

	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState blockState, @NotNull BlockGetter world,
	                                          @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return Shapes.empty();
	}

	/** Soused se změnil → engine má okamžitě přeskanovat. */
	@Override
	public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
	                            @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean isMoving) {
		super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
		if (!level.isClientSide && level.getBlockEntity(pos) instanceof EngineBlockEntity engine) {
			engine.markDirty();
		}
	}
}