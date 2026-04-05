package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.CameraBlock;
import cz.maxtechnik.dif.block.MonitorBlock;
import cz.maxtechnik.dif.client.ClientCameraHandler;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.MonitorState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
public class MonitorBlockEntity extends BlockEntity {
    private BlockPos linkedCameraPos = null;

    public MonitorBlockEntity(BlockPos pos, BlockState state) {
        super(DifModBlockEntities.MONITOR.get(), pos, state); // Tady doplň svůj BlockEntityType!
    }

    public void linkCamera(BlockPos camPos) {
        this.linkedCameraPos = camPos;
        if (level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(MonitorBlock.STATE, MonitorState.INACTIVE), 3);
        }
        setChanged();
    }

	public InteractionResult useMonitor(Player player) {
		if (level == null) return InteractionResult.PASS;

		if (linkedCameraPos == null) {
			player.displayClientMessage(Component.literal("No camera linked!"), true);
			return InteractionResult.FAIL;
		}

		// KONTROLA EXISTENCE:
		if (!(level.getBlockState(linkedCameraPos).getBlock() instanceof CameraBlock)) {
			this.linkedCameraPos = null; // Zrušíme link
			level.setBlock(worldPosition, getBlockState().setValue(MonitorBlock.STATE, MonitorState.NO_SIGNAL), 3);
			player.displayClientMessage(Component.literal("Link lost! Camera was destroyed."), true);
			return InteractionResult.FAIL;
		}

		if (level.isClientSide) {
			ClientCameraHandler.enterCamera(linkedCameraPos);
		} else {
			level.setBlock(worldPosition, getBlockState().setValue(MonitorBlock.STATE, MonitorState.ACTIVE), 3);
		}
		return InteractionResult.SUCCESS;
	}

    public void setInactive() {
        if (level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(MonitorBlock.STATE, MonitorState.INACTIVE), 3);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("CamX")) {
            this.linkedCameraPos = new BlockPos(tag.getInt("CamX"), tag.getInt("CamY"), tag.getInt("CamZ"));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (linkedCameraPos != null) {
            tag.putInt("CamX", linkedCameraPos.getX());
            tag.putInt("CamY", linkedCameraPos.getY());
            tag.putInt("CamZ", linkedCameraPos.getZ());
        }
    }
}