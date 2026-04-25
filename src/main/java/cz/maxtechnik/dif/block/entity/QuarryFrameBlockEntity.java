package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class QuarryFrameBlockEntity extends BlockEntity {

    private BlockPos ownerPos = null;
    private boolean  isDying  = false;

    public QuarryFrameBlockEntity(BlockPos pos, BlockState state) {
        super(DifModBlockEntities.QUARRY_FRAME.get(), pos, state);
    }

    // Tik jen při isDying – jinak nulová zátěž
    public static void tick(Level level, BlockPos pos, QuarryFrameBlockEntity be) {
        if (!level.isClientSide && be.isDying) level.removeBlock(pos, false);
    }

    public void setOwner(BlockPos quarryPos) {
        if (ownerPos != null) return; // majitel již nastaven, ignorovat
        ownerPos = quarryPos;
        setChanged();
    }

    public void scheduleRemoval() { isDying = true; ownerPos = null; setChanged(); }

    public BlockPos getOwnerPos() { return ownerPos; }

    @Override protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (ownerPos != null) { tag.putInt("OwnX", ownerPos.getX()); tag.putInt("OwnY", ownerPos.getY()); tag.putInt("OwnZ", ownerPos.getZ()); }
        tag.putBoolean("Dying", isDying);
    }

    @Override public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("OwnX")) ownerPos = new BlockPos(tag.getInt("OwnX"), tag.getInt("OwnY"), tag.getInt("OwnZ"));
        isDying = tag.getBoolean("Dying");
    }
}