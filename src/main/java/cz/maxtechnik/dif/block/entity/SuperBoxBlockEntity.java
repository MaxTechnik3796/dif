package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.gui.menu.SuperBoxMenu;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class SuperBoxBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {

	private final ItemStackHandler inventory = new ItemStackHandler(231) {
		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
		}
	};

	public ItemStackHandler getInventory() { return inventory; }

	public SuperBoxBlockEntity(BlockPos position, BlockState state) {
		super(DifModBlockEntities.SUPER_BOX.get(), position, state);
	}

	@Override
	protected void loadAdditional(@NotNull CompoundTag compound, @NotNull HolderLookup.Provider provider) {
		super.loadAdditional(compound, provider);
		if (compound.contains("inventory"))
			inventory.deserializeNBT(provider, compound.getCompound("inventory"));
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag compound, @NotNull HolderLookup.Provider provider) {
		super.saveAdditional(compound, provider);
		compound.put("inventory", inventory.serializeNBT(provider));
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider) {
		return this.saveWithFullMetadata(provider);
	}

	@Override
	public int getContainerSize() {
		return inventory.getSlots();
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < inventory.getSlots(); i++)
			if (!inventory.getStackInSlot(i).isEmpty()) return false;
		return true;
	}

	@Override
	public @NotNull Component getDefaultName() {
		return Component.translatable("gui.dif.super_box");
	}

	@Override
	protected @NotNull AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory) {
		return new SuperBoxMenu(id, inventory, this.worldPosition);
	}

	@Override
	public @NotNull Component getDisplayName() {
		return Component.translatable("gui.dif.super_box");
	}

	@Override
	protected @NotNull NonNullList<ItemStack> getItems() {
		NonNullList<ItemStack> list = NonNullList.withSize(inventory.getSlots(), ItemStack.EMPTY);
		for (int i = 0; i < inventory.getSlots(); i++) list.set(i, inventory.getStackInSlot(i));
		return list;
	}

	@Override
	protected void setItems(@NotNull NonNullList<ItemStack> stacks) {
		for (int i = 0; i < stacks.size() && i < inventory.getSlots(); i++)
			inventory.setStackInSlot(i, stacks.get(i));
	}

	@Override
	public boolean canPlaceItem(int index, @NotNull ItemStack stack) { return true; }

	@Override
	public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
		return IntStream.range(0, inventory.getSlots()).toArray();
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction) {
		return true;
	}

	@Override
	public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
		return true;
	}
}