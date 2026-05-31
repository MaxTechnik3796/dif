package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.BlastSmelteryController;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.BlastSmelteryRecipe;
import cz.maxtechnik.dif.util.MultiblockHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static cz.maxtechnik.dif.DifMod.goggleTooltipFix;

public class BlastSmelteryControllerBlockEntity extends AbstractMultiblockControllerBlockEntity<BlastSmelteryRecipe> {

    // ── Vzor struktury ────────────────────────────────────────────────────────

    private static final Predicate<BlockState>[][][] PATTERN = MultiblockHelper.buildSolidShellPattern(
            MultiblockHelper.of(DifModBlocks.BLAST_SMELTERY.get()),
            MultiblockHelper.of(DifModBlocks.BLAST_SMELTERY.get())
    );

    // ── Fluid tanky ───────────────────────────────────────────────────────────

    /** Vstupní fluid — lze pouze čerpat dovnitř. */
    public final FluidTank fluidInputTank = new FluidTank(16000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            cachedRecipe = null;
            setChanged();
        }
    };

    /** Výstupní fluid — lze pouze čerpat ven / brát kbelíkem. */
    public final FluidTank fluidOutputTank = new FluidTank(16000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            setChanged();
        }
    };

    // ── Konstruktor ───────────────────────────────────────────────────────────

    public BlastSmelteryControllerBlockEntity(BlockPos pos, BlockState blockState) {
        super(DifModBlockEntities.BLAST_SMELTERY_CONTROLLER.get(), pos, blockState);
    }

    // ── Ticker factory (voláno z BlastSmelteryController bloku) ──────────────

    public static <T extends net.minecraft.world.level.block.entity.BlockEntity>
    BlockEntityTicker<T> ticker(BlockEntityType<T> type) {
        BlockEntityType<BlastSmelteryControllerBlockEntity> expected = DifModBlockEntities.BLAST_SMELTERY_CONTROLLER.get();
        return type.equals(expected)
                ? (lvl, pos, state, be) -> ((BlastSmelteryControllerBlockEntity) be).tick(lvl, pos, state)
                : null;
    }

    // ── AbstractMultiblockControllerBlockEntity – implementace ────────────────

    @Override
    protected Predicate<BlockState>[][][] getPattern() { return PATTERN; }

    @Override
    protected DirectionProperty getFacingProperty() { return BlastSmelteryController.FACING; }

    @Override
    protected BooleanProperty getFormedProperty() { return BlastSmelteryController.FORMED; }

    @Override
    protected BooleanProperty getActiveProperty() { return BlastSmelteryController.ACTIVE; }

    @Override
    protected Component getGoggleName() {
        return Component.literal("◆ Blast Smeltery");
    }

    @Override
    protected ChatFormatting getGoggleNameColor() { return ChatFormatting.RED; }

    // ── Validace vstupu ───────────────────────────────────────────────────────

    /**
     * BlastSmeltery umí pracovat i jen s fluidem (bez itemu) a naopak,
     * takže vstup je platný pokud aspoň jeden z nich odpovídá nějakému receptu.
     * Konkrétní shoda se řeší až v {@link #findRecipe}.
     */
    @Override
    protected boolean hasValidInput() {
        return true; // recept sám ověří shodu item + fluid
    }

    // ── Recipe ────────────────────────────────────────────────────────────────

    @Override
    protected @Nullable BlastSmelteryRecipe findRecipe(Level level) {
        final ItemStack  itemIn  = inventory.getStackInSlot(SLOT_INPUT);
        final FluidStack fluidIn = fluidInputTank.getFluid();

        if (cachedRecipe != null && cachedRecipe.matches(itemIn, fluidIn)) return cachedRecipe;

        for (var holder : level.getRecipeManager().getAllRecipesFor(DifModRecipes.BLAST_SMELTERY_TYPE.get())) {
            if (holder.value().matches(itemIn, fluidIn)) {
                cachedRecipe = holder.value();
                return cachedRecipe;
            }
        }
        cachedRecipe = null;
        return null;
    }

    @Override
    protected int getProcessingTime(BlastSmelteryRecipe recipe) { return recipe.processingTime(); }

    @Override
    protected boolean canOutput(BlastSmelteryRecipe recipe) {
        final ItemStack currentOut   = inventory.getStackInSlot(SLOT_OUTPUT);
        final ItemStack recipeItemOut = recipe.itemResult();

        boolean canItem = recipeItemOut.isEmpty()
                || currentOut.isEmpty()
                || (ItemStack.isSameItemSameComponents(currentOut, recipeItemOut)
                && currentOut.getCount() + recipeItemOut.getCount() <= currentOut.getMaxStackSize());

        boolean canFluid = !recipe.hasFluidOutput()
                || fluidOutputTank.fill(recipe.fluidOutput(), IFluidHandler.FluidAction.SIMULATE) >= recipe.fluidOutput().getAmount();

        return canItem && canFluid;
    }

    @Override
    protected void finishRecipe(BlastSmelteryRecipe recipe) {
        // Spotřebuj vstupní item
        if (recipe.hasItemInput()) {
            ItemStack inSlot = inventory.getStackInSlot(SLOT_INPUT);
            inSlot.shrink(recipe.itemIngredientCount());
            inventory.setStackInSlot(SLOT_INPUT, inSlot);
        }

        // Spotřebuj vstupní fluid
        if (recipe.hasFluidInput()) {
            fluidInputTank.drain(recipe.fluidInput().getAmount(), IFluidHandler.FluidAction.EXECUTE);
        }

        // Vlož výstupní item
        if (recipe.hasItemOutput()) {
            ItemStack outSlot   = inventory.getStackInSlot(SLOT_OUTPUT);
            ItemStack resultCopy = recipe.itemResult().copy();
            if (outSlot.isEmpty()) {
                inventory.setStackInSlot(SLOT_OUTPUT, resultCopy);
            } else {
                outSlot.grow(resultCopy.getCount());
                inventory.setStackInSlot(SLOT_OUTPUT, outSlot);
            }
        }

        // Vlož výstupní fluid
        if (recipe.hasFluidOutput()) {
            fluidOutputTank.fill(recipe.fluidOutput(), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    // ── Claiming ──────────────────────────────────────────────────────────────

    @Override
    protected boolean brickCanBeClaimedBy(Level level, BlockPos brickPos, BlockPos controllerPos) {
        return !(level.getBlockEntity(brickPos) instanceof BlastSmelteryBlockEntity brick)
                || brick.canBeClaimedBy(controllerPos);
    }

    @Override
    protected void setBrickController(Level level, BlockPos brickPos, @Nullable BlockPos owner) {
        if (level.getBlockEntity(brickPos) instanceof BlastSmelteryBlockEntity brick) {
            brick.setControllerPos(owner);
        }
    }

    // ── Kbelík (nejdřív výstupní tank, pak vstupní) ───────────────────────────

    @Override
    protected void tryFillBucket(Player player, InteractionHand hand, ItemStack heldBucket) {
        FluidTank source = fluidOutputTank.getFluidAmount() >= 1000 ? fluidOutputTank
                : (fluidInputTank.getFluidAmount() >= 1000 ? fluidInputTank : null);
        if (source == null) return;

        final var drained = source.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) return;

        heldBucket.shrink(1);
        final ItemStack filled = new ItemStack(drained.getFluid().getBucket());
        if (heldBucket.isEmpty()) player.setItemInHand(hand, filled);
        else if (!player.getInventory().add(filled)) player.drop(filled, false);
        setChanged();
    }

    // ── Goggle tooltip ────────────────────────────────────────────────────────

    @Override
    protected void appendFormedTooltip(List<Component> tooltip) {
        appendItemSlot(tooltip,  goggleTooltipFix + " ▶ Item In: ",   inventory.getStackInSlot(SLOT_INPUT));
        appendItemSlot(tooltip,  goggleTooltipFix + " ▶ Item Out: ",  inventory.getStackInSlot(SLOT_OUTPUT));
        appendFluidSlot(tooltip, goggleTooltipFix + " ▶ Fluid In: ",  fluidInputTank);
        appendFluidSlot(tooltip, goggleTooltipFix + " ▶ Fluid Out: ", fluidOutputTank);
    }

    @Override
    public @Nullable IFluidHandler getFluidCapability(@Nullable net.minecraft.core.Direction side) {
        return MultiblockHelper.combinedInOut(fluidInputTank, fluidOutputTank);
    }

    // ── Display name ──────────────────────────────────────────────────────────

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.dif.blast_smeltery");
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.dif.blast_smeltery");
    }

    // ── NBT – fluid tanky ─────────────────────────────────────────────────────

    @Override
    protected void saveExtraData(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        tag.put("fluidInputTank",  fluidInputTank.writeToNBT(provider, new CompoundTag()));
        tag.put("fluidOutputTank", fluidOutputTank.writeToNBT(provider, new CompoundTag()));
    }

    @Override
    protected void loadExtraData(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        if (tag.get("fluidInputTank")  instanceof CompoundTag t) fluidInputTank.readFromNBT(provider, t);
        if (tag.get("fluidOutputTank") instanceof CompoundTag t) fluidOutputTank.readFromNBT(provider, t);
    }
}