package cz.maxtechnik.dif.gui.menu;

import cz.maxtechnik.dif.block.entity.OldChest;
import cz.maxtechnik.dif.init.gui.DifModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class OldChestMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
	// Konstanty pro rozložení
	private static final int SLOT_SIZE = 18;
	private static final int SLOT_X_SPACING = 18;
	private static final int SLOT_Y_SPACING = 18;
	private static final int CHEST_START_X = 8;
	private static final int CHEST_START_Y = 18;

	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess mainAccess = ContainerLevelAccess.NULL;

	// Seznam všech handlerů (hlavní + sousedé)
	private final List<IItemHandler> connectedHandlers = new ArrayList<>();

	private final Map<Integer, Slot> customSlots = new HashMap<>();
	private boolean bound = false;
	private BlockEntity boundBlockEntity = null;

	// Celkový počet slotů ve všech bednách dohromady
	private int totalChestSlots = 0;

	public OldChestMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
		super(DifModMenus.OLD_CHEST_MENU.get(), id);
		this.entity = inv.player;
		this.world = inv.player.level();

		BlockPos pos = null;
		if (extraData != null) {
			pos = extraData.readBlockPos();
			this.x = pos.getX();
			this.y = pos.getY();
			this.z = pos.getZ();
			mainAccess = ContainerLevelAccess.create(world, pos);
		}

		// 1. Najdeme hlavní blok a sousedy
		if (pos != null) {
			this.boundBlockEntity = this.world.getBlockEntity(pos);
			// Přidáme hlavní blok jako první
			addBlockHandler(this.boundBlockEntity);

			// Zkontrolujeme sousedy (North, East, South, West)
			checkNeighbor(pos.north());
			checkNeighbor(pos.east());
			checkNeighbor(pos.south());
			checkNeighbor(pos.west());

			// Spustíme animaci otevření pro hlavní blok (volitelně i pro sousedy, pokud chcete)
			if (this.boundBlockEntity instanceof OldChest blockEntity) {
				blockEntity.startOpen(inv.player);
			}
		}

		// Fallback: Pokud se nic nenašlo (např. chyba na klientovi), vytvoříme dummy handler
		if (connectedHandlers.isEmpty()) {
			this.connectedHandlers.add(new ItemStackHandler(27));
		}

		// 2. Generování slotů pro všechny nalezené bedny
		int currentY = CHEST_START_Y;
		int totalIndex = 0;

		for (IItemHandler handler : connectedHandlers) {
			int slots = handler.getSlots();
			int rows = slots / 9;
			// Ošetření pro případ, že počet slotů není dělitelný 9 (zbytek se dá na další řádek)
			if (slots % 9 != 0) rows++;

			for (int i = 0; i < slots; i++) {
				int row = i / 9;
				int col = i % 9;

				int slotX = CHEST_START_X + col * SLOT_X_SPACING;
				int slotY = currentY + row * SLOT_Y_SPACING;

				SlotItemHandler newSlot = new SlotItemHandler(handler, i, slotX, slotY);
				this.addSlot(newSlot);
				this.customSlots.put(totalIndex, newSlot);
				totalIndex++;
			}

			// Posuneme Y pro další bednu (přidáme mezeru mezi bednami, nebo navážeme hned)
			currentY += rows * SLOT_Y_SPACING;
		}

		this.totalChestSlots = totalIndex;

		// 3. Generování inventáře hráče
		// Vypočítáme Y pozici inventáře hráče tak, aby byla pod všemi bednami
		// Základní offset 140 je pro 3 řádky beden. My to musíme upravit.
		// Přidáme malý padding (např. 13 pixelů jako ve vanilla GUI mezi chest a inv)
		int playerInvY = currentY + 13;

		for (int si = 0; si < 3; ++si) {
			for (int sj = 0; sj < 9; ++sj) {
				this.addSlot(new Slot(inv, sj + (si + 1) * 9, 8 + sj * 18, playerInvY + si * 18));
			}
		}
		for (int si = 0; si < 9; ++si) {
			this.addSlot(new Slot(inv, si, 8 + si * 18, playerInvY + 58));
		}
	}

	/**
	 * Pomocná metoda pro kontrolu souseda.
	 * Pokud je blok na dané pozici STEJNÝ jako hlavní blok, přidá jeho handler.
	 */
	private void checkNeighbor(BlockPos neighborPos) {
		if (this.boundBlockEntity == null) return;

		BlockState mainState = this.boundBlockEntity.getBlockState();
		BlockState neighborState = this.world.getBlockState(neighborPos);

		// Kontrola: Je to stejný blok?
		if (mainState.getBlock() == neighborState.getBlock()) {
			BlockEntity neighborBE = this.world.getBlockEntity(neighborPos);
			addBlockHandler(neighborBE);
		}
	}

	private void addBlockHandler(BlockEntity be) {
		if (be != null) {
			be.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler -> {
				this.connectedHandlers.add(handler);
				this.bound = true;
			});
		}
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		if (this.bound) {
			if (this.boundBlockEntity != null)
				return AbstractContainerMenu.stillValid(this.mainAccess, player, this.boundBlockEntity.getBlockState().getBlock());
		}
		return true;
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();

			// Pokud klikneme na slot v bednách (index < totalChestSlots)
			if (index < this.totalChestSlots) {
				if (!this.moveItemStackTo(itemstack1, this.totalChestSlots, this.slots.size(), true))
					return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1, itemstack);
			}
			// Pokud klikneme v inventáři hráče -> přesun do beden
			else if (!this.moveItemStackTo(itemstack1, 0, this.totalChestSlots, false)) {
				// Logika pro přesun v rámci inventáře hráče (hotbar <-> main)
				if (index < this.totalChestSlots + 27) {
					if (!this.moveItemStackTo(itemstack1, this.totalChestSlots + 27, this.slots.size(), true))
						return ItemStack.EMPTY;
				} else {
					if (!this.moveItemStackTo(itemstack1, this.totalChestSlots, this.totalChestSlots + 27, false))
						return ItemStack.EMPTY;
				}
				return ItemStack.EMPTY;
			}

			if (itemstack1.getCount() == 0)
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();
			if (itemstack1.getCount() == itemstack.getCount())
				return ItemStack.EMPTY;
			slot.onTake(playerIn, itemstack1);
		}
		return itemstack;
	}

	@Override
	public void removed(@NotNull Player playerIn) {
		if (this.boundBlockEntity instanceof OldChest blockEntity) {
			blockEntity.stopOpen(playerIn);
		}
		super.removed(playerIn);
		// Poznámka: Tady by se nemělo nic dropovat, protože itemy zůstávají v bednách (TileEntities).
		// Kód pro dropování při zničení bloku by měl být v Block.onRemove(), ne v Menu.
	}

	public Map<Integer, Slot> get() {
		return customSlots;
	}

	public BlockEntity getBlockEntity() {
		return this.boundBlockEntity;
	}
}