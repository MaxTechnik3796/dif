package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipes.DistillationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DistillationTankBlockEntity extends BlockEntity {

    public static final int CAPACITY = 8000; // 8 buckets
    private static final int SEQUENCE_TICKS = 5;
    private static final int MAX_TOWER = 16;

    public final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide)
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            // Pokud se fluid změnil, invaliduj cache receptu
            needsCacheUpdate = true;
        }
    };

    // Controller stav – platí jen pokud isController()
    private int tickCounter = 0;
    private boolean needsCacheUpdate = true;
    @Nullable private DistillationRecipe cachedRecipe = null;
    @Nullable private List<BlockPos> cachedTower = null; // pozice tanků nad controllerem

    public DistillationTankBlockEntity(BlockPos pos, BlockState state) {
        super(DifModBlockEntities.DISTILLATION_TANK.get(), pos, state);
    }

    // -------------------------------------------------------------------------
    // Fluid handler
    // -------------------------------------------------------------------------

    public IFluidHandler getFluidHandler() { return tank; }

    // -------------------------------------------------------------------------
    // Blaze Burner detekce
    // -------------------------------------------------------------------------

    public enum HeatLevel { NONE, HEATED, SUPERHEATED }

    public static HeatLevel getHeatBelow(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        try {
            for (var prop : below.getProperties()) {
                if (!prop.getName().equals("heat_level")) continue;
                String val = below.getValue(prop).toString().toLowerCase();
                if (val.equals("seething")) return HeatLevel.SUPERHEATED;
                if (val.equals("kindled"))  return HeatLevel.HEATED;
                return HeatLevel.NONE;
            }
        } catch (Exception ignored) {}
        return HeatLevel.NONE;
    }

    // -------------------------------------------------------------------------
    // Server tick
    // -------------------------------------------------------------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                   DistillationTankBlockEntity be) {
        // Zkontroluj jestli je pod ním Blaze Burner
        HeatLevel heat = getHeatBelow(level, pos);
        if (heat == HeatLevel.NONE) return; // není controller

        be.tickCounter++;

        // Aktualizuj cache každých 40 ticků nebo při invalidaci
        if (be.needsCacheUpdate || be.tickCounter % 40 == 0) {
            be.updateCache(level, pos);
            be.needsCacheUpdate = false;
        }

        // Nemáme recept nebo tank je prázdný
        if (be.cachedRecipe == null || be.tank.isEmpty()) return;

        // Zpracuj každých 5 ticků
        if (be.tickCounter % SEQUENCE_TICKS != 0) return;

        // Žádný tank nad controllerem → nic neděláme
        if (be.cachedTower == null || be.cachedTower.isEmpty()) return;

        DistillationRecipe recipe = be.cachedRecipe;
        boolean superheated = heat == HeatLevel.SUPERHEATED;
        int mbIn = recipe.getMbPerSequence(superheated);

        // Pokud nemáme dost vstupu, vezmi co máme
        mbIn = Math.min(mbIn, be.tank.getFluidAmount());
        if (mbIn == 0) return;

        // Spočítej výstupy proporcionálně
        float ratio = (float) mbIn / recipe.getInput().getAmount();
        List<FluidStack> outputs = recipe.getOutputs();

        // Zkontroluj prostor ve výstupních tancích (jen pro dostupné tanky)
        // Pokud tank chybí → ten output se voiduje (přeskočíme)
        for (int i = 0; i < outputs.size(); i++) {
            if (i >= be.cachedTower.size()) break; // chybí tank = voiduje se
            BlockPos tankPos = be.cachedTower.get(i);
            if (!(level.getBlockEntity(tankPos) instanceof DistillationTankBlockEntity outputBE)) break;

            int outMb = Math.max(1, Math.round(outputs.get(i).getAmount() * ratio));
            FluidStack current = outputBE.tank.getFluid();
            if (!current.isEmpty() && !current.is(outputs.get(i).getFluid())) return; // nekompatibilní fluid
            if (outputBE.tank.getFluidAmount() + outMb > CAPACITY) return; // plný tank
        }

        // Proveď operaci
        be.tank.drain(mbIn, IFluidHandler.FluidAction.EXECUTE);

        for (int i = 0; i < outputs.size(); i++) {
            if (i >= be.cachedTower.size()) break; // chybí tank = voidujeme tento output
            BlockPos tankPos = be.cachedTower.get(i);
            if (!(level.getBlockEntity(tankPos) instanceof DistillationTankBlockEntity outputBE)) break;
            int outMb = Math.max(1, Math.round(outputs.get(i).getAmount() * ratio));
            outputBE.tank.fill(new FluidStack(outputs.get(i).getFluid(), outMb),
                    IFluidHandler.FluidAction.EXECUTE);
        }

        be.setChanged();
    }

    // -------------------------------------------------------------------------
    // Cache aktualizace
    // -------------------------------------------------------------------------

    private void updateCache(Level level, BlockPos pos) {
        // Najdi recept pro fluid v tanku
        if (tank.isEmpty()) {
            cachedRecipe = null;
            cachedTower = null;
            return;
        }

        Optional<RecipeHolder<DistillationRecipe>> opt = level.getRecipeManager()
                .getAllRecipesFor(DifModRecipes.DISTILLATION_TYPE.get())
                .stream()
                .filter(r -> r.value().getInput().getFluid() == tank.getFluid().getFluid())
                .findFirst();

        if (opt.isEmpty()) {
            cachedRecipe = null;
            cachedTower = null;
            return;
        }

        cachedRecipe = opt.get().value();

        // Skenuj věž – bloky hned nad controllerem bez mezery, max 16
        List<BlockPos> tower = new ArrayList<>();
        for (int y = 1; y <= MAX_TOWER; y++) {
            BlockPos above = pos.above(y);
            if (level.getBlockEntity(above) instanceof DistillationTankBlockEntity) {
                tower.add(above);
            } else {
                break; // mezera = konec věže
            }
        }
        cachedTower = tower;
    }

    public void invalidateCache() {
        needsCacheUpdate = true;
        cachedRecipe = null;
        cachedTower = null;
    }

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("tank", tank.writeToNBT(provider, new CompoundTag()));
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("tank")) tank.readFromNBT(provider, tag.getCompound("tank"));
        needsCacheUpdate = true;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider) {
        return saveWithFullMetadata(provider);
    }
}