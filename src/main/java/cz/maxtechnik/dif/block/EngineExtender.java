package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.EngineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class EngineExtender extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty TOP = BooleanProperty.create("top");

	public EngineExtender(Properties properties) {
		super(properties.noOcclusion());
		registerDefaultState(stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(TOP, false));
	}

	@Override
	public int getLightBlock(@NotNull BlockState blockState, @NotNull BlockGetter worldIn, @NotNull BlockPos pos) {
		return 0;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, TOP);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		final Level level = context.getLevel();
		final Direction clicked = context.getClickedFace();
		final BlockPos pos = context.getClickedPos();

		if (clicked == Direction.UP) {
			// Umístění shora: pokud je pod ním Engine, převzít jeho FACING
			Direction facing = level.getBlockState(pos.below()).getBlock() instanceof Engine
					? level.getBlockState(pos.below()).getValue(Engine.FACING)
					: context.getHorizontalDirection();
			return defaultBlockState().setValue(FACING, facing).setValue(TOP, true);
		}

		Direction facing = (clicked == Direction.DOWN)
				? context.getHorizontalDirection().getOpposite()
				: clicked;
		return defaultBlockState().setValue(FACING, facing).setValue(TOP, false);
	}

	@Override
	public @NotNull BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	public @NotNull BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	public float getShadeBrightness(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos pos) {
		return 1F;
	}

	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState blockState, @NotNull BlockGetter world,
	                                          @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return Shapes.empty();
	}

	/**
	 * Při odebrání extenderu (vč. /setblock pokud volá onRemove) probudit okolní enginy.
	 * Engine v každém ze 4 horizontálních směrů + pod blokem je potenciálně dotčený.
	 */
	@Override
	public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos,
	                     BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			notifyAdjacentEngines(level, pos);
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	private static void notifyAdjacentEngines(Level level, BlockPos pos) {
		// Engine může být pod (extender shora), nebo ve 4 horizontálních směrech (extender ze strany)
		markIfEngine(level, pos.below());
		markIfEngine(level, pos.north());
		markIfEngine(level, pos.south());
		markIfEngine(level, pos.east());
		markIfEngine(level, pos.west());
	}

	private static void markIfEngine(Level level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof EngineBlockEntity engine) {
			engine.markDirty();
		}
	}
}