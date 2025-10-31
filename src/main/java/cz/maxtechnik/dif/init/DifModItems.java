package cz.maxtechnik.dif.init;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.*;
import cz.maxtechnik.dif.item.food.*;
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

    public static final RegistryObject<Item>EXAMPLE_ITEM=REGISTRY.register("example_item",Basic::new);
    public static final RegistryObject<Item>QUESTION_MARK=REGISTRY.register("question_mark",Basic::new);

    public static final RegistryObject<Item>CHERRY=REGISTRY.register("cherry",()->new Custom(1,0.1F));
    public static final RegistryObject<Item>TRESNOVICE=REGISTRY.register("tresnovice",Tresnovice::new);
    public static final RegistryObject<Item>ROTTEN_BELT=REGISTRY.register("rotten_belt",Bad::new);
    public static final RegistryObject<Item>ROTTEN_APPLE=REGISTRY.register("rotten_apple",Bad::new);
	public static final RegistryObject<Item>BOTTLE_OF_MOLOTOVUV_KOKTEJL=REGISTRY.register("bottle_of_molotovuv_koktejl",MolotovuvKoktejl::new);
	public static final RegistryObject<Item>BOTTLE_OF_URANOVEJ_KOKTEJL=REGISTRY.register("bottle_of_uranovej_koktejl",UranovejKoktejl::new);

	public static final RegistryObject<Item>MATY_DRINK=REGISTRY.register("maty_drink",MatyDrink::new);
	public static final RegistryObject<Item>MATA=REGISTRY.register("mata",()->new Custom(1,0.2F));
	public static final RegistryObject<Item>MATA_PLANT=block(DifModBlocks.MATA_PLANT);
	public static final RegistryObject<Item>MATY_BLOCK=block(DifModBlocks.MATY_BLOCK);

    public static final RegistryObject<Item>BLUESTONE=REGISTRY.register("bluestone",Basic::new);
    public static final RegistryObject<Item>CPU_SINGULARITY=REGISTRY.register("cpu_singularity",Basic::new);
    public static final RegistryObject<Item>INCOMPLETE_CPU_SINGULARITY=REGISTRY.register("incomplete_cpu_singularity",Basic::new);
	public static final RegistryObject<Item>MASTICKA=REGISTRY.register("masticka",Basic::new);
	public static final RegistryObject<Item>BLUE_PLATE=REGISTRY.register("blue_plate",Basic::new);
	public static final RegistryObject<Item>HEAVY_PLATE=REGISTRY.register("heavy_plate",Basic::new);

    public static final RegistryObject<Item>EXAMPLE_BLOCK=block(DifModBlocks.EXAMPLE_BLOCK);
    public static final RegistryObject<Item>THE_DIFFERENTIAL=block(DifModBlocks.THE_DIFFERENTIAL);
    public static final RegistryObject<Item>EVENT_BUS=block(DifModBlocks.EVENT_BUS);
    public static final RegistryObject<Item>GENERATOR=block(DifModBlocks.GENERATOR);
    public static final RegistryObject<Item>HOSPITAL_HANDLE=block(DifModBlocks.HOSPITAL_HANDLE);
	public static final RegistryObject<Item>SINGULARITATOR=block(DifModBlocks.SINGULARITATOR);

	public static final RegistryObject<Item>DEEPSLATED_ARROW=block(DifModBlocks.DEEPSLATED_ARROW);
    public static final RegistryObject<Item>STONED_ARROW=block(DifModBlocks.STONED_ARROW);
    public static final RegistryObject<Item>WOODED_ARROW=block(DifModBlocks.WOODED_ARROW);

	public static final RegistryObject<Item>PEDROCK=block(DifModBlocks.PEDROCK);
	public static final RegistryObject<Item>ANDESITE_LATTICE=block(DifModBlocks.ANDESITE_LATTICE);
	public static final RegistryObject<Item>ANDESITE_WINDOW=block(DifModBlocks.ANDESITE_WINDOW);

    //MusicDiscs:
    public static final RegistryObject<Item>CLAIRDELUNE=REGISTRY.register("clairdelune",()->new MusicDiscDesc2(5,6320,DifMod.MODID,"clairdelune"));
    public static final RegistryObject<Item>CREMEKA=REGISTRY.register("cremeka",()->new MusicDiscDesc2(15,3860,DifMod.MODID,"cremeka"));
    public static final RegistryObject<Item>FURT_TA_STEJNA_HRA=REGISTRY.register("furt_ta_stejna_hra",()->new MusicDiscDesc2(15,2100,DifMod.MODID,"furt_ta_stejna_hra"));
    public static final RegistryObject<Item>MATY_CREATE=REGISTRY.register("maty_create",()->new MusicDiscDesc2(10,1900,DifMod.MODID,"maty_create"));
    public static final RegistryObject<Item>MATY_PADA_STREAM=REGISTRY.register("maty_pada_stream",()->new MusicDiscDesc2(15,3966,DifMod.MODID,"maty_pada_stream"));
    public static final RegistryObject<Item>MAYONNAISE=REGISTRY.register("mayonnaise",()->new MusicDiscDesc2(15,180,DifMod.MODID,"mayonnaise"));
    public static final RegistryObject<Item>REDSTONE=REGISTRY.register("redstone",()->new MusicDiscDesc2(15,3960,DifMod.MODID,"redstone"));



}
