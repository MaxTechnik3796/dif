package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import cz.maxtechnik.dif.block.CokeOvenController;
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

public class CokeOvenBlockEntity extends BlockEntity implements IHaveGoggleInformation {

	/** Which controller this brick belongs to, null if not part of any formed structure. */
	@Nullable
	private BlockPos controllerPos = null;

	public CokeOvenBlockEntity(BlockPos pos, BlockState blockState) {
		super(DifModBlockEntities.COKE_OVEN.get(), pos, blockState);
	}

	// ── Controller ownership ────────────────────────────────────────────

	public @Nullable BlockPos getControllerPos() {
		return controllerPos;
	}

	/** Called by controller when forming – claims this brick. */
	public void setControllerPos(@Nullable BlockPos pos) {
		this.controllerPos = pos;
		setChanged();
		if (level != null && !level.isClientSide)
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	/** True if this brick is unclaimed OR already owned by the given controller. */
	public boolean canBeClaimedBy(BlockPos claimerPos) {
		return controllerPos == null || controllerPos.equals(claimerPos);
	}

	// ── Controller lookup (for goggle delegation) ───────────────────────

	public @Nullable CokeOvenControllerBlockEntity getFormedController() {
		if (level == null) return null;
		if (controllerPos == null) return null;
		if (level.getBlockEntity(controllerPos) instanceof CokeOvenControllerBlockEntity ctrl) {
			BlockState state = level.getBlockState(controllerPos);
			if (state.hasProperty(CokeOvenController.FORMED) && state.getValue(CokeOvenController.FORMED)) {
				return ctrl;
			}
		}
		return null;
	}

	// ── Goggle tooltip ──────────────────────────────────────────────────

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		CokeOvenControllerBlockEntity controller = getFormedController();
		if (controller != null) {
			return controller.addToGoggleTooltip(tooltip, isPlayerSneaking);
		}
		tooltip.add(Component.literal("◆ Coke Oven").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
		tooltip.add(Component.literal(" Structure is NOT formed!").withStyle(ChatFormatting.RED));
		return true;
	}

	// ── NBT ────────────────────────────────────────────────────────────

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		if (controllerPos != null) {
			tag.putIntArray("controllerPos", new int[]{controllerPos.getX(), controllerPos.getY(), controllerPos.getZ()});
		}
	}

	@Override
	protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		if (tag.contains("controllerPos")) {
			int[] c = tag.getIntArray("controllerPos");
			controllerPos = (c.length == 3) ? new BlockPos(c[0], c[1], c[2]) : null;
		} else {
			controllerPos = null;
		}
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider) {
		return this.saveWithFullMetadata(provider);
	}
}
