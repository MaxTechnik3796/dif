package cz.maxtechnik.dif.init;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.BasicItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DifModItems {
    public static final DeferredRegister<Item>REGISTRY=DeferredRegister.create(ForgeRegistries.ITEMS,DifMod.MODID);
    private static RegistryObject<Item>block(RegistryObject<Block>block){assert block.getId()!=null;return REGISTRY.register(block.getId().getPath(),()->new BlockItem(block.get(),new Item.Properties()));}
    private static RegistryObject<Item>doubleBlock(RegistryObject<Block>block){assert block.getId()!=null;return REGISTRY.register(block.getId().getPath(),()->new DoubleHighBlockItem(block.get(),new Item.Properties()));}

    public static final RegistryObject<Item>EXAMPLE_ITEM=REGISTRY.register("example_item", BasicItem::new);
    public static final RegistryObject<Item>EXAMPLE_BLOCK=block(DifModBlocks.EXAMPLE_BLOCK);



}
