package cz.maxtechnik.dif.gui.menu;

import cz.maxtechnik.dif.block.entity.CopperBarrel;
import cz.maxtechnik.dif.init.gui.DifModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
public class CopperBarrelMenu extends AbstractContainerMenu implements Supplier<Map<Integer,Slot>>{
	// Konstanty pro 4-řádkovou bednu
	private static final int ROWS=5;
	private static final int SLOTS_PER_ROW=9;
	private static final int CONTAINER_SLOTS=ROWS*SLOTS_PER_ROW; // 45
	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess access=ContainerLevelAccess.NULL;
	private IItemHandler internal;
	private final Map<Integer,Slot> customSlots=new HashMap<>();
	private boolean bound=false;
	private BlockEntity boundBlockEntity=null;
	public CopperBarrelMenu(int id,Inventory inv,FriendlyByteBuf extraData){
		super(DifModMenus.COPPER_BARREL_MENU.get(),id);
		this.entity=inv.player;
		this.world=inv.player.level();
		this.internal=new ItemStackHandler(CONTAINER_SLOTS); // 45 slotů
		BlockPos pos=null;
		if(extraData!=null){
			pos=extraData.readBlockPos();
			this.x=pos.getX();
			this.y=pos.getY();
			this.z=pos.getZ();
			access=ContainerLevelAccess.create(world,pos);
		}
		if(pos!=null){
			boundBlockEntity=this.world.getBlockEntity(pos);
			if(boundBlockEntity!=null){
				boundBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER,null).ifPresent(capability->{
					this.internal=capability;
					this.bound=true;
				});
				if(boundBlockEntity instanceof CopperBarrel be){
					be.startOpen(inv.player);
				}
			}
		}
		// --- Generování slotů barelu (4 řádky) ---
		int startX=8;
		int startY=18;
		int index=0;
		for(int row=0;row<ROWS;row++){
			for(int col=0;col<SLOTS_PER_ROW;col++){
				int sx=startX+col*18;
				int sy=startY+row*18;
				this.customSlots.put(index,this.addSlot(new SlotItemHandler(internal,index,sx,sy)));
				index++;
			}
		}
		// --- Generování inventáře hráče ---
		// Pozice Y pro inventář hráče se vypočítá tak, aby navazovala na 4. řádek barelu.
		// 18 (start) + 4 * 18 (barel) + 14 (mezera) = 104
		int invOffsetX=8;
		int invOffsetY=startY+(ROWS*18)+13; // Automatický výpočet Y (cca 104)
		for(int si=0;si<3;++si){
			for(int sj=0;sj<9;++sj){
				this.addSlot(new Slot(inv,sj+(si+1)*9,invOffsetX+sj*18,invOffsetY+si*18));
			}
		}
		for(int si=0;si<9;++si){
			this.addSlot(new Slot(inv,si,invOffsetX+si*18,invOffsetY+58));
		}
	}
	// Getter pro počet řádků (použije Screen pro výpočet výšky)
	public int getRows(){
		return ROWS;
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
			// Pokud je to slot v barelu (index < 45)
			if(index<CONTAINER_SLOTS){
				if(!this.moveItemStackTo(itemstack1,CONTAINER_SLOTS,this.slots.size(),true))
					return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1,itemstack);
			}
			// Pokud je to v inventáři hráče
			else if(!this.moveItemStackTo(itemstack1,0,CONTAINER_SLOTS,false)){
				if(index<CONTAINER_SLOTS+27){
					if(!this.moveItemStackTo(itemstack1,CONTAINER_SLOTS+27,this.slots.size(),true))
						return ItemStack.EMPTY;
				}else{
					if(!this.moveItemStackTo(itemstack1,CONTAINER_SLOTS,CONTAINER_SLOTS+27,false))
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
	// ... (moveItemStackTo, removed, get, getBlockEntity zůstávají stejné)
	@Override
	public void removed(@NotNull Player playerIn){
		if(this.boundBlockEntity instanceof CopperBarrel be){
			be.stopOpen(playerIn);
		}
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
	public BlockEntity getBlockEntity(){
		return this.boundBlockEntity;
	}
}