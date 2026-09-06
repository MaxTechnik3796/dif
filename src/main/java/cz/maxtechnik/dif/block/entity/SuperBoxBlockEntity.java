package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.gui.menu.SuperBoxMenu;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
public class SuperBoxBlockEntity extends RandomizableContainerBlockEntity{
	public static final int CONTAINER_SIZE=231;
	private NonNullList<ItemStack> items=NonNullList.withSize(CONTAINER_SIZE,ItemStack.EMPTY);
	private final IItemHandler itemHandler=new InvWrapper(this);
	public IItemHandler getInventory(){
		return itemHandler;
	}
	public SuperBoxBlockEntity(BlockPos position,BlockState blockState){
		super(DifModBlockEntities.SUPER_BOX.get(),position,blockState);
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag compound,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(compound,provider);
		this.items=NonNullList.withSize(this.getContainerSize(),ItemStack.EMPTY);
		if(!this.tryLoadLootTable(compound)){
			if(compound.contains("Items",CompoundTag.TAG_LIST)){
				ContainerHelper.loadAllItems(compound,this.items,provider);
			}else if(compound.contains("inventory")){
				ItemStackHandler handler=new ItemStackHandler(this.getContainerSize());
				handler.deserializeNBT(provider,compound.getCompound("inventory"));
				for(int i=0;i<handler.getSlots()&&i<this.items.size();i++){
					this.items.set(i,handler.getStackInSlot(i));
				}
			}
		}
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag compound,@NotNull HolderLookup.Provider provider){
		super.saveAdditional(compound,provider);
		if(!this.trySaveLootTable(compound)){
			ContainerHelper.saveAllItems(compound,this.items,provider);
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
		return CONTAINER_SIZE;
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
		return this.items;
	}
	@Override
	protected void setItems(@NotNull NonNullList<ItemStack> itemStacks){
		this.items=itemStacks;
	}
}