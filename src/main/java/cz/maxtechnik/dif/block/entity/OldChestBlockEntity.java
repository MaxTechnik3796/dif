package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.gui.menu.OldChestMenu;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
public class OldChestBlockEntity extends RandomizableContainerBlockEntity{
	public static final int CONTAINER_SIZE=27;
	private NonNullList<ItemStack> items=NonNullList.withSize(CONTAINER_SIZE,ItemStack.EMPTY);
	private final IItemHandler itemHandler=new InvWrapper(this);
	public IItemHandler getInventory(){
		return itemHandler;
	}
	public OldChestBlockEntity(BlockPos position,BlockState blockState){
		super(DifModBlockEntities.OLD_CHEST.get(),position,blockState);
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
		return Component.translatable("container.dif.old_chest");
	}
	@Override
	public @NotNull AbstractContainerMenu createMenu(int id,@NotNull Inventory inv){
		FriendlyByteBuf buffer=new FriendlyByteBuf(Unpooled.buffer());
		buffer.writeBlockPos(this.worldPosition);
		return new OldChestMenu(id,inv,buffer);
	}
	@Override
	public @NotNull Component getDisplayName(){
		return Component.translatable("container.dif.old_chest");
	}
	@Override
	protected @NotNull NonNullList<ItemStack> getItems(){
		return this.items;
	}
	@Override
	protected void setItems(@NotNull NonNullList<ItemStack> stacks){
		this.items=stacks;
	}
}