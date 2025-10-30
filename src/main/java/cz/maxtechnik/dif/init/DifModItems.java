package cz.maxtechnik.dif.init;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.BasicItem;
import cz.maxtechnik.dif.item.MusicDisc;
import cz.maxtechnik.dif.item.food.BasicFood;
import cz.maxtechnik.dif.item.food.Tresnovice;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DifModItems{
    public static final DeferredRegister<Item>REGISTRY=DeferredRegister.create(ForgeRegistries.ITEMS,DifMod.MODID);
    private static RegistryObject<Item>block(RegistryObject<Block>block){assert block.getId()!=null;return REGISTRY.register(block.getId().getPath(),()->new BlockItem(block.get(),new Item.Properties()));}
    private static RegistryObject<Item>doubleBlock(RegistryObject<Block>block){assert block.getId()!=null;return REGISTRY.register(block.getId().getPath(),()->new DoubleHighBlockItem(block.get(),new Item.Properties()));}

    public static final RegistryObject<Item>EXAMPLE_ITEM=REGISTRY.register("example_item",BasicItem::new);
    public static final RegistryObject<Item>CHERRY=REGISTRY.register("cherry",()->new BasicFood(1,0.1F));
    public static final RegistryObject<Item>TRESNOVICE=REGISTRY.register("tresnovice", Tresnovice::new);

    public static final RegistryObject<Item>EXAMPLE_BLOCK=block(DifModBlocks.EXAMPLE_BLOCK);



    //MusicDiscs:
    public static final RegistryObject<Item>CLAIRDELUNE=REGISTRY.register("clairdelune",()->new MusicDisc(5,6320,DifMod.MODID,"clairdelune"));
    public static final RegistryObject<Item>CREMEKA=REGISTRY.register("cremeka",()->new MusicDisc(15,3860,DifMod.MODID,"cremeka"));
    public static final RegistryObject<Item>FURT_TA_STEJNA_HRA=REGISTRY.register("furt_ta_stejna_hra",()->new MusicDisc(15,2100,DifMod.MODID,"furt_ta_stejna_hra"));
    public static final RegistryObject<Item>MATY_CREATE=REGISTRY.register("maty_create",()->new MusicDisc(10,1900,DifMod.MODID,"maty_create"));
    public static final RegistryObject<Item>MATY_PADA_STREAM=REGISTRY.register("maty_pada_stream",()->new MusicDisc(15,3966,DifMod.MODID,"maty_pada_stream"));
    public static final RegistryObject<Item>MAYONNAISE=REGISTRY.register("mayonnaise",()->new MusicDisc(15,160,DifMod.MODID,"mayonnaise"));
    public static final RegistryObject<Item>REDSTONE=REGISTRY.register("redstone",()->new MusicDisc(15,3960,DifMod.MODID,"redstone"));



}
