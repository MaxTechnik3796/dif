package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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
	protected void saveAdditional(CompoundTag tag,@NotNull HolderLookup.Provider provider){
		tag.put("inventory",itemHandler.serializeNBT(provider));
		super.saveAdditional(tag,provider);
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(tag,provider);
		if(tag.contains("inventory")){
			itemHandler.deserializeNBT(provider,tag.getCompound("inventory"));
		}
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