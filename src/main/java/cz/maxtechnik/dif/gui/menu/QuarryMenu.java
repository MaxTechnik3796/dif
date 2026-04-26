package cz.maxtechnik.dif.gui.menu;

import cz.maxtechnik.dif.block.entity.QuarryBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.gui.DifModMenus;
import cz.maxtechnik.dif.item.quarry.DrillHeadItem;
import cz.maxtechnik.dif.item.quarry.EngineItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class QuarryMenu extends AbstractContainerMenu {
	public final Level level;
	public final Player player;
	private final IItemHandler internal;
	private final ContainerData data;
	private final ContainerLevelAccess access;
	private final BlockEntity boundBlockEntity;

	public QuarryMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
		super(DifModMenus.QUARRY.get(), id);
		this.player = inv.player;
		this.level = inv.player.level();
		BlockPos pos = extraData.readBlockPos();
		this.access = ContainerLevelAccess.create(level, pos);
		this.boundBlockEntity = level.getBlockEntity(pos);
		
		if (this.boundBlockEntity instanceof QuarryBlockEntity qbe) {
			this.internal = qbe.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(new ItemStackHandler(3));
			this.data = qbe.dataAccess;
		} else {
			this.internal = new ItemStackHandler(3);
			this.data = new SimpleContainerData(6);
		}
		
		this.addDataSlots(this.data);
		addQuarrySlots();
		addPlayerSlots(inv);
	}

	public QuarryMenu(int id, Inventory inv, QuarryBlockEntity qbe) {
		super(DifModMenus.QUARRY.get(), id);
		this.player = inv.player;
		this.level = inv.player.level();
		this.access = ContainerLevelAccess.create(level, qbe.getBlockPos());
		this.boundBlockEntity = qbe;
		
		this.internal = qbe.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(new ItemStackHandler(3));
		this.data = qbe.dataAccess;
		
		this.addDataSlots(this.data);
		addQuarrySlots();
		addPlayerSlots(inv);
	}

	private void addQuarrySlots() {
		this.addSlot(new SlotItemHandler(internal, 0, 8, 17) {
			@Override public boolean mayPlace(@NotNull ItemStack stack) {
				return stack.getItem() instanceof DrillHeadItem;
			}
		});
		this.addSlot(new SlotItemHandler(internal, 1, 8, 35) {
			@Override public boolean mayPlace(@NotNull ItemStack stack) {
				return stack.getItem() instanceof EngineItem;
			}
		});
		this.addSlot(new SlotItemHandler(internal, 2, 8, 53) {
			@Override public boolean mayPlace(@NotNull ItemStack stack) {
				return stack.getItem() instanceof EngineItem ||
					   stack.getItem() == DifModItems.QUARRY_LIQUID_REMOVER.get() ||
					   stack.getItem() == Items.ENCHANTED_BOOK;
			}
		});
	}

	private void addPlayerSlots(Inventory inv) {
		for (int row = 0; row < 3; ++row)
			for (int col = 0; col < 9; ++col)
				this.addSlot(new Slot(inv, col + (row + 1) * 9, 8 + col * 18, 84 + row * 18));
		for (int col = 0; col < 9; ++col)
			this.addSlot(new Slot(inv, col, 8 + col * 18, 142));
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		if (this.boundBlockEntity != null) {
			return AbstractContainerMenu.stillValid(this.access, player, this.boundBlockEntity.getBlockState().getBlock());
		}
		return true;
	}

	public int getStateOrdinal()   { return this.data.get(0); }
	public int getSpeed()          { return this.data.get(1); }
	public int getFECost()         { return this.data.get(2); }
	public int getAreaX()          { return this.data.get(3); }
	public int getAreaZ()          { return this.data.get(4); }
	public int getStatusMode()     { return this.data.get(5); }

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < 3) {
				if (!this.moveItemStackTo(itemstack1, 3, this.slots.size(), true)) return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1, itemstack);
			} else if (!this.moveItemStackTo(itemstack1, 0, 3, false)) {
				if (index < 3 + 27) {
					if (!this.moveItemStackTo(itemstack1, 3 + 27, this.slots.size(), true)) return ItemStack.EMPTY;
				} else {
					if (!this.moveItemStackTo(itemstack1, 3, 3 + 27, false)) return ItemStack.EMPTY;
				}
				return ItemStack.EMPTY;
			}
			if (itemstack1.isEmpty()) slot.set(ItemStack.EMPTY);
			else slot.setChanged();
			if (itemstack1.getCount() == itemstack.getCount()) return ItemStack.EMPTY;
			slot.onTake(player, itemstack1);
		}
		return itemstack;
	}
}
