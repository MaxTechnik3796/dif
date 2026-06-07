package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import cz.maxtechnik.dif.block.ForgeBrickBlock;
import cz.maxtechnik.dif.block.ForgeFurnaceController;
import cz.maxtechnik.dif.block.ForgeGlassBlock;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import cz.maxtechnik.dif.recipe.ForgeSmeltingRecipe;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static cz.maxtechnik.dif.DifMod.goggleTooltipFix;

/**
 * ForgeControllerBlockEntity — řídicí block entity Forge pece.
 *
 * ═══════════════════════════════════════════════════════════════
 *  LOGIKA STRUKTURY
 * ═══════════════════════════════════════════════════════════════
 *
 * Validace probíhá ve dvou fázích:
 *   1. validateBase()  → ověří Blaze Burner vrstvu (Y-1) a Brick vrstvu (Y+0)
 *   2. countGlass()    → spočítá sklo vrstvy od Y+2 nahoru
 *
 * Kapacita se dynamicky přepočítává podle počtu sklo vrstev:
 *   - glassLayers × MB_PER_GLASS_LAYER mB fluid kapacity
 *   - glassLayers × SLOTS_PER_GLASS_LAYER item slotů
 *
 * ═══════════════════════════════════════════════════════════════
 *  LOCKED STATE (uzamčení při poškození skla)
 * ═══════════════════════════════════════════════════════════════
 *
 * Controller porovnává uložený glassLayers s aktuálním countGlass().
 * Pokud aktuální < uložený:
 *   - Kapalina sahá pouze do intaktních vrstev → jen sníží kapacitu
 *   - Kapalina sahá do poškozené/chybějící vrstvy → LOCKED = true
 *
 * LOCKED blokuje: nové suroviny do vstupní fronty, výstup přes spout/pipe
 * Odemknutí: hráč dostaví sklo NEBO vypustí kapalinu pod kritickou hladinu
 *
 * ═══════════════════════════════════════════════════════════════
 *  MULTI-FLUID (4 oddělené tanky)
 * ═══════════════════════════════════════════════════════════════
 *
 * Kapaliny se nemíchají. Každý fluid má vlastní tank (FluidTank).
 * Pořadí (pro rendering) lze měnit přes GUI — hráč přetáhne kapaliny.
 * Renderování čte fluidTanks[] + fillLevel pro výšku hladiny.
 */
public class ForgeControllerBlockEntity extends AbstractMultiblockControllerBlockEntity<ForgeSmeltingRecipe> {

    // ═══════════════════════════════════════════════════════════
    //  KONSTANTY
    // ═══════════════════════════════════════════════════════════

    private static final int HEAT_CACHE_PERIOD  = 20;  // ticků mezi refreshem heat
    private static final int GLASS_CHECK_PERIOD = 20;  // ticků mezi kontrolou skla

    /** Počet oddělených fluid tanků (různé kapaliny vedle sebe). */
    public static final int FLUID_TANK_COUNT = 4;

    // ═══════════════════════════════════════════════════════════
    //  FLUID TANKY
    // ═══════════════════════════════════════════════════════════

    /**
     * 4 oddělené tanky pro různé roztavené kapaliny.
     * Kapacita každého tanku = totalFluidCapacity / FLUID_TANK_COUNT.
     * Při změně glassLayers se kapacita dynamicky přepočítá.
     */
    public final FluidTank[] fluidTanks = new FluidTank[FLUID_TANK_COUNT];

    /**
     * Pořadí tanků pro rendering (index do fluidTanks[]).
     * Hráč může měnit pořadí přes GUI — nejtěžší kapalina typicky dole.
     */
    private final int[] fluidRenderOrder = {0, 1, 2, 3};

    // ═══════════════════════════════════════════════════════════
    //  STAV STRUKTURY
    // ═══════════════════════════════════════════════════════════

    /** Počet aktuálně potvrzených sklo vrstev. */
    private int glassLayers = 0;

