package cz.maxtechnik.dif.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class MegaTorchBlock extends Block {
	public static final BooleanProperty FORMED = BooleanProperty.create("formed");
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, 4);

	public MegaTorchBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FORMED, false).setValue(PART, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FORMED, PART);
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!level.isClientSide) checkMultiblock(level, pos);
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		if (!level.isClientSide) checkMultiblock(level, pos);
	}

	private void checkMultiblock(Level level, BlockPos pos) {
		if (!level.getBlockState(pos).is(this)) return;

		BlockPos bottom = findBottom(level, pos);
		int height = countHeight(level, bottom);
		boolean isValid = height == 5 && noHorizontalNeighbors(level, bottom);
		boolean isCurrentlyFormed = level.getBlockState(bottom).getValue(FORMED);

		if (isValid && !isCurrentlyFormed) {
			for (int i = 0; i < 5; i++) {
				level.setBlock(bottom.above(i), this.defaultBlockState().setValue(FORMED, true).setValue(PART, i), 3);
			}
			MegaTorchRegistry.addTorch(level, bottom); // Uložíme do paměti
		} else if (!isValid && isCurrentlyFormed) {
			for (int i = 0; i < height; i++) {
				BlockState s = level.getBlockState(bottom.above(i));
				if (s.is(this) && s.getValue(FORMED)) {
					level.setBlock(bottom.above(i), s.setValue(FORMED, false).setValue(PART, 0), 3);
				}
			}
			MegaTorchRegistry.removeTorch(level, bottom); // Smažeme z paměti
		}
	}

	private BlockPos findBottom(Level level, BlockPos pos) {
		BlockPos current = pos;
		while (level.getBlockState(current.below()).is(this)) {
			current = current.below();
		}
		return current;
	}

	private int countHeight(Level level, BlockPos bottom) {
		int height = 0;
		while (level.getBlockState(bottom.above(height)).is(this)) {
			height++;
		}
		return height;
	}

	private boolean noHorizontalNeighbors(Level level, BlockPos bottom) {
		for (int i = 0; i < 5; i++) {
			for (Direction dir : Direction.Plane.HORIZONTAL) {
				if (level.getBlockState(bottom.above(i).relative(dir)).is(this)) {
					return false;
				}
			}
		}
		return true;
	}
}