package cz.maxtechnik.dif.block.generator;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;

/**
 * Abstraktní BlockEntity pro všechny generátory na kapalinu.
 *
 * Čte veškerou konfiguraci z {@link GeneratorDefinition} uloženého na bloku.
 * Podtřídy potřebují pouze:
 *  1. Volat {@code super(type, pos, state)} v konstruktoru.
 *  2. Volitelně přepsat {@link #canRun()} pro extra podmínky.
 */
public abstract class AbstractFluidGeneratorBlockEntity extends GeneratingKineticBlockEntity {

    protected SmartFluidTankBehaviour tank;

    private GeneratorDefinition cachedDefinition;
    private float lastSpeed    = Float.NaN;
    private float lastCapacity = Float.NaN;

    protected AbstractFluidGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ── Přístup k definici ────────────────────────────────────────────────────

    protected GeneratorDefinition definition() {
        if (cachedDefinition == null) {
            if (level != null && level.getBlockState(worldPosition).getBlock()
                    instanceof AbstractFluidGeneratorBlock b) {
                cachedDefinition = b.getDefinition();
            }
        }
        return cachedDefinition;
    }

    // ── Behaviours ────────────────────────────────────────────────────────────

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        int capacity = cachedDefinition != null ? cachedDefinition.tankCapacity() : 8000;
        tank = SmartFluidTankBehaviour.single(this, capacity);
        behaviours.add(tank);
    }

    // ── Kinetika ─────────────────────────────────────────────────────────────

    @Override
    public float getGeneratedSpeed() {
        if (!canRun()) return 0f;
        GeneratorDefinition def = definition();
        if (def == null) return 0f;
        return def.generatedSpeed();
    }

    @Override
    public float calculateAddedStressCapacity() {
        GeneratorDefinition def = definition();
        if (def == null) return 0f;
        lastCapacityProvided = def.generatedCapacity();
        return lastCapacityProvided;
    }

    // ── Podmínka pro běh ─────────────────────────────────────────────────────

    /**
     * Určuje, zda generátor může aktuálně vyrábět výkon.
     * Přepiš v podtřídách pro extra podmínky (např. teplota, pára).
     */
    protected boolean canRun() {
        if (level == null) return false;
        if (getBlockState().hasProperty(AbstractFluidGeneratorBlock.POWERED)
                && getBlockState().getValue(AbstractFluidGeneratorBlock.POWERED)) {
            return false;
        }
        return !tank.getPrimaryHandler().getFluid().isEmpty();
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) return;

        GeneratorDefinition def = definition();
        if (def == null) return;

        float speed    = getGeneratedSpeed();
        float capacity = calculateAddedStressCapacity();

        if (speed != lastSpeed || capacity != lastCapacity) {
            reActivateSource = true;
            lastSpeed    = speed;
            lastCapacity = capacity;
        }

        if (!canRun() || isOverStressed()) {
            if (hasNetwork()) getOrCreateNetwork().remove(this);
            detachKinetics();
            removeSource();
            return;
        }

        // Spotřeba kapaliny
        tank.getPrimaryHandler().drain(def.fluidConsumptionPerTick(), IFluidHandler.FluidAction.EXECUTE);
    }

    // ── Goggle tooltip ────────────────────────────────────────────────────────

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (getGeneratedSpeed() != 0)
            super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability());
        return true;
    }

    // ── NBT ──────────────────────────────────────────────────────────────────

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        cachedDefinition = null;
    }

    // ── Přístup k nádrži ─────────────────────────────────────────────────────

    public FluidTank getFluidTank() {
        return tank.getPrimaryHandler();
    }

    // ── Helper pro registraci capability ─────────────────────────────────────

    /**
     * Zavolej z konkrétního BE:
     * <pre>{@code
     *   public static void registerCapabilities(RegisterCapabilitiesEvent event) {
     *       AbstractFluidGeneratorBlockEntity.registerCap(
     *           event,
     *           DifModBlockEntities.STEAM_GENERATOR.get(),
     *           SteamGeneratorDefinition.INSTANCE
     *       );
     *   }
     * }</pre>
     * Capability je dostupná na všech stranách MIMO osu hřídele.
     */
    public static <T extends AbstractFluidGeneratorBlockEntity> void registerCap(
            RegisterCapabilitiesEvent event,
            BlockEntityType<T> type,
            GeneratorDefinition def) {

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                type,
                (be, side) -> {
                    if (side != null && side.getAxis() == def.shaftAxis()) return null;
                    return be.tank.getCapability();
                }
        );
    }
}
