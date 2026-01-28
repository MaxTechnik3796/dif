package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.PortalBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PortalBlock extends BaseEntityBlock {
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final DirectionProperty EXTENSION_DIR = DirectionProperty.create("extension_dir", Direction.values());
	public static final BooleanProperty IS_BLUE = BooleanProperty.create("is_blue");

	// Velmi tenké hitboxy pro detekci kolize (0.5 pixelu)
	private static final VoxelShape NORTH_SHAPE = Block.box(0, 0, 15.5, 16, 16, 16);
	private static final VoxelShape SOUTH_SHAPE = Block.box(0, 0, 0, 16, 16, 0.5);
	private static final VoxelShape WEST_SHAPE = Block.box(15.5, 0, 0, 16, 16, 16);
	private static final VoxelShape EAST_SHAPE = Block.box(0, 0, 0, 0.5, 16, 16);
	private static final VoxelShape UP_SHAPE = Block.box(0, 0, 0, 16, 0.5, 16);
	private static final VoxelShape DOWN_SHAPE = Block.box(0, 15.5, 0, 16, 16, 16);

	public PortalBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(HALF, DoubleBlockHalf.LOWER)
				.setValue(FACING, Direction.NORTH)
				.setValue(EXTENSION_DIR, Direction.UP)
				.setValue(IS_BLUE, true));
	}

	@Override
	public void neighborChanged(@NotNull BlockState state,Level level,@NotNull BlockPos pos,@NotNull Block block,@NotNull BlockPos fromPos,boolean isMoving) {
		if (!level.isClientSide) {
			Direction backDir = state.getValue(FACING).getOpposite();
			if (pos.relative(backDir).equals(fromPos) && level.isEmptyBlock(fromPos)) {
				level.destroyBlock(pos, false);
			}
		}
	}

	public @NotNull VoxelShape getShape(BlockState state,@NotNull BlockGetter level,@NotNull BlockPos pos,@NotNull CollisionContext context) {
		Direction facing = state.getValue(FACING);
		// 0.15 pixelu tloušťka, aby to "obolilo" 0.1 pixelu tlustý model a neprosvítalo to
		return switch (facing) {
			case NORTH -> Block.box(0, 0, 15.9, 16, 16, 16.1);
			case SOUTH -> Block.box(0, 0, -0.1, 16, 16, 0.1);
			case WEST ->  Block.box(15.9, 0, 0, 16.1, 16, 16);
			case EAST ->  Block.box(-0.1, 0, 0, 0.1, 16, 16);
			case UP ->    Block.box(0, -0.1, 0, 16, 0.1, 16);
			case DOWN ->  Block.box(0, 15.9, 0, 16, 16.1, 16);
		};
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HALF, FACING, EXTENSION_DIR, IS_BLUE);
	}

	@Override
	public @NotNull BlockState updateShape(BlockState state,@NotNull Direction facing,@NotNull BlockState facingState,@NotNull LevelAccessor world,@NotNull BlockPos pos,@NotNull BlockPos facingPos) {
		DoubleBlockHalf half = state.getValue(HALF);
		Direction ext = state.getValue(EXTENSION_DIR);
		if ((facing == ext && half == DoubleBlockHalf.LOWER) || (facing == ext.getOpposite() && half == DoubleBlockHalf.UPPER)) {
			return facingState.is(this) ? state : Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, facing, facingState, world, pos, facingPos);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new PortalBlockEntity(pos, state) : null;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,@NotNull BlockState state,@NotNull BlockEntityType<T> type) {
		return !level.isClientSide && state.getValue(HALF) == DoubleBlockHalf.LOWER
				? createTickerHelper(type, (BlockEntityType<PortalBlockEntity>) DifModBlockEntities.PORTAL.get(), PortalBlockEntity::tick) : null;
	}

	@Override public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.MODEL; }
}