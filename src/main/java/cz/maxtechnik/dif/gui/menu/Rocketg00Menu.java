package cz.maxtechnik.dif.gui.menu;

import cz.maxtechnik.dif.init.gui.DifModMenus;
import cz.maxtechnik.dif.block.entity.SpaceShipBE;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

public class Rocketg00Menu extends AbstractContainerMenu {
	public final int x, y, z;
	public final Player entity;
	public final Level world;
	private final IItemHandler shipInventory;

	public Rocketg00Menu(int id, Inventory inv, FriendlyByteBuf extraData) {
		this(id, inv, extraData.readBlockPos());
	}

	public Rocketg00Menu(int id, Inventory inv, BlockPos pos) {
		super(DifModMenus.ROCKETG_00.get(), id);
		this.entity = inv.player;
		this.world = inv.player.level();
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();

		BlockEntity be = world.getBlockEntity(pos);
		if (be != null) {
			this.shipInventory = be.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
		} else {
			this.shipInventory = null;
		}

		// 1. PALIVOVÝ SLOT (Index 0 z SpaceShipBE)
		// Umístěno doprostřed mezi planety a inventář (x=111, y=105)
		if (this.shipInventory != null) {
			this.addSlot(new SlotItemHandler(shipInventory, 0, 111, 105) {
				@Override
				public boolean mayPlace(@NotNull ItemStack stack) {
					return stack.is(Items.DIAMOND); // Povolí jen diamanty
				}
			});

			// 2. NÁKLADOVÝ PROSTOR (Indexy 1 - 26) - volitelné, pokud chceš i bednu v lodi
			// Zde je příklad mřížky pod palivem
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 9; j++) {
					this.addSlot(new SlotItemHandler(shipInventory, 1 + j + i * 9, 44 + j * 18, 125 + i * 18));
				}
			}
		}

		// 3. INVENTÁŘ HRÁČE (Hotbar)
		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inv, i, 44 + i * 18, 200));
		}
		// 4. INVENTÁŘ HRÁČE (Batoh)
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inv, j + (i + 1) * 9, 44 + j * 18, 144 + i * 18));
			}
		}
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return true;
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			// Upravená logika Shift-Clicku pro tvůj počet slotů
			if (index < 19) { // 1 palivo + 18 náklad = 19 slotů lodi
				if (!this.moveItemStackTo(itemstack1, 19, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if (!this.moveItemStackTo(itemstack1, 0, 19, false)) {
				return ItemStack.EMPTY;
			}
			if (itemstack1.isEmpty()) slot.set(ItemStack.EMPTY);
			else slot.setChanged();
		}
		return itemstack;
	}
}