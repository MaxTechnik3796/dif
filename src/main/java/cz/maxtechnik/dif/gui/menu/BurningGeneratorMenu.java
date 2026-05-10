package cz.maxtechnik.dif.gui.menu;

import cz.maxtechnik.dif.block.entity.BurningGeneratorBlockEntity;
import cz.maxtechnik.dif.init.gui.DifModMenus;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
public class BurningGeneratorMenu extends AbstractContainerMenu implements Supplier<Map<Integer,Slot>>{
	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess access=ContainerLevelAccess.NULL;
	private IItemHandler internal;
	private final Map<Integer,Slot> customSlots=new HashMap<>();
	private boolean bound=false;
	private BlockEntity boundBlockEntity=null;
	private final ContainerData data;
	public BurningGeneratorMenu(int id,Inventory inv,FriendlyByteBuf extraData){
		super(DifModMenus.GENERATOR.get(),id);
		this.entity=inv.player;
		this.world=inv.player.level();
		this.internal=new ItemStackHandler(1);
		BlockPos pos=null;
		if(extraData!=null){
			pos=extraData.readBlockPos();
			this.x=pos.getX();
			this.y=pos.getY();
			this.z=pos.getZ();
			access=ContainerLevelAccess.create(world,pos);
		}
		if(pos!=null){
			BlockEntity be=this.world.getBlockEntity(pos);
			if(be instanceof BurningGeneratorBlockEntity gen){
				// Používáme getItemHandler() přímo — capability je registrovaná v DifMod
				this.internal=gen.getItemHandler();
				this.data=gen.dataAccess;
				this.bound=true;
				this.boundBlockEntity=gen;
			}else{
				this.data=new SimpleContainerData(7);
			}
		}else{
			this.data=new SimpleContainerData(7);
		}
		this.addDataSlots(this.data);
		this.customSlots.put(0,this.addSlot(new SlotItemHandler(internal,0,79,35)));
		for(int si=0;si<3;++si)
			for(int sj=0;sj<9;++sj)
				this.addSlot(new Slot(inv,sj+(si+1)*9,8+sj*18,84+si*18));
		for(int si=0;si<9;++si)
			this.addSlot(new Slot(inv,si,8+si*18,142));
	}
	public BurningGeneratorMenu(int id,Inventory inv,BlockPos pos){
		super(DifModMenus.GENERATOR.get(),id);
		this.entity=inv.player;
		this.world=inv.player.level();
		this.internal=new ItemStackHandler(1);
		this.x=pos.getX();
		this.y=pos.getY();
		this.z=pos.getZ();
		this.access=ContainerLevelAccess.create(world,pos);
		BlockEntity be=world.getBlockEntity(pos);
		if(be instanceof BurningGeneratorBlockEntity gen){
			this.internal=gen.getItemHandler();
			this.data=gen.dataAccess;
			this.bound=true;
			this.boundBlockEntity=gen;
		}else{
			this.data=new SimpleContainerData(7);
		}
		this.addDataSlots(this.data);
		this.customSlots.put(0,this.addSlot(new SlotItemHandler(internal,0,79,35)));
		for(int si=0;si<3;++si)
			for(int sj=0;sj<9;++sj)
				this.addSlot(new Slot(inv,sj+(si+1)*9,8+sj*18,84+si*18));
		for(int si=0;si<9;++si)
			this.addSlot(new Slot(inv,si,8+si*18,142));
	}
	public int getBurnTime(){
		return this.data.get(0);
	}
	public int getMaxBurnTime(){
		return this.data.get(1);
	}
	public int getLit(){
		return this.data.get(2);
	}
	public int getEnergyStored(){
		return this.data.get(3);
	}
	public int getMaxEnergyStored(){
		return this.data.get(4);
	}
	public int getFuel(){
		return this.data.get(5);
	}
	public int getEmpty(){
		return this.data.get(6);
	}
	@Override
	public boolean stillValid(@NotNull Player player){
		if(this.bound&&this.boundBlockEntity!=null)
			return AbstractContainerMenu.stillValid(this.access,player,this.boundBlockEntity.getBlockState().getBlock());
		return true;
	}
	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn,int index){
		ItemStack itemstack=ItemStack.EMPTY;
		Slot slot=this.slots.get(index);
		if(slot.hasItem()){
			ItemStack itemstack1=slot.getItem();
			itemstack=itemstack1.copy();
			if(index<1){
				if(!this.moveItemStackTo(itemstack1,1,this.slots.size(),true)) return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1,itemstack);
			}else if(!this.moveItemStackTo(itemstack1,0,1,false)){
				if(index<1+27){
					if(!this.moveItemStackTo(itemstack1,1+27,this.slots.size(),true)) return ItemStack.EMPTY;
				}else{
					if(!this.moveItemStackTo(itemstack1,1,1+27,false)) return ItemStack.EMPTY;
				}
				return ItemStack.EMPTY;
			}
			if(itemstack1.getCount()==0) slot.set(ItemStack.EMPTY);
			else slot.setChanged();
			if(itemstack1.getCount()==itemstack.getCount()) return ItemStack.EMPTY;
			slot.onTake(playerIn,itemstack1);
		}
		return itemstack;
	}
	@Override
	public void removed(@NotNull Player playerIn){
		super.removed(playerIn);
		if(!bound&&playerIn instanceof ServerPlayer serverPlayer){
			if(!serverPlayer.isAlive()||serverPlayer.hasDisconnected()){
				for(int j=0;j<internal.getSlots();++j)
					playerIn.drop(internal.extractItem(j,internal.getStackInSlot(j).getCount(),false),false);
			}else{
				for(int i=0;i<internal.getSlots();++i)
					playerIn.getInventory().placeItemBackInInventory(internal.extractItem(i,internal.getStackInSlot(i).getCount(),false));
			}
		}
	}
	@Override
	public Map<Integer,Slot> get(){
		return customSlots;
	}
}