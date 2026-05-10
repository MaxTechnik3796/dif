package cz.maxtechnik.dif.gui.menu;

import cz.maxtechnik.dif.block.entity.SpaceshipBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.gui.DifModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class SpaceshipMenu extends AbstractContainerMenu{
	public final int x,y,z;
	public final Player entity;
	public final Level world;

	public SpaceshipMenu(int id,Inventory inv,FriendlyByteBuf extraData){
		this(id,inv,extraData.readBlockPos());
	}

	public SpaceshipMenu(int id,Inventory inv,BlockPos pos){
		super(DifModMenus.SPACESHIP.get(),id);
		this.entity=inv.player;
		this.world=inv.player.level();
		this.x=pos.getX();
		this.y=pos.getY();
		this.z=pos.getZ();

		// Získej handler přímo z BE – funguje na klientu i serveru
		BlockEntity be=world.getBlockEntity(pos);
		IItemHandler shipInventory;
		if(be instanceof SpaceshipBlockEntity sbe){
			shipInventory=sbe.getItemHandler();
		}else{
			// Fallback – prázdný handler (nikdy by neměl nastat)
			shipInventory=new ItemStackHandler(9);
		}

		// 1. PALIVOVÝ SLOT (slot 0 – pouze RocketFuel)
		this.addSlot(new SlotItemHandler(shipInventory,0,206,114){
			@Override
			public boolean mayPlace(@NotNull ItemStack stack){
				return stack.is(DifModItems.ROCKET_FUEL.get());
			}
		});

		// 2. NÁKLADOVÝ PROSTOR (sloty 1–8)
		for(int i=0;i<8;i++){
			this.addSlot(new SlotItemHandler(shipInventory,1+i,44+i*18,108));
		}

		// 3. INVENTÁŘ HRÁČE – batoh
		for(int i=0;i<3;i++){
			for(int j=0;j<9;j++){
				this.addSlot(new Slot(inv,j+(i+1)*9,44+j*18,142+i*18));
			}
		}

		// 4. HOTBAR
		for(int i=0;i<9;i++){
			this.addSlot(new Slot(inv,i,44+i*18,200));
		}
	}

	@Override
	public boolean stillValid(@NotNull Player player){
		return true;
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player,int index){
		ItemStack itemstack=ItemStack.EMPTY;
		Slot slot=this.slots.get(index);
		if(slot.hasItem()){
			ItemStack itemstack1=slot.getItem();
			itemstack=itemstack1.copy();
			if(index<9){
				if(!this.moveItemStackTo(itemstack1,9,this.slots.size(),true)) return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1,itemstack);
			}else if(!this.moveItemStackTo(itemstack1,0,9,false)){
				return ItemStack.EMPTY;
			}
			if(itemstack1.isEmpty()) slot.set(ItemStack.EMPTY);
			else slot.setChanged();
			if(itemstack1.getCount()==itemstack.getCount()) return ItemStack.EMPTY;
			slot.onTake(player,itemstack1);
		}
		return itemstack;
	}
}