package cz.maxtechnik.dif.item;

import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class NuclearBombItem extends BlockItem {
    public NuclearBombItem() {
        super(DifModBlocks.NUCLEAR_BOMB.get(), new Item.Properties().stacksTo(16));
    }
}