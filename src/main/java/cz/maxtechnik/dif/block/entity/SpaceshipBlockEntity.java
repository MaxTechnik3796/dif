package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cz.maxtechnik.dif.gui.menu.SpaceshipMenu;
public class SpaceshipBlockEntity extends BlockEntity implements MenuProvider{
	private final ItemStackHandler itemHandler=new ItemStackHandler(9){
		@Override
		protected void onContentsChanged(int slot){
			setChanged();
		}
	};
	private LazyOptional<IItemHandler> lazyItemHandler=LazyOptional.empty();
	public SpaceshipBlockEntity(BlockPos pos,BlockState state){
		super(DifModBlockEntities.SPACESHIP.get(),pos,state);
	}
	@Override
	public @NotNull Component getDisplayName(){
		return Component.translatable("block.dif.spaceship");
	}
	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id,@NotNull Inventory inventory,@NotNull Player player){
		return new SpaceshipMenu(id,inventory,this.worldPosition);
	}
	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap,@Nullable Direction side){
		if(cap==ForgeCapabilities.ITEM_HANDLER){
			return lazyItemHandler.cast();
		}
		return super.getCapability(cap,side);
	}
	@Override
	public void onLoad(){
		super.onLoad();
		lazyItemHandler=LazyOptional.of(()->itemHandler);
	}
	@Override
	public void invalidateCaps(){
		super.invalidateCaps();
		lazyItemHandler.invalidate();
	}
	@Override
	protected void saveAdditional(CompoundTag tag){
		tag.put("inventory",itemHandler.serializeNBT());
		super.saveAdditional(tag);
	}
	@Override
	public void load(@NotNull CompoundTag tag){
		super.load(tag);
		itemHandler.deserializeNBT(tag.getCompound("inventory"));
	}
	public void drops(){
		SimpleContainer inventory=new SimpleContainer(itemHandler.getSlots());
		for(int i=0;i<itemHandler.getSlots();i++){
			inventory.setItem(i,itemHandler.getStackInSlot(i));
		}
		assert this.level!=null;
		Containers.dropContents(this.level,this.worldPosition,inventory);
	}
}