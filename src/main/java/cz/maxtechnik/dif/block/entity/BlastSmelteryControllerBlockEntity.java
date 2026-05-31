package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import cz.maxtechnik.dif.block.BlastSmelteryController;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.BlastSmelteryRecipe;
import cz.maxtechnik.dif.util.MultiblockHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static cz.maxtechnik.dif.DifMod.goggleTooltipFix;

public class BlastSmelteryControllerBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, IHaveGoggleInformation {

    private static final Predicate<BlockState>[][][] PATTERN = MultiblockHelper.buildSolidShellPattern(
            MultiblockHelper.of(DifModBlocks.BLAST_SMELTERY.get()),
            MultiblockHelper.of(DifModBlocks.BLAST_SMELTERY.get())
    );

    private static final int FORMED_REVALIDATE_PERIOD = 40;
    private static final int UNFORMED_REVALIDATE_PERIOD = 20;

    private static final int SLOT_INPUT  = 0;
    private static final int SLOT_OUTPUT = 1;
    private static final int[] SLOTS_ALL = {SLOT_INPUT, SLOT_OUTPUT};

    // ── Inventář (item vstup + item výstup) ─────────────────────────────────
    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot == SLOT_INPUT) cachedRecipe = null;
            setChanged();
        }
    };

    public ItemStackHandler getInventory() { return inventory; }

    // ── Fluid tanky ──────────────────────────────────────────────────────────
    /** Vstupní fluid tank – lze pouze čerpat dovnitř (pump in), nelze pumpovat ven. */
    public final FluidTank fluidInputTank = new FluidTank(16000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            cachedRecipe = null;
            setChanged();
        }
    };

    /** Výstupní fluid tank – lze pouze čerpat ven (pump out), nebo brát kbelíkem. */
    public final FluidTank fluidOutputTank = new FluidTank(16000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            setChanged();
        }
    };

    // ── Stav ────────────────────────────────────────────────────────────────
    private int progress   = 0;
    private int totalTime  = 0;
    public boolean forceValidation = true;
    private final int tickOffset = (int) (Math.random() * UNFORMED_REVALIDATE_PERIOD);
    private boolean isConflicted = false;

    @Nullable
    private transient BlastSmelteryRecipe cachedRecipe;

    // ── Konstruktor ──────────────────────────────────────────────────────────
    public BlastSmelteryControllerBlockEntity(BlockPos pos, BlockState blockState) {
        super(DifModBlockEntities.BLAST_SMELTERY_CONTROLLER.get(), pos, blockState);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // ── Server tick ──────────────────────────────────────────────────────────
    public static void serverTick(Level level, BlockPos pos, BlockState blockState, BlastSmelteryControllerBlockEntity be) {
        final Direction intoStructure = blockState.getValue(BlastSmelteryController.FACING).getOpposite();
        final boolean wasFormed       = blockState.getValue(BlastSmelteryController.FORMED);
        final long gameTime           = level.getGameTime() + be.tickOffset;
        final int period              = wasFormed ? FORMED_REVALIDATE_PERIOD : UNFORMED_REVALIDATE_PERIOD;
        final boolean shouldValidate  = be.forceValidation || gameTime % period == 0;

        boolean isFormed = wasFormed;
        if (shouldValidate) {
            be.forceValidation = false;
            isFormed = MultiblockHelper.isValid(level, pos, intoStructure, PATTERN);
            if (isFormed && !wasFormed) {
                if (!canClaimAllBricks(level, pos, intoStructure)) {
                    isFormed = false;
                    if (!be.isConflicted) {
                        be.isConflicted = true;
                        be.setChanged();
                    }
                }
            }
        }

        if (isFormed != wasFormed) {
            if (isFormed) {
                claimBricks(level, pos, intoStructure, pos);
                be.isConflicted = false;
            } else {
                claimBricks(level, pos, intoStructure, null);
                be.progress   = 0;
                be.totalTime  = 0;
                be.cachedRecipe = null;
            }
            blockState = blockState.setValue(BlastSmelteryController.FORMED, isFormed).setValue(BlastSmelteryController.ACTIVE, false);
            level.setBlock(pos, blockState, 3);
            be.setChanged();
        }

        if (!isFormed) return;

        // Hledáme recept
        final ItemStack  itemIn   = be.inventory.getStackInSlot(SLOT_INPUT);
        final FluidStack fluidIn  = be.fluidInputTank.getFluid();
        final BlastSmelteryRecipe recipe = be.getRecipeFor(level, itemIn, fluidIn);

        if (recipe == null) {
            be.resetProgressAndDeactivate(level, pos, blockState);
            return;
        }

        be.totalTime = recipe.processingTime();

        // Zkontroluj výstupní místo
        final ItemStack  currentItemOut  = be.inventory.getStackInSlot(SLOT_OUTPUT);
        final ItemStack  recipeItemOut   = recipe.itemResult();
        final boolean canOutputItem = recipeItemOut.isEmpty()
                || currentItemOut.isEmpty()
                || (ItemStack.isSameItemSameComponents(currentItemOut, recipeItemOut)
                    && currentItemOut.getCount() + recipeItemOut.getCount() <= currentItemOut.getMaxStackSize());
        final boolean canOutputFluid = !recipe.hasFluidOutput()
                || be.fluidOutputTank.fill(recipe.fluidOutput(), IFluidHandler.FluidAction.SIMULATE) >= recipe.fluidOutput().getAmount();

        if (!canOutputItem || !canOutputFluid) {
            be.setActive(level, pos, blockState, false);
            return;
        }

        be.setActive(level, pos, blockState, true);
        be.progress++;
        if (be.progress >= be.totalTime) {
            finishRecipe(be, recipe);
        } else if (be.progress % 10 == 0) be.setChanged();
    }

    private static void finishRecipe(BlastSmelteryControllerBlockEntity be, BlastSmelteryRecipe recipe) {
        be.progress = 0;

        // Spotřebuj vstupní item
        if (recipe.hasItemInput()) {
            ItemStack inSlot = be.inventory.getStackInSlot(SLOT_INPUT);
            inSlot.shrink(recipe.itemIngredientCount());
            be.inventory.setStackInSlot(SLOT_INPUT, inSlot);
        }

        // Spotřebuj vstupní fluid
        if (recipe.hasFluidInput()) {
            be.fluidInputTank.drain(recipe.fluidInput().getAmount(), IFluidHandler.FluidAction.EXECUTE);
        }

        // Vlož výstupní item
        if (recipe.hasItemOutput()) {
            ItemStack outSlot = be.inventory.getStackInSlot(SLOT_OUTPUT);
            ItemStack resultCopy = recipe.itemResult().copy();
            if (outSlot.isEmpty()) {
                be.inventory.setStackInSlot(SLOT_OUTPUT, resultCopy);
            } else {
                outSlot.grow(resultCopy.getCount());
                be.inventory.setStackInSlot(SLOT_OUTPUT, outSlot);
            }
        }

        // Vlož výstupní fluid
        if (recipe.hasFluidOutput()) {
            be.fluidOutputTank.fill(recipe.fluidOutput(), IFluidHandler.FluidAction.EXECUTE);
        }

        be.cachedRecipe = null;
        be.setChanged();
    }

    private void resetProgressAndDeactivate(Level level, BlockPos pos, BlockState blockState) {
        if (progress != 0 || totalTime != 0) {
            progress  = 0;
            totalTime = 0;
            setChanged();
        }
        setActive(level, pos, blockState, false);
    }

    private void setActive(Level level, BlockPos pos, BlockState state, boolean active) {
        if (state.getValue(BlastSmelteryController.ACTIVE) != active)
            level.setBlock(pos, state.setValue(BlastSmelteryController.ACTIVE, active), 3);
    }

    // ── Brick helpers ────────────────────────────────────────────────────────
    @FunctionalInterface
    private interface BrickVisitor {
        boolean visit(BlockPos.MutableBlockPos pos);
    }

    private static void forEachBrick(BlockPos controllerPos, Direction intoStructure, BrickVisitor visitor) {
        final Direction right = intoStructure.getClockWise();
        final BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    if (y == 1 && x == 1 && z == 0) continue;
                    mp.set(controllerPos).move(intoStructure, z).move(right, x - 1).move(Direction.UP, y - 1);
                    if (!visitor.visit(mp)) return;
                }
            }
        }
    }

    private static boolean canClaimAllBricks(Level level, BlockPos controllerPos, Direction intoStructure) {
        final boolean[] ok = {true};
        forEachBrick(controllerPos, intoStructure, mp -> {
            if (level.getBlockEntity(mp) instanceof BlastSmelteryBlockEntity brick && !brick.canBeClaimedBy(controllerPos)) {
                ok[0] = false;
                return false;
            }
            return true;
        });
        return ok[0];
    }

    private static void claimBricks(Level level, BlockPos controllerPos, Direction intoStructure, @Nullable BlockPos owner) {
        forEachBrick(controllerPos, intoStructure, mp -> {
            if (level.getBlockEntity(mp) instanceof BlastSmelteryBlockEntity brick) brick.setControllerPos(owner);
            return true;
        });
    }

    // ── Recipe lookup ────────────────────────────────────────────────────────
    @Nullable
    private BlastSmelteryRecipe getRecipeFor(Level level, ItemStack itemIn, FluidStack fluidIn) {
        final BlastSmelteryRecipe cached = cachedRecipe;
        if (cached != null && cached.matches(itemIn, fluidIn)) return cached;
        final List<RecipeHolder<BlastSmelteryRecipe>> all =
                level.getRecipeManager().getAllRecipesFor(DifModRecipes.BLAST_SMELTERY_TYPE.get());
        for (RecipeHolder<BlastSmelteryRecipe> holder : all) {
            if (holder.value().matches(itemIn, fluidIn)) {
                cachedRecipe = holder.value();
                return cachedRecipe;
            }
        }
        cachedRecipe = null;
        return null;
    }

    // ── Goggle tooltip ────────────────────────────────────────────────────────
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal(goggleTooltipFix + "◆ Blast Smeltery").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        final BlockState state = getBlockState();
        final boolean formed = state.hasProperty(BlastSmelteryController.FORMED) && state.getValue(BlastSmelteryController.FORMED);
        if (!formed) {
            tooltip.add(isConflicted
                    ? Component.literal(goggleTooltipFix + " ⚠ Structure already use some blocks").withStyle(ChatFormatting.DARK_RED)
                    : Component.literal(goggleTooltipFix + " Structure is NOT formed!").withStyle(ChatFormatting.RED));
            return true;
        }
        // Item sloty
        appendItemSlot(tooltip, goggleTooltipFix + " ▶ Item In: ",  inventory.getStackInSlot(SLOT_INPUT));
        appendItemSlot(tooltip, goggleTooltipFix + " ▶ Item Out: ", inventory.getStackInSlot(SLOT_OUTPUT));
        // Fluid tanky
        appendFluidSlot(tooltip, goggleTooltipFix + " ▶ Fluid In: ",  fluidInputTank);
        appendFluidSlot(tooltip, goggleTooltipFix + " ▶ Fluid Out: ", fluidOutputTank);
        // Postup
        if (state.getValue(BlastSmelteryController.ACTIVE) && totalTime > 0) {
            int pct      = (int) (((double) progress / totalTime) * 100.0);
            int secsLeft = Math.max(0, (totalTime - progress) / 20);
            tooltip.add(Component.literal(goggleTooltipFix + " ▶ Progress: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(pct + "% (" + secsLeft + "s left)").withStyle(ChatFormatting.GREEN)));
        } else {
            tooltip.add(Component.literal(goggleTooltipFix + " ▶ Status: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Idle").withStyle(ChatFormatting.YELLOW)));
        }
        return true;
    }

    private static void appendItemSlot(List<Component> tooltip, String label, ItemStack stack) {
        tooltip.add(Component.literal(label).withStyle(ChatFormatting.GRAY)
                .append(Component.literal(stack.isEmpty() ? "Empty" : stack.getCount() + "x " + stack.getHoverName().getString())
                        .withStyle(stack.isEmpty() ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE)));
    }

    private static void appendFluidSlot(List<Component> tooltip, String label, FluidTank tank) {
        final FluidStack fluid = tank.getFluid();
        tooltip.add(Component.literal(label).withStyle(ChatFormatting.GRAY)
                .append(fluid.isEmpty()
                        ? Component.literal("Empty").withStyle(ChatFormatting.DARK_GRAY)
                        : Component.literal(fluid.getAmount() + "/" + tank.getCapacity() + " mB " + fluid.getHoverName().getString()).withStyle(ChatFormatting.AQUA)));
    }

    // ── Interakce hráče ──────────────────────────────────────────────────────
    /**
     * Kbelík:
     *  - primárně bere z výstupního tanku
     *  - pokud je výstupní prázdný, bere ze vstupního (max 1 kbelík)
     * Item v ruce:
     *  - vkládá/bere z item slotů (stejná logika jako CokeOven)
     */
    public boolean handleInteraction(Player player, InteractionHand hand) {
        if (level == null || level.isClientSide) return true;
        final ItemStack held = player.getItemInHand(hand);

        if (held.getItem() == Items.BUCKET) {
            tryFillBucket(player, hand, held);
            return true;
        }

        if (!held.isEmpty()) {
            final ItemStack currentInput = inventory.getStackInSlot(SLOT_INPUT);
            if (currentInput.isEmpty() || ItemStack.isSameItemSameComponents(currentInput, held)) {
                final ItemStack remaining = inventory.insertItem(SLOT_INPUT, held.copy(), false);
                player.setItemInHand(hand, remaining);
            } else {
                player.setItemInHand(hand, currentInput);
                inventory.setStackInSlot(SLOT_INPUT, held.copy());
            }
        } else {
            ItemStack out = inventory.getStackInSlot(SLOT_OUTPUT);
            if (!out.isEmpty()) {
                player.setItemInHand(hand, out.copy());
                inventory.setStackInSlot(SLOT_OUTPUT, ItemStack.EMPTY);
            } else {
                ItemStack in = inventory.getStackInSlot(SLOT_INPUT);
                if (!in.isEmpty()) {
                    player.setItemInHand(hand, in.copy());
                    inventory.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
                }
            }
        }
        setChanged();
        return true;
    }

    private void tryFillBucket(Player player, InteractionHand hand, ItemStack heldBucket) {
        // Nejdřív zkus výstupní tank
        FluidTank source = fluidOutputTank.getFluidAmount() >= 1000 ? fluidOutputTank
                         : (fluidInputTank.getFluidAmount() >= 1000 ? fluidInputTank : null);
        if (source == null) return;

        final FluidStack drained = source.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) return;

        heldBucket.shrink(1);
        final ItemStack filled = new ItemStack(drained.getFluid().getBucket());
        if (heldBucket.isEmpty()) player.setItemInHand(hand, filled);
        else if (!player.getInventory().add(filled)) player.drop(filled, false);
        setChanged();
    }

    // ── WorldlyContainer ─────────────────────────────────────────────────────
    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) { return SLOTS_ALL; }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack itemStack, @Nullable Direction side) {
        return index == SLOT_INPUT;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack itemStack, @NotNull Direction side) {
        return index == SLOT_OUTPUT;
    }

    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack itemStack) { return index == SLOT_INPUT; }

    @Override
    public int getContainerSize() { return inventory.getSlots(); }

    @Override
    public @NotNull ItemStack getItem(int index) { return inventory.getStackInSlot(index); }

    @Override
    public void setItem(int index, @NotNull ItemStack itemStack) { inventory.setStackInSlot(index, itemStack); }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) { return inventory.extractItem(slot, amount, false); }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = inventory.getStackInSlot(index);
        inventory.setStackInSlot(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < inventory.getSlots(); i++) if (!inventory.getStackInSlot(i).isEmpty()) return false;
        return true;
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        NonNullList<ItemStack> list = NonNullList.withSize(inventory.getSlots(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.getSlots(); i++) list.set(i, inventory.getStackInSlot(i));
        return list;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> stacks) {
        for (int i = 0; i < stacks.size() && i < inventory.getSlots(); i++)
            inventory.setStackInSlot(i, stacks.get(i));
    }

    @Override
    protected @NotNull Component getDefaultName() { return Component.translatable("container.dif.blast_smeltery"); }

    @Override
    public @NotNull Component getDisplayName() { return Component.translatable("container.dif.blast_smeltery"); }

    @Override
    public @NotNull AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory) {
        return ChestMenu.threeRows(id, inventory);
    }

    // ── NBT ──────────────────────────────────────────────────────────────────
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("inventory"))    inventory.deserializeNBT(provider, tag.getCompound("inventory"));
        if (tag.get("fluidInputTank")  instanceof CompoundTag t) fluidInputTank.readFromNBT(provider, t);
        if (tag.get("fluidOutputTank") instanceof CompoundTag t) fluidOutputTank.readFromNBT(provider, t);
        progress      = tag.getInt("progress");
        totalTime     = tag.getInt("totalTime");
        isConflicted  = tag.getBoolean("isConflicted");
        cachedRecipe  = null;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("inventory",      inventory.serializeNBT(provider));
        tag.put("fluidInputTank",  fluidInputTank.writeToNBT(provider, new CompoundTag()));
        tag.put("fluidOutputTank", fluidOutputTank.writeToNBT(provider, new CompoundTag()));
        tag.putInt("progress",     progress);
        tag.putInt("totalTime",    totalTime);
        tag.putBoolean("isConflicted", isConflicted);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider) { return saveWithFullMetadata(provider); }
}
