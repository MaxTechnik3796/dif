package cz.maxtechnik.dif.gui.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import cz.maxtechnik.dif.block.entity.SpecialCrafting;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.gui.DifModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class SpecialCraftingMenu extends RecipeBookMenu<CraftingContainer>{
	public static final int RESULT_SLOT=0;
	public static final int CRAFT_SLOT_START=1;
	public static final int CRAFT_SLOT_END=10;
	public static final int INV_SLOT_START=10;
	public static final int INV_SLOT_END=37;
	public static final int BAR_SLOT_START=37;
	public static final int BAR_SLOT_END=46;
	public final CraftingContainer craftSlots=new TransientCraftingContainer(this, 3, 3);
	public final ResultContainer resultSlots=new ResultContainer();
	private IItemHandler internal;
	public final Player player;
	private ContainerLevelAccess access=ContainerLevelAccess.NULL;
	public final Level world;
	public int x,y,z;
	private final Map<Integer,Slot> customSlots=new HashMap<>();
	private boolean bound=false;
	private BlockEntity boundBlockEntity=null;


	public SpecialCraftingMenu(int id,Inventory inventory,FriendlyByteBuf extraData) {
		super(DifModMenus.SPECIAL_CRAFTING_MENU.get(),id);
		this.player=inventory.player;
		this.world=inventory.player.level();
		this.internal=new ItemStackHandler(1);
		BlockPos pos=null;
		if(extraData!=null) {
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
		if (blockEntityFromWorld instanceof SpecialCrafting specialCraftingBlockEntity){
			this.internal=specialCraftingBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER,null).orElse(new ItemStackHandler(2));
			this.bound=true;
			this.boundBlockEntity=specialCraftingBlockEntity;
		} else {
			this.internal=new ItemStackHandler(2);
		}
		int slotSize=18;
		int gridSlotOffsetX=30;
		int gridSlotOffsetY=17;
		int invSlotOffsetX=8;
		int invSlotOffsetY=84;
		int barSlotOffsetY=142;
		this.addSlot(new ResultSlot(inventory.player,this.craftSlots,this.resultSlots,0,124,35));
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				this.addSlot(new SlotItemHandler(internal,j+3*i,gridSlotOffsetX+slotSize*j,gridSlotOffsetY+slotSize*i));
			}
		}
		for(int i=0;i<3;i++){
			for(int j=0;j<9;j++){
				this.addSlot(new Slot(inventory,9+j+9*i,invSlotOffsetX+slotSize*j,invSlotOffsetY+slotSize*i));
			}
		}
		for(int i=0;i<9;i++){
			this.addSlot(new Slot(inventory,i,invSlotOffsetX+slotSize*i,barSlotOffsetY));
		}
	}
	protected static void slotChangedCraftingGrid(AbstractContainerMenu menu,Level world,Player player,CraftingContainer container,ResultContainer result) {
      if (!world.isClientSide) {
         ServerPlayer serverplayer = (ServerPlayer)player;
         ItemStack itemstack = ItemStack.EMPTY;
         Optional<CraftingRecipe> optional = Objects.requireNonNull(world.getServer()).getRecipeManager().getRecipeFor(RecipeType.CRAFTING,container,world);
         if (optional.isPresent()) {
            CraftingRecipe craftingrecipe = optional.get();
            if (result.setRecipeUsed(world, serverplayer, craftingrecipe)) {
               ItemStack itemstack1 = craftingrecipe.assemble(container, world.registryAccess());
               if (itemstack1.isItemEnabled(world.enabledFeatures())) {
                  itemstack = itemstack1;
               }
            }
         }
         result.setItem(0, itemstack);
         menu.setRemoteSlot(0, itemstack);
         serverplayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId,menu.incrementStateId(), 0, itemstack));
      }
   }
	public void slotsChanged(@NotNull Container container) {
      this.access.execute((world,pos)->slotChangedCraftingGrid(this,world, this.player, this.craftSlots, this.resultSlots));
   }
	public void fillCraftSlotsStackedContents(@NotNull StackedContents stackedContents) {
      this.craftSlots.fillStackedContents(stackedContents);
   }
	public void clearCraftingContent() {
      this.craftSlots.clearContent();
      this.resultSlots.clearContent();
   }
	public boolean recipeMatches(@NotNull Recipe<? super CraftingContainer>container){
      return container.matches(this.craftSlots,this.player.level());
   }
	public void removed(@NotNull Player player){
		super.removed(player);
		if (!bound && player instanceof ServerPlayer serverPlayer) {
			if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) {
				for (int j = 0; j < internal.getSlots(); ++j) {
					player.drop(internal.extractItem(j, internal.getStackInSlot(j).getCount(), false), false);
				}
			} else {
				for (int i = 0; i < internal.getSlots(); ++i) {
					player.getInventory().placeItemBackInInventory(internal.extractItem(i, internal.getStackInSlot(i).getCount(), false));
				}
			}
		}
	}
	public boolean stillValid(@NotNull Player player) {
      return stillValid(this.access, player,DifModBlocks.EXAMPLE_BLOCK.get());
   }
	public @NotNull ItemStack quickMoveStack(@NotNull Player player,int id) {
      ItemStack itemstack=ItemStack.EMPTY;
      Slot slot=this.slots.get(id);
      if(slot.hasItem()){
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if(id==0){
            this.access.execute((p_39378_,p_39379_)->itemstack1.getItem().onCraftedBy(itemstack1, p_39378_, player));
            if (!this.moveItemStackTo(itemstack1, 10, 46, true)) {
               return ItemStack.EMPTY;
            }
            slot.onQuickCraft(itemstack1, itemstack);
         }else if(id>=10&&id<46){
            if (!this.moveItemStackTo(itemstack1, 1, 10, false)) {
               if (id< 37) {
                  if (!this.moveItemStackTo(itemstack1, 37, 46, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (!this.moveItemStackTo(itemstack1, 10, 37, false)) {
                  return ItemStack.EMPTY;
               }
            }
         } else if (!this.moveItemStackTo(itemstack1, 10, 46, false)) {
            return ItemStack.EMPTY;
         }
         if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }
         slot.onTake(player, itemstack1);
         if (id== 0) {
            player.drop(itemstack1, false);
         }
      }
      return itemstack;
   }
	public boolean canTakeItemForPickAll(@NotNull ItemStack itemStack,Slot slot) {
      return slot.container!=this.resultSlots && super.canTakeItemForPickAll(itemStack,slot);
   }
	public int getResultSlotIndex() {
      return 0;
   }
	public int getGridWidth() {
      return 3;
   }
	public int getGridHeight() {
      return 3;
   }
	public int getSize() {
      return 10;
   }
	public @NotNull RecipeBookType getRecipeBookType() {
      return RecipeBookType.CRAFTING;
   }
	public boolean shouldMoveToInventory(int id) {
      return id!=this.getResultSlotIndex();
   }
}