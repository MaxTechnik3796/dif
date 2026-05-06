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

import net.neoforged.neoforge.capabilities.Capabilities;
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
	public BurningGeneratorMenu(int id, Inventory inv, FriendlyByteBuf extraData){
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
		BlockEntity blockEntityFromWorld=null;
		if(pos!=null){
			blockEntityFromWorld=this.world.getBlockEntity(pos);
		}
		if(blockEntityFromWorld instanceof BurningGeneratorBlockEntity generatorBlockEntity){
			IItemHandler cap = world.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
			this.internal = cap != null ? cap : new ItemStackHandler(2);
			this.data=generatorBlockEntity.dataAccess;
			this.bound=true;
			this.boundBlockEntity=generatorBlockEntity;
		}else{
			this.internal=new ItemStackHandler(2);
			this.data=new SimpleContainerData(3);
		}
		this.addDataSlots(this.data);
		this.customSlots.put(0,this.addSlot(new SlotItemHandler(internal,0,79,35){
		}));
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
		if(this.bound){
			if(this.boundBlockEntity!=null)
				return AbstractContainerMenu.stillValid(this.access,player,this.boundBlockEntity.getBlockState().getBlock());
		}
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
				if(!this.moveItemStackTo(itemstack1,1,this.slots.size(),true))
					return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1,itemstack);
			}else if(!this.moveItemStackTo(itemstack1,0,1,false)){
				if(index<1+27){
					if(!this.moveItemStackTo(itemstack1,1+27,this.slots.size(),true))
						return ItemStack.EMPTY;
				}else{
					if(!this.moveItemStackTo(itemstack1,1,1+27,false))
						return ItemStack.EMPTY;
				}
				return ItemStack.EMPTY;
			}
			if(itemstack1.getCount()==0)
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();
			if(itemstack1.getCount()==itemstack.getCount())
				return ItemStack.EMPTY;
			slot.onTake(playerIn,itemstack1);
		}
		return itemstack;
	}
	@Override
	protected boolean moveItemStackTo(@NotNull ItemStack p_38904_,int p_38905_,int p_38906_,boolean p_38907_){
		boolean flag=false;
		int i=p_38905_;
		if(p_38907_){
			i=p_38906_-1;
		}
		if(p_38904_.isStackable()){
			while(!p_38904_.isEmpty()){
				if(p_38907_){
					if(i<p_38905_){
						break;
					}
				}else if(i>=p_38906_){
					break;
				}
				Slot slot=this.slots.get(i);
				ItemStack itemstack=slot.getItem();
				if(slot.mayPlace(itemstack)&&!itemstack.isEmpty()&&ItemStack.isSameItemSameComponents(p_38904_,itemstack)){
					int j=itemstack.getCount()+p_38904_.getCount();
					int maxSize=Math.min(slot.getMaxStackSize(),p_38904_.getMaxStackSize());
					if(j<=maxSize){
						p_38904_.setCount(0);
						itemstack.setCount(j);
						slot.set(itemstack);
						flag=true;
					}else if(itemstack.getCount()<maxSize){
						p_38904_.shrink(maxSize-itemstack.getCount());
						itemstack.setCount(maxSize);
						slot.set(itemstack);
						flag=true;
					}
				}
				if(p_38907_){
					--i;
				}else{
					++i;
				}
			}
		}
		if(!p_38904_.isEmpty()){
			if(p_38907_){
				i=p_38906_-1;
			}else{
				i=p_38905_;
			}
			while(true){
				if(p_38907_){
					if(i<p_38905_){
						break;
					}
				}else if(i>=p_38906_){
					break;
				}
				Slot slot1=this.slots.get(i);
				ItemStack itemstack1=slot1.getItem();
				if(itemstack1.isEmpty()&&slot1.mayPlace(p_38904_)){
					if(p_38904_.getCount()>slot1.getMaxStackSize()){
						slot1.setByPlayer(p_38904_.split(slot1.getMaxStackSize()));
					}else{
						slot1.setByPlayer(p_38904_.split(p_38904_.getCount()));
					}
					slot1.setChanged();
					flag=true;
					break;
				}
				if(p_38907_){
					--i;
				}else{
					++i;
				}
			}
		}
		return flag;
	};
	@Override
	public void removed(@NotNull Player playerIn){
		super.removed(playerIn);
		if(!bound&&playerIn instanceof ServerPlayer serverPlayer){
			if(!serverPlayer.isAlive()||serverPlayer.hasDisconnected()){
				for(int j=0;j<internal.getSlots();++j){
					playerIn.drop(internal.extractItem(j,internal.getStackInSlot(j).getCount(),false),false);
				}
			}else{
				for(int i=0;i<internal.getSlots();++i){
					playerIn.getInventory().placeItemBackInInventory(internal.extractItem(i,internal.getStackInSlot(i).getCount(),false));
				}
			}
		}
	}
	public Map<Integer,Slot> get(){
		return customSlots;
	}
}
