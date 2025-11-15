package cz.maxtechnik.dif.gui.menu;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class PortableCraftingMenu extends RecipeBookMenu<CraftingContainer>{
	public static final int RESULT_SLOT = 0;
	private static final int CRAFT_SLOT_START = 1;
	private static final int CRAFT_SLOT_END = 10;
	private static final int INV_SLOT_START = 10;
	private static final int INV_SLOT_END = 37;
	private static final int USE_ROW_SLOT_START = 37;
	private static final int USE_ROW_SLOT_END = 46;
	final CraftingContainer craftSlots = new TransientCraftingContainer(this, 3, 3);
	private final Player player;
    private final Container craftingContainer;
	private final ResultContainer resultSlots = new ResultContainer();
	private final ContainerLevelAccess access;

	// Konstruktor, který přijímá pouze ID, inventář hráče a úroveň,
    // kde se CraftingMenu vytvoří (ContainerLevelAccess je ignorován)
    public PortableCraftingMenu(int id, Inventory inventory, Level level) {
        // Zavoláme rodičovský konstruktor, kde ContainerLevelAccess.NULL říká,
        // že se nejedná o Crafting Table blok.
		super(MenuType.CRAFTING,id);
		this.access =ContainerLevelAccess.NULL;
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

		//this.player = inventory.player;
        this.craftingContainer = this.craftSlots; // Crafting grid je uložen v craftSlots
    }

	protected static void slotChangedCraftingGrid(AbstractContainerMenu p_150547_,Level p_150548_,Player p_150549_,CraftingContainer p_150550_,ResultContainer p_150551_) {
	   if (!p_150548_.isClientSide) {
		  ServerPlayer serverplayer = (ServerPlayer)p_150549_;
		  ItemStack itemstack = ItemStack.EMPTY;
		  Optional<CraftingRecipe> optional = Objects.requireNonNull(p_150548_.getServer()).getRecipeManager().getRecipeFor(RecipeType.CRAFTING, p_150550_, p_150548_);
		  if (optional.isPresent()) {
			 CraftingRecipe craftingrecipe = optional.get();
			 if (p_150551_.setRecipeUsed(p_150548_, serverplayer, craftingrecipe)) {
				ItemStack itemstack1 = craftingrecipe.assemble(p_150550_, p_150548_.registryAccess());
				if (itemstack1.isItemEnabled(p_150548_.enabledFeatures())) {
				   itemstack = itemstack1;
				}
			 }
		  }

		  p_150551_.setItem(0, itemstack);
		  p_150547_.setRemoteSlot(0, itemstack);
		  serverplayer.connection.send(new ClientboundContainerSetSlotPacket(p_150547_.containerId, p_150547_.incrementStateId(), 0, itemstack));
	   }
	}

	// TATO KLÍČOVÁ METODA ZAJIŠŤUJE VRÁCENÍ PŘEDMĚTŮ PŘI ZAVŘENÍ
    @Override
    public void removed(@NotNull Player player) {
        // Kontrola, zda je to server-side. Na klientovi se to řešit nemusí.
        if (!player.level().isClientSide) {
            // Zavoláme statickou metodu, která vyhodí všechny předměty
            // z Crafting kontejneru zpět do inventáře hráče nebo na zem.
            // Tato metoda je ekvivalentní tomu, co dělá Crafting Table.
            clearContainer(player, this.craftingContainer);
        }
		super.removed(player);
		this.access.execute((p_39371_,p_39372_) -> {
		   this.clearContainer(player, this.craftSlots);
		});
	}

    // Tuto metodu přepíšeme, aby Crafting Menu fungovalo kdekoliv, ne jen u bloku.
    // Zde vždy vracíme 'true'.
    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

	public void slotsChanged(@NotNull Container container) {
	   this.access.execute((world,pos) -> {
		  slotChangedCraftingGrid(this,world, this.player, this.craftSlots, this.resultSlots);
	   });
	}

	public void fillCraftSlotsStackedContents(@NotNull StackedContents stackedContents) {
	   this.craftSlots.fillStackedContents(stackedContents);
	}

	public void clearCraftingContent() {
	   this.craftSlots.clearContent();
	   this.resultSlots.clearContent();
	}

	public boolean recipeMatches(Recipe<? super CraftingContainer> p_39384_) {
	   return p_39384_.matches(this.craftSlots, this.player.level());
	}

	public @NotNull ItemStack quickMoveStack(@NotNull Player player,int p_39392_) {
	   ItemStack itemstack = ItemStack.EMPTY;
	   Slot slot = this.slots.get(p_39392_);
	   if (slot.hasItem()) {
		  ItemStack itemstack1 = slot.getItem();
		  itemstack = itemstack1.copy();
		  if (p_39392_ == 0) {
			 this.access.execute((p_39378_, p_39379_) -> {
				itemstack1.getItem().onCraftedBy(itemstack1, p_39378_, player);
			 });
			 if (!this.moveItemStackTo(itemstack1, 10, 46, true)) {
				return ItemStack.EMPTY;
			 }

			 slot.onQuickCraft(itemstack1, itemstack);
		  } else if (p_39392_ >= 10 && p_39392_ < 46) {
			 if (!this.moveItemStackTo(itemstack1, 1, 10, false)) {
				if (p_39392_ < 37) {
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
		  if (p_39392_ == 0) {
			  player.drop(itemstack1, false);
		  }
	   }

	   return itemstack;
	}

	public boolean canTakeItemForPickAll(@NotNull ItemStack itemStack,Slot slot) {
	   return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemStack, slot);
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

	public boolean shouldMoveToInventory(int p_150553_) {
	   return p_150553_ != this.getResultSlotIndex();
	}
}