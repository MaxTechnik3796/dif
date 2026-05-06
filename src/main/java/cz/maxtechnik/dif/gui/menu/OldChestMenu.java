package cz.maxtechnik.dif.gui.menu;

import cz.maxtechnik.dif.block.entity.OldChestBlockEntity;
import cz.maxtechnik.dif.init.gui.DifModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
public class OldChestMenu extends AbstractContainerMenu implements Supplier<Map<Integer,Slot>>{
	// Konstanty pro rozložení
	private static final int SLOT_X_SPACING=18;
	private static final int SLOT_Y_SPACING=18;
	private static final int CHEST_START_X=8;
	private static final int CHEST_START_Y=18;
	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess mainAccess=ContainerLevelAccess.NULL;
	// Seznam všech handlerů (hlavní + sousedé)
	private final List<IItemHandler> connectedHandlers=new ArrayList<>();
	private final Map<Integer,Slot> customSlots=new HashMap<>();
	private boolean bound=false;
	private BlockEntity boundBlockEntity=null;
	// Celkový počet slotů ve všech bednách dohromady
	private int totalChestSlots=0;
	// Počet řádků beden (pro GUI renderování)
	private int chestRows=0;
	public OldChestMenu(int id,Inventory inv,FriendlyByteBuf extraData){
		super(DifModMenus.OLD_CHEST.get(),id);
		this.entity=inv.player;
		this.world=inv.player.level();
		BlockPos pos=null;
		if(extraData!=null){
			pos=extraData.readBlockPos();
			this.x=pos.getX();
			this.y=pos.getY();
			this.z=pos.getZ();
			mainAccess=ContainerLevelAccess.create(world,pos);
		}
		// 1. Najdeme hlavní blok a sousedy
		if(pos!=null){
			this.boundBlockEntity=this.world.getBlockEntity(pos);
			// Přidáme hlavní blok jako první
			addBlockHandler(this.boundBlockEntity);
			// Zkontrolujeme sousedy (North, East, South, West)
			checkNeighbor(pos.north());
			checkNeighbor(pos.east());
			checkNeighbor(pos.south());
			checkNeighbor(pos.west());
			// Spustíme animaci otevření pro hlavní blok
			if(this.boundBlockEntity instanceof OldChestBlockEntity blockEntity){
				blockEntity.startOpen(inv.player);
			}
		}
		// Fallback
		if(connectedHandlers.isEmpty()){
			this.connectedHandlers.add(new ItemStackHandler(27));
		}
		// 2. Generování slotů pro všechny nalezené bedny
		int currentY=CHEST_START_Y;
		int totalIndex=0;
		this.chestRows=0;
		for(IItemHandler handler: connectedHandlers){
			int slots=handler.getSlots();
			int rows=slots/9;
			if(slots%9!=0) rows++;
			this.chestRows+=rows; // Počítáme celkové řádky
			for(int i=0;i<slots;i++){
				int row=i/9;
				int col=i%9;
				// Výpočet pozice slotu. Sloty se skládají pod sebe.
				// Pro vizuální centrování na obrazovce se postará Screen třída,
				// ale zde musíme zajistit, že sloty mají správné relativní souřadnice v rámci kontejneru.
				int slotX=CHEST_START_X+col*SLOT_X_SPACING;
				int slotY=currentY+row*SLOT_Y_SPACING;
				SlotItemHandler newSlot=new SlotItemHandler(handler,i,slotX,slotY);
				this.addSlot(newSlot);
				this.customSlots.put(totalIndex,newSlot);
				totalIndex++;
			}
			// Posuneme Y pro další bednu
			currentY+=rows*SLOT_Y_SPACING;
		}
		this.totalChestSlots=totalIndex;
		// 3. Generování inventáře hráče
		// Pozice inventáře hráče závisí na počtu řádků beden.
		// Standardní mezera mezi bednou a inventářem je cca 14 pixelů (v textuře 18-4=14)
		// Pokud je 1 bedna (3 řádky), Y je 18 + 3*18 + 14 = 86 (cca 84 ve vanilla)
		// Tady to vypočítáme dynamicky:
		int playerInvY=currentY+13; // 13px mezera
		for(int si=0;si<3;++si){
			for(int sj=0;sj<9;++sj){
				this.addSlot(new Slot(inv,sj+(si+1)*9,8+sj*18,playerInvY+si*18));
			}
		}
		for(int si=0;si<9;++si){
			this.addSlot(new Slot(inv,si,8+si*18,playerInvY+58));
		}
	}
	/**
	 * Pomocná metoda pro kontrolu souseda.
	 */
	private void checkNeighbor(BlockPos neighborPos){
		if(this.boundBlockEntity==null) return;
		BlockState mainState=this.boundBlockEntity.getBlockState();
		BlockState neighborState=this.world.getBlockState(neighborPos);
		if(mainState.getBlock()==neighborState.getBlock()){
			BlockEntity neighborBE=this.world.getBlockEntity(neighborPos);
			addBlockHandler(neighborBE);
		}
	}
	private void addBlockHandler(BlockEntity be){
		if(be!=null){
			IItemHandler capability = world.getCapability(Capabilities.ItemHandler.BLOCK, be.getBlockPos(), null);
			if(capability != null) {
				this.connectedHandlers.add(capability);
				this.bound=true;
			}
		}
	}
	// Getter pro počet řádků (pro Screen, aby věděl jak velké okno vykreslit)
	public int getChestRows(){
		return this.chestRows;
	}
	@Override
	public boolean stillValid(@NotNull Player player){
		if(this.bound){
			if(this.boundBlockEntity!=null)
				return AbstractContainerMenu.stillValid(this.mainAccess,player,this.boundBlockEntity.getBlockState().getBlock());
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
			if(index<this.totalChestSlots){
				if(!this.moveItemStackTo(itemstack1,this.totalChestSlots,this.slots.size(),true))
					return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1,itemstack);
			}else if(!this.moveItemStackTo(itemstack1,0,this.totalChestSlots,false)){
				if(index<this.totalChestSlots+27){
					if(!this.moveItemStackTo(itemstack1,this.totalChestSlots+27,this.slots.size(),true))
						return ItemStack.EMPTY;
				}else{
					if(!this.moveItemStackTo(itemstack1,this.totalChestSlots,this.totalChestSlots+27,false))
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
	public void removed(@NotNull Player playerIn){
		if(this.boundBlockEntity instanceof OldChestBlockEntity blockEntity){
			blockEntity.stopOpen(playerIn);
		}
		super.removed(playerIn);
	}
	public Map<Integer,Slot> get(){
		return customSlots;
	}
	public BlockEntity getBlockEntity(){
		return this.boundBlockEntity;
	}
}