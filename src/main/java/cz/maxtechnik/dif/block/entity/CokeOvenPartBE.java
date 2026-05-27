package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Minimální marker BlockEntity pro 26 ne-master bloků Coke Ovenu.
 * Drží pouze jednu věc: pozici mastera.
 * Capability deleguje na mastera — registrováno v DifMod.
 */
public class CokeOvenPartBE extends BlockEntity {

    @Nullable private BlockPos masterPos = null;

    public CokeOvenPartBE(BlockPos pos, BlockState state) {
        super(DifModBlockEntities.COKE_OVEN_PART.get(), pos, state);
    }

    public void setMaster(@Nullable BlockPos masterPos) {
        this.masterPos = masterPos;
        setChanged();
    }

    @Nullable
    public BlockPos getMasterPos() { return masterPos; }

    @Nullable
    public CokeOvenBlockEntity getMaster() {
        if (level == null || masterPos == null) return null;
        var be = level.getBlockEntity(masterPos);
        return be instanceof CokeOvenBlockEntity m ? m : null;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider reg) {
        super.saveAdditional(tag, reg);
        if (masterPos != null) tag.put("m", NbtUtils.writeBlockPos(masterPos));
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider reg) {
        super.loadAdditional(tag, reg);
        masterPos = tag.contains("m") ? NbtUtils.readBlockPos(tag, "m").orElse(null) : null;
    }
}