package cz.maxtechnik.dif.block.entity.barrel;

import cz.maxtechnik.dif.block.barrel.AndesiteBarrel;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;
public class AndesiteBarrelBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer{
	public static final int CONTAINER_SIZE=36;
	private NonNullList<ItemStack> items=NonNullList.withSize(CONTAINER_SIZE,ItemStack.EMPTY);
	private final IItemHandler itemHandler=new InvWrapper(this);
	private final ContainerOpenersCounter openersCounter=new ContainerOpenersCounter(){
		@Override
		protected void onOpen(@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState blockState){
			level.playSound(null,pos,SoundEvents.BARREL_OPEN,SoundSource.BLOCKS,1F,1F);
			level.setBlock(pos,blockState.setValue(AndesiteBarrel.OPEN,true),3);
		}
		@Override
		protected void onClose(@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState blockState){
			level.playSound(null,pos,SoundEvents.BARREL_CLOSE,SoundSource.BLOCKS,1F,1F);
			level.setBlock(pos,blockState.setValue(AndesiteBarrel.OPEN,false),3);
		}
		@Override
		protected void openerCountChanged(@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState blockState,int prevOpenCount,int openCount){
		}
		@Override
		protected boolean isOwnContainer(Player player){
			if(player.containerMenu instanceof ChestMenu menu){
				return menu.getContainer()==AndesiteBarrelBlockEntity.this;
			}
			return false;
		}
	};
	public AndesiteBarrelBlockEntity(BlockPos position,BlockState blockState){
		super(DifModBlockEntities.ANDESITE_BARREL.get(),position,blockState);
	}
	public IItemHandler getInventory(){
		return itemHandler;
	}
	@Override
	public int getContainerSize(){
		return CONTAINER_SIZE;
	}
	@Override
	protected @NotNull NonNullList<ItemStack> getItems(){
		return this.items;
	}
	@Override
	protected void setItems(@NotNull NonNullList<ItemStack> itemStacks){
		this.items=itemStacks;
	}
	@Override
	public @NotNull Component getDefaultName(){
		return Component.translatable("container.dif.andesite_barrel");
	}
	@Override
	public @NotNull AbstractContainerMenu createMenu(int id,@NotNull Inventory inv){
		return new ChestMenu(MenuType.GENERIC_9x4,id,inv,this,4);
	}
	@Override
	public void startOpen(@NotNull Player player){
		if(!this.remove&&!player.isSpectator()){
			assert this.getLevel()!=null;
			this.openersCounter.incrementOpeners(player,this.getLevel(),this.getBlockPos(),this.getBlockState());
		}
	}
	@Override
	public void stopOpen(@NotNull Player player){
		if(!this.remove&&!player.isSpectator()){
			assert this.getLevel()!=null;
			this.openersCounter.decrementOpeners(player,this.getLevel(),this.getBlockPos(),this.getBlockState());
		}
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag compound,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(compound,provider);
		this.items=NonNullList.withSize(this.getContainerSize(),ItemStack.EMPTY);
		if(!this.tryLoadLootTable(compound)){
			if(compound.contains("Items",CompoundTag.TAG_LIST))
				ContainerHelper.loadAllItems(compound,this.items,provider);
			else if(compound.contains("inventory")){
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
		if(!this.trySaveLootTable(compound))
			ContainerHelper.saveAllItems(compound,this.items,provider);
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
	public int @NotNull [] getSlotsForFace(@NotNull Direction side){
		return IntStream.range(0,this.getContainerSize()).toArray();
	}
	@Override
	public boolean canPlaceItemThroughFace(int index,@NotNull ItemStack itemStack,@Nullable Direction direction){
		return true;
	}
	@Override
	public boolean canTakeItemThroughFace(int index,@NotNull ItemStack itemStack,@NotNull Direction direction){
		return true;
	}
}