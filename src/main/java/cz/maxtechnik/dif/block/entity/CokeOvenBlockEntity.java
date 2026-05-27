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

	/** Pozice controlleru který tuto cihlu vlastní; null pokud cihla není součástí žádné struktury. */
	@Nullable
	private BlockPos controllerPos = null;

	public CokeOvenBlockEntity(BlockPos pos, BlockState blockState) {
		super(DifModBlockEntities.COKE_OVEN.get(), pos, blockState);
	}

	// ── Vlastnictví ─────────────────────────────────────────────────────

	public @Nullable BlockPos getControllerPos() {
		return controllerPos;
	}

	/** Voláno controllerem při formování/uvolnění cihly. */
	public void setControllerPos(@Nullable BlockPos pos) {
		if ((controllerPos == null && pos == null)
				|| (controllerPos != null && controllerPos.equals(pos))) return; // no-op
		this.controllerPos = pos;
		setChanged();
		if (level != null && !level.isClientSide) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	/**
	 * True pokud je cihla volná, nebo už ji vlastní právě dotazující controller,
	 * nebo pokud starý vlastník už neexistuje (rozbitý/odebraný přes /setblock).
	 * Tím zabráníme dead-locku v případě, že byl controller odstraněn bez updatu.
	 */
	public boolean canBeClaimedBy(BlockPos claimerPos) {
		if (controllerPos == null || controllerPos.equals(claimerPos)) return true;
		if (level == null) return false;
		// Ghost-controller fallback: vlastník už není formovaný controller
		BlockState ownerState = level.getBlockState(controllerPos);
		if (!(level.getBlockEntity(controllerPos) instanceof CokeOvenControllerBlockEntity)
				|| !ownerState.hasProperty(CokeOvenController.FORMED)
				|| !ownerState.getValue(CokeOvenController.FORMED)) {
			controllerPos = null; // self-heal
			setChanged();
			return true;
		}
		return false;
	}

	// ── Lookup controlleru (pro goggle delegaci) ────────────────────────

	public @Nullable CokeOvenControllerBlockEntity getFormedController() {
		if (level == null || controllerPos == null) return null;
		BlockState s = level.getBlockState(controllerPos);
		if (s.hasProperty(CokeOvenController.FORMED) && s.getValue(CokeOvenController.FORMED)
				&& level.getBlockEntity(controllerPos) instanceof CokeOvenControllerBlockEntity ctrl) {
			return ctrl;
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
			tag.putIntArray("controllerPos",
					new int[]{controllerPos.getX(), controllerPos.getY(), controllerPos.getZ()});
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
		return saveWithFullMetadata(provider);
	}
}