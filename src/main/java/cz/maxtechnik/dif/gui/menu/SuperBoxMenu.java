package cz.maxtechnik.dif.gui.menu;

import cz.maxtechnik.dif.init.gui.DifModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
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
	private boolean bound = false;
	private BlockEntity boundBlockEntity = null;

	// Dynamicky spočítáme počet slotů, abychom se vyhnuli chybám
	private final int containerSlotsCount;

	public SuperBoxMenu(int id, Inventory inv, BlockPos pos) {
		super(DifModMenus.SUPER_BOX.get(), id);
		this.entity = inv.player;
		this.world = inv.player.level();

		// 1. Nejprve zkusíme získat Capability, pokud jsme u bloku
		if (pos != null) {
			this.access = ContainerLevelAccess.create(world, pos);
			this.boundBlockEntity = world.getBlockEntity(pos);
			if (boundBlockEntity != null) {
				// V 1.21.1 vyžaduje getCapability tyto parametry
				IItemHandler capability = world.getCapability(Capabilities.ItemHandler.BLOCK, pos, boundBlockEntity.getBlockState(), boundBlockEntity, null);
				if (capability != null) {
					this.internal = capability;
					this.bound = true;
				}
			}
		}

		// Pokud nejsme u bloku (např. batoh v ruce), vytvoříme dočasný handler
		// Velikost musí odpovídat tvému GUI (222 je reálný počet v tvém cyklu)
		if (this.internal == null) {
			this.internal = new ItemStackHandler(222);
		}

		// 2. Přidání slotů kontejneru
		int[] xs = {5, 23, 41, 59, 77, 95, 113, 131, 149, 167, 185, 203, 221, 239, 257, 275, 293, 311, 329, 347, 365, 383, 401};
		int[] ys = {16, 34, 52, 70, 88, 106, 124, 142, 160, 178, 196, 214};

		int slotIndex = 0;
		for (int row = 0; row < ys.length; row++) {
			for (int col = 0; col < xs.length; col++) {
				// Mezera pro hráčský inventář
				if (row >= 6 && col >= 7 && col <= 15) continue;

				if (slotIndex < internal.getSlots()) {
					Slot s = this.addSlot(new SlotItemHandler(internal, slotIndex, xs[col], ys[row]));
					this.customSlots.put(slotIndex, s);
					slotIndex++;
				}
			}
		}
		this.containerSlotsCount = slotIndex; // Uložíme si reálný počet (222)

		// 3. Přidání hráčského inventáře (střed)
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
		if (this.bound && this.boundBlockEntity != null) {
			return AbstractContainerMenu.stillValid(this.access, player, this.boundBlockEntity.getBlockState().getBlock());
		}
		return true;
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();

			// Pokud klikáme v Super Boxu, přesuneme do inventáře hráče
			if (index < containerSlotsCount) {
				if (!this.moveItemStackTo(itemstack1, containerSlotsCount, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			}
			// Pokud klikáme v inventáři hráče, přesuneme do Super Boxu
			else if (!this.moveItemStackTo(itemstack1, 0, containerSlotsCount, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		return itemstack;
	}

	@Override
	public void removed(@NotNull Player player) {
		super.removed(player);
		// Pokud kontejner není svázán s blokem (je to jen virtuální GUI), vysypeme věci na zem
		if (!bound && player instanceof ServerPlayer) {
			for (int i = 0; i < internal.getSlots(); i++) {
				ItemStack stack = internal.getStackInSlot(i);
				if (!stack.isEmpty()) {
					player.drop(stack, false);
				}
			}
		}
	}

	@Override
	public Map<Integer, Slot> get() {
		return customSlots;
	}
}