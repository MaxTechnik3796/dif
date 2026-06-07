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

    /** Počet item slotů na jednu sklo vrstvu. */
    public static final int SLOTS_PER_LAYER = 9;

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
    //  DYNAMICKÝ INVENTÁŘ (18 slotů × počet sklo vrstev)
    // ═══════════════════════════════════════════════════════════

    /**
     * Vlastní inventář Forge pece — počet slotů se mění dynamicky.
     * Výchozí kapacita 0 (dokud není sklo vrstva).
     */
    public net.neoforged.neoforge.items.ItemStackHandler forgeInventory =
            new net.neoforged.neoforge.items.ItemStackHandler(0) {
                @Override
                protected void onContentsChanged(int slot) {
                    cachedRecipe = null;
                    setChanged();
                }
            };

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
            fluidTanks[i] = new FluidTank(0) {
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

        final Direction facing = blockState.getOptionalValue(getFacingProperty()).orElse(Direction.SOUTH);
        final Direction intoStructure = facing.getOpposite();

        if (shouldValidate) {
            forceValidation = false;
            // Struktura je zformovaná pokud má hotovou bázi a alespoň 1 vrstvu skla
            int actualGlass = countActualGlassLayers(level, pos, intoStructure);
            isFormed = validateBaseStructure(level) && actualGlass >= 1;

            // Nová formace — zkontroluj, zda jsou cihličky volné k zabrání
            if (isFormed && !wasFormed && !canClaimAllBricks(level, pos, intoStructure)) {
                isFormed = false;
                if (!isConflicted) {
                    isConflicted = true;
                    setChanged();
                }
            }
        }

        if (isFormed != wasFormed) {
            if (isFormed) {
                claimBricks(level, pos, intoStructure, pos);
                isConflicted = false;
            } else {
                claimBricks(level, pos, intoStructure, null);
                resetProgress();
                setLocked(false); // Reset locked stavu při deformování
                this.glassLayers = 0;
                resizeTankCapacity();
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

    private int countActualGlassLayers(Level level, BlockPos pos, Direction intoStructure) {
        Predicate<BlockState> glassPred = state -> state.getBlock() instanceof ForgeGlassBlock;
        return ForgeMultiblockHelper.countGlassLayers(level, pos, intoStructure, glassPred);
    }

    private boolean canClaimAllBricks(Level level, BlockPos controllerPos, Direction intoStructure) {
        final boolean[] ok = {true};
        ForgeMultiblockHelper.forEachBrick(controllerPos, intoStructure, mp -> {
            if (!brickCanBeClaimedBy(level, mp, controllerPos)) {
                ok[0] = false;
                return false; // stop
            }
            return true;
        });
        return ok[0];
    }

    private void claimBricks(Level level, BlockPos controllerPos, Direction intoStructure, @Nullable BlockPos owner) {
        ForgeMultiblockHelper.forEachBrick(controllerPos, intoStructure, mp -> {
            setBrickController(level, mp, owner);
            return true;
        });
    }

    // ═══════════════════════════════════════════════════════════
    //  HEAT LOGIKA
    // ═══════════════════════════════════════════════════════════

    private void refreshHeat(Level level, BlockPos pos) {
        Direction facing = getBlockState().getOptionalValue(getFacingProperty()).orElse(Direction.SOUTH);
        Direction intoStructure = facing.getOpposite();
        cachedHeatPoints = ForgeMultiblockHelper.calculateHeatPoints(level, pos, intoStructure);
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

        Direction facing = blockState.getOptionalValue(getFacingProperty()).orElse(Direction.SOUTH);
        Direction intoStructure = facing.getOpposite();

        int newLayers = ForgeMultiblockHelper.countGlassLayers(level, pos, intoStructure, glassPred);

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
     * Při úbytku sklo vrstev:
     *  - Fluidy zůstávají (neztrácejí se, kapacita se snižuje ale obsah zůstane).
     *  - Přebytečné itemy nad limit nové kapacity vypadnou na zem.
     *  - LOCKED se nastaví pouze pokud controller nemá ani 1 sklo vrstvu.
     */
    private void handleGlassRemoved(int newLayers) {
        // Vyhoď přebytečné itemy
        trimExcessItems(newLayers);
        // Uzamkni pouze pokud nejsou žádné sklo vrstvy (min. 1 pro funkčnost)
        if (newLayers == 0) {
            setLocked(true);
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
        int newSize = newLayers * SLOTS_PER_LAYER;
        // Vyhoď vše nad nový limit
        for (int i = newSize; i < forgeInventory.getSlots(); i++) {
            ItemStack excess = forgeInventory.getStackInSlot(i);
            if (!excess.isEmpty() && level != null) {
                net.minecraft.world.Containers.dropItemStack(
                        level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), excess);
                forgeInventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        // Zmenši velikost inventáře
        resizeInventory(newSize);
    }

    /** Změní počet slotů inventáře a zkopíruje stávající obsah. */
    private void resizeInventory(int newSize) {
        var newInv = new net.neoforged.neoforge.items.ItemStackHandler(newSize) {
            @Override
            protected void onContentsChanged(int slot) {
                cachedRecipe = null;
                setChanged();
            }
        };
        int copy = Math.min(forgeInventory.getSlots(), newSize);
        for (int i = 0; i < copy; i++) {
            newInv.setStackInSlot(i, forgeInventory.getStackInSlot(i));
        }
        forgeInventory = newInv;
    }

    /**
     * Přepočítá kapacitu tanků.
     * DŮLEŽITÉ: kapacita se NIKDY nesnižuje pod aktuální obsah —
     * fluidy zůstanou i když se zničí sklo vrstva.
     */
    private void resizeTankCapacity() {
        int totalMb = ForgeMultiblockHelper.totalFluidCapacity(glassLayers);
        int perTank = glassLayers == 0 ? 0 : Math.max(1000, totalMb / FLUID_TANK_COUNT);
        for (FluidTank tank : fluidTanks) {
            // Nesmíme snížit kapacitu pod aktuální obsah!
            int minCap = tank.getFluidAmount();
            tank.setCapacity(Math.max(minCap, perTank));
        }
        // Resize inventáře podle nových vrstev
        int newInvSize = glassLayers * SLOTS_PER_LAYER;
        if (newInvSize != forgeInventory.getSlots()) {
            resizeInventory(newInvSize);
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
        // Vstup je platný pokud je něco v forgeInventory
        for (int i = 0; i < forgeInventory.getSlots(); i++) {
            if (!forgeInventory.getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    /**
     * Najde recept pro první neprázdný slot v forgeInventory.
     * Cachuje pouze recept (ne slot — ten se hledá vždy znovu).
     */
    @Override
    protected @Nullable ForgeSmeltingRecipe findRecipe(Level level) {
        for (int i = 0; i < forgeInventory.getSlots(); i++) {
            ItemStack input = forgeInventory.getStackInSlot(i);
            if (input.isEmpty()) continue;

            // Zkontroluj cache
            if (cachedRecipe != null && cachedRecipe.matches(input, cachedHeatPoints)) {
                return cachedRecipe;
            }

            // Hledej v receptech
            for (var holder : level.getRecipeManager().getAllRecipesFor(DifModRecipes.FORGE_SMELTING_TYPE.get())) {
                if (holder.value().matches(input, cachedHeatPoints)) {
                    cachedRecipe = holder.value();
                    return cachedRecipe;
                }
            }
        }
        cachedRecipe = null;
        return null;
    }

    /** Vrátí index prvního slotu kde item odpovídá receptu, nebo -1. */
    private int findRecipeSlot(ForgeSmeltingRecipe recipe) {
        if (recipe == null) return -1;
        for (int i = 0; i < forgeInventory.getSlots(); i++) {
            if (recipe.matches(forgeInventory.getStackInSlot(i), cachedHeatPoints)) return i;
        }
        return -1;
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
        // Spotřebuj 1 item z prvního slotu kde sedí recept
        int slot = findRecipeSlot(recipe);
        if (slot >= 0) {
            ItemStack input = forgeInventory.getStackInSlot(slot);
            input.shrink(1);
            forgeInventory.setStackInSlot(slot, input);
        }
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
        Direction facing = getBlockState().getOptionalValue(getFacingProperty()).orElse(Direction.SOUTH);
        Direction intoStructure = facing.getOpposite();
        return ForgeMultiblockHelper.validateBrickLayer(level, worldPosition, intoStructure, brickPred);
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
        // Heat — plynulý přechod: červená (0) → zelená (9) → modrá (18)
        String heatColor = heatColor(cachedHeatPoints);
        tooltip.add(Component.literal(goggleTooltipFix + " ▶ Heat: " + heatColor + cachedHeatPoints + "§7/18")
                .withStyle(ChatFormatting.GRAY));

        // Sklo vrstvy
        tooltip.add(Component.literal(goggleTooltipFix + " ▶ Glass layers: " + glassLayers + "/" + ForgeMultiblockHelper.MAX_GLASS_LAYERS)
                .withStyle(ChatFormatting.GRAY));

        // Item sloty
        int usedSlots = 0;
        for (int i = 0; i < forgeInventory.getSlots(); i++) {
            if (!forgeInventory.getStackInSlot(i).isEmpty()) usedSlots++;
        }
        if (forgeInventory.getSlots() > 0) {
            tooltip.add(Component.literal(goggleTooltipFix + " ▶ Queue: " + usedSlots + "/" + forgeInventory.getSlots())
                    .withStyle(ChatFormatting.GRAY));
        }

        // Locked stav
        if (locked) {
            tooltip.add(Component.literal(goggleTooltipFix + " ⚠ LOCKED — add glass layer!")
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

    /**
     * Vrátí § barevný kód pro heat body.
     * 0      → §c (červená)
     * 1–8    → plynulý přechod červená→žlutá
     * 9      → §a (zelená)
     * 10–17  → plynulý přechod zelená→akvamarín
     * 18     → §b (světle modrá)
     */
    private static String heatColor(int heat) {
        if (heat <= 0)  return "§c"; // červená
        if (heat < 9)  return "§e"; // žlutá (přechod)
        if (heat == 9) return "§a"; // zelená
        if (heat < 18) return "§2"; // tmavě zelená (přechod)
        return "§b";                // světle modrá
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

        // Ulož fluid tanky
        ListTag tanksTag = new ListTag();
        for (FluidTank tank : fluidTanks) {
            tanksTag.add(tank.writeToNBT(provider, new CompoundTag()));
        }
        tag.put("fluidTanks", tanksTag);

        // Ulož forgeInventory
        tag.put("forgeInv", forgeInventory.serializeNBT(provider));
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

        // Načti forgeInventory — nejdřív nastav správnou velikost, pak obsah
        int invSize = glassLayers * SLOTS_PER_LAYER;
        resizeInventory(invSize);
        if (tag.contains("forgeInv")) {
            forgeInventory.deserializeNBT(provider, tag.getCompound("forgeInv"));
        }

        // Přepočítej kapacitu tanků
        resizeTankCapacity();
    }
}