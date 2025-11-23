package cz.maxtechnik.dif.init.basic;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.*;
import cz.maxtechnik.dif.item.food.*;
import cz.maxtechnik.dif.item.food.create.*;
import cz.maxtechnik.dif.item.tool.*;
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

    public static final RegistryObject<Item>EXAMPLE_ITEM=REGISTRY.register("example_item",Test::new);
    public static final RegistryObject<Item>QUESTION_MARK=REGISTRY.register("question_mark",Basic::new);

    public static final RegistryObject<Item>FERNET=REGISTRY.register("fernet",Fernet::new);
    public static final RegistryObject<Item>CHERRY=REGISTRY.register("cherry",()->new Custom(1,0.1F));
    public static final RegistryObject<Item>TRESNOVICE=REGISTRY.register("tresnovice",Tresnovice::new);
    public static final RegistryObject<Item>ROTTEN_BELT=REGISTRY.register("rotten_belt",Bad::new);
    public static final RegistryObject<Item>ROTTEN_APPLE=REGISTRY.register("rotten_apple",Bad::new);
	public static final RegistryObject<Item>BOTTLE_OF_MOLOTOVUV_KOKTEJL=REGISTRY.register("bottle_of_molotovuv_koktejl",MolotovuvKoktejl::new);
	public static final RegistryObject<Item>BOTTLE_OF_URANOVEJ_KOKTEJL=REGISTRY.register("bottle_of_uranovej_koktejl",UranovejKoktejl::new);

	public static final RegistryObject<Item>CREATE_CAN=REGISTRY.register("create_can",Can::new);
	public static final RegistryObject<Item>CREATE_BOWL=REGISTRY.register("create_bowl",Bowl::new);
	public static final RegistryObject<Item>SUPER_HEATED_CREATE_BOWL=REGISTRY.register("super_heated_create_bowl",Super::new);

	public static final RegistryObject<Item>JADERNEJ_SUTR=REGISTRY.register("jadernej_sutr",JadernejSutr::new);
	public static final RegistryObject<Item>ITEM_5261=REGISTRY.register("item_5261",Basic::new);
	public static final RegistryObject<Item>VHS=REGISTRY.register("vhs",()->new StackSize(16));

	public static final RegistryObject<Item>COIN_00=REGISTRY.register("coin_00",Basic::new);
	public static final RegistryObject<Item>COIN_01=REGISTRY.register("coin_01",Basic::new);
	public static final RegistryObject<Item>COIN_02=REGISTRY.register("coin_02",Basic::new);
	public static final RegistryObject<Item>COIN_03=REGISTRY.register("coin_03",Basic::new);

	public static final RegistryObject<Item>MATY_DRINK=REGISTRY.register("maty_drink",MatyDrink::new);
	public static final RegistryObject<Item>MATA=REGISTRY.register("mata",()->new Custom(1,0.2F));
	public static final RegistryObject<Item>MATA_PLANT=block(DifModBlocks.MATA_PLANT);
	public static final RegistryObject<Item>MATY_BLOCK=block(DifModBlocks.MATY_BLOCK);

	public static final RegistryObject<Item>CANOLA_SEEDS=REGISTRY.register("canola_seeds",Canola::new);
	public static final RegistryObject<Item>CANOLA_PLANT=block(DifModBlocks.CANOLA_PLANT);

	public static final RegistryObject<Item>PORTAL_GUN=REGISTRY.register("portal_gun",PortalGun::new);
	public static final RegistryObject<Item>LASER_HOOKAH=REGISTRY.register("laser_hookah",LaserHookah::new);
	public static final RegistryObject<Item>BAN_HAMMER=REGISTRY.register("ban_hammer",BanHammer::new);
	public static final RegistryObject<Item>MITHRIL=REGISTRY.register("mithril",Basic::new);
	public static final RegistryObject<Item>MITHRIL_PLATE=REGISTRY.register("mithril_plate",Basic::new);
	public static final RegistryObject<Item>INCOMPLETE_MITHRIL_PLATE=REGISTRY.register("incomplete_mithril_plate",Basic::new);


    public static final RegistryObject<Item>BLUESTONE=REGISTRY.register("bluestone",Basic::new);
    public static final RegistryObject<Item>CPU_SINGULARITY=REGISTRY.register("cpu_singularity",Basic::new);
    public static final RegistryObject<Item>INCOMPLETE_CPU_SINGULARITY=REGISTRY.register("incomplete_cpu_singularity",Basic::new);
	public static final RegistryObject<Item>MASTICKA=REGISTRY.register("masticka",Basic::new);
	public static final RegistryObject<Item>BLUE_PLATE=REGISTRY.register("blue_plate",Basic::new);
	public static final RegistryObject<Item>HEAVY_PLATE=REGISTRY.register("heavy_plate",Basic::new);

    public static final RegistryObject<Item>EXAMPLE_BLOCK=block(DifModBlocks.EXAMPLE_BLOCK);
    public static final RegistryObject<Item>THE_DIFFERENTIAL=block(DifModBlocks.THE_DIFFERENTIAL);
    public static final RegistryObject<Item>EVENT_BUS=block(DifModBlocks.EVENT_BUS);
    public static final RegistryObject<Item>BURNING_GENERATOR=block(DifModBlocks.BURNING_GENERATOR);
    public static final RegistryObject<Item>HOSPITAL_HANDLE=block(DifModBlocks.HOSPITAL_HANDLE);
	public static final RegistryObject<Item>SINGULARITATOR=block(DifModBlocks.SINGULARITATOR);
	public static final RegistryObject<Item>SUPER_BOX=block(DifModBlocks.SUPER_BOX);
	public static final RegistryObject<Item>BRASS_BARREL=block(DifModBlocks.BRASS_BARREL);
	public static final RegistryObject<Item>ANDESITE_BARREL=block(DifModBlocks.ANDESITE_BARREL);
	public static final RegistryObject<Item>STURDY_BARREL=block(DifModBlocks.STURDY_BARREL);

	public static final RegistryObject<Item>RUBY=REGISTRY.register("ruby",Basic::new);
	public static final RegistryObject<Item>RUBY_ORE=block(DifModBlocks.RUBY_ORE);
	public static final RegistryObject<Item>RUBY_BLOCK=block(DifModBlocks.RUBY_BLOCK);

	public static final RegistryObject<Item>SOLAR_PANEL_00=block(DifModBlocks.SOLAR_PANEL_00);
	public static final RegistryObject<Item>SOLAR_PANEL_01=block(DifModBlocks.SOLAR_PANEL_01);
	public static final RegistryObject<Item>SOLAR_PANEL_02=block(DifModBlocks.SOLAR_PANEL_02);
	public static final RegistryObject<Item>SOLAR_PANEL_03=block(DifModBlocks.SOLAR_PANEL_03);
	public static final RegistryObject<Item>SOLAR_PANEL_04=block(DifModBlocks.SOLAR_PANEL_04);

	public static final RegistryObject<Item>DEEPSLATED_ARROW=block(DifModBlocks.DEEPSLATED_ARROW);
    public static final RegistryObject<Item>STONED_ARROW=block(DifModBlocks.STONED_ARROW);
    public static final RegistryObject<Item>WOODED_ARROW=block(DifModBlocks.WOODED_ARROW);

	public static final RegistryObject<Item>BAUXITE_ORE=block(DifModBlocks.BAUXITE_ORE);
	public static final RegistryObject<Item>DEEPSLATE_BAUXITE_ORE=block(DifModBlocks.DEEPSLATE_BAUXITE_ORE);
	public static final RegistryObject<Item>RAW_BAUXITE=REGISTRY.register("raw_bauxite",Basic::new);
	public static final RegistryObject<Item>CRUSHED_RAW_BAUXITE=REGISTRY.register("crushed_raw_bauxite",Basic::new);
	public static final RegistryObject<Item>ALUMINUM_INGOT=REGISTRY.register("aluminum_ingot",Basic::new);
	public static final RegistryObject<Item>ALUMINUM_PROFILE=block(DifModBlocks.ALUMINUM_PROFILE);

	public static final RegistryObject<Item>BITCOIN_BLOCK=block(DifModBlocks.BITCOIN_BLOCK);
	public static final RegistryObject<Item>SOLANA_BLOCK=block(DifModBlocks.SOLANA_BLOCK);
	public static final RegistryObject<Item>CINDER_FLOUR_BLOCK=block(DifModBlocks.CINDER_FLOUR_BLOCK);
	public static final RegistryObject<Item>PEDROCK=block(DifModBlocks.PEDROCK);
	public static final RegistryObject<Item>ANDESITE_LATTICE=block(DifModBlocks.ANDESITE_LATTICE);
	public static final RegistryObject<Item>ANDESITE_WINDOW=block(DifModBlocks.ANDESITE_WINDOW);

	public static final RegistryObject<Item>ENERGY_BLOCK=block(DifModBlocks.ENERGY_BLOCK);
	public static final RegistryObject<Item>BUDDING_ENERGY=block(DifModBlocks.BUDDING_ENERGY);
	public static final RegistryObject<Item>ENERGY_CLUSTER=block(DifModBlocks.ENERGY_CLUSTER);
	public static final RegistryObject<Item>LARGE_ENERGY_BUD=block(DifModBlocks.LARGE_ENERGY_BUD);
	public static final RegistryObject<Item>MEDIUM_ENERGY_BUD=block(DifModBlocks.MEDIUM_ENERGY_BUD);
	public static final RegistryObject<Item>SMALL_ENERGY_BUD=block(DifModBlocks.SMALL_ENERGY_BUD);
	public static final RegistryObject<Item>ENERGY_SHARD=REGISTRY.register("energy_shard",Basic::new);

    //MusicDiscs:
    public static final RegistryObject<Item>CLAIRDELUNE=REGISTRY.register("clairdelune",()->new MusicDiscDesc2(5,6320,DifMod.MODID,"clairdelune"));
    public static final RegistryObject<Item>CREMEKA=REGISTRY.register("cremeka",()->new MusicDiscDesc2(15,3860,DifMod.MODID,"cremeka"));
    public static final RegistryObject<Item>FURT_TA_STEJNA_HRA=REGISTRY.register("furt_ta_stejna_hra",()->new MusicDiscDesc2(15,2100,DifMod.MODID,"furt_ta_stejna_hra"));
    public static final RegistryObject<Item>MATY_CREATE=REGISTRY.register("maty_create",()->new MusicDiscDesc2(10,1900,DifMod.MODID,"maty_create"));
    public static final RegistryObject<Item>MATY_PADA_STREAM=REGISTRY.register("maty_pada_stream",()->new MusicDiscDesc2(15,3966,DifMod.MODID,"maty_pada_stream"));
    public static final RegistryObject<Item>MAYONNAISE=REGISTRY.register("mayonnaise",()->new MusicDiscDesc2(15,180,DifMod.MODID,"mayonnaise"));
    public static final RegistryObject<Item>REDSTONE=REGISTRY.register("redstone",()->new MusicDiscDesc2(15,3960,DifMod.MODID,"redstone"));



}