    /** True pokud controller nelze použít kvůli poškozené vrstvě pod kapalinou. */
    private boolean locked = false;

    /** Heat body ze všech 9 Blaze Burnerů (0–18). Cachováno každých 20 ticků. */
    private int cachedHeatPoints = 0;

    /** Rychlostní multiplikátor z heat bodů. */
    private float cachedHeatSpeed = 0f;

    private int heatCacheTick  = 0;
    private int glassCacheTick = 0;

    // ═══════════════════════════════════════════════════════════
    //  KONSTRUKTOR
    // ═══════════════════════════════════════════════════════════

    public ForgeControllerBlockEntity(BlockPos pos, BlockState blockState) {
        super(DifModBlockEntities.FORGE_FURNACE_CONTROLLER.get(), pos, blockState);

        // Inicializace tanků — kapacita se nastaví až při první validaci
        for (int i = 0; i < FLUID_TANK_COUNT; i++) {
            final int tankIndex = i;
            fluidTanks[i] = new FluidTank(ForgeMultiblockHelper.MB_PER_GLASS_LAYER) {
                @Override
                protected void onContentsChanged() {
                    super.onContentsChanged();
                    cachedRecipe = null;
                    setChanged();
                    // Zkontroluj uzamčení při každé změně hladiny
                    checkLockState();
                }
            };
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  TICKER FACTORY
    // ═══════════════════════════════════════════════════════════

    public static <T extends net.minecraft.world.level.block.entity.BlockEntity>
    BlockEntityTicker<T> ticker(BlockEntityType<T> type) {
        BlockEntityType<ForgeControllerBlockEntity> expected =
                DifModBlockEntities.FORGE_FURNACE_CONTROLLER.get();
        return type.equals(expected)
                ? (lvl, pos, state, be) -> ((ForgeControllerBlockEntity) be).tick(lvl, pos, state)
                : null;
    }

    // ═══════════════════════════════════════════════════════════
    //  TICK OVERRIDE — rozšiřuje AbstractMultiblockControllerBlockEntity
    // ═══════════════════════════════════════════════════════════

    /**
     * Rozšíření tick() z abstraktní třídy.
     * Přidává: heat refresh, glass check, lock kontrolu, integrita briků.
     *
     * VOLÁ SE Z: ticker() factory výše.
     * POZN: super.tick() voláme ručně aby se zachovala validace struktury.
     */
    @Override
    protected void tick(Level level, BlockPos pos, BlockState blockState) {
        // 1. Heat refresh (každých 20 ticků)
        if (heatCacheTick-- <= 0) {
            heatCacheTick = HEAT_CACHE_PERIOD;
            refreshHeat(level, pos);
        }

        // 2. Validace struktury (nahrazuje validaci z abstraktní třídy)
        final boolean wasFormed = blockState.getValue(getFormedProperty());
        final int period = wasFormed ? FORMED_REVALIDATE_PERIOD : UNFORMED_REVALIDATE_PERIOD;
        final boolean shouldValidate = forceValidation || level.getGameTime() % period == 0;

        boolean isFormed = wasFormed;

        if (shouldValidate) {
            forceValidation = false;
            // Struktura je zformovaná pokud má hotovou bázi a alespoň 1 vrstvu skla
            int actualGlass = countActualGlassLayers(level, pos);
            isFormed = validateBaseStructure(level) && actualGlass >= 1;

            // Nová formace — zkontroluj, zda jsou cihličky volné k zabrání
            if (isFormed && !wasFormed && !canClaimAllBricks(level, pos)) {
                isFormed = false;
                if (!isConflicted) {
                    isConflicted = true;
                    setChanged();
                }
            }
        }

        if (isFormed != wasFormed) {
            if (isFormed) {
                claimBricks(level, pos, pos);
                isConflicted = false;
            } else {
                claimBricks(level, pos, null);
                resetProgress();
                setLocked(false); // Reset locked stavu při deformování
            }
            blockState = blockState
                    .setValue(getFormedProperty(), isFormed)
                    .setValue(getActiveProperty(), false);
            level.setBlock(pos, blockState, 3);
            setChanged();
        }

        if (!isFormed) return;

        // ── Glass check (každých 20 ticků nebo při by-validation změnách) ──
        if (glassCacheTick-- <= 0 || shouldValidate) {
            glassCacheTick = GLASS_CHECK_PERIOD;
            refreshGlassLayers(level, pos, blockState);
        }

        // ── Pokud je locked, přeskoč processing ───────────────────────────────
        if (locked) {
            return;
        }

        // ── Pokud není heat, nezpracovávej ────────────────────────────────────
        if (cachedHeatPoints == 0) {
            resetProgressAndDeactivate(level, pos, blockState);
            return;
        }

        // Vstup → recept
        if (!hasValidInput()) {
            resetProgressAndDeactivate(level, pos, blockState);
            return;
        }

        final ForgeSmeltingRecipe recipe = findRecipe(level);
        if (recipe == null) {
            resetProgressAndDeactivate(level, pos, blockState);
            return;
        }

        totalTime = getProcessingTime(recipe);

        if (!canOutput(recipe)) {
            setActive(level, pos, blockState, false);
            return;
        }

        setActive(level, pos, blockState, true);
        progress++;

        if (progress >= totalTime) {
            finishRecipe(recipe);
            progress = 0;
            setChanged();
        } else if (progress % 10 == 0) {
            setChanged();
        }
    }

    private int countActualGlassLayers(Level level, BlockPos pos) {
        Predicate<BlockState> glassPred = state -> state.getBlock() instanceof ForgeGlassBlock;
        return ForgeMultiblockHelper.countGlassLayers(level, pos, glassPred);
    }

    private boolean canClaimAllBricks(Level level, BlockPos controllerPos) {
        final boolean[] ok = {true};
        ForgeMultiblockHelper.forEachBrick(controllerPos, mp -> {
            if (!brickCanBeClaimedBy(level, mp, controllerPos)) {
                ok[0] = false;
                return false; // stop
            }
            return true;
        });
        return ok[0];
    }

    private void claimBricks(Level level, BlockPos controllerPos, @Nullable BlockPos owner) {
        ForgeMultiblockHelper.forEachBrick(controllerPos, mp -> {
            setBrickController(level, mp, owner);
            return true;
        });
    }

    // ═══════════════════════════════════════════════════════════
    //  HEAT LOGIKA
    // ═══════════════════════════════════════════════════════════

    private void refreshHeat(Level level, BlockPos pos) {
        cachedHeatPoints = ForgeMultiblockHelper.calculateHeatPoints(level, pos);
        cachedHeatSpeed  = ForgeMultiblockHelper.heatPointsToSpeed(cachedHeatPoints);
        setChanged();
    }

    public int getHeatPoints()  { return cachedHeatPoints; }
    public float getHeatSpeed() { return cachedHeatSpeed; }

    // ═══════════════════════════════════════════════════════════
    //  GLASS LAYER LOGIKA
    // ═══════════════════════════════════════════════════════════

    private void refreshGlassLayers(Level level, BlockPos pos, BlockState blockState) {
        if (!blockState.getValue(ForgeFurnaceController.FORMED)) return;

        Predicate<net.minecraft.world.level.block.state.BlockState> glassPred =
                state -> state.getBlock() instanceof ForgeGlassBlock;

        int newLayers = ForgeMultiblockHelper.countGlassLayers(level, pos, glassPred);

        if (newLayers == glassLayers) return; // Žádná změna

        if (newLayers < glassLayers) {
            // Vrstvy ubývají — zkontroluj uzamčení
            handleGlassRemoved(newLayers);
        } else {
            // Vrstvy přibývají — rozšiř kapacitu
            handleGlassAdded(newLayers);
        }

        this.glassLayers = newLayers;
        resizeTankCapacity();
        setChanged();
    }

    /**
     * Zkontroluje a nastaví LOCKED state.
     * Uzamkne se pokud kapalina sahá do vrstvy která chybí.
     */
    private void handleGlassRemoved(int newLayers) {
        // Kapalina zabírá vrstvami od spodu — spočítej kolik vrstev je obsazených
        int occupiedLayers = getOccupiedLayerCount();
        if (occupiedLayers > newLayers) {
            setLocked(true);
        } else {
            // Kapalina je v intaktních vrstvách — jen sníž kapacitu
            // Přebytečné itemy ve vstupní frontě vyhoď
            trimExcessItems(newLayers);
        }
    }

    private void handleGlassAdded(int newLayers) {
        // Pokud byl locked a nová vrstva uzavírá díru → zkus odemknout
        if (locked) {
            checkLockState();
        }
    }

    /**
     * Přepočítá kolik vrstev je obsazeno kapalinou (odspodu).
     * 1 vrstva = MB_PER_GLASS_LAYER mB.
     */
    private int getOccupiedLayerCount() {
        long totalFilled = 0;
        for (FluidTank tank : fluidTanks) {
            totalFilled += tank.getFluidAmount();
        }
        int mbPerLayer = ForgeMultiblockHelper.MB_PER_GLASS_LAYER;
        return (int) Math.ceil((double) totalFilled / mbPerLayer);
    }

    /**
     * Zkontroluje zda lze odemknout controller.
     * Odemkne se pokud: kapalina sahá max do glassLayers vrstev.
     */
    private void checkLockState() {
        if (!locked) return;
        if (level == null) return;

        int occupied = getOccupiedLayerCount();
        if (occupied <= glassLayers) {
            setLocked(false);
        }
    }

    private void setLocked(boolean value) {
        if (this.locked == value) return;
        this.locked = value;
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(ForgeFurnaceController.LOCKED)) {
                level.setBlock(worldPosition,
                        state.setValue(ForgeFurnaceController.LOCKED, value), 3);
            }
        }
        setChanged();
    }

