// ═══════════════════════════════════════════════════════════════════
// CokeOvenMenu.java
// ═══════════════════════════════════════════════════════════════════
package cz.maxtechnik.dif.gui.menu;

import cz.maxtechnik.dif.block.entity.CokeOvenBlockEntity;
import cz.maxtechnik.dif.init.gui.DifModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Menu pro Coke Oven.
 * Slot 0 = vstup (56,35), Slot 1 = výstup (116,35).
 * ContainerData: [0]=progress, [1]=totalTime, [2]=fluidAmount, [3]=fluidCap.
 */
public class CokeOvenMenu extends AbstractContainerMenu {

    private final CokeOvenBlockEntity be;
    private final Level level;
    private final ContainerData data;

    // Server-side
    public CokeOvenMenu(int id, Inventory inv, CokeOvenBlockEntity be) {
        super(DifModMenus.COKE_OVEN.get(), id);
        this.be    = be;
        this.level = inv.player.level();
        this.data  = new ContainerData() {
            public int get(int i) { return switch (i) {
                case 0 -> be.getProgress();
                case 1 -> be.getTotalTime();
                case 2 -> (int) be.fluidTank.getFluidAmount();
                case 3 -> be.fluidTank.getCapacity();
                default -> 0;
            }; }
            public void set(int i, int v) {}
            public int getCount() { return 4; }
        };
        addDataSlots(data);

        addSlot(new SlotItemHandler(be.inventory, CokeOvenBlockEntity.SLOT_INPUT, 56, 35));
        addSlot(new SlotItemHandler(be.inventory, CokeOvenBlockEntity.SLOT_OUTPUT, 116, 35) {
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
        });

        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 9; c++)
                addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, 84 + r * 18));
        for (int c = 0; c < 9; c++)
            addSlot(new Slot(inv, c, 8 + c * 18, 142));
    }

    // Client-side (packet)
    public CokeOvenMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, getEntity(inv, buf));
    }

    private static CokeOvenBlockEntity getEntity(Inventory inv, FriendlyByteBuf buf) {
        var be = inv.player.level().getBlockEntity(buf.readBlockPos());
        if (be instanceof CokeOvenBlockEntity c) return c;
        throw new IllegalStateException("Expected CokeOvenBlockEntity");
    }

    public int getProgressBarWidth() {
        int t = data.get(1); return t == 0 ? 0 : data.get(0) * 24 / t;
    }
    public int getFluidBarHeight() {
        int c = data.get(3); return c == 0 ? 0 : data.get(2) * 52 / c;
    }
    public int getFluidAmount()   { return data.get(2); }
    public int getFluidCapacity() { return data.get(3); }
    public CokeOvenBlockEntity getBlockEntity() { return be; }

    @Override public boolean stillValid(@NotNull Player p) {
        return stillValid(ContainerLevelAccess.create(level, be.getBlockPos()), p,
                be.getBlockState().getBlock());
    }

    @Override public @NotNull ItemStack quickMoveStack(@NotNull Player p, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), copy = stack.copy();
        if (index < 2) { if (!moveItemStackTo(stack, 2, slots.size(), true)) return ItemStack.EMPTY; }
        else           { if (!moveItemStackTo(stack, 0, 1, false))           return ItemStack.EMPTY; }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}