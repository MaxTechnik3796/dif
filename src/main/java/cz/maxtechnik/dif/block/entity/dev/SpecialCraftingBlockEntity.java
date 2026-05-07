package cz.maxtechnik.dif.block.entity.dev;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.gui.menu.SpecialCraftingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

/**
 * Out of service (Unfinished)
 **/
public class SpecialCraftingBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
	private NonNullList<ItemStack> stacks = NonNullList.withSize(10, ItemStack.EMPTY);

	public SpecialCraftingBlockEntity(BlockPos position, BlockState state) {
		super(DifModBlockEntities.SPECIAL_CRAFTING.get(), position, state);
	}

	// ✅ Capabilities se v NeoForge 1.21.1 registrují staticky v DifModBlockEntities (nebo samostatné třídě):
	// B.EVENT: RegisterCapabilitiesEvent
	// event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DifModBlockEntities.SPECIAL_CRAFTING.get(),
	//     (be, side) -> side == null ? null : new SidedInvWrapper(be, side));

	@Override
	protected void loadAdditional(@NotNull CompoundTag compound, HolderLookup.@NotNull Provider registries) {
		super.loadAdditional(compound, registries);
		if (!this.tryLoadLootTable(compound))
			this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound, this.stacks, registries); // ✅ registries parametr
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag compound, HolderLookup.@NotNull Provider registries) {
		super.saveAdditional(compound, registries);
		if (!this.trySaveLootTable(compound)) {
			ContainerHelper.saveAllItems(compound, this.stacks, registries); // ✅ registries parametr
		}
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
		return this.saveWithFullMetadata(registries); // ✅ registries parametr
	}

	@Override
	public int getContainerSize() {
		return stacks.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.stacks)
			if (!itemstack.isEmpty()) return false;
		return true;
	}

	@Override
	public @NotNull Component getDefaultName() {
		return Component.literal("Special Crafting");
	}

	@Override
	public @NotNull AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory) {
		return new SpecialCraftingMenu(id, inventory, this.worldPosition); // ✅ správné
	}

	@Override
	protected @NotNull NonNullList<ItemStack> getItems() {
		return this.stacks;
	}

	@Override
	protected void setItems(@NotNull NonNullList<ItemStack> stacks) {
		this.stacks = stacks;
	}

	@Override
	public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
		return true;
	}

	@Override
	public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
		return IntStream.range(0, this.getContainerSize()).toArray();
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction) {
		return false;
	}

	@Override
	public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
		return false;
	}
}