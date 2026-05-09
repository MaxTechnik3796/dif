package cz.maxtechnik.dif.gui.menu;

import cz.maxtechnik.dif.init.gui.DifModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SuperBoxMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
	public final Level world;
	public final Player entity;
	private ContainerLevelAccess access = ContainerLevelAccess.NULL;
	private IItemHandler internal;
	private final Map<Integer, Slot> customSlots = new HashMap<>();
	private BlockEntity boundBlockEntity = null;
	private final int containerSlotsCount;

	public SuperBoxMenu(int id, Inventory inv, BlockPos pos) {
		super(DifModMenus.SUPER_BOX.get(), id);
		this.entity = inv.player;
		this.world = inv.player.level();

		if (pos != null) {
			this.access = ContainerLevelAccess.create(world, pos);
			this.boundBlockEntity = world.getBlockEntity(pos);
			if (boundBlockEntity != null) {
				IItemHandler capability = world.getCapability(Capabilities.ItemHandler.BLOCK, pos, boundBlockEntity.getBlockState(), boundBlockEntity, null);
				if (capability != null) this.internal = capability;
			}
		}

		if (this.internal == null) {
			this.internal = new ItemStackHandler(231);
		}

		// 23 sloupců, 12 řádků
		// Řádky 0-6 (7 řádků): všechny sloty (7×23 = 161)
		// Řádky 7-11 (5 řádků): vynecháme cols 7-15 (9 slotů) → 5×14 = 70
		// Celkem: 231
		int[] xs = {5, 23, 41, 59, 77, 95, 113, 131, 149, 167, 185, 203, 221, 239, 257, 275, 293, 311, 329, 347, 365, 383, 401};
		int[] ys = {16, 34, 52, 70, 88, 106, 124, 142, 160, 178, 196, 214};

		int slotIndex = 0;
		for (int row = 0; row < ys.length; row++) {
			for (int col = 0; col < xs.length; col++) {
				// Mezera pro hráčský inventář začíná od řádku 7
				if (row >= 7 && col >= 7 && col <= 15) continue;
				if (slotIndex < internal.getSlots()) {
					Slot s = this.addSlot(new SlotItemHandler(internal, slotIndex, xs[col], ys[row]));
					this.customSlots.put(slotIndex, s);
					slotIndex++;
				}
			}
		}
		this.containerSlotsCount = slotIndex;

		// Hráčský inventář – umístěn do mezery (cols 7-15, rows 7-11)
		// xs[7]=131, ys[7]=142
		int invOffsetX = 123;
		int invOffsetY = 72;
		for (int si = 0; si < 3; ++si)
			for (int sj = 0; sj < 9; ++sj)
				this.addSlot(new Slot(inv, sj + (si + 1) * 9, invOffsetX + 8 + sj * 18, invOffsetY + 84 + si * 18));
		for (int si = 0; si < 9; ++si)
			this.addSlot(new Slot(inv, si, invOffsetX + 8 + si * 18, invOffsetY + 142));
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		if (boundBlockEntity != null)
			return AbstractContainerMenu.stillValid(this.access, player, boundBlockEntity.getBlockState().getBlock());
		return true;
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < containerSlotsCount) {
				if (!this.moveItemStackTo(itemstack1, containerSlotsCount, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if (!this.moveItemStackTo(itemstack1, 0, containerSlotsCount, false)) {
				return ItemStack.EMPTY;
			}
			if (itemstack1.isEmpty()) slot.set(ItemStack.EMPTY);
			else slot.setChanged();
		}
		return itemstack;
	}

	@Override
	public void removed(@NotNull Player player) {
		super.removed(player);
	}

	@Override
	public Map<Integer, Slot> get() { return customSlots; }
}