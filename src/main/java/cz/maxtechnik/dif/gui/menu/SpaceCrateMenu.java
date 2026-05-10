package cz.maxtechnik.dif.gui.menu;

import cz.maxtechnik.dif.block.entity.SpaceCrateBlockEntity;
import cz.maxtechnik.dif.init.gui.DifModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SpaceCrateMenu extends AbstractContainerMenu implements Supplier<Map<Integer,Slot>>{
	private static final int ROWS=3;
	private static final int SLOTS_PER_ROW=9;
	private static final int CONTAINER_SLOTS=ROWS*SLOTS_PER_ROW;
	public final Level world;
	public final Player entity;
	public int x,y,z;
	private ContainerLevelAccess access=ContainerLevelAccess.NULL;
	private final Map<Integer,Slot> customSlots=new HashMap<>();
	private BlockEntity boundBlockEntity=null;

	public SpaceCrateMenu(int id,Inventory inv,FriendlyByteBuf extraData){
		super(DifModMenus.SPACE_CRATE.get(),id);
		this.entity=inv.player;
		this.world=inv.player.level();
		IItemHandler internal=new ItemStackHandler(CONTAINER_SLOTS);
		if(extraData!=null){
			BlockPos pos=extraData.readBlockPos();
			this.x=pos.getX(); this.y=pos.getY(); this.z=pos.getZ();
			access=ContainerLevelAccess.create(world,pos);
			boundBlockEntity=world.getBlockEntity(pos);
			if(boundBlockEntity!=null){
				IItemHandler handler=world.getCapability(Capabilities.ItemHandler.BLOCK,pos,null);
				if(handler!=null) internal=handler;
				if(boundBlockEntity instanceof SpaceCrateBlockEntity be) be.startOpen(inv.player);
			}
		}
		int index=0;
		for(int row=0;row<ROWS;row++)
			for(int col=0;col<SLOTS_PER_ROW;col++)
				customSlots.put(index,this.addSlot(new SlotItemHandler(internal,index++,8+col*18,18+row*18)));
		for(int si=0;si<3;si++)
			for(int sj=0;sj<9;sj++)
				this.addSlot(new Slot(inv,sj+(si+1)*9,8+sj*18,84+si*18));
		for(int si=0;si<9;si++)
			this.addSlot(new Slot(inv,si,8+si*18,142));
	}

	public int getRows(){ return ROWS; }

	@Override
	public boolean stillValid(@NotNull Player player){
		if(boundBlockEntity!=null)
			return AbstractContainerMenu.stillValid(access,player,boundBlockEntity.getBlockState().getBlock());
		return true;
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player,int index){
		ItemStack itemstack=ItemStack.EMPTY;
		Slot slot=this.slots.get(index);
		if(slot.hasItem()){
			ItemStack itemstack1=slot.getItem();
			itemstack=itemstack1.copy();
			if(index<CONTAINER_SLOTS){
				if(!this.moveItemStackTo(itemstack1,CONTAINER_SLOTS,this.slots.size(),true)) return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1,itemstack);
			}else if(!this.moveItemStackTo(itemstack1,0,CONTAINER_SLOTS,false)){
				if(index<CONTAINER_SLOTS+27){
					if(!this.moveItemStackTo(itemstack1,CONTAINER_SLOTS+27,this.slots.size(),true)) return ItemStack.EMPTY;
				}else{
					if(!this.moveItemStackTo(itemstack1,CONTAINER_SLOTS,CONTAINER_SLOTS+27,false)) return ItemStack.EMPTY;
				}
				return ItemStack.EMPTY;
			}
			if(itemstack1.getCount()==0) slot.set(ItemStack.EMPTY);
			else slot.setChanged();
			if(itemstack1.getCount()==itemstack.getCount()) return ItemStack.EMPTY;
			slot.onTake(player,itemstack1);
		}
		return itemstack;
	}

	@Override
	public void removed(@NotNull Player player){
		if(boundBlockEntity instanceof SpaceCrateBlockEntity be) be.stopOpen(player);
		super.removed(player);
	}

	@Override public Map<Integer,Slot> get(){ return customSlots; }
	public BlockEntity getBlockEntity(){ return boundBlockEntity; }
}