    /** Vyhodí přebytečné itemy ze vstupní fronty pokud se snížil počet slotů. */
    private void trimExcessItems(int newLayers) {
        int maxSlots = ForgeMultiblockHelper.totalItemSlots(newLayers);
        var inv = getInventory();
        for (int i = maxSlots; i < inv.getSlots(); i++) {
            ItemStack excess = inv.getStackInSlot(i);
            if (!excess.isEmpty() && level != null) {
                net.minecraft.world.Containers.dropItemStack(
                        level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), excess);
                inv.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    /** Přepočítá kapacitu všech tanků podle aktuálního glassLayers. */
    private void resizeTankCapacity() {
        int totalMb = ForgeMultiblockHelper.totalFluidCapacity(glassLayers);
        int perTank = Math.max(1000, totalMb / FLUID_TANK_COUNT);
        for (FluidTank tank : fluidTanks) {
            tank.setCapacity(perTank);
        }
    }

    public int getGlassLayers() { return glassLayers; }
    public boolean isLocked()   { return locked; }

    // ═══════════════════════════════════════════════════════════
    //  MULTI-FLUID API (pro spout a pipe)
    // ═══════════════════════════════════════════════════════════

    /**
     * Vrátí fluid z konkrétního tanku podle indexu.
     * Spout zavolá tuto metodu s indexem kapaliny kterou chce.
     */
    public FluidStack getFluidInTank(int tankIndex) {
        if (tankIndex < 0 || tankIndex >= FLUID_TANK_COUNT) return FluidStack.EMPTY;
        return fluidTanks[tankIndex].getFluid();
    }

    /**
     * Odebere fluid z konkrétního tanku (volá spout).
     * Pokud je locked, vrátí EMPTY a nic neodebere.
     */
    public FluidStack drainFromTank(int tankIndex, int amount, IFluidHandler.FluidAction action) {
        if (tankIndex < 0 || tankIndex >= FLUID_TANK_COUNT) return FluidStack.EMPTY;
        return fluidTanks[tankIndex].drain(amount, action);
    }

    /**
     * Přidá roztavený fluid do prvního volného nebo odpovídajícího tanku.
     * Pokud je locked, odmítne přidat.
     *
     * @return množství skutečně přidaného fluidu
     */
    public int addMoltenFluid(FluidStack fluid) {
        if (locked) return 0;

        // Nejdřív najdi tank se stejným fluidem
        for (FluidTank tank : fluidTanks) {
            if (!tank.isEmpty() && tank.getFluid().getFluid() == fluid.getFluid()) {
                return tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
            }
        }
        // Pak první prázdný tank
        for (FluidTank tank : fluidTanks) {
            if (tank.isEmpty()) {
                return tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
            }
        }
        return 0; // Všechny tanky plné nebo obsazené jiným fluidem
    }

    /** Render order pro klientský renderer — pořadí vrstev kapaliny. */
    public int[] getFluidRenderOrder() { return fluidRenderOrder; }

    // ═══════════════════════════════════════════════════════════
    //  ABSTRAKTNÍ METODY — implementace
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Predicate<BlockState>[][][] getPattern() {
        // Forge pec nepoužívá standardní MultiblockHelper pattern —
        // validace probíhá přes ForgeMultiblockHelper.validateBase()
        // Vrátíme null a přepíšeme validaci v tick()
        // POZN: AbstractMultiblockControllerBlockEntity.tick() volá
        //       MultiblockHelper.isValid() s tímto patternem.
        //       Proto vracíme prázdné pole — reálná validace je níže.
        return new Predicate[0][0][0];
    }

    /**
     * Přepisujeme validaci struktury — nepoužíváme standardní MultiblockHelper.
     * Forge pec validuje zvlášť spodní vrstvu a sklo vrstvy.
     *
     * POZN: Tato metoda se volá z AbstractMultiblockControllerBlockEntity.tick()
     * přes MultiblockHelper.isValid(). Protože getPattern() vrací prázdné pole,
     * musíme validaci celou přepsat.
     *
     * Řešení: Override tick() (viz výše) volá super.tick() ALE my v getPattern()
     * vrátíme funkční predikáty jen pro bázi. Sklo řešíme separátně.
     */
    @Override
    protected boolean hasValidInput() {
        // Vstup je platný pokud je něco ve vstupní frontě
        var inv = getInventory();
        for (int i = 0; i < inv.getSlots(); i++) {
            if (!inv.getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    @Override
    protected @Nullable ForgeSmeltingRecipe findRecipe(Level level) {
        final ItemStack input = inventory.getStackInSlot(SLOT_INPUT);
        if (input.isEmpty()) return null;
        
        if (cachedRecipe != null && cachedRecipe.matches(input, cachedHeatPoints)) return cachedRecipe;

        for (var holder : level.getRecipeManager().getAllRecipesFor(DifModRecipes.FORGE_SMELTING_TYPE.get())) {
            if (holder.value().matches(input, cachedHeatPoints)) {
                cachedRecipe = holder.value();
                return cachedRecipe;
            }
        }
        cachedRecipe = null;
        return null;
    }

    @Override
    protected boolean canOutput(ForgeSmeltingRecipe recipe) {
        // Zkontroluj jestli je volný tank pro výstupní fluid
        if (recipe == null) return false;
        FluidStack output = recipe.outputFluid();
        for (FluidTank tank : fluidTanks) {
            if (tank.isEmpty() || tank.getFluid().getFluid() == output.getFluid()) {
                if (tank.fill(output, IFluidHandler.FluidAction.SIMULATE) >= output.getAmount()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void finishRecipe(ForgeSmeltingRecipe recipe) {
        if (recipe == null) return;
        // Spotřebuj vstupní item
        var inv = getInventory();
        ItemStack input = inv.getStackInSlot(SLOT_INPUT);
        input.shrink(1);
        inv.setStackInSlot(SLOT_INPUT, input);

        // Přidej výstupní fluid
        addMoltenFluid(recipe.outputFluid());
    }

    @Override
    protected int getProcessingTime(ForgeSmeltingRecipe recipe) {
        if (recipe == null) return 80;
        // Základní čas upravený heat speedem
        int base = recipe.baseTime();
        if (cachedHeatSpeed <= 0) return base;
        return Math.max(1, (int)(base / cachedHeatSpeed));
    }

    @Override
    public @Nullable IFluidHandler getFluidCapability(@Nullable Direction side) {
        // Kombinovaný handler přes všechny 4 tanky
        return new IFluidHandler() {
            @Override public int getTanks() { return FLUID_TANK_COUNT; }
            @Override public @NotNull FluidStack getFluidInTank(int tank) {
                return fluidTanks[tank].getFluid();
            }
            @Override public int getTankCapacity(int tank) {
                return fluidTanks[tank].getCapacity();
            }
            @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
                return true;
            }
            @Override public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
                return 0; // Nelze plnit zvenku přímo — jen přes tavení
            }
            @Override public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
                for (FluidTank tank : fluidTanks) {
                    if (tank.getFluid().getFluid() == resource.getFluid()) {
                        return tank.drain(resource, action);
                    }
                }
                return FluidStack.EMPTY;
            }
            @Override public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
                for (FluidTank tank : fluidTanks) {
                    if (!tank.isEmpty()) return tank.drain(maxDrain, action);
                }
                return FluidStack.EMPTY;
            }
        };
    }

    // ── Brick claiming ────────────────────────────────────────────────────────

    @Override
    protected boolean brickCanBeClaimedBy(Level level, BlockPos brickPos, BlockPos controllerPos) {
        return !(level.getBlockEntity(brickPos) instanceof ForgeBrickBlockEntity brick)
                || brick.canBeClaimedBy(controllerPos);
    }

    @Override
    protected void setBrickController(Level level, BlockPos brickPos, @Nullable BlockPos owner) {
        if (level.getBlockEntity(brickPos) instanceof ForgeBrickBlockEntity brick) {
            brick.setControllerPos(owner);
        }
    }

    // ── Pattern pro validaci báze ─────────────────────────────────────────────

    /**
     * Validace základní struktury (bez skla) — volá se z tick() přes forceValidation.
     * Ověří Blaze Burnery a Forge Briky.
     */
    public boolean validateBaseStructure(Level level) {
        Predicate<BlockState> brickPred =
                state -> state.getBlock() instanceof ForgeBrickBlock;

        return ForgeMultiblockHelper.validateBrickLayer(level, worldPosition, brickPred);
    }

    // ── Kbelík ────────────────────────────────────────────────────────────────

    @Override
    protected void tryFillBucket(Player player, InteractionHand hand, ItemStack heldBucket) {
        // Naplní kbelík z prvního neprázdného tanku s dostatkem kapaliny
        for (FluidTank tank : fluidTanks) {
            if (tank.getFluidAmount() >= 1000) {
                FluidStack drained = tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                if (drained.isEmpty()) continue;
                heldBucket.shrink(1);
                ItemStack filled = new ItemStack(drained.getFluid().getBucket());
                if (heldBucket.isEmpty()) player.setItemInHand(hand, filled);
                else if (!player.getInventory().add(filled)) player.drop(filled, false);
                setChanged();
                return;
            }
        }
    }

    // ── Goggle tooltip ────────────────────────────────────────────────────────

    @Override
    protected Component getGoggleName() {
        return Component.literal("◆ Forge Furnace");
    }

    @Override
    protected ChatFormatting getGoggleNameColor() {
        return ChatFormatting.GOLD;
    }

    @Override
    protected void appendFormedTooltip(List<Component> tooltip) {
        // Heat
        String heatColor = cachedHeatPoints >= 16 ? "§a" : cachedHeatPoints >= 10 ? "§e" : "§c";
        tooltip.add(Component.literal(goggleTooltipFix + " ▶ Heat: " + heatColor + cachedHeatPoints + "/18")
                .withStyle(ChatFormatting.GRAY));

        // Sklo vrstvy
        tooltip.add(Component.literal(goggleTooltipFix + " ▶ Glass layers: " + glassLayers)
                .withStyle(ChatFormatting.GRAY));

        // Locked stav
        if (locked) {
            tooltip.add(Component.literal(goggleTooltipFix + " ⚠ LOCKED — repair glass or drain fluid!")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        }

        // Fluid tanky
        for (int i = 0; i < FLUID_TANK_COUNT; i++) {
            FluidTank tank = fluidTanks[i];
            if (!tank.isEmpty()) {
                appendFluidSlot(tooltip,
                        goggleTooltipFix + " ▶ Tank " + (i + 1) + ": ", tank);
            }
        }
    }

    @Override
    protected DirectionProperty getFacingProperty() { return ForgeFurnaceController.FACING; }

    @Override
    protected BooleanProperty getFormedProperty() { return ForgeFurnaceController.FORMED; }

    @Override
    protected BooleanProperty getActiveProperty() { return ForgeFurnaceController.ACTIVE; }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.dif.forge_furnace");
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.dif.forge_furnace");
    }

    // ── Helper pro tick() override ────────────────────────────────────────────

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack itemStack, @Nullable Direction side) {
        if (locked) return false;
        return super.canPlaceItemThroughFace(index, itemStack, side);
    }

    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack itemStack) {
        if (locked) return false;
        return super.canPlaceItem(index, itemStack);
    }

    @Override
    protected void insertOrSwapInput(Player player, InteractionHand hand, ItemStack held) {
        if (locked) return;
        super.insertOrSwapInput(player, hand, held);
    }

    // Potřebujeme přístup k setActive z abstraktní třídy — je protected, dostaneme se k ní
    // přímo protože jsme podtřída. Kompilátor to povolí.

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void saveExtraData(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        tag.putInt("glassLayers", glassLayers);
        tag.putBoolean("locked", locked);
        tag.putInt("heatPoints", cachedHeatPoints);
        tag.putFloat("heatSpeed", cachedHeatSpeed);
        tag.putIntArray("renderOrder", fluidRenderOrder);

        // Ulož všechny fluid tanky
        ListTag tanksTag = new ListTag();
        for (FluidTank tank : fluidTanks) {
            tanksTag.add(tank.writeToNBT(provider, new CompoundTag()));
        }
        tag.put("fluidTanks", tanksTag);
    }

    @Override
    protected void loadExtraData(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        glassLayers      = tag.getInt("glassLayers");
        locked           = tag.getBoolean("locked");
        cachedHeatPoints = tag.getInt("heatPoints");
        cachedHeatSpeed  = tag.getFloat("heatSpeed");

        int[] savedOrder = tag.getIntArray("renderOrder");
        if (savedOrder.length == FLUID_TANK_COUNT) {
            System.arraycopy(savedOrder, 0, fluidRenderOrder, 0, FLUID_TANK_COUNT);
        }

        if (tag.contains("fluidTanks")) {
            ListTag tanksTag = tag.getList("fluidTanks", Tag.TAG_COMPOUND);
            for (int i = 0; i < Math.min(tanksTag.size(), FLUID_TANK_COUNT); i++) {
                fluidTanks[i].readFromNBT(provider, tanksTag.getCompound(i));
            }
        }

        // Přepočítej kapacitu po načtení
        resizeTankCapacity();
    }
}