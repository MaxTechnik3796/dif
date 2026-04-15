package cz.maxtechnik.dif.gui.menu;

import cz.maxtechnik.dif.init.gui.DifModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
public class MegaBackpackMenu extends AbstractContainerMenu {
	public static final int ROWS = 9;
	public static final int COLS = 13;
	public static final int SLOTS_PER_PAGE = ROWS * COLS;
	// Uprav tento konstruktor v MegaBackpackMenu.java
	public MegaBackpackMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
		this(id, playerInv);
		if (extraData != null) {
			extraData.readInt(); // Tady se přečte ta nula z paketu (4 bajty)
		}
	}
	public MegaBackpackMenu(int id, Inventory playerInv) {
		super(DifModMenus.MEGA_BACKPACK.get(), id);

		// Vytvoříme dočasný container pro 117 slotů (na straně klienta)
		// Na serveru se sem pak nahrají data z .dat souboru
		// Tohle je virtuální inventář pro zobrazení v GUI
		Container backpackInventory=new SimpleContainer(SLOTS_PER_PAGE);

		// 1. Sloty Batohu (13x9)
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				this.addSlot(new Slot(backpackInventory, j + i * COLS, 8 + j * 18, 18 + i * 18));
			}
		}

		// 2. Sloty Hráče (posuneme je dolů pod batoh)
		addPlayerInventory(playerInv);
	}

	private void addPlayerInventory(Inventory playerInv) {
		int startY = 18 + (ROWS * 18) + 10; // Dynamický výpočet pod batoh
		int startX = 8 + (2 * 18); // Vycentrování (protože batoh je širší než hráč)

		// Player Inventory
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInv, j + i * 9 + 9, startX + j * 18, startY + i * 18));
			}
		}
		// Hotbar
		for (int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(playerInv, k, startX + k * 18, startY + 58));
		}
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player,int index) {
		// Základní logika pro Shift+Click (zjednodušeno)
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return true; // Pro testování zatím true, aby se menu nezavíralo
	}
}