package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.CameraBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CameraBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public CameraBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Kamera bude koukat směrem od hráče při položení
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state) {
        return new CameraBlockEntity(pos, state);
    }
	@Override
	public void onRemove(BlockState state,@NotNull Level level,@NotNull BlockPos pos,BlockState newState,boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			// Tady je problém: Kamera neví, který monitor ji sleduje.
			// Nejlepší je, aby se ClientCameraHandler v každém ticku díval,
			// jestli na jeho pozici stále stojí CameraBlock.
			super.onRemove(state, level, pos, newState, isMoving);
		}
	}
}