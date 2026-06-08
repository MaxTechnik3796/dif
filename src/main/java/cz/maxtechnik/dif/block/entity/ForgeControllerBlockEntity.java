package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.ForgeBrick;
import cz.maxtechnik.dif.block.ForgeFurnaceController;
import cz.maxtechnik.dif.block.ForgeGlass;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.ForgeMaterialRecipe;
import cz.maxtechnik.dif.util.ForgeMultiblockHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

public class ForgeControllerBlockEntity extends AbstractMultiblockControllerBlockEntity<ForgeMaterialRecipe> {

    private static final int HEAT_CACHE_PERIOD  = 20;
    private static final int GLASS_CHECK_PERIOD = 20;

    public static final int FLUID_TANK_COUNT = 32;
    public static final int SLOTS_PER_LAYER  = 9;

    public final FluidTank[] fluidTanks = new FluidTank[FLUID_TANK_COUNT];
    private final int[] fluidRenderOrder = new int[FLUID_TANK_COUNT];

    private int preferredOutputTank = -1;

    public net.neoforged.neoforge.items.ItemStackHandler forgeInventory =
            new net.neoforged.neoforge.items.ItemStackHandler(0) {
                @Override protected void onContentsChanged(int slot) { cachedRecipe = null; setChanged(); }
                @Override public int getSlotLimit(int slot) { return 1; }
            };

    private int   glassLayers     = 0;
    private boolean locked        = false;
    private int   cachedHeatPoints = 0;
    private float cachedHeatSpeed  = 0f;
    private int   heatCacheTick   = 0;
    private int   glassCacheTick  = 0;
    private boolean compacting    = false;

    public ForgeControllerBlockEntity(BlockPos pos, BlockState blockState) {
        super(DifModBlockEntities.FORGE_FURNACE_CONTROLLER.get(), pos, blockState);
        for (int i = 0; i < FLUID_TANK_COUNT; i++) {
            fluidTanks[i] = new FluidTank(0) {
                @Override protected void onContentsChanged() {
                    super.onContentsChanged();
                    cachedRecipe = null;
                    setChanged();
                    checkLockState();
                }
            };
            fluidRenderOrder[i] = i;
        }
    }

    public static <T extends net.minecraft.world.level.block.entity.BlockEntity>
    BlockEntityTicker<T> ticker(BlockEntityType<T> type) {
        BlockEntityType<ForgeControllerBlockEntity> expected = DifModBlockEntities.FORGE_FURNACE_CONTROLLER.get();
        return type.equals(expected)
                ? (lvl, pos, state, be) -> ((ForgeControllerBlockEntity) be).tick(lvl, pos, state)
                : null;
    }

    @Override
    protected void tick(Level level, BlockPos pos, BlockState blockState) {
        if (heatCacheTick-- <= 0) { heatCacheTick = HEAT_CACHE_PERIOD; refreshHeat(level, pos); }

        final boolean wasFormed = blockState.getValue(getFormedProperty());
        final int period = wasFormed ? FORMED_REVALIDATE_PERIOD : UNFORMED_REVALIDATE_PERIOD;
        final boolean shouldValidate = forceValidation || level.getGameTime() % period == 0;
        boolean isFormed = wasFormed;

        final Direction facing        = blockState.getOptionalValue(getFacingProperty()).orElse(Direction.SOUTH);
        final Direction intoStructure = facing.getOpposite();

        if (shouldValidate) {
            forceValidation = false;
            int actualGlass = countActualGlassLayers(level, pos, intoStructure);
            isFormed = validateBaseStructure(level) && actualGlass >= 1;
            if (isFormed && !wasFormed && !canClaimAllBricks(level, pos, intoStructure)) {
                isFormed = false;
                if (!isConflicted) { isConflicted = true; setChanged(); }
            }
        }

        if (isFormed != wasFormed) {
            if (isFormed) { claimBricks(level, pos, intoStructure, pos); isConflicted = false; }
            else { claimBricks(level, pos, intoStructure, null); resetProgress(); setLocked(false); glassLayers = 0; resizeTankCapacity(); }
            blockState = blockState.setValue(getFormedProperty(), isFormed).setValue(getActiveProperty(), false);
            level.setBlock(pos, blockState, 3);
            setChanged();
        }

        if (!isFormed) return;

        if (glassCacheTick-- <= 0 || shouldValidate) { glassCacheTick = GLASS_CHECK_PERIOD; refreshGlassLayers(level, pos, blockState); }
        if (locked) return;
        if (cachedHeatPoints == 0) { resetProgressAndDeactivate(level, pos, blockState); return; }
        if (!hasValidInput())      { resetProgressAndDeactivate(level, pos, blockState); return; }

        final ForgeMaterialRecipe recipe = findRecipe(level);
        if (recipe == null) { resetProgressAndDeactivate(level, pos, blockState); return; }

        totalTime = getProcessingTime(recipe);
        if (!canOutput(recipe)) { setActive(level, pos, blockState, false); return; }

        setActive(level, pos, blockState, true);
        progress++;
        if (progress >= totalTime) { finishRecipe(recipe); progress = 0; setChanged(); }
        else if (progress % 10 == 0) setChanged();
    }

    private void refreshHeat(Level level, BlockPos pos) {
        Direction intoStructure = getBlockState().getOptionalValue(getFacingProperty()).orElse(Direction.SOUTH).getOpposite();
        cachedHeatPoints = ForgeMultiblockHelper.calculateHeatPoints(level, pos, intoStructure);
        cachedHeatSpeed  = ForgeMultiblockHelper.heatPointsToSpeed(cachedHeatPoints);
        setChanged();
    }

    public int   getHeatPoints() { return cachedHeatPoints; }
    public float getHeatSpeed()  { return cachedHeatSpeed; }

    private int countActualGlassLayers(Level level, BlockPos pos, Direction intoStructure) {
        return ForgeMultiblockHelper.countGlassLayers(level, pos, intoStructure,
                state -> state.getBlock() instanceof ForgeGlass);
    }

    private void refreshGlassLayers(Level level, BlockPos pos, BlockState blockState) {
        if (!blockState.getValue(ForgeFurnaceController.FORMED)) return;
        Direction intoStructure = blockState.getOptionalValue(getFacingProperty()).orElse(Direction.SOUTH).getOpposite();
        int newLayers = ForgeMultiblockHelper.countGlassLayers(level, pos, intoStructure,
                state -> state.getBlock() instanceof ForgeGlass);
        if (newLayers == glassLayers) return;
        if (newLayers < glassLayers) handleGlassRemoved(newLayers);
        else if (locked) checkLockState();
        this.glassLayers = newLayers;
        resizeTankCapacity();
        propagateControllerPosToGlass(level, pos, blockState, newLayers);
        setChanged();
    }

    private void propagateControllerPosToGlass(Level level, BlockPos ctrlPos, BlockState blockState, int layers) {
        Direction intoStr = blockState.getOptionalValue(getFacingProperty()).orElse(Direction.SOUTH).getOpposite();
        Direction right   = intoStr.getClockWise();
        int scanDepth = Math.max(layers, ForgeMultiblockHelper.MAX_GLASS_LAYERS);
        for (int layer = 1; layer <= scanDepth; layer++)
            for (int z = 0; z < 3; z++)
                for (int x = -1; x <= 1; x++) {
                    BlockPos gp = ctrlPos.relative(intoStr, z).relative(right, x).above(layer);
                    if (level.getBlockEntity(gp) instanceof ForgeGlassBlockEntity gbe)
                        gbe.setControllerPos(layer <= layers ? ctrlPos : null);
                }
    }

    private void handleGlassRemoved(int newLayers) {
        trimExcessItems(newLayers);
        if (newLayers == 0) setLocked(true);
    }

    private int getOccupiedLayerCount() {
        long total = 0;
        for (FluidTank t : fluidTanks) total += t.getFluidAmount();
        return (int) Math.ceil((double) total / ForgeMultiblockHelper.MB_PER_GLASS_LAYER);
    }

    private void checkLockState() {
        if (!locked || level == null) return;
        if (getOccupiedLayerCount() <= glassLayers) setLocked(false);
    }

    private void setLocked(boolean value) {
        if (this.locked == value) return;
        this.locked = value;
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(ForgeFurnaceController.LOCKED))
                level.setBlock(worldPosition, state.setValue(ForgeFurnaceController.LOCKED, value), 3);
        }
        setChanged();
    }

    private void trimExcessItems(int newLayers) {
        int newSize = newLayers * SLOTS_PER_LAYER;
        for (int i = newSize; i < forgeInventory.getSlots(); i++) {
            ItemStack excess = forgeInventory.getStackInSlot(i);
            if (!excess.isEmpty() && level != null) {
                net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), excess);
                forgeInventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        resizeInventory(newSize);
    }

    private void resizeInventory(int newSize) {
        var newInv = new net.neoforged.neoforge.items.ItemStackHandler(newSize) {
            @Override protected void onContentsChanged(int slot) { cachedRecipe = null; setChanged(); }
            @Override public int getSlotLimit(int slot) { return 1; }
        };
        int copy = Math.min(forgeInventory.getSlots(), newSize);
        for (int i = 0; i < copy; i++) newInv.setStackInSlot(i, forgeInventory.getStackInSlot(i));
        forgeInventory = newInv;
    }

    private void resizeTankCapacity() {
        int totalMb = ForgeMultiblockHelper.totalFluidCapacity(glassLayers);
        for (FluidTank tank : fluidTanks) tank.setCapacity(Math.max(tank.getFluidAmount(), totalMb));
        int newInvSize = glassLayers * SLOTS_PER_LAYER;
        if (newInvSize != forgeInventory.getSlots()) resizeInventory(newInvSize);
    }

    public int     getGlassLayers() { return glassLayers; }
    public boolean isLocked()       { return locked; }

    public void setPreferredOutputTank(int tankIndex, Player player) {
        if (tankIndex < 0 || tankIndex >= FLUID_TANK_COUNT) {
            preferredOutputTank = -1;
            player.displayClientMessage(Component.literal("§7Output: auto"), true);
        } else if (!fluidTanks[tankIndex].isEmpty()) {
            preferredOutputTank = tankIndex;
            promoteToBottom(tankIndex);
            FluidStack fs = fluidTanks[tankIndex].getFluid();
            player.displayClientMessage(
                    Component.literal("§6Output: §f" + fs.getHoverName().getString()
                            + " §7(" + formatMb(fs.getAmount()) + ")"), true);
        }
        setChanged();
    }

    private void promoteToBottom(int tankIdx) {
        int pos = -1;
        for (int i = 0; i < FLUID_TANK_COUNT; i++) {
            if (fluidRenderOrder[i] == tankIdx) { pos = i; break; }
        }
        if (pos <= 0) return;
        for (int i = pos; i > 0; i--) fluidRenderOrder[i] = fluidRenderOrder[i - 1];
        fluidRenderOrder[0] = tankIdx;
    }

    public int[]  getFluidRenderOrder()     { return fluidRenderOrder; }
    public int    getPreferredOutputTank()  { return preferredOutputTank; }

    public void applyRenderOrder(int[] newOrder) {
        if (newOrder.length != FLUID_TANK_COUNT) return;
        System.arraycopy(newOrder, 0, fluidRenderOrder, 0, FLUID_TANK_COUNT);
        setChanged();
    }

    public FluidStack getFluidInTank(int idx) {
        return (idx < 0 || idx >= FLUID_TANK_COUNT) ? FluidStack.EMPTY : fluidTanks[idx].getFluid();
    }

    public FluidStack drainFromTank(int idx, int amount, IFluidHandler.FluidAction action) {
        if (idx < 0 || idx >= FLUID_TANK_COUNT) return FluidStack.EMPTY;
        return fluidTanks[idx].drain(amount, action);
    }

    public int getTotalFluidAmount() {
        int total = 0;
        for (FluidTank t : fluidTanks) total += t.getFluidAmount();
        return total;
    }

    public int addMoltenFluid(FluidStack fluid) {
        if (locked || fluid.isEmpty()) return 0;
        int spaceLeft = Math.max(0, ForgeMultiblockHelper.totalFluidCapacity(glassLayers) - getTotalFluidAmount());
        if (spaceLeft <= 0) return 0;
        FluidStack res = fluid.copy();
        if (res.getAmount() > spaceLeft) res.setAmount(spaceLeft);
        for (FluidTank t : fluidTanks) {
            if (!t.isEmpty() && t.getFluid().getFluid() == res.getFluid()) { int f = t.fill(res, IFluidHandler.FluidAction.EXECUTE); compactFluids(); return f; }
        }
        for (FluidTank t : fluidTanks) {
            if (t.isEmpty()) { int f = t.fill(res, IFluidHandler.FluidAction.EXECUTE); compactFluids(); return f; }
        }
        return 0;
    }

    public void compactFluids() {
        if (compacting) return;
        compacting = true;
        try {
            for (int i = 0; i < FLUID_TANK_COUNT - 1; i++) {
                int ti = fluidRenderOrder[i];
                if (fluidTanks[ti].isEmpty()) {
                    for (int j = i + 1; j < FLUID_TANK_COUNT; j++) {
                        int si = fluidRenderOrder[j];
                        if (!fluidTanks[si].isEmpty()) {
                            fluidTanks[ti].setFluid(fluidTanks[si].getFluid().copy());
                            fluidTanks[si].setFluid(FluidStack.EMPTY);
                            if (preferredOutputTank == si) preferredOutputTank = ti;
                            setChanged();
                            break;
                        }
                    }
                }
            }
        } finally {
            compacting = false;
        }
    }

    @Override
    public boolean handleInteraction(Player player, InteractionHand hand) {
        if (level == null || level.isClientSide) return true;
        final ItemStack held = player.getItemInHand(hand);

        if (held.getItem() == Items.BUCKET) {
            tryFillBucket(player, hand, held);
            return true;
        }

        if (!held.isEmpty()) {
            if (locked) return true;
            int inserted = 0;
            for (int i = 0; i < forgeInventory.getSlots() && !held.isEmpty(); i++) {
                if (forgeInventory.getStackInSlot(i).isEmpty()) {
                    forgeInventory.insertItem(i, held.copyWithCount(1), false);
                    held.shrink(1);
                    inserted++;
                }
            }
            if (inserted > 0) { player.setItemInHand(hand, held); setChanged(); }
            return true;
        }

        if (player.isShiftKeyDown()) {
            for (int i = forgeInventory.getSlots() - 1; i >= 0; i--) {
                ItemStack stack = forgeInventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    if (!player.getInventory().add(stack.copy())) player.drop(stack.copy(), false);
                    forgeInventory.setStackInSlot(i, ItemStack.EMPTY);
                    setChanged();
                    return true;
                }
            }
            return true;
        }

        for (int i = forgeInventory.getSlots() - 1; i >= 0; i--) {
            ItemStack stack = forgeInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemStack one = stack.copyWithCount(1);
                boolean stacked = false;
                for (int s = 0; s < player.getInventory().getContainerSize(); s++) {
                    ItemStack inv = player.getInventory().getItem(s);
                    if (!inv.isEmpty() && ItemStack.isSameItemSameComponents(inv, one) && inv.getCount() < inv.getMaxStackSize()) {
                        inv.grow(1); stacked = true; break;
                    }
                }
                if (!stacked) player.setItemInHand(hand, one);
                stack.shrink(1);
                forgeInventory.setStackInSlot(i, stack.isEmpty() ? ItemStack.EMPTY : stack);
                setChanged();
                return true;
            }
        }
        return true;
    }

    @Override protected Predicate<BlockState>[][][] getPattern() { return new Predicate[0][0][0]; }

    @Override
    protected boolean hasValidInput() {
        for (int i = 0; i < forgeInventory.getSlots(); i++)
            if (!forgeInventory.getStackInSlot(i).isEmpty()) return true;
        return false;
    }

    @Override
    protected @Nullable ForgeMaterialRecipe findRecipe(Level level) {
        for (int i = 0; i < forgeInventory.getSlots(); i++) {
            ItemStack input = forgeInventory.getStackInSlot(i);
            if (input.isEmpty()) continue;
            if (cachedRecipe != null && cachedRecipe.matchesItem(input, cachedHeatPoints)) return cachedRecipe;
            for (var h : level.getRecipeManager().getAllRecipesFor(DifModRecipes.FORGE_MATERIAL_TYPE.get()))
                if (h.value().matchesItem(input, cachedHeatPoints)) { cachedRecipe = h.value(); return cachedRecipe; }
        }
        cachedRecipe = null;
        return null;
    }

    private int findRecipeSlot(ForgeMaterialRecipe recipe) {
        if (recipe == null) return -1;
        for (int i = 0; i < forgeInventory.getSlots(); i++)
            if (recipe.matchesItem(forgeInventory.getStackInSlot(i), cachedHeatPoints)) return i;
        return -1;
    }

    @Override
    protected boolean canOutput(ForgeMaterialRecipe recipe) {
        if (recipe == null) return false;
        for (int i = 0; i < forgeInventory.getSlots(); i++) {
            ItemStack input = forgeInventory.getStackInSlot(i);
            if (input.isEmpty()) continue;
            FluidStack out = recipe.getOutputFor(input);
            if (out.isEmpty()) continue;
            for (FluidTank t : fluidTanks)
                if ((t.isEmpty() || t.getFluid().getFluid() == out.getFluid())
                        && t.fill(out, IFluidHandler.FluidAction.SIMULATE) >= out.getAmount()) return true;
        }
        return false;
    }

    @Override
    protected void finishRecipe(ForgeMaterialRecipe recipe) {
        if (recipe == null) return;
        int slot = findRecipeSlot(recipe);
        if (slot < 0) return;
        ItemStack input = forgeInventory.getStackInSlot(slot);
        FluidStack out  = recipe.getOutputFor(input);
        input.shrink(1);
        forgeInventory.setStackInSlot(slot, input);
        if (!out.isEmpty()) addMoltenFluid(out);
    }

    @Override
    protected int getProcessingTime(ForgeMaterialRecipe recipe) {
        if (recipe == null || cachedHeatSpeed <= 0) return 80;
        int baseTime = recipe.baseTime();
        for (int i = 0; i < forgeInventory.getSlots(); i++) {
            ItemStack input = forgeInventory.getStackInSlot(i);
            if (!input.isEmpty() && recipe.matchesItem(input, cachedHeatPoints)) {
                baseTime = recipe.getProcessingTimeFor(input);
                break;
            }
        }
        return Math.max(1, (int)(baseTime / cachedHeatSpeed));
    }

    @Override
    public @Nullable IFluidHandler getFluidCapability(@Nullable Direction side) {
        return new IFluidHandler() {
            @Override public int getTanks() { return FLUID_TANK_COUNT; }
            @Override public @NotNull FluidStack getFluidInTank(int tank) { return fluidTanks[tank].getFluid(); }
            @Override public int getTankCapacity(int tank) { return fluidTanks[tank].getCapacity(); }
            @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return true; }

            @Override public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
                if (locked || resource.isEmpty()) return 0;
                int space = Math.max(0, ForgeMultiblockHelper.totalFluidCapacity(glassLayers) - getTotalFluidAmount());
                if (space <= 0) return 0;
                FluidStack toFill = resource.copy();
                if (toFill.getAmount() > space) toFill.setAmount(space);
                for (FluidTank t : fluidTanks) {
                    if (!t.isEmpty() && t.getFluid().getFluid() == toFill.getFluid()) {
                        int filled = t.fill(toFill, action);
                        if (action.execute() && filled > 0) compactFluids();
                        return filled;
                    }
                }
                for (FluidTank t : fluidTanks) {
                    if (t.isEmpty()) {
                        int filled = t.fill(toFill, action);
                        if (action.execute() && filled > 0) compactFluids();
                        return filled;
                    }
                }
                return 0;
            }

            @Override public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
                for (FluidTank t : fluidTanks) {
                    if (t.getFluid().getFluid() == resource.getFluid()) {
                        FluidStack d = t.drain(resource, action);
                        if (action.execute() && !d.isEmpty()) compactFluids();
                        return d;
                    }
                }
                return FluidStack.EMPTY;
            }

            @Override public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
                if (preferredOutputTank >= 0 && preferredOutputTank < FLUID_TANK_COUNT
                        && !fluidTanks[preferredOutputTank].isEmpty()) {
                    FluidStack d = fluidTanks[preferredOutputTank].drain(maxDrain, action);
                    if (action.execute() && !d.isEmpty()) compactFluids();
                    return d;
                }
                for (int i = 0; i < FLUID_TANK_COUNT; i++) {
                    int idx = fluidRenderOrder[i];
                    if (!fluidTanks[idx].isEmpty()) {
                        FluidStack d = fluidTanks[idx].drain(maxDrain, action);
                        if (action.execute() && !d.isEmpty()) compactFluids();
                        return d;
                    }
                }
                return FluidStack.EMPTY;
            }
        };
    }

    private boolean canClaimAllBricks(Level level, BlockPos ctrlPos, Direction intoStructure) {
        final boolean[] ok = {true};
        ForgeMultiblockHelper.forEachBrick(ctrlPos, intoStructure, mp -> {
            if (!brickCanBeClaimedBy(level, mp, ctrlPos)) { ok[0] = false; return false; }
            return true;
        });
        return ok[0];
    }

    private void claimBricks(Level level, BlockPos ctrlPos, Direction intoStructure, @Nullable BlockPos owner) {
        ForgeMultiblockHelper.forEachBrick(ctrlPos, intoStructure, mp -> { setBrickController(level, mp, owner); return true; });
    }

    @Override
    protected boolean brickCanBeClaimedBy(Level level, BlockPos brickPos, BlockPos ctrlPos) {
        return !(level.getBlockEntity(brickPos) instanceof ForgeBrickBlockEntity b) || b.canBeClaimedBy(ctrlPos);
    }

    @Override
    protected void setBrickController(Level level, BlockPos brickPos, @Nullable BlockPos owner) {
        if (level.getBlockEntity(brickPos) instanceof ForgeBrickBlockEntity b) b.setControllerPos(owner);
    }

    public boolean validateBaseStructure(Level level) {
        Direction intoStructure = getBlockState().getOptionalValue(getFacingProperty()).orElse(Direction.SOUTH).getOpposite();
        return ForgeMultiblockHelper.validateBrickLayer(level, worldPosition, intoStructure,
                state -> state.getBlock() instanceof ForgeBrick);
    }

    @Override
    protected void tryFillBucket(Player player, InteractionHand hand, ItemStack heldBucket) {
        int[] order = preferredOutputTank >= 0
                ? new int[]{preferredOutputTank, fluidRenderOrder[0], fluidRenderOrder[1], fluidRenderOrder[2],
                fluidRenderOrder[3], fluidRenderOrder[4], fluidRenderOrder[5], fluidRenderOrder[6],
                fluidRenderOrder[7], fluidRenderOrder[8], fluidRenderOrder[9], fluidRenderOrder[10], fluidRenderOrder[11],
                fluidRenderOrder[12], fluidRenderOrder[13], fluidRenderOrder[14], fluidRenderOrder[15],
                fluidRenderOrder[16], fluidRenderOrder[17], fluidRenderOrder[18], fluidRenderOrder[19],
                fluidRenderOrder[20], fluidRenderOrder[21], fluidRenderOrder[22], fluidRenderOrder[23],
                fluidRenderOrder[24], fluidRenderOrder[25], fluidRenderOrder[26], fluidRenderOrder[27],
                fluidRenderOrder[28], fluidRenderOrder[29], fluidRenderOrder[30], fluidRenderOrder[31]}
                : fluidRenderOrder;
        for (int idx : order) {
            if (idx < 0 || idx >= FLUID_TANK_COUNT) continue;
            if (fluidTanks[idx].getFluidAmount() < 1000) continue;
            FluidStack drained = fluidTanks[idx].drain(1000, IFluidHandler.FluidAction.EXECUTE);
            if (drained.isEmpty()) continue;
            heldBucket.shrink(1);
            ItemStack filled = new ItemStack(drained.getFluid().getBucket());
            if (heldBucket.isEmpty()) player.setItemInHand(hand, filled);
            else if (!player.getInventory().add(filled)) player.drop(filled, false);
            compactFluids();
            setChanged();
            return;
        }
    }

    @Override protected Component getGoggleName() { return Component.literal("◆ Forge Furnace"); }
    @Override protected ChatFormatting getGoggleNameColor() { return ChatFormatting.GOLD; }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal(goggleTooltipFix + getGoggleName().getString())
                .withStyle(getGoggleNameColor(), ChatFormatting.BOLD));
        final BlockState state  = getBlockState();
        final boolean    formed = state.hasProperty(getFormedProperty()) && state.getValue(getFormedProperty());
        if (!formed) {
            tooltip.add(isConflicted
                    ? Component.literal(goggleTooltipFix + " ⚠ Structure already uses some blocks").withStyle(ChatFormatting.DARK_RED)
                    : Component.literal(goggleTooltipFix + " Structure is NOT formed!").withStyle(ChatFormatting.RED));
            return true;
        }
        appendFormedTooltip(tooltip, isPlayerSneaking);
        if (state.getValue(getActiveProperty()) && totalTime > 0) {
            int pct      = (int)(((double) progress / totalTime) * 100.0);
            int secsLeft = Math.max(0, (totalTime - progress) / 20);
            tooltip.add(Component.literal(goggleTooltipFix + " ▶ Progress: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(pct + "% (" + secsLeft + "s left)").withStyle(ChatFormatting.GREEN)));
        } else {
            tooltip.add(Component.literal(goggleTooltipFix + " ▶ Status: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Idle").withStyle(ChatFormatting.YELLOW)));
        }
        return true;
    }

    @Override
    protected void appendFormedTooltip(List<Component> tooltip) {
        appendFormedTooltip(tooltip, false);
    }

    private void appendFormedTooltip(List<Component> tooltip, boolean sneaking) {
        tooltip.add(Component.literal(goggleTooltipFix + " ▶ Heat: " + heatColor(cachedHeatPoints) + cachedHeatPoints + "§7/18").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(goggleTooltipFix + " ▶ Layers: " + glassLayers + "/" + ForgeMultiblockHelper.MAX_GLASS_LAYERS).withStyle(ChatFormatting.GRAY));
        int used = 0;
        for (int i = 0; i < forgeInventory.getSlots(); i++) if (!forgeInventory.getStackInSlot(i).isEmpty()) used++;
        if (forgeInventory.getSlots() > 0) tooltip.add(Component.literal(goggleTooltipFix + " ▶ Queue: " + used + "/" + forgeInventory.getSlots()).withStyle(ChatFormatting.GRAY));
        if (locked) tooltip.add(Component.literal(goggleTooltipFix + " ⚠ LOCKED").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));

        java.util.List<FluidTank> filled = new java.util.ArrayList<>();
        for (int i = 0; i < FLUID_TANK_COUNT; i++) {
            int idx = fluidRenderOrder[i];
            if (!fluidTanks[idx].isEmpty()) filled.add(fluidTanks[idx]);
        }
        if (filled.isEmpty()) return;

        int showCount = sneaking ? filled.size() : Math.min(4, filled.size());
        for (int i = 0; i < showCount; i++) {
            FluidTank t = filled.get(i);
            boolean isPref = false;
            for (int j = 0; j < FLUID_TANK_COUNT; j++) {
                if (fluidTanks[j] == t && j == preferredOutputTank) { isPref = true; break; }
            }
            String prefix = isPref ? "§6▶ §r" : "";
            appendFluidSlot(tooltip, goggleTooltipFix + " " + prefix, t);
        }
        if (!sneaking && filled.size() > 4) {
            tooltip.add(Component.literal(goggleTooltipFix + " §8[Shift] show all " + filled.size() + " fluids").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static String heatColor(int heat) {
        if (heat <= 0) return "§c"; if (heat < 9) return "§e"; if (heat == 9) return "§a"; if (heat < 18) return "§2"; return "§b";
    }

    private static String formatMb(int mb) {
        return mb >= 1000 ? String.format("%.1fB", mb / 1000f) : mb + "mB";
    }

    @Override protected DirectionProperty getFacingProperty() { return ForgeFurnaceController.FACING; }
    @Override protected BooleanProperty   getFormedProperty() { return ForgeFurnaceController.FORMED; }
    @Override protected BooleanProperty   getActiveProperty() { return ForgeFurnaceController.ACTIVE; }
    @Override protected @NotNull Component getDefaultName()   { return Component.translatable("container.dif.forge_furnace"); }
    @Override public @NotNull Component getDisplayName()      { return Component.translatable("container.dif.forge_furnace"); }

    @Override public net.neoforged.neoforge.items.ItemStackHandler getInventory() { return forgeInventory; }

    @Override public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        int[] s = new int[forgeInventory.getSlots()]; for (int i = 0; i < s.length; i++) s[i] = i; return s;
    }
    @Override public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction side) { return !locked && index >= 0 && index < forgeInventory.getSlots(); }
    @Override public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction side)   { return index >= 0 && index < forgeInventory.getSlots(); }
    @Override public boolean canPlaceItem(int index, @NotNull ItemStack stack) { return !locked && index >= 0 && index < forgeInventory.getSlots(); }
    @Override public int getContainerSize() { return forgeInventory.getSlots(); }
    @Override public @NotNull ItemStack getItem(int index) { return forgeInventory.getStackInSlot(index); }
    @Override public void setItem(int index, @NotNull ItemStack stack) { forgeInventory.setStackInSlot(index, stack); }
    @Override public @NotNull ItemStack removeItem(int slot, int amount) { return forgeInventory.extractItem(slot, amount, false); }
    @Override public @NotNull ItemStack removeItemNoUpdate(int index) { ItemStack s = forgeInventory.getStackInSlot(index); forgeInventory.setStackInSlot(index, ItemStack.EMPTY); return s; }
    @Override public boolean isEmpty() { for (int i = 0; i < forgeInventory.getSlots(); i++) if (!forgeInventory.getStackInSlot(i).isEmpty()) return false; return true; }
    @Override protected @NotNull net.minecraft.core.NonNullList<ItemStack> getItems() {
        var list = net.minecraft.core.NonNullList.withSize(forgeInventory.getSlots(), ItemStack.EMPTY);
        for (int i = 0; i < forgeInventory.getSlots(); i++) list.set(i, forgeInventory.getStackInSlot(i));
        return list;
    }
    @Override protected void setItems(@NotNull net.minecraft.core.NonNullList<ItemStack> stacks) {
        for (int i = 0; i < stacks.size() && i < forgeInventory.getSlots(); i++) forgeInventory.setStackInSlot(i, stacks.get(i));
    }
    @Override protected void insertOrSwapInput(Player player, InteractionHand hand, ItemStack held) { }
    @Override protected void extractOutput(Player player, InteractionHand hand) { }

    @Override
    protected void saveExtraData(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        tag.putInt("glassLayers", glassLayers);
        tag.putBoolean("locked", locked);
        tag.putInt("heatPoints", cachedHeatPoints);
        tag.putFloat("heatSpeed", cachedHeatSpeed);
        tag.putIntArray("renderOrder", fluidRenderOrder);
        tag.putInt("preferredOutputTank", preferredOutputTank);
        ListTag tanksTag = new ListTag();
        for (FluidTank t : fluidTanks) tanksTag.add(t.writeToNBT(provider, new CompoundTag()));
        tag.put("fluidTanks", tanksTag);
        tag.put("forgeInv", forgeInventory.serializeNBT(provider));
    }

    @Override
    protected void loadExtraData(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        glassLayers          = tag.getInt("glassLayers");
        locked               = tag.getBoolean("locked");
        cachedHeatPoints     = tag.getInt("heatPoints");
        cachedHeatSpeed      = tag.getFloat("heatSpeed");
        preferredOutputTank  = tag.contains("preferredOutputTank") ? tag.getInt("preferredOutputTank") : -1;
        int[] savedOrder = tag.getIntArray("renderOrder");
        if (savedOrder.length == FLUID_TANK_COUNT) {
            System.arraycopy(savedOrder, 0, fluidRenderOrder, 0, FLUID_TANK_COUNT);
        } else {
            for (int i = 0; i < FLUID_TANK_COUNT; i++) fluidRenderOrder[i] = i;
        }
        if (tag.contains("fluidTanks")) {
            ListTag tl = tag.getList("fluidTanks", Tag.TAG_COMPOUND);
            for (int i = 0; i < Math.min(tl.size(), FLUID_TANK_COUNT); i++) fluidTanks[i].readFromNBT(provider, tl.getCompound(i));
        }
        int invSize = glassLayers * SLOTS_PER_LAYER;
        resizeInventory(invSize);
        if (tag.contains("forgeInv")) forgeInventory.deserializeNBT(provider, tag.getCompound("forgeInv"));
        resizeTankCapacity();
    }
}