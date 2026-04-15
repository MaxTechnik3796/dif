package cz.maxtechnik.dif.gui.menu;

import cz.maxtechnik.dif.init.events.BackpackSavedData;
import cz.maxtechnik.dif.init.gui.DifModMenus;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MegaBackpackMenu extends AbstractContainerMenu {
	public static final int ROWS = 9;
	public static final int COLS = 13;
	public static final int SLOTS_PER_PAGE = ROWS * COLS;

	private int currentPage = 0;
	private final Container backpackInventory;
	private final Player player;

	// Konstruktor pro klienta (Forge ho volá při otevírání)
	public MegaBackpackMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
		this(id, playerInv); // Volá hlavní konstruktor níže
		if (extraData != null && extraData.readableBytes() >= 4) {
			extraData.readInt();
		}
	}

	// Hlavní konstruktor
	public MegaBackpackMenu(int id, Inventory playerInv) {
		super(DifModMenus.MEGA_BACKPACK.get(), id);
		this.player = playerInv.player;
		this.backpackInventory = new SimpleContainer(SLOTS_PER_PAGE);

		// NAČTENÍ DAT ZE SERVERU
		if (!player.level().isClientSide) {
			BackpackSavedData data = BackpackSavedData.get(player.level());
			NonNullList<ItemStack> allItems = data.getOrCreateInventory(player.getUUID());
			loadPage(allItems);
		}

		// 1. Sloty Batohu (13x9)
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				this.addSlot(new Slot(backpackInventory, j + i * COLS, 8 + j * 18, 18 + i * 18));
			}
		}
		this.addDataSlot(new DataSlot() {
			@Override
			public int get() {
				return currentPage;
			}

			@Override
			public void set(int value) {
				currentPage = value;
			}
		});
		// 2. Sloty Hráče
		addPlayerInventory(playerInv);
	}

	// ULOŽENÍ DAT PŘI ZAVŘENÍ
	@Override
	public void removed(Player pPlayer) {
		super.removed(pPlayer);
		if (!pPlayer.level().isClientSide) {
			saveCurrentPage();
		}
	}

	public void saveCurrentPage() {
		if (player.level().isClientSide) return;

		BackpackSavedData data = BackpackSavedData.get(player.level());
		NonNullList<ItemStack> allItems = data.getOrCreateInventory(player.getUUID());

		int startOffset = currentPage * SLOTS_PER_PAGE;
		for (int i = 0; i < SLOTS_PER_PAGE; i++) {
			allItems.set(startOffset + i, backpackInventory.getItem(i));
		}
		data.setDirty(); // Toto zajistí uložení na disk
	}

	private void loadPage(NonNullList<ItemStack> allItems) {
		int startOffset = currentPage * SLOTS_PER_PAGE;
		for (int i = 0; i < SLOTS_PER_PAGE; i++) {
			backpackInventory.setItem(i, allItems.get(startOffset + i));
		}
	}

	public void changePage(int delta) {
		if (player.level().isClientSide) return;

		// 1. Uložit itemy z aktuální stránky do celkového seznamu
		saveCurrentPage();

		// 2. Posunout index stránky (0-15 pro 16 stránek)
		this.currentPage = Math.max(0, Math.min(15, currentPage + delta));

		// 3. Načíst itemy z nové stránky do slotů
		BackpackSavedData data = BackpackSavedData.get(player.level());
		loadPage(data.getOrCreateInventory(player.getUUID()));

		// 4. Klíčový krok: Poslat změnu klientovi, aby se mu překreslily ikonky itemů
		this.broadcastChanges();
	}

	private void addPlayerInventory(Inventory playerInv) {
		int startY = 18 + (ROWS * 18) + 10;
		int startX = 8 + (2 * 18);

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInv, j + i * 9 + 9, startX + j * 18, startY + i * 18));
			}
		}
		for (int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(playerInv, k, startX + k * 18, startY + 58));
		}
	}
	public int getCurrentPage() {
		return this.currentPage;
	}
	@Override
	public boolean stillValid(@NotNull Player pPlayer) {
		return true;
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
		// Tady by měla být logika pro Shift-Click, zatím vracíme prázdno
		return ItemStack.EMPTY;
	}
}