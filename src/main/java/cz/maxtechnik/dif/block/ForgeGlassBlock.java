package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.ForgeControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Forge Glass — sklo vrstva Forge pece.
 *
 * Extends TransparentBlock (Minecraft 1.21.1 — GlassBlock/AbstractGlassBlock
 * were merged into HalfTransparentBlock → TransparentBlock).
 * skipRendering is inherited from HalfTransparentBlock and automatically
 * hides internal faces between adjacent ForgeGlassBlock blocks.
 */
public class ForgeGlassBlock extends TransparentBlock {

    public ForgeGlassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // skipRendering inherited from HalfTransparentBlock:
    //   adjacentBlockState.is(this) → hides shared face between two ForgeGlass blocks

    @Override
    protected void onRemove(
            BlockState blockState,
            @NotNull Level level,
            @NotNull BlockPos pos,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!blockState.is(newState.getBlock()) && !level.isClientSide) {
            notifyNearbyController(level, pos);
        }
        super.onRemove(blockState, level, pos, newState, movedByPiston);
    }

    @Override
    protected void onPlace(
            @NotNull BlockState blockState,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull BlockState oldState,
            boolean movedByPiston
    ) {
        super.onPlace(blockState, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            notifyNearbyController(level, pos);
        }
    }

    /**
     * Searches for a ForgeControllerBlockEntity below this position
     * (max MAX_GLASS_LAYERS + 2 blocks down) and forces revalidation.
     */
    private void notifyNearbyController(Level level, BlockPos glassPos) {
        int searchDepth = cz.maxtechnik.dif.util.ForgeMultiblockHelper.MAX_GLASS_LAYERS + 2;
        for (int dy = 1; dy <= searchDepth; dy++) {
            int y = glassPos.getY() - dy;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos check = new BlockPos(glassPos.getX() + dx, y, glassPos.getZ() + dz);
                    if (level.getBlockEntity(check) instanceof ForgeControllerBlockEntity ctrl) {
                        ctrl.forceValidation = true;
                        return;
                    }
                }
            }
        }
    }
}