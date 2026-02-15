package cz.maxtechnik.dif.init.basic;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.fluid.bucket.*;
import cz.maxtechnik.dif.item.*;
import cz.maxtechnik.dif.item.food.*;
import cz.maxtechnik.dif.item.food.create.*;
import cz.maxtechnik.dif.item.modular.ModularPart;
import cz.maxtechnik.dif.item.modular.tool.*;
import cz.maxtechnik.dif.item.random.*;
import cz.maxtechnik.dif.item.tool.*;
import cz.maxtechnik.dif.item.armor.*;
import cz.maxtechnik.dif.init.other.DifModTiers;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
public class DifModItems{
    public static final DeferredRegister<Item>REGISTRY=DeferredRegister.create(ForgeRegistries.ITEMS,DifMod.MODID);
	public static final DeferredRegister<Item>V_REGISTRY=DeferredRegister.create(ForgeRegistries.ITEMS,"minecraft");
    private static RegistryObject<Item>block(RegistryObject<Block>block){assert block.getId()!=null;return REGISTRY.register(block.getId().getPath(),()->new BlockItem(block.get(),new Item.Properties()));}
    private static RegistryObject<Item>doubleBlock(RegistryObject<Block>block){assert block.getId()!=null;return REGISTRY.register(block.getId().getPath(),()->new DoubleHighBlockItem(block.get(),new Item.Properties()));}

	public static final RegistryObject<Item>MODULAR_PICKAXE=REGISTRY.register("modular_pickaxe",ModularPickaxe::new);
	public static final RegistryObject<Item>MODULAR_AXE=REGISTRY.register("modular_axe",ModularAxe::new);
	public static final RegistryObject<Item>MODULAR_SHOVEL=REGISTRY.register("modular_shovel",ModularShovel::new);
	public static final RegistryObject<Item>MODULAR_SWORD=REGISTRY.register("modular_sword",ModularSword::new);

	public static final RegistryObject<Item>MODULAR_PART_PICKAXE_HEAD=REGISTRY.register("pickaxe_head",ModularPart::new);
	public static final RegistryObject<Item>MODULAR_PART_AXE_HEAD=REGISTRY.register("axe_head",ModularPart::new);
	public static final RegistryObject<Item>MODULAR_PART_SHOVEL_HEAD=REGISTRY.register("shovel_head",ModularPart::new);
	public static final RegistryObject<Item>MODULAR_PART_SWORD_HEAD=REGISTRY.register("sword_head",ModularPart::new);

	public static final RegistryObject<Item>MODULAR_PART_BINDING=REGISTRY.register("binding",ModularPart::new);
	public static final RegistryObject<Item>MODULAR_PART_SWORD_BINDING=REGISTRY.register("sword_binding",ModularPart::new);

	public static final RegistryObject<Item>MODULAR_PART_HANDLE=REGISTRY.register("handle",ModularPart::new);


	public static final RegistryObject<Item>EXAMPLE_BLOCK=block(DifModBlocks.EXAMPLE_BLOCK);

	public static final RegistryObject<Item>BEER_BUCKET=REGISTRY.register("beer_bucket",BeerBucket::new);
	public static final RegistryObject<Item>XP_BUCKET=REGISTRY.register("xp_bucket",XpBucket::new);
	public static final RegistryObject<Item>FUEL_BUCKET=REGISTRY.register("fuel_bucket",FuelBucket::new);

	public static final RegistryObject<Item>END_PORTAL=V_REGISTRY.register("end_portal",()->new BlockItem(Blocks.END_PORTAL,new Item.Properties()));
	public static final RegistryObject<Item>END_GATEWAY=V_REGISTRY.register("end_gateway",()->new BlockItem(Blocks.END_GATEWAY,new Item.Properties()));
	public static final RegistryObject<Item>NETHER_PORTAL=V_REGISTRY.register("nether_portal",()->new BlockItem(Blocks.NETHER_PORTAL,new Item.Properties()));
	public static final RegistryObject<Item>WATER=V_REGISTRY.register("water",()->new BlockItem(Blocks.WATER,new Item.Properties()));
	public static final RegistryObject<Item>LAVA=V_REGISTRY.register("lava",()->new BlockItem(Blocks.LAVA,new Item.Properties().fireResistant()));
	public static final RegistryObject<Item>FIRE=V_REGISTRY.register("fire",()->new BlockItem(Blocks.FIRE,new Item.Properties().fireResistant()));

	public static final RegistryObject<Item>BEER=REGISTRY.register("beer",()->new Beer(DifModBlocks.BEER.get(),new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationMod(1F).alwaysEat().build())));
    public static final RegistryObject<Item>EXAMPLE_ITEM=REGISTRY.register("example_item",Test::new);
    public static final RegistryObject<Item>QUESTION_MARK=REGISTRY.register("question_mark",Basic::new);

	public static final RegistryObject<Item>CHERRY_BOTTLE=REGISTRY.register("cherry_bottle",CherryBottle::new);
	public static final RegistryObject<Item>NETHER_WART_BOTTLE=REGISTRY.register("nether_wart_bottle",NetherWartBottle::new);
	public static final RegistryObject<Item>WINE=REGISTRY.register("wine",Wine::new);
    public static final RegistryObject<Item>FERNET=REGISTRY.register("fernet",Fernet::new);
	public static final RegistryObject<Item>SUGAR_MUSHROOM=REGISTRY.register("sugar_mushroom",()->new Custom(3,0.5F));
    public static final RegistryObject<Item>CHERRY=REGISTRY.register("cherry",()->new Custom(1,0.1F));
    public static final RegistryObject<Item>ROTTEN_BELT=REGISTRY.register("rotten_belt",Bad::new);
    public static final RegistryObject<Item>ROTTEN_APPLE=REGISTRY.register("rotten_apple",Bad::new);
	public static final RegistryObject<Item>BOTTLE_OF_MOLOTOVUV_KOKTEJL=REGISTRY.register("bottle_of_molotovuv_koktejl",MolotovuvKoktejl::new);
	public static final RegistryObject<Item>BOTTLE_OF_URANOVEJ_KOKTEJL=REGISTRY.register("bottle_of_uranovej_koktejl",UranovejKoktejl::new);
	public static final RegistryObject<Item>BUCKET_OF_CHICKEN=REGISTRY.register("bucket_of_chicken",BucketOfChicken::new);
	public static final RegistryObject<Item>FRIES=REGISTRY.register("fries",()->new Custom(3,0.45F));
	public static final RegistryObject<Item>HORSE_MEAT=REGISTRY.register("horse_meat",()->new CustomMeat(2,0.1F));
	public static final RegistryObject<Item>COOKED_HORSE_MEAT=REGISTRY.register("cooked_horse_meat",()->new CustomMeat(6,0.8F));
	public static final RegistryObject<Item>BURNED_TOAST=REGISTRY.register("burned_toast",()->new Custom(3,0.1F));


	public static final RegistryObject<Item>CREATE_CAN=REGISTRY.register("create_can",Can::new);
	public static final RegistryObject<Item>CREATE_BOWL=REGISTRY.register("create_bowl",Bowl::new);
	public static final RegistryObject<Item>SUPER_HEATED_CREATE_BOWL=REGISTRY.register("super_heated_create_bowl",Super::new);

	public static final RegistryObject<Item>JADERNEJ_SUTR=REGISTRY.register("jadernej_sutr",JadernejSutr::new);
	public static final RegistryObject<Item>ITEM_5261=REGISTRY.register("item_5261",Basic::new);
	public static final RegistryObject<Item>VHS=REGISTRY.register("vhs",()->new StackSize(16));
	public static final RegistryObject<Item>SPRING=REGISTRY.register("spring",Basic::new);
	public static final RegistryObject<Item>SOLDERING_IRON=REGISTRY.register("soldering_iron",()->new StackSize(1));
	public static final RegistryObject<Item>DRILL=REGISTRY.register("drill",()->new StackSize(1));
	public static final RegistryObject<Item>SCREWDRIVER=REGISTRY.register("screwdriver",()->new StackSize(1));
	public static final RegistryObject<Item>INCOMPLETE_UNIVERSAL=REGISTRY.register("incomplete_universal",Basic::new);

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
	public static final RegistryObject<Item>ELECTRUM_DESTROYER=REGISTRY.register("electrum_destroyer",ElectrumDestroyer::new);
	public static final RegistryObject<Item>MITHRIL=REGISTRY.register("mithril",Basic::new);
	public static final RegistryObject<Item>MITHRIL_PLATE=REGISTRY.register("mithril_plate",Basic::new);
	public static final RegistryObject<Item>INCOMPLETE_MITHRIL_PLATE=REGISTRY.register("incomplete_mithril_plate",Basic::new);


    public static final RegistryObject<Item>BLUESTONE=REGISTRY.register("bluestone",Basic::new);
    public static final RegistryObject<Item>CPU_SINGULARITY=REGISTRY.register("cpu_singularity",Basic::new);
    public static final RegistryObject<Item>INCOMPLETE_CPU_SINGULARITY=REGISTRY.register("incomplete_cpu_singularity",Basic::new);
	public static final RegistryObject<Item>MASTICKA=REGISTRY.register("masticka",Basic::new);
	public static final RegistryObject<Item>BLUE_PLATE=REGISTRY.register("blue_plate",Basic::new);
	public static final RegistryObject<Item>HEAVY_PLATE=REGISTRY.register("heavy_plate",Basic::new);

    public static final RegistryObject<Item>XP_STORAGE=block(DifModBlocks.XP_STORAGE);
    public static final RegistryObject<Item>THE_DIFFERENTIAL=block(DifModBlocks.THE_DIFFERENTIAL);
    public static final RegistryObject<Item>EVENT_BUS=block(DifModBlocks.EVENT_BUS);
	public static final RegistryObject<Item>VENT=block(DifModBlocks.VENT);
	public static final RegistryObject<Item>WASHING_MACHINE=block(DifModBlocks.WASHING_MACHINE);
	public static final RegistryObject<Item>AIR_CONDITIONING=block(DifModBlocks.AIR_CONDITIONING);
    public static final RegistryObject<Item>BURNING_GENERATOR=block(DifModBlocks.BURNING_GENERATOR);
	public static final RegistryObject<Item>FLUID_HATCH=block(DifModBlocks.FLUID_HATCH);
    public static final RegistryObject<Item>HOSPITAL_HANDLE=block(DifModBlocks.HOSPITAL_HANDLE);
	public static final RegistryObject<Item>SINGULARITATOR=block(DifModBlocks.SINGULARITATOR);
	public static final RegistryObject<Item>SUPER_BOX=block(DifModBlocks.SUPER_BOX);
	public static final RegistryObject<Item>OLD_CHEST=block(DifModBlocks.OLD_CHEST);

	public static final RegistryObject<Item>ANDESITE_BARREL=block(DifModBlocks.ANDESITE_BARREL);
	public static final RegistryObject<Item>COPPER_BARREL=block(DifModBlocks.COPPER_BARREL);
	public static final RegistryObject<Item>BRASS_BARREL=block(DifModBlocks.BRASS_BARREL);

	public static final RegistryObject<Item>RUBY=REGISTRY.register("ruby",Basic::new);
	public static final RegistryObject<Item>RUBY_ORE=block(DifModBlocks.RUBY_ORE);
	public static final RegistryObject<Item>RUBY_BLOCK=block(DifModBlocks.RUBY_BLOCK);

	public static final RegistryObject<Item>RAM=REGISTRY.register("ram",()->new StackSize(16));
	public static final RegistryObject<Item>EXPLOSIVE_RAM=REGISTRY.register("explosive_ram",ExplosiveRam::new);

	public static final RegistryObject<Item>SOLAR_PANEL_INC=REGISTRY.register("solar_panel_inc",Basic::new);
	public static final RegistryObject<Item>SOLAR_PANEL_00=block(DifModBlocks.SOLAR_PANEL_00);
	public static final RegistryObject<Item>SOLAR_PANEL_01=block(DifModBlocks.SOLAR_PANEL_01);
	public static final RegistryObject<Item>SOLAR_PANEL_02=block(DifModBlocks.SOLAR_PANEL_02);
	public static final RegistryObject<Item>SOLAR_PANEL_03=block(DifModBlocks.SOLAR_PANEL_03);
	public static final RegistryObject<Item>SOLAR_PANEL_04=block(DifModBlocks.SOLAR_PANEL_04);
	public static final RegistryObject<Item>SOLAR_PANEL_00_W=block(DifModBlocks.SOLAR_PANEL_00_W);
	public static final RegistryObject<Item>SOLAR_PANEL_01_W=block(DifModBlocks.SOLAR_PANEL_01_W);
	public static final RegistryObject<Item>SOLAR_PANEL_02_W=block(DifModBlocks.SOLAR_PANEL_02_W);
	public static final RegistryObject<Item>SOLAR_PANEL_03_W=block(DifModBlocks.SOLAR_PANEL_03_W);
	public static final RegistryObject<Item>SOLAR_PANEL_04_W=block(DifModBlocks.SOLAR_PANEL_04_W);

	public static final RegistryObject<Item>DEEPSLATED_ARROW=block(DifModBlocks.DEEPSLATED_ARROW);
    public static final RegistryObject<Item>STONED_ARROW=block(DifModBlocks.STONED_ARROW);
    public static final RegistryObject<Item>WOODED_ARROW=block(DifModBlocks.WOODED_ARROW);

	public static final RegistryObject<Item>BAUXITE_ORE=block(DifModBlocks.BAUXITE_ORE);
	public static final RegistryObject<Item>DEEPSLATE_BAUXITE_ORE=block(DifModBlocks.DEEPSLATE_BAUXITE_ORE);
	public static final RegistryObject<Item>RAW_BAUXITE=REGISTRY.register("raw_bauxite",Basic::new);
	public static final RegistryObject<Item>CRUSHED_RAW_BAUXITE=REGISTRY.register("crushed_raw_bauxite",Basic::new);
	public static final RegistryObject<Item>ALUMINUM_INGOT=REGISTRY.register("aluminum_ingot",Basic::new);
	public static final RegistryObject<Item>ALUMINUM_NUGGET=REGISTRY.register("aluminum_nugget",Basic::new);
	public static final RegistryObject<Item>ALUMINUM_BLOCK=block(DifModBlocks.ALUMINUM_BLOCK);
	public static final RegistryObject<Item>ALUMINUM_PROFILE=block(DifModBlocks.ALUMINUM_PROFILE);

	public static final RegistryObject<Item>BITCOIN_BLOCK=block(DifModBlocks.BITCOIN_BLOCK);
	public static final RegistryObject<Item>SOLANA_BLOCK=block(DifModBlocks.SOLANA_BLOCK);
	public static final RegistryObject<Item>CINDER_FLOUR_BLOCK=block(DifModBlocks.CINDER_FLOUR_BLOCK);
	public static final RegistryObject<Item>PEDROCK=block(DifModBlocks.PEDROCK);
	public static final RegistryObject<Item>ANDESITE_LATTICE=block(DifModBlocks.ANDESITE_LATTICE);
	public static final RegistryObject<Item>ANDESITE_WINDOW=block(DifModBlocks.ANDESITE_WINDOW);
	public static final RegistryObject<Item>SMOOTH_STONE_DOUBLE_SLAB=block(DifModBlocks.SMOOTH_STONE_DOUBLE_SLAB);
	public static final RegistryObject<Item>IRON_BARS_BLOCK=block(DifModBlocks.IRON_BARS_BLOCK);
	public static final RegistryObject<Item>GLITCH_BLOCK=block(DifModBlocks.GLITCH_BLOCK);
	public static final RegistryObject<Item>TREE_BARK_BLOCK=block(DifModBlocks.TREE_BARK_BLOCK);

	public static final RegistryObject<Item>ENERGY_BLOCK=block(DifModBlocks.ENERGY_BLOCK);
	public static final RegistryObject<Item>BUDDING_ENERGY=block(DifModBlocks.BUDDING_ENERGY);
	public static final RegistryObject<Item>ENERGY_CLUSTER=block(DifModBlocks.ENERGY_CLUSTER);
	public static final RegistryObject<Item>LARGE_ENERGY_BUD=block(DifModBlocks.LARGE_ENERGY_BUD);
	public static final RegistryObject<Item>MEDIUM_ENERGY_BUD=block(DifModBlocks.MEDIUM_ENERGY_BUD);
	public static final RegistryObject<Item>SMALL_ENERGY_BUD=block(DifModBlocks.SMALL_ENERGY_BUD);
	public static final RegistryObject<Item>ENERGY_SHARD=REGISTRY.register("energy_shard",Basic::new);

	//Compressed:
	public static final RegistryObject<Item>C1_COBBLESTONE=block(DifModBlocks.C1_COBBLESTONE);
	public static final RegistryObject<Item>C2_COBBLESTONE=block(DifModBlocks.C2_COBBLESTONE);
	public static final RegistryObject<Item>C3_COBBLESTONE=block(DifModBlocks.C3_COBBLESTONE);
	public static final RegistryObject<Item>C4_COBBLESTONE=block(DifModBlocks.C4_COBBLESTONE);
	public static final RegistryObject<Item>C5_COBBLESTONE=block(DifModBlocks.C5_COBBLESTONE);
	public static final RegistryObject<Item>C6_COBBLESTONE=block(DifModBlocks.C6_COBBLESTONE);
	public static final RegistryObject<Item>C7_COBBLESTONE=block(DifModBlocks.C7_COBBLESTONE);
	public static final RegistryObject<Item>C8_COBBLESTONE=block(DifModBlocks.C8_COBBLESTONE);
	public static final RegistryObject<Item>C9_COBBLESTONE=block(DifModBlocks.C9_COBBLESTONE);

    //MusicDiscs:
    public static final RegistryObject<Item>CREMEKA=REGISTRY.register("cremeka",()->new MusicDiscDesc2(15,3860,DifMod.MODID,"cremeka"));
    public static final RegistryObject<Item>FURT_TA_STEJNA_HRA=REGISTRY.register("furt_ta_stejna_hra",()->new MusicDiscDesc2(15,2100,DifMod.MODID,"furt_ta_stejna_hra"));
    public static final RegistryObject<Item>MATY_CREATE=REGISTRY.register("maty_create",()->new MusicDiscDesc2(10,1900,DifMod.MODID,"maty_create"));
    public static final RegistryObject<Item>MATY_PADA_STREAM=REGISTRY.register("maty_pada_stream",()->new MusicDiscDesc2(15,3966,DifMod.MODID,"maty_pada_stream"));
    public static final RegistryObject<Item>MAYONNAISE=REGISTRY.register("mayonnaise",()->new MusicDiscDesc2(15,180,DifMod.MODID,"mayonnaise"));
    public static final RegistryObject<Item>REDSTONE=REGISTRY.register("redstone",()->new MusicDiscDesc2(15,3960,DifMod.MODID,"redstone"));

    //Weapons:
	public static final RegistryObject<Item>WOODEN_BATTLE_AXE=REGISTRY.register("wooden_battle_axe",()->new BattleAxeItem(Tiers.WOOD,6,-3.4F,new Item.Properties()));
	public static final RegistryObject<Item>GOLDEN_BATTLE_AXE=REGISTRY.register("golden_battle_axe",()->new BattleAxeItem(Tiers.GOLD,8,-3.4F,new Item.Properties()));
	public static final RegistryObject<Item>STONE_BATTLE_AXE=REGISTRY.register("stone_battle_axe",()->new BattleAxeItem(Tiers.STONE,7,-3.4F,new Item.Properties()));
	public static final RegistryObject<Item>IRON_BATTLE_AXE=REGISTRY.register("iron_battle_axe",()->new BattleAxeItem(Tiers.IRON,7,-3.3F,new Item.Properties()));
	public static final RegistryObject<Item>DIAMOND_BATTLE_AXE=REGISTRY.register("diamond_battle_axe",()->new BattleAxeItem(Tiers.DIAMOND,7,-3.3F,new Item.Properties()));
	public static final RegistryObject<Item>NETHERITE_BATTLE_AXE=REGISTRY.register("netherite_battle_axe",()->new BattleAxeItem(Tiers.NETHERITE,7,-3.3F,new Item.Properties()));
	public static final RegistryObject<Item>COPPER_BATTLE_AXE=REGISTRY.register("copper_battle_axe",()->new BattleAxeItem(DifModTiers.COPPER,6,-3.3F,new Item.Properties()));


	public static final RegistryObject<Item>WOODEN_KATANA=REGISTRY.register("wooden_katana",()->new SwordItem(Tiers.WOOD,1,-1F,new Item.Properties()));
	public static final RegistryObject<Item>GOLDEN_KATANA=REGISTRY.register("golden_katana",()->new SwordItem(Tiers.GOLD,2,-1F,new Item.Properties()));
	public static final RegistryObject<Item>STONE_KATANA=REGISTRY.register("stone_katana",()->new SwordItem(Tiers.STONE,1,-1F,new Item.Properties()));
	public static final RegistryObject<Item>IRON_KATANA=REGISTRY.register("iron_katana",()->new SwordItem(Tiers.IRON,1,-1F,new Item.Properties()));
	public static final RegistryObject<Item>DIAMOND_KATANA=REGISTRY.register("diamond_katana",()->new SwordItem(Tiers.DIAMOND,1,-1F,new Item.Properties()));
	public static final RegistryObject<Item>NETHERITE_KATANA=REGISTRY.register("netherite_katana",()->new SwordItem(Tiers.NETHERITE,1,-1F,new Item.Properties()));
	public static final RegistryObject<Item>COPPER_KATANA=REGISTRY.register("copper_katana",()->new SwordItem(DifModTiers.COPPER,0,-1F,new Item.Properties()));


	//Copper Tools:
	public static final RegistryObject<Item>COPPER_SHOVEL=REGISTRY.register("copper_shovel",()->new ShovelItem(DifModTiers.COPPER,0.5F,-3.0F,new Item.Properties()));
	public static final RegistryObject<Item>COPPER_PICKAXE=REGISTRY.register("copper_pickaxe",()->new PickaxeItem(DifModTiers.COPPER,0,-2.8F,new Item.Properties()));
	public static final RegistryObject<Item>COPPER_SWORD=REGISTRY.register("copper_sword",()->new SwordItem(DifModTiers.COPPER,2,-2.4F,new Item.Properties()));
	public static final RegistryObject<Item>COPPER_AXE=REGISTRY.register("copper_axe",()->new AxeItem(DifModTiers.COPPER,6,-3.0F,new Item.Properties()));
	public static final RegistryObject<Item>COPPER_HOE=REGISTRY.register("copper_hoe",()->new HoeItem(DifModTiers.COPPER,-2,0.0F,new Item.Properties()));

	public static final RegistryObject<Item>COPPER_HELMET=REGISTRY.register("copper_helmet",CopperArmor.Helmet::new);
	public static final RegistryObject<Item>COPPER_CHESTPLATE=REGISTRY.register("copper_chestplate",CopperArmor.Chestplate::new);
	public static final RegistryObject<Item>COPPER_LEGGINGS=REGISTRY.register("copper_leggings",CopperArmor.Leggings::new);
	public static final RegistryObject<Item>COPPER_BOOTS=REGISTRY.register("copper_boots",CopperArmor.Boots::new);

	//Space:
	public static final RegistryObject<Item>AURORA_CASING=block(DifModBlocks.AURORA_CASING);
	public static final RegistryObject<Item>AURORA_INGOT=REGISTRY.register("aurora_ingot",Basic::new);

	public static final RegistryObject<Item>ROCKET_FUEL=REGISTRY.register("rocket_fuel",()->new StackSize(16));
	public static final RegistryObject<Item>EMPTY_ROCKET_FUEL=REGISTRY.register("empty_rocket_fuel",()->new StackSize(16));

	public static final RegistryObject<Item>SPACE_SUIT_HELMET=REGISTRY.register("space_suit_helmet",SpaceSuit.Helmet::new);
	public static final RegistryObject<Item>SPACE_SUIT_CHESTPLATE=REGISTRY.register("space_suit_chestplate",SpaceSuit.Chestplate::new);
	public static final RegistryObject<Item>SPACE_SUIT_LEGGINGS=REGISTRY.register("space_suit_leggings",SpaceSuit.Leggings::new);
	public static final RegistryObject<Item>SPACE_SUIT_BOOTS=REGISTRY.register("space_suit_boots",SpaceSuit.Boots::new);

	public static final RegistryObject<Item>CARBON_SUIT_HELMET=REGISTRY.register("carbon_suit_helmet",CarbonSuit.Helmet::new);
	public static final RegistryObject<Item>CARBON_SUIT_CHESTPLATE=REGISTRY.register("carbon_suit_chestplate",CarbonSuit.Chestplate::new);
	public static final RegistryObject<Item>CARBON_SUIT_LEGGINGS=REGISTRY.register("carbon_suit_leggings",CarbonSuit.Leggings::new);
	public static final RegistryObject<Item>CARBON_SUIT_BOOTS=REGISTRY.register("carbon_suit_boots",CarbonSuit.Boots::new);



	public static final RegistryObject<Item>SPACESHIP=block(DifModBlocks.SPACESHIP);
	public static final RegistryObject<Item>SPACE_ENGINE=block(DifModBlocks.SPACE_ENGINE);

	public static final RegistryObject<Item>SPACE_SCAFFOLDING=block(DifModBlocks.SPACE_SCAFFOLDING);
	public static final RegistryObject<Item>SPACE_CASING=block(DifModBlocks.SPACE_CASING);
	public static final RegistryObject<Item>SPACE_CASING_REINFORCED=block(DifModBlocks.SPACE_CASING_REINFORCED);
	public static final RegistryObject<Item>SPACE_CASING_METAL=block(DifModBlocks.SPACE_CASING_METAL);
	public static final RegistryObject<Item>SPACE_DOOR=doubleBlock(DifModBlocks.SPACE_DOOR);
	public static final RegistryObject<Item>SPACE_CORRIDOR=block(DifModBlocks.SPACE_CORRIDOR);
	public static final RegistryObject<Item>SPACE_CRATE=block(DifModBlocks.SPACE_CRATE);
	public static final RegistryObject<Item>SOLAR_PANEL_BLOCK=block(DifModBlocks.SOLAR_PANEL_BLOCK);



}
