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
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.stream.IntStream;
public class SuperBoxBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer{
	private NonNullList<ItemStack> stacks=NonNullList.withSize(231,ItemStack.EMPTY);
	public SuperBoxBlockEntity(BlockPos position,BlockState state){
		super(DifModBlockEntities.SUPER_BOX.get(),position,state);
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag compound,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(compound,provider);
		if(!this.tryLoadLootTable(compound))
			this.stacks=NonNullList.withSize(this.getContainerSize(),ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound,this.stacks,provider);
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag compound,@NotNull HolderLookup.Provider provider){
		super.saveAdditional(compound,provider);
		if(!this.trySaveLootTable(compound)){
			ContainerHelper.saveAllItems(compound,this.stacks,provider);
		}
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider){
		return this.saveWithFullMetadata(provider);
	}
	@Override
	public int getContainerSize(){
		return stacks.size();
	}
	@Override
	public boolean isEmpty(){
		for(ItemStack itemstack: this.stacks)
			if(!itemstack.isEmpty())
				return false;
		return true;
	}
	@Override
	public @NotNull Component getDefaultName(){
		return Component.translatable("gui.dif.super_box");
	}
	@Override
	protected @NotNull AbstractContainerMenu createMenu(int id,@NotNull Inventory inventory){
		return new SuperBoxMenu(id,inventory,this.worldPosition);
	}
	@Override
	public @NotNull Component getDisplayName(){
		return Component.translatable("gui.dif.super_box");
	}
	@Override
	protected @NotNull NonNullList<ItemStack> getItems(){
		return this.stacks;
	}
	@Override
	protected void setItems(@NotNull NonNullList<ItemStack> stacks){
		this.stacks=stacks;
	}
	@Override
	public boolean canPlaceItem(int index,@NotNull ItemStack stack){
		return true;
	}
	@Override
	public int @NotNull [] getSlotsForFace(@NotNull Direction side){
		return IntStream.range(0,this.getContainerSize()).toArray();
	}
	@Override
	public boolean canPlaceItemThroughFace(int index,@NotNull ItemStack stack,@Nullable Direction direction){
		return this.canPlaceItem(index,stack);
	}
	@Override
	public boolean canTakeItemThroughFace(int index,@NotNull ItemStack stack,@NotNull Direction direction){
		return true;
	}
}
