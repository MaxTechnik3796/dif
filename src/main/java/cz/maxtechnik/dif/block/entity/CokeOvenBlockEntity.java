package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.CokeOvenRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * BlockEntity pro Coke Oven Controller.
 *
 * INVENTORY:
 *   slot 0 = vstup (uhlí atd.)
 *   slot 1 = výstup (coke coal atd.)
 *
 * FLUID:
 *   tank = výstup (creosote oil, 8000 mB)
 *
 * CREATE KONTRAPTION:
 *   Pec se pozastaví když je součástí kontraption (moving = true).
 *   Stav se uloží do NBT a obnoví se po zastavení kontraption.
 *
 * CAPABILITY STRANY (registrováno v DifMod):
 *   Zadní strana  → item input (slot 0)
 *   Boční strany  → item output (slot 1)
 *   Spodní strana → fluid output
 */
public class CokeOvenBlockEntity extends BlockEntity implements MenuProvider {

    // ── Konstanty ────────────────────────────────────────────────────────────
    public static final int SLOT_INPUT  = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int TANK_CAPACITY = 8000; // mB

    // ── Inventory ─────────────────────────────────────────────────────────────
    public final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            // Invaliduj recept cache při změně vstupu
            if (slot == SLOT_INPUT) cachedRecipe = null;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            // Výstupní slot nepřijímá nic zvenku
            return slot == SLOT_INPUT;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == SLOT_INPUT ? 64 : 64;
        }
    };

    // ── Fluid tank (output only) ──────────────────────────────────────────────
    public final FluidTank fluidTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    // ── Stav pece ────────────────────────────────────────────────────────────
    /** Je pec správně sestavena (3×3×3 struktura validní)? */
    private boolean formed = false;
    /** Je pec součástí pohybující se kontraption? (Create) */
    private boolean moving = false;

    // ── Recipe cache ─────────────────────────────────────────────────────────
    @Nullable private CokeOvenRecipe cachedRecipe = null;
    private int progress  = 0;
    private int totalTime = CokeOvenRecipe.DEFAULT_TIME;

    public CokeOvenBlockEntity(BlockPos pos, BlockState state) {
        super(DifModBlockEntities.COKE_OVEN.get(), pos, state);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Tick
    // ══════════════════════════════════════════════════════════════════════════

    public static void serverTick(Level level, BlockPos pos, BlockState state, CokeOvenBlockEntity be) {
        // Pozastaveno: kontraption nebo pec není sestavena
        if (be.moving || !be.formed) {
            if (be.progress > 0) { be.progress = 0; be.setChanged(); }
            return;
        }

        ItemStack input = be.inventory.getStackInSlot(SLOT_INPUT);
        if (input.isEmpty()) {
            be.resetProgress();
            return;
        }

        // Najdi recept (cached)
        if (be.cachedRecipe == null) {
            be.cachedRecipe = findRecipe(level, input).orElse(null);
            if (be.cachedRecipe != null) {
                be.totalTime = be.cachedRecipe.processingTime();
                be.progress  = 0;
            }
        }

        if (be.cachedRecipe == null) {
            be.resetProgress();
            return;
        }

        // Zkontroluj že výstupní slot má místo
        ItemStack outputPreview = be.cachedRecipe.result().copy();
        if (!canInsertOutput(be.inventory.getStackInSlot(SLOT_OUTPUT), outputPreview)) {
            return; // stall — výstup plný
        }

        // Zkontroluj fluid místo
        if (be.cachedRecipe.hasFluidOutput()) {
            FluidStack fluidOut = be.cachedRecipe.fluidOutput().copy();
            if (be.fluidTank.fill(fluidOut, IFluidHandler.FluidAction.SIMULATE) < fluidOut.getAmount()) {
                return; // stall — fluid tank plný
            }
        }

        // Progress
        be.progress++;
        be.setChanged();

        if (be.progress >= be.totalTime) {
            be.progress = 0;
            // Odeber vstup
            be.inventory.extractItem(SLOT_INPUT, be.cachedRecipe.ingredientCount(), false);
            // Vlož výstup
            be.inventory.insertItem(SLOT_OUTPUT, outputPreview, false);
            // Vlož fluid
            if (be.cachedRecipe.hasFluidOutput()) {
                be.fluidTank.fill(be.cachedRecipe.fluidOutput().copy(), IFluidHandler.FluidAction.EXECUTE);
            }
            // Invaliduj recept cache — vstup se mohl změnit
            be.cachedRecipe = null;
        }
    }

    private void resetProgress() {
        if (progress != 0) { progress = 0; cachedRecipe = null; setChanged(); }
    }

    /** Zkontroluje jestli lze přidat výstupní item do slotu. */
    private static boolean canInsertOutput(ItemStack existing, ItemStack toInsert) {
        if (existing.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(existing, toInsert)) return false;
        return existing.getCount() + toInsert.getCount() <= existing.getMaxStackSize();
    }

    private static Optional<CokeOvenRecipe> findRecipe(Level level, ItemStack input) {
        for (RecipeHolder<CokeOvenRecipe> holder :
                level.getRecipeManager().getAllRecipesFor(DifModRecipes.COKE_OVEN_TYPE.get())) {
            if (holder.value().matches(input)) return Optional.of(holder.value());
        }
        return Optional.empty();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Formed state (strukturní validace)
    // ══════════════════════════════════════════════════════════════════════════

    public boolean isFormed() { return formed; }

    public void setFormed(boolean formed) {
        this.formed = formed;
        if (!formed) resetProgress();
        setChanged();
        if (level != null && !level.isClientSide) sendData();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Create kontraption podpora
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Volá se když Create začne pohybovat blokem (kontraption).
     * Pozastaví zpracování — pec nepracuje během pohybu.
     */
    public void onMovingStart() {
        moving = true;
        formed = false; // při pohybu je struktura rozbitá
        setChanged();
    }

    /**
     * Volá se když Create zastaví kontraption a blok se znovu usadí.
     * Pec bude potřebovat znovu validaci struktury.
     */
    public void onMovingEnd() {
        moving = false;
        setChanged();
    }

    public boolean isMoving() { return moving; }

    // ══════════════════════════════════════════════════════════════════════════
    // GUI (MenuProvider)
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.dif.coke_oven_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInv, @NotNull Player player) {
        // TODO: vrátit CokeOvenMenu(windowId, playerInv, this) až bude GUI hotové
        return null;
    }

    // ── Gettery pro GUI ───────────────────────────────────────────────────────
    public int getProgress()   { return progress; }
    public int getTotalTime()  { return totalTime; }

    /** Progress pro GUI progress bar (0–100). */
    public int getProgressPercent() {
        if (totalTime == 0) return 0;
        return (int) ((float) progress / totalTime * 100);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // NBT
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.put("fluid",     fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("progress",  progress);
        tag.putInt("totalTime", totalTime);
        tag.putBoolean("formed", formed);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        fluidTank.readFromNBT(registries, tag.getCompound("fluid"));
        progress  = tag.getInt("progress");
        totalTime = tag.getInt("totalTime");
        formed    = tag.getBoolean("formed");
        cachedRecipe = null; // vždy znovu najít recept po načtení
    }

    // ── Sync packet (client update pro GUI) ───────────────────────────────────
    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("progress",  progress);
        tag.putInt("totalTime", totalTime);
        tag.putBoolean("formed", formed);
        tag.put("fluid", fluidTank.writeToNBT(registries, new CompoundTag()));
        return tag;
    }

    private void sendData() {
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}