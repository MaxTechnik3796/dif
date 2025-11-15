package cz.maxtechnik.dif.gui.menu;

import java.util.Objects;
import java.util.Optional;

import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.gui.DifModMenus;
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
import org.jetbrains.annotations.NotNull;

public class SpecialCraftingMenu extends RecipeBookMenu<CraftingContainer>{
	public static final int RESULT_SLOT = 0;
	public static final int CRAFT_SLOT_START = 1;
	public static final int CRAFT_SLOT_END = 10;
	public static final int INV_SLOT_START = 10;
	public static final int INV_SLOT_END = 37;
	public static final int USE_ROW_SLOT_START = 37;
	public static final int USE_ROW_SLOT_END = 46;
	public final CraftingContainer craftSlots = new TransientCraftingContainer(this, 3, 3);
	public final ResultContainer resultSlots = new ResultContainer();
	public final ContainerLevelAccess access;
	public final Player player;

   public SpecialCraftingMenu(int id,Inventory inventory,FriendlyByteBuf friendlyByteBuf) {
      this(id,inventory, ContainerLevelAccess.NULL);
   }

   public SpecialCraftingMenu(int id,Inventory inventory,ContainerLevelAccess access) {
      super(DifModMenus.SPECIAL_CRAFTING_MENU.get(),id);
      this.access =access;
      this.player = inventory.player;
      this.addSlot(new ResultSlot(inventory.player, this.craftSlots, this.resultSlots, 0, 124, 35));

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 3; ++j) {
            this.addSlot(new Slot(this.craftSlots, j + i * 3, 30 + j * 18, 17 + i * 18));
         }
      }

      for(int k = 0; k < 3; ++k) {
         for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(inventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
         }
      }

      for(int l = 0; l < 9; ++l) {
         this.addSlot(new Slot(inventory, l, 8 + l * 18, 142));
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
   public void removed(@NotNull Player player) {
      super.removed(player);
      this.access.execute((world, pos) ->this.clearContainer(player, this.craftSlots));
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
      return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemStack,slot);
   }
   public int getResultSlotIndex() {
      return 0;
   }
   public int getGridWidth() {
      return this.craftSlots.getWidth();
   }
   public int getGridHeight() {
      return this.craftSlots.getHeight();
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