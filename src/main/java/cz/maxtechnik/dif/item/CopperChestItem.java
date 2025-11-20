package cz.maxtechnik.dif.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import cz.maxtechnik.dif.block.CopperChestBlock;

public class CopperChestItem extends BlockItem {
    public CopperChestItem(CopperChestBlock block) {
        super(block, new Item.Properties());
    }
}
