package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.gui.menu.CokeOvenMenu;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.CokeOvenRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
 * Master BlockEntity Coke Ovenu.
 * Jeden z 27 bloků — ten na který hráč klikl wrenchem.
 *
 * Obsahuje:
 *   - inventory: slot 0 = vstup, slot 1 = výstup
 *   - fluidTank: výstup (creosote oil, 8000 mB)
 *   - recipe cache + progress
 *   - structureOrigin: uložený origin 3×3×3 kostky pro spolehlivý deform
 */
public class CokeOvenBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_INPUT  = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int TANK_CAP    = 8000;

    public final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override protected void onContentsChanged(int slot) {
            setChanged();
            if (slot == SLOT_INPUT) cachedRecipe = null;
        }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack s) {
            return slot == SLOT_INPUT;
        }
    };

    public final FluidTank fluidTank = new FluidTank(TANK_CAP) {
        @Override protected void onContentsChanged() { setChanged(); }
    };

    @Nullable private CokeOvenRecipe cachedRecipe = null;
    private int progress  = 0;
    private int totalTime = CokeOvenRecipe.DEFAULT_TIME;

    // FIX: Uložený origin struktury — aby deform fungoval spolehlivě i při zničení bloku
    @Nullable private BlockPos structureOrigin = null;

    public CokeOvenBlockEntity(BlockPos pos, BlockState state) {
        super(DifModBlockEntities.COKE_OVEN.get(), pos, state);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Structure origin getter/setter
    // ══════════════════════════════════════════════════════════════════════════

    public void setStructureOrigin(BlockPos origin) {
        this.structureOrigin = origin.immutable();
        setChanged();
    }

    @Nullable
    public BlockPos getStructureOrigin() {
        return structureOrigin;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Tick
    // ══════════════════════════════════════════════════════════════════════════

    public static void serverTick(Level level, BlockPos pos, BlockState state, CokeOvenBlockEntity be) {
        ItemStack input = be.inventory.getStackInSlot(SLOT_INPUT);
        if (input.isEmpty()) { be.resetProgress(); return; }

        // Cached recept
        if (be.cachedRecipe == null) {
            be.cachedRecipe = findRecipe(level, input).orElse(null);
            if (be.cachedRecipe != null) { be.totalTime = be.cachedRecipe.processingTime(); be.progress = 0; }
        }
        if (be.cachedRecipe == null) { be.resetProgress(); return; }

        // Zkontroluj místo pro výstup
        if (!canInsert(be.inventory.getStackInSlot(SLOT_OUTPUT), be.cachedRecipe.result())) return;
        if (be.cachedRecipe.hasFluidOutput()) {
            if (be.fluidTank.fill(be.cachedRecipe.fluidOutput().copy(),
                    IFluidHandler.FluidAction.SIMULATE) < be.cachedRecipe.fluidOutput().getAmount()) return;
        }

        be.progress++;
        be.setChanged();

        if (be.progress >= be.totalTime) {
            be.progress = 0;
            be.inventory.extractItem(SLOT_INPUT, be.cachedRecipe.ingredientCount(), false);
            be.inventory.insertItem(SLOT_OUTPUT, be.cachedRecipe.result().copy(), false);
            if (be.cachedRecipe.hasFluidOutput())
                be.fluidTank.fill(be.cachedRecipe.fluidOutput().copy(), IFluidHandler.FluidAction.EXECUTE);
            be.cachedRecipe = null;
        }
    }

    private void resetProgress() {
        if (progress != 0) { progress = 0; cachedRecipe = null; setChanged(); }
    }

    private static boolean canInsert(ItemStack existing, ItemStack toAdd) {
        if (existing.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(existing, toAdd)) return false;
        return existing.getCount() + toAdd.getCount() <= existing.getMaxStackSize();
    }

    private static Optional<CokeOvenRecipe> findRecipe(Level level, ItemStack input) {
        for (RecipeHolder<CokeOvenRecipe> h :
                level.getRecipeManager().getAllRecipesFor(DifModRecipes.COKE_OVEN_TYPE.get()))
            if (h.value().matches(input)) return Optional.of(h.value());
        return Optional.empty();
    }

    // ── Gettery pro menu ──────────────────────────────────────────────────────
    public int getProgress()  { return progress; }
    public int getTotalTime() { return totalTime; }

    // ── MenuProvider ──────────────────────────────────────────────────────────
    @Override public @NotNull Component getDisplayName() {
        return Component.translatable("block.dif.coke_oven");
    }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new CokeOvenMenu(id, inv, this);
    }

    // ── NBT ───────────────────────────────────────────────────────────────────
    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider reg) {
        super.saveAdditional(tag, reg);
        tag.put("inv",   inventory.serializeNBT(reg));
        tag.put("fluid", fluidTank.writeToNBT(reg, new CompoundTag()));
        tag.putInt("progress",  progress);
        tag.putInt("totalTime", totalTime);
        // FIX: Ulož origin struktury
        if (structureOrigin != null) tag.put("origin", NbtUtils.writeBlockPos(structureOrigin));
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider reg) {
        super.loadAdditional(tag, reg);
        inventory.deserializeNBT(reg, tag.getCompound("inv"));
        fluidTank.readFromNBT(reg, tag.getCompound("fluid"));
        progress  = tag.getInt("progress");
        totalTime = tag.getInt("totalTime");
        cachedRecipe = null;
        // FIX: Načti origin struktury
        structureOrigin = tag.contains("origin") ? NbtUtils.readBlockPos(tag, "origin").orElse(null) : null;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider reg) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("progress",  progress);
        tag.putInt("totalTime", totalTime);
        tag.put("fluid", fluidTank.writeToNBT(reg, new CompoundTag()));
        return tag;
    }
}