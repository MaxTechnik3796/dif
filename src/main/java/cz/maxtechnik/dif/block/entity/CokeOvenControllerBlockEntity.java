package cz.maxtechnik.dif.block.entity;

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
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;
public class CokeOvenControllerBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer{
	public CokeOvenControllerBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.COKE_OVEN_CONTROLLER.get(),pos,blockState);
	};
	private final ItemStackHandler inventory=new ItemStackHandler(2){
		@Override
		protected void onContentsChanged(int slot){
			setChanged();
		}
	};
	public ItemStackHandler getInventory(){
		return inventory;
	}
	public final FluidTank fluidTank=new FluidTank(8000){
		@Override
		protected void onContentsChanged(){
			super.onContentsChanged();
			setChanged();
			if(level!=null)
				level.sendBlockUpdated(worldPosition,level.getBlockState(worldPosition),level.getBlockState(worldPosition),2);
		}
	};
	@Override
	public int @NotNull [] getSlotsForFace(@NotNull Direction side){
		return IntStream.range(0,inventory.getSlots()).toArray();
	}
	@Override
	public boolean canPlaceItemThroughFace(int index,@NotNull ItemStack itemStack,@Nullable Direction direction){
		return this.canPlaceItem(index,itemStack);
	}
	@Override
	public boolean canTakeItemThroughFace(int index,@NotNull ItemStack itemStack,@NotNull Direction direction){
		return index==1;
	}
	@Override
	public boolean canPlaceItem(int index,@NotNull ItemStack itemStack){
		return index==0;
	}
	@Override
	public int getContainerSize(){
		return inventory.getSlots();
	}
	@Override
	protected @NotNull Component getDefaultName(){
		return Component.translatable("container.dif.coke_oven");
	}
	@Override
	public @NotNull Component getDisplayName(){
		return Component.translatable("container.dif.coke_oven");
	}
	@Override
	protected @NotNull NonNullList<ItemStack> getItems(){
		NonNullList<ItemStack> list=NonNullList.withSize(inventory.getSlots(),ItemStack.EMPTY);
		for(int i=0;i<inventory.getSlots();i++) list.set(i,inventory.getStackInSlot(i));
		return list;
	}
	@Override
	protected void setItems(@NotNull NonNullList<ItemStack> stacks){
		for(int i=0;i<stacks.size()&&i<inventory.getSlots();i++)
			inventory.setStackInSlot(i,stacks.get(i));
	}
	@Override
	public @NotNull ItemStack getItem(int i){
		return inventory.getStackInSlot(i);
	}
	@Override
	public void setItem(int i,@NotNull ItemStack itemStack){
		inventory.setStackInSlot(i,itemStack);
	}
	@Override
	public @NotNull AbstractContainerMenu createMenu(int id,@NotNull Inventory inventory){
		return ChestMenu.threeRows(id,inventory);
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(tag,provider);
		if(tag.contains("inventory"))
			inventory.deserializeNBT(provider,tag.getCompound("inventory"));
		if(tag.get("fluidTank") instanceof CompoundTag fluidTag)
			fluidTank.readFromNBT(provider,fluidTag);
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.saveAdditional(tag,provider);
		tag.put("inventory",inventory.serializeNBT(provider));
		tag.put("fluidTank",fluidTank.writeToNBT(provider,new CompoundTag()));
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
	public boolean isEmpty(){
		for(int i=0;i<inventory.getSlots();i++)
			if(!inventory.getStackInSlot(i).isEmpty()) return false;
		return true;
	}
}
