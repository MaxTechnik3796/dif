package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.ForgeFurnaceController;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity pro Forge Brick.
 *
 * Uchovává integritu briku (0–100).
 * Integrita klesá při:
 *   - Tavení endgame materiálů bez dostatečného chlazení
 *   - Dlouhodobém Superheated provozu (volitelné, nastavíš v configu)
 *
 * Pod 20 % → controller zobrazí varování v goggle tooltipu.
 * Na 0 %   → brik "praskne" (zavolá onRemove → controller se rozloží).
 *
 * Integrita se opravuje speciálním itemem (Forge Repair Kit) —
 * klikneš na brik a doplní integritu.
 */
public class ForgeBrickBlockEntity extends AbstractMultiblockBrickBlockEntity {

    /** Aktuální integrita 0–100. */
    private int integrity = 100;

    public ForgeBrickBlockEntity(BlockPos pos, BlockState blockState) {
        super(DifModBlockEntities.FORGE_BRICK.get(), pos, blockState);
    }

    // ── Integrita ─────────────────────────────────────────────────────────────

    public int getIntegrity() {
        return integrity;
    }

    public void setIntegrity(int value) {
        int clamped = Math.max(0, Math.min(100, value));
        if (this.integrity == clamped) return;
        this.integrity = clamped;
        setChanged();

        // Prasknutí — odstraň blok ze světa, to spustí onRemove → controller se rozloží
        if (this.integrity <= 0 && level != null && !level.isClientSide) {
            level.destroyBlock(worldPosition, false);
        }
    }

    /** Sníží integritu o {@code amount}. Bezpečně clampuje na 0. */
    public void degradeIntegrity(int amount) {
        setIntegrity(this.integrity - amount);
    }

    /** Vrátí true pokud je integrita pod hranicí varování (20 %). */
    public boolean isWarningLevel() {
        return integrity <= 20;
    }

    // ── AbstractMultiblockBrickBlockEntity ────────────────────────────────────

    @Override
    protected @Nullable AbstractMultiblockControllerBlockEntity<?> resolveController(BlockPos pos) {
        if (level == null) return null;
        var state = level.getBlockState(pos);
        // Ověří že na dané pozici je skutečně zformovaný ForgeFurnace controller
        if (state.hasProperty(ForgeFurnaceController.FORMED)
                && state.getValue(ForgeFurnaceController.FORMED)
                && level.getBlockEntity(pos) instanceof ForgeControllerBlockEntity ctrl) {
            return ctrl;
        }
        return null;
    }

    @Override
    protected String getGoggleDisplayName() {
        return "◆ Forge Furnace";
    }

    @Override
    protected ChatFormatting getGoggleNameColor() {
        return ChatFormatting.GOLD;
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("integrity", integrity);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        integrity = tag.contains("integrity") ? tag.getInt("integrity") : 100;
    }
}