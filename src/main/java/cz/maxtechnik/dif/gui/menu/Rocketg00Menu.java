package cz.maxtechnik.dif.gui.menu;

import cz.maxtechnik.dif.init.gui.DifModMenus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
public class Rocketg00Menu extends AbstractContainerMenu {
    public final int x, y, z;
    public final Player entity;
    public final Level world;

    // Konstruktor pro klienta (při otevírání přes packet)
    public Rocketg00Menu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, extraData.readBlockPos());
    }

    // Hlavní konstruktor
    public Rocketg00Menu(int id, Inventory inv, BlockPos pos) {
        super(DifModMenus.ROCKETG_00.get(), id);
        this.entity = inv.player;
        this.world = inv.player.level();
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();

        // Přidání slotů inventáře hráče (hotbar)
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(inv, i, 39 + i * 18, 194));
        }
        // Přidání slotů inventáře hráče (batoh)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(inv, j + (i + 1) * 9, 39 + j * 18, 136 + i * 18));
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // Zde můžeš přidat kontrolu vzdálenosti od rakety
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 36) {
                if (!this.moveItemStackTo(itemstack1, 36, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(itemstack1, 0, 36, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }
}