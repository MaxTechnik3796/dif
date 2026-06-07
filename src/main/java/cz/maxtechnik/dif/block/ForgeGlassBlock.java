package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.ForgeControllerBlockEntity;
import cz.maxtechnik.dif.block.entity.ForgeGlassBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Forge Glass — sklo vrstva Forge pece.
 *
 * Extends TransparentBlock (Minecraft 1.21.1).
 * Implements EntityBlock so each glass block has a ForgeGlassBlockEntity
 * that stores the controller position, allowing the glass-side BER to
 * render fluid without requiring the controller to be in the camera frustum.
 */
public class ForgeGlassBlock extends TransparentBlock implements EntityBlock {

    public ForgeGlassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // ── EntityBlock ───────────────────────────────────────────────────────────

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ForgeGlassBlockEntity(pos, state);
    }

    // ── Placement / removal ───────────────────────────────────────────────────

    @Override
    protected void onRemove(
            BlockState blockState,
            @NotNull Level level,
            @NotNull BlockPos pos,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!blockState.is(newState.getBlock()) && !level.isClientSide) {
            // Clear controller reference in the block entity before it is removed
            if (level.getBlockEntity(pos) instanceof ForgeGlassBlockEntity gbe) {
                gbe.setControllerPos(null);
            }
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

    // ── Helper ────────────────────────────────────────────────────────────────

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