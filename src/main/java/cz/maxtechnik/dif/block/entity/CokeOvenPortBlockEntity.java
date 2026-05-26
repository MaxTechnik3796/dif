package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * BlockEntity pro Port blok.
 *
 * Drží pozici controlleru a poskytuje capability které delegují
 * na CokeOvenBlockEntity controlleru.
 *
 * TYP PORTU (PortType) určuje co port dělá:
 *   ITEM_INPUT  → vkládá do slot 0 controlleru
 *   ITEM_OUTPUT → vybírá ze slot 1 controlleru
 *   FLUID_OUTPUT → vybírá z fluid tanku controlleru
 */
public class CokeOvenPortBlockEntity extends BlockEntity {

    public enum PortType { ITEM_INPUT, ITEM_OUTPUT, FLUID_OUTPUT }

    @Nullable private BlockPos controllerPos = null;
    private PortType portType = PortType.ITEM_INPUT;

    public CokeOvenPortBlockEntity(BlockPos pos, BlockState state) {
        super(DifModBlockEntities.COKE_OVEN_PORT.get(), pos, state);
    }

    public void setup(BlockPos controllerPos, PortType type) {
        this.controllerPos = controllerPos;
        this.portType      = type;
        setChanged();
    }

    /** Vrátí controller BE pokud existuje a je platný. */
    @Nullable
    private CokeOvenBlockEntity getController() {
        if (level == null || controllerPos == null) return null;
        BlockEntity be = level.getBlockEntity(controllerPos);
        return be instanceof CokeOvenBlockEntity cobe && cobe.isFormed() ? cobe : null;
    }

    // ── Item capability delegát ───────────────────────────────────────────────

    /**
     * IItemHandler který deleguje na inventory controlleru.
     * Registruj toto v DifMod pro COKE_OVEN_PORT BlockEntityType.
     */
    public final IItemHandler itemHandler = new IItemHandler() {
        @Override public int getSlots() { return 1; }

        @Override
        public @NotNull net.minecraft.world.item.ItemStack getStackInSlot(int slot) {
            CokeOvenBlockEntity ctrl = getController();
            if (ctrl == null) return net.minecraft.world.item.ItemStack.EMPTY;
            int ctrlSlot = portType == PortType.ITEM_INPUT ? CokeOvenBlockEntity.SLOT_INPUT : CokeOvenBlockEntity.SLOT_OUTPUT;
            return ctrl.inventory.getStackInSlot(ctrlSlot);
        }

        @Override
        public @NotNull net.minecraft.world.item.ItemStack insertItem(int slot, @NotNull net.minecraft.world.item.ItemStack stack, boolean simulate) {
            if (portType != PortType.ITEM_INPUT) return stack;
            CokeOvenBlockEntity ctrl = getController();
            if (ctrl == null) return stack;
            return ctrl.inventory.insertItem(CokeOvenBlockEntity.SLOT_INPUT, stack, simulate);
        }

        @Override
        public @NotNull net.minecraft.world.item.ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (portType != PortType.ITEM_OUTPUT) return net.minecraft.world.item.ItemStack.EMPTY;
            CokeOvenBlockEntity ctrl = getController();
            if (ctrl == null) return net.minecraft.world.item.ItemStack.EMPTY;
            return ctrl.inventory.extractItem(CokeOvenBlockEntity.SLOT_OUTPUT, amount, simulate);
        }

        @Override public int getSlotLimit(int slot) { return 64; }

        @Override
        public boolean isItemValid(int slot, @NotNull net.minecraft.world.item.ItemStack stack) {
            return portType == PortType.ITEM_INPUT;
        }
    };

    // ── Fluid capability delegát ──────────────────────────────────────────────

    /**
     * IFluidHandler který deleguje na fluid tank controlleru.
     * Registruj toto v DifMod pro COKE_OVEN_PORT BlockEntityType.
     */
    public final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return 1; }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            CokeOvenBlockEntity ctrl = getController();
            return ctrl != null ? ctrl.fluidTank.getFluid() : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            CokeOvenBlockEntity ctrl = getController();
            return ctrl != null ? ctrl.fluidTank.getCapacity() : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return false; } // output only

        @Override
        public int fill(FluidStack resource, FluidAction action) { return 0; } // output only

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (portType != PortType.FLUID_OUTPUT) return FluidStack.EMPTY;
            CokeOvenBlockEntity ctrl = getController();
            if (ctrl == null) return FluidStack.EMPTY;
            return ctrl.fluidTank.drain(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (portType != PortType.FLUID_OUTPUT) return FluidStack.EMPTY;
            CokeOvenBlockEntity ctrl = getController();
            if (ctrl == null) return FluidStack.EMPTY;
            return ctrl.fluidTank.drain(maxDrain, action);
        }
    };

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (controllerPos != null) tag.put("controllerPos", NbtUtils.writeBlockPos(controllerPos));
        tag.putString("portType", portType.name());
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("controllerPos")) controllerPos = NbtUtils.readBlockPos(tag, "controllerPos").orElse(null);
        try { portType = PortType.valueOf(tag.getString("portType")); }
        catch (Exception e) { portType = PortType.ITEM_INPUT; }
    }
}