package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import cz.maxtechnik.dif.block.BlastSmelteryController;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static cz.maxtechnik.dif.DifMod.goggleTooltipFix;

public class BlastSmelteryBlockEntity extends BlockEntity implements IHaveGoggleInformation {
    @Nullable
    private BlockPos controllerPos = null;

    public BlastSmelteryBlockEntity(BlockPos pos, BlockState blockState) {
        super(DifModBlockEntities.BLAST_SMELTERY.get(), pos, blockState);
    }

    public @Nullable BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerPos(@Nullable BlockPos pos) {
        if ((controllerPos == null && pos == null) || (controllerPos != null && controllerPos.equals(pos))) return;
        this.controllerPos = pos;
        setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public boolean canBeClaimedBy(BlockPos claimerPos) {
        if (controllerPos == null || controllerPos.equals(claimerPos)) return true;
        if (level == null) return false;
        BlockState ownerState = level.getBlockState(controllerPos);
        if (!(level.getBlockEntity(controllerPos) instanceof BlastSmelteryControllerBlockEntity)
                || !ownerState.hasProperty(BlastSmelteryController.FORMED)
                || !ownerState.getValue(BlastSmelteryController.FORMED)) {
            controllerPos = null;
            setChanged();
            return true;
        }
        return false;
    }

    public @Nullable BlastSmelteryControllerBlockEntity getFormedController() {
        if (level == null || controllerPos == null) return null;
        BlockState blockState = level.getBlockState(controllerPos);
        if (blockState.hasProperty(BlastSmelteryController.FORMED)
                && blockState.getValue(BlastSmelteryController.FORMED)
                && level.getBlockEntity(controllerPos) instanceof BlastSmelteryControllerBlockEntity ctrl) {
            return ctrl;
        }
        return null;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        BlastSmelteryControllerBlockEntity controller = getFormedController();
        if (controller != null) return controller.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal(goggleTooltipFix + "◆ Blast Smeltery").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        tooltip.add(Component.literal(goggleTooltipFix + " Structure is NOT formed!").withStyle(ChatFormatting.RED));
        return true;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (controllerPos != null) tag.putIntArray("controllerPos", new int[]{controllerPos.getX(), controllerPos.getY(), controllerPos.getZ()});
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("controllerPos")) {
            int[] c = tag.getIntArray("controllerPos");
            controllerPos = (c.length == 3) ? new BlockPos(c[0], c[1], c[2]) : null;
        } else controllerPos = null;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider) {
        return saveWithFullMetadata(provider);
    }
}
