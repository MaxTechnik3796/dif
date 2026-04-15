package cz.maxtechnik.dif.gui.menu;

import com.mojang.datafixers.util.Pair;
import cz.maxtechnik.dif.init.events.MegaBackpackSavedData;
import cz.maxtechnik.dif.init.gui.DifModMenus;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
public class MegaBackpackMenu extends AbstractContainerMenu{
	public static final int ROWS=13;
	public static final int COLS=17;
	public static final int SLOTS_PER_PAGE=ROWS*COLS;
	private final CraftingContainer craftSlots=new TransientCraftingContainer(this,3,3);
	private final ResultContainer resultSlots=new ResultContainer();
	private int currentPage=0;
	private final Container backpackInventory;
	private final Player player;
	// Konstruktor pro klienta (Forge ho volá při otevírání)
	public MegaBackpackMenu(int id,Inventory playerInv,FriendlyByteBuf extraData){
		this(id,playerInv); // Volá hlavní konstruktor níže
		if(extraData!=null&&extraData.readableBytes()>=4){
			extraData.readInt();
		}
	}
	// Hlavní konstruktor
	public MegaBackpackMenu(int id,Inventory playerInv){
		super(DifModMenus.MEGA_BACKPACK.get(),id);
		this.player=playerInv.player;
		this.backpackInventory=new SimpleContainer(SLOTS_PER_PAGE);
		// NAČTENÍ DAT ZE SERVERU
		if(!playerInv.player.level().isClientSide){
			MegaBackpackSavedData data=MegaBackpackSavedData.get(playerInv.player.level());
			loadPage(data.getOrCreateInventory(playerInv.player.getUUID()));
		}
		// 1. Sloty Batohu (13x9)
		for(int i=0;i<ROWS;i++){
			for(int j=0;j<COLS;j++){
				this.addSlot(new Slot(backpackInventory,j+i*COLS,171+j*18,13+i*18));
			}
		}
		this.addDataSlot(new DataSlot(){
			@Override
			public int get(){
				return currentPage;
			}
			@Override
			public void set(int value){
				currentPage=value;
			}
		});
		// 2. Sloty Hráče
		addPlayerInventory(playerInv);
		addCrafting(playerInv);
	}
	private void saveCurrentPage(){
		if(player.level().isClientSide) return;
		MegaBackpackSavedData data=MegaBackpackSavedData.get(player.level());
		NonNullList<ItemStack> allItems=data.getOrCreateInventory(player.getUUID());
		int startOffset=currentPage*SLOTS_PER_PAGE;
		for(int i=0;i<SLOTS_PER_PAGE;i++){
			// ZDE: Vždy používej .copy(), jinak se itemy "přesouvají" místo ukládání
			allItems.set(startOffset+i,backpackInventory.getItem(i).copy());
		}
		data.setDirty();
	}
	private void loadPage(NonNullList<ItemStack> allItems){
		int startOffset=currentPage*SLOTS_PER_PAGE;
		for(int i=0;i<SLOTS_PER_PAGE;i++){
			// ZDE: Také při načítání je lepší copy, aby container vlastnil unikátní instanci
			backpackInventory.setItem(i,allItems.get(startOffset+i).copy());
		}
	}
	public void changePage(int delta){
		if(player.level().isClientSide) return;
		// 1. Uložit itemy z aktuální stránky do celkového seznamu
		saveCurrentPage();
		// 2. Posunout index stránky (0-15 pro 16 stránek)
		this.currentPage=Math.max(0,Math.min(15,currentPage+delta));
		// 3. Načíst itemy z nové stránky do slotů
		MegaBackpackSavedData data=MegaBackpackSavedData.get(player.level());
		loadPage(data.getOrCreateInventory(player.getUUID()));
		// 4. Klíčový krok: Poslat změnu klientovi, aby se mu překreslily ikonky itemů
		this.broadcastChanges();
	}
	private void addPlayerInventory(Inventory playerInv){
		int startY=171;
		int startX=5;
		for(int i=0;i<3;++i){
			for(int j=0;j<9;++j){
				this.addSlot(new Slot(playerInv,j+i*9+9,startX+j*18,startY+i*18));
			}
		}
		for(int k=0;k<9;++k){
			this.addSlot(new Slot(playerInv,k,startX+k*18,startY+58));
		}
		for(int i=0;i<4;i++){
			final EquipmentSlot slotType=EquipmentSlot.values()[5-i]; // START_INDEX pro armor
			int finalI=i;
			this.addSlot(new Slot(playerInv,39-finalI,5,81+finalI*18){
				@Override
				public int getMaxStackSize(){
					return 1;
				}
				@Override
				public boolean mayPlace(@NotNull ItemStack stack){
					return stack.canEquip(slotType,player);
				}
				@Override
				public Pair<ResourceLocation,ResourceLocation> getNoItemIcon(){
					return switch(finalI){
						case 0 -> Pair.of(InventoryMenu.BLOCK_ATLAS,InventoryMenu.EMPTY_ARMOR_SLOT_HELMET);
						case 1 -> Pair.of(InventoryMenu.BLOCK_ATLAS,InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE);
						case 2 -> Pair.of(InventoryMenu.BLOCK_ATLAS,InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS);
						case 3 -> Pair.of(InventoryMenu.BLOCK_ATLAS,InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS);
						default -> throw new IllegalStateException("Unexpected value: "+finalI);
					};
				}
			});
		}
		this.addSlot(new Slot(playerInv,40,74,135){
			@Override
			public Pair<ResourceLocation,ResourceLocation> getNoItemIcon(){
				return Pair.of(InventoryMenu.BLOCK_ATLAS,InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
			}
		});
	}
	private void addCrafting(Inventory playerInv){
		this.addSlot(new ResultSlot(playerInv.player,this.craftSlots,this.resultSlots,0,131,107));
		for(int row=0;row<3;row++){
			for(int col=0;col<3;col++){
				this.addSlot(new Slot(this.craftSlots,col+row*3,113+col*18,28+row*18));
			}
		}
	}
	public int getCurrentPage(){
		return this.currentPage;
	}
	@Override
	public boolean stillValid(@NotNull Player pPlayer){
		return true;
	}
	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer,int pIndex){
		// Tady by měla být logika pro Shift-Click, zatím vracíme prázdno
		return ItemStack.EMPTY;
	}
	@Override
	public void slotsChanged(@NotNull Container container){
		if(!this.player.level().isClientSide){
			// Získáme recept jako Optional
			var recipeOptional=this.player.level().getRecipeManager()
					.getRecipeFor(RecipeType.CRAFTING,this.craftSlots,this.player.level());
			if(recipeOptional.isPresent()){
				// Zkus .value() nebo .get() podle toho, co tvoje verze podporuje
				CraftingRecipe recipe=recipeOptional.get();
				this.resultSlots.setItem(0,recipe.assemble(this.craftSlots,this.player.level().registryAccess()));
			}else{
				this.resultSlots.setItem(0,ItemStack.EMPTY);
			}
			this.broadcastChanges();
		}
	}
	@Override
	public void removed(@NotNull Player player){
		super.removed(player);
		if(!player.level().isClientSide){
			saveCurrentPage();
		}
		this.clearContainer(player,this.craftSlots);
	}
}