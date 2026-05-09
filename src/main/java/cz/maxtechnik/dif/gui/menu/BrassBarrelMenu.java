package cz.maxtechnik.dif.gui.menu;

import cz.maxtechnik.dif.block.entity.barrel.BrassBarrelBlockEntity;
import cz.maxtechnik.dif.init.gui.DifModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BrassBarrelMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
	private static final int CONTAINER_SLOTS = 54;

	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess access = ContainerLevelAccess.NULL;
    private final Map<Integer, Slot> customSlots = new HashMap<>();
	private BlockEntity boundBlockEntity = null;

	public BrassBarrelMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
		super(DifModMenus.BRASS_BARREL.get(), id);
		this.entity = inv.player;
		this.world = inv.player.level();
        IItemHandler internal = new ItemStackHandler(CONTAINER_SLOTS);

		if (extraData != null) {
			BlockPos pos = extraData.readBlockPos();
			this.x = pos.getX();
			this.y = pos.getY();
			this.z = pos.getZ();
			access = ContainerLevelAccess.create(world, pos);
			boundBlockEntity = this.world.getBlockEntity(pos);
			if (boundBlockEntity != null) {
				IItemHandler handler = this.world.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
				if (handler != null) internal = handler;
				if (boundBlockEntity instanceof BrassBarrelBlockEntity be) be.startOpen(inv.player);
			}
		}

		int startX = 8, startY = 18, index = 0;
		for (int row = 0; row < 6; row++)
			for (int col = 0; col < 9; col++)
				this.customSlots.put(index, this.addSlot(new SlotItemHandler(internal, index++, startX + col * 18, startY + row * 18)));

		int invOffsetX = 8;
		int invOffsetY = 140;
		for (int si = 0; si < 3; ++si)
			for (int sj = 0; sj < 9; ++sj)
				this.addSlot(new Slot(inv, sj + (si + 1) * 9, invOffsetX + sj * 18, invOffsetY + si * 18));
		for (int si = 0; si < 9; ++si)
			this.addSlot(new Slot(inv, si, invOffsetX + si * 18, invOffsetY + 58));
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		if (boundBlockEntity != null)
			return AbstractContainerMenu.stillValid(this.access, player, boundBlockEntity.getBlockState().getBlock());
		return true;
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < CONTAINER_SLOTS) {
				if (!this.moveItemStackTo(itemstack1, CONTAINER_SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1, itemstack);
			} else if (!this.moveItemStackTo(itemstack1, 0, CONTAINER_SLOTS, false)) {
				if (index < CONTAINER_SLOTS + 27) {
					if (!this.moveItemStackTo(itemstack1, CONTAINER_SLOTS + 27, this.slots.size(), true)) return ItemStack.EMPTY;
				} else {
					if (!this.moveItemStackTo(itemstack1, CONTAINER_SLOTS, CONTAINER_SLOTS + 27, false)) return ItemStack.EMPTY;
				}
				return ItemStack.EMPTY;
			}
			if (itemstack1.getCount() == 0) slot.set(ItemStack.EMPTY);
			else slot.setChanged();
			if (itemstack1.getCount() == itemstack.getCount()) return ItemStack.EMPTY;
			slot.onTake(playerIn, itemstack1);
		}
		return itemstack;
	}

	@Override
	public void removed(@NotNull Player playerIn) {
		if (boundBlockEntity instanceof BrassBarrelBlockEntity be) be.stopOpen(playerIn);
		super.removed(playerIn);
	}

	public Map<Integer, Slot> get() { return customSlots; }
	public BlockEntity getBlockEntity() { return this.boundBlockEntity; }
}