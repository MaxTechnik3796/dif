package cz.maxtechnik.dif.init.basic;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import cz.maxtechnik.dif.init.other.DifModTiers;
import cz.maxtechnik.dif.item.*;
import cz.maxtechnik.dif.item.food.*;
import cz.maxtechnik.dif.item.modular.ModularPart;
import cz.maxtechnik.dif.item.quarry.DrillHeadItem;
import cz.maxtechnik.dif.item.tool.GodTotemItem;
import cz.maxtechnik.dif.item.modular.tool.*;
import cz.maxtechnik.dif.item.tool.*;
import cz.maxtechnik.dif.item.armor.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import cz.maxtechnik.dif.init.events.QuarryStats;
import cz.maxtechnik.dif.item.quarry.EngineItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
public class DifModItems{
	public static final DeferredRegister.Items REGISTRY=DeferredRegister.createItems(DifMod.MODID);
	public static final DeferredRegister.Items V_REGISTRY= DeferredRegister.createItems("minecraft");
	private static DeferredItem<Item> block(DeferredBlock<Block> block){
		assert block.getId()!=null;
		return REGISTRY.register(block.getId().getPath(),()->new BlockItem(block.get(),new Item.Properties()));
	}
	private static DeferredItem<Item> doubleBlock(DeferredBlock<Block> block){
		assert block.getId()!=null;
		return REGISTRY.register(block.getId().getPath(),()->new DoubleHighBlockItem(block.get(),new Item.Properties()));
	}
	public static final DeferredItem<Item> SLEEPING_BAG = block(DifModBlocks.SLEEPING_BAG);
	public static final DeferredItem<Item> LAP_TIMER = REGISTRY.register("lap_timer", () -> new BlockItem(DifModBlocks.LAP_TIMER.get(), new Item.Properties()));

	public static final DeferredItem<Item> QUARRY=block(DifModBlocks.QUARRY);
	public static final DeferredItem<Item>QUARRY_FRAME=block(DifModBlocks.QUARRY_FRAME);
	public static final DeferredItem<Item>QUARRY_LANDMARK=block(DifModBlocks.QUARRY_LANDMARK);
	

	// Quarry Upgrades & Components:
	public static final DeferredItem<Item> QUARRY_DRILL_IRON=REGISTRY.register("quarry_drill_iron",()->new DrillHeadItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<Item> QUARRY_DRILL_DIAMOND=REGISTRY.register("quarry_drill_diamond",()->new DrillHeadItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<Item> QUARRY_ENGINE_IRON=REGISTRY.register("quarry_engine_iron",()->new EngineItem(new Item.Properties().stacksTo(1),QuarryStats.IRON_ENGINE_QP_GEN,QuarryStats.IRON_ENGINE_FE_COST));
	public static final DeferredItem<Item> QUARRY_ENGINE_GOLD=REGISTRY.register("quarry_engine_gold",()->new EngineItem(new Item.Properties().stacksTo(1),QuarryStats.GOLD_ENGINE_QP_GEN,QuarryStats.GOLD_ENGINE_FE_COST));
	public static final DeferredItem<Item> QUARRY_ENGINE_DIAMOND=REGISTRY.register("quarry_engine_diamond",()->new EngineItem(new Item.Properties().stacksTo(1),QuarryStats.DIAMOND_ENGINE_QP_GEN,QuarryStats.DIAMOND_ENGINE_FE_COST));

	public static final DeferredItem<Item> DISTILLATION_CONTROLLER = block(DifModBlocks.DISTILLATION_CONTROLLER);
	public static final DeferredItem<Item> DISTILLATION_TANK = block(DifModBlocks.DISTILLATION_TANK);

	public static final DeferredItem<Item>REMOTE_MINECART=REGISTRY.register("remote_minecart",()->new RemoteMinecartItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<Item>REMOTE_CONTROLLER=REGISTRY.register("remote_controller",()->new StackSize(1));
	public static final DeferredItem<Item>REMOTE_MINECART_BLOCK=block(DifModBlocks.REMOTE_MINECART_BLOCK);

	public static final DeferredItem<Item>CAMERA_MONITOR=block(DifModBlocks.CAMERA_MONITOR);
	public static final DeferredItem<Item>CAMERA=block(DifModBlocks.CAMERA);
	public static final DeferredItem<Item>CAMERA_LINK=REGISTRY.register("camera_link",()->new CameraLink(new Item.Properties().stacksTo(1)));

	public static final DeferredItem<Item> MEGA_TORCH = block(DifModBlocks.MEGA_TORCH);

	public static final DeferredItem<Item>PHANTOM_RING=REGISTRY.register("phantom_ring",PhantomRing::new);

	public static final DeferredItem<Item> GOD_TOTEM = REGISTRY.register("god_totem", () -> new GodTotemItem(new Item.Properties()));
	public static final DeferredItem<Item> BAN_HAMMER = REGISTRY.register("ban_hammer", BanHammer::new);

	public static final DeferredItem<Item>FAST_POWERED_RAIL=block(DifModBlocks.FAST_POWERED_RAIL);
	public static final DeferredItem<Item>FAST_RAIL=block(DifModBlocks.FAST_RAIL);

	public static final DeferredItem<Item> CHUNK_LOADER_1X1 = REGISTRY.register("chunk_loader_1x1",()->new BlockItem(DifModBlocks.CHUNK_LOADER_1X1.get(),new Item.Properties()){@Override public boolean isFoil(@NotNull ItemStack stack){return true;}});
	public static final DeferredItem<Item> CHUNK_LOADER_3X3 = REGISTRY.register("chunk_loader_3x3",()->new BlockItem(DifModBlocks.CHUNK_LOADER_3X3.get(),new Item.Properties()){@Override public boolean isFoil(@NotNull ItemStack stack){return true;}});

	//Modular Tools:
	public static final DeferredItem<Item> MODULAR_PICKAXE=REGISTRY.register("modular_pickaxe",ModularPickaxe::new);
	public static final DeferredItem<Item> MODULAR_AXE=REGISTRY.register("modular_axe",ModularAxe::new);
	public static final DeferredItem<Item> MODULAR_SHOVEL=REGISTRY.register("modular_shovel",ModularShovel::new);
	public static final DeferredItem<Item> MODULAR_SWORD=REGISTRY.register("modular_sword",ModularSword::new);

	//Modular Parts:
	public static final DeferredItem<Item> MODULAR_PART_PICKAXE_HEAD = REGISTRY.registerItem("pickaxe_head", ModularPart::new);
	public static final DeferredItem<Item> MODULAR_PART_AXE_HEAD = REGISTRY.registerItem("axe_head", ModularPart::new);
	public static final DeferredItem<Item> MODULAR_PART_SHOVEL_HEAD = REGISTRY.registerItem("shovel_head", ModularPart::new);
	public static final DeferredItem<Item> MODULAR_PART_SWORD_HEAD = REGISTRY.registerItem("sword_head", ModularPart::new);
	public static final DeferredItem<Item> MODULAR_PART_BINDING = REGISTRY.registerItem("binding", ModularPart::new);
	public static final DeferredItem<Item> MODULAR_PART_SWORD_BINDING = REGISTRY.registerItem("sword_binding", ModularPart::new);
	public static final DeferredItem<Item> MODULAR_PART_HANDLE = REGISTRY.registerItem("handle", ModularPart::new);

	public static final DeferredItem<Item> PORTAL_GUN=REGISTRY.register("portal_gun",PortalGun::new);

	public static final DeferredItem<Item> ELECTRO_RUNNERS=REGISTRY.register("electro_runners",ElectroRunners.Boots::new);

	//Jetpack
	public static final DeferredItem<Item> JETPACK=REGISTRY.register("jetpack",Jetpack.Chestplate::new);
	public static final DeferredItem<Item> JETPACK_FUEL=REGISTRY.register("jetpack_fuel",Basic::new);
	public static final DeferredItem<Item> JETPACK_TURBO_FUEL=REGISTRY.register("jetpack_turbo_fuel",Basic::new);
	public static final DeferredItem<Item> JETPACK_CANISTER=REGISTRY.register("jetpack_canister",Basic::new);

	//Fluid:
	public static final DeferredItem<Item> BEER_BUCKET=REGISTRY.register("beer_bucket",()->new BucketItem(DifModFluids.BEER.get(),new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
	public static final DeferredItem<Item> XP_BUCKET=REGISTRY.register("xp_bucket",()->new BucketItem(DifModFluids.XP.get(),new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
	public static final DeferredItem<Item> FUEL_BUCKET=REGISTRY.register("fuel_bucket",()->new BucketItem(DifModFluids.FUEL.get(),new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
	public static final DeferredItem<Item>CIDER_BUCKET=REGISTRY.register("cider_bucket",()->new BucketItem(DifModFluids.CIDER.get(),new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
	public static final DeferredItem<Item>CRUDE_OIL_BUCKET=REGISTRY.register("crude_oil_bucket",()->new BucketItem(DifModFluids.CRUDE_OIL.get(),new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
	public static final DeferredItem<Item>JETPACK_FUEL_BUCKET=REGISTRY.register("jetpack_fuel_bucket",()->new BucketItem(DifModFluids.JETPACK_FUEL.get(),new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
	public static final DeferredItem<Item>JETPACK_TURBO_FUEL_BUCKET=REGISTRY.register("jetpack_turbo_fuel_bucket",()->new BucketItem(DifModFluids.JETPACK_TURBO_FUEL.get(),new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
	public static final DeferredItem<Item>SUNFLOWER_OIL_BUCKET=REGISTRY.register("sunflower_oil_bucket",()->new BucketItem(DifModFluids.SUNFLOWER_OIL.get(),new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

	//Vanilla + :
	public static final DeferredItem<Item> END_PORTAL=V_REGISTRY.register("end_portal",()->new BlockItem(Blocks.END_PORTAL,new Item.Properties()));


	public static final DeferredItem<Item> END_GATEWAY=V_REGISTRY.register("end_gateway",()->new BlockItem(Blocks.END_GATEWAY,new Item.Properties()));
	public static final DeferredItem<Item> NETHER_PORTAL=V_REGISTRY.register("nether_portal",()->new BlockItem(Blocks.NETHER_PORTAL,new Item.Properties()));
	public static final DeferredItem<Item> WATER=V_REGISTRY.register("water",()->new BlockItem(Blocks.WATER,new Item.Properties()));
	public static final DeferredItem<Item> LAVA=V_REGISTRY.register("lava",()->new BlockItem(Blocks.LAVA,new Item.Properties().fireResistant()));
	public static final DeferredItem<Item> FIRE=V_REGISTRY.register("fire",()->new BlockItem(Blocks.FIRE,new Item.Properties().fireResistant()));

	//Food:
	public static final DeferredItem<Item> BEER=REGISTRY.register("beer",()->new Beer(DifModBlocks.BEER.get(),new Item.Properties().food(DifModFoods.BEER)));

	public static final DeferredItem<Item> CHERRY_BOTTLE=REGISTRY.register("cherry_bottle",()->new RetvalFoods(new Item.Properties().rarity(Rarity.UNCOMMON).food(DifModFoods.CHERRY_BOTTLE),Items.GLASS_BOTTLE,UseAnim.DRINK));
	public static final DeferredItem<Item> NETHER_WART_BOTTLE=REGISTRY.register("nether_wart_bottle",()->new RetvalFoods(new Item.Properties().rarity(Rarity.UNCOMMON).food(DifModFoods.NETHER_WART_BOTTLE),Items.GLASS_BOTTLE,UseAnim.DRINK));
	public static final DeferredItem<Item> WINE=REGISTRY.register("wine",()->new RetvalFoods(new Item.Properties().food(DifModFoods.WINE).rarity(Rarity.UNCOMMON),Items.GLASS_BOTTLE,UseAnim.DRINK));
	public static final DeferredItem<Item> FERNET=REGISTRY.register("fernet",()->new RetvalFoods(new Item.Properties().food(DifModFoods.FERNET).rarity(Rarity.UNCOMMON).food(DifModFoods.FERNET),Items.GLASS_BOTTLE,UseAnim.DRINK));
	public static final DeferredItem<Item> SUGAR_MUSHROOM=REGISTRY.register("sugar_mushroom",()->new Item(new Item.Properties().food(DifModFoods.SUGAR_MUSHROOM)));
	public static final DeferredItem<Item> CHERRY=REGISTRY.register("cherry",()->new Item(new Item.Properties().food(DifModFoods.CHERRY)));

	public static final DeferredItem<Item>FLAT_DOUGH=REGISTRY.register("flat_dough",()->new Item((new Item.Properties()).food(DifModFoods.FLAT_DOUGH)));
	public static final DeferredItem<Item>BAGUETTE=REGISTRY.register("baguette",()->new Item(new Item.Properties().food(Foods.BREAD)));

	public static final DeferredItem<Item> BOTTLE_OF_MOLOTOVUV_KOKTEJL=REGISTRY.register("bottle_of_molotovuv_koktejl",MolotovuvKoktejl::new);
	public static final DeferredItem<Item> BOTTLE_OF_URANOVEJ_KOKTEJL=REGISTRY.register("bottle_of_uranovej_koktejl",UranovejKoktejl::new);

	public static final DeferredItem<Item> BUCKET_OF_CHICKEN=REGISTRY.register("bucket_of_chicken",()->new RetvalFoods(new Item.Properties().food(DifModFoods.BUCKET_OF_CHICKEN),Items.BUCKET,UseAnim.EAT));
	public static final DeferredItem<Item> FRIES=REGISTRY.register("fries",()->new Item(new Item.Properties().food(DifModFoods.FRIES)));
	public static final DeferredItem<Item> RIZEK=REGISTRY.register("rizek",()->new Item(new Item.Properties().food(DifModFoods.RIZEK)));

	public static final DeferredItem<Item> HORSE_MEAT=REGISTRY.register("horse_meat",()->new Item(new Item.Properties().food(DifModFoods.HORSE_MEAT)));
	public static final DeferredItem<Item> COOKED_HORSE_MEAT=REGISTRY.register("cooked_horse_meat",()->new Item(new Item.Properties().food(DifModFoods.COOKED_HORSE_MEAT)));

	public static final DeferredItem<Item> CIDER_BOTTLE=REGISTRY.register("cider_bottle",()->new RetvalFoods(new Item.Properties().food(DifModFoods.CIDER_BOTTLE),Items.GLASS_BOTTLE,UseAnim.DRINK));
	public static final DeferredItem<Item> BURNED_TOAST=REGISTRY.register("burned_toast",()->new Item(new Item.Properties().food(DifModFoods.BURNED_TOAST)));

	public static final DeferredItem<Item> CREATE_CAN=REGISTRY.register("create_can",()->new RetvalFoods(new Item.Properties().food(DifModFoods.CRETE_CAN),Objects.requireNonNull(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:andesite_alloy"))),UseAnim.EAT));
	public static final DeferredItem<Item> CREATE_BOWL=REGISTRY.register("create_bowl",()->new RetvalFoods(new Item.Properties().food(DifModFoods.CREATE_BOWL),Items.BOWL,UseAnim.EAT));
	public static final DeferredItem<Item> SUPER_HEATED_CREATE_BOWL=REGISTRY.register("super_heated_create_bowl",()->new RetvalFoods(new  Item.Properties().food(DifModFoods.CREATE_SUPER),Items.BOWL,UseAnim.EAT));

	public static final DeferredItem<Item> MATY_DRINK=REGISTRY.register("maty_drink",()->new RetvalFoods(new Item.Properties().rarity(Rarity.UNCOMMON).food(DifModFoods.MATY_DRINK),Items.GLASS_BOTTLE,UseAnim.DRINK));
	public static final DeferredItem<Item> MATA=REGISTRY.register("mata",()->new Item(new Item.Properties().food(DifModFoods.MATA)));
	public static final DeferredItem<Item> MATA_PLANT=block(DifModBlocks.MATA_PLANT);
	public static final DeferredItem<Item> MATY_BLOCK=block(DifModBlocks.MATY_BLOCK);

	public static final DeferredItem<Item> CANOLA_SEEDS=REGISTRY.register("canola_seeds",Canola::new);
	public static final DeferredItem<Item> CANOLA_PLANT=block(DifModBlocks.CANOLA_PLANT);

	//Tech & Stuff:
	public static final DeferredItem<Item> QUESTION_MARK=REGISTRY.register("question_mark",Basic::new);
	public static final DeferredItem<Item> INCOMPLETE_UNIVERSAL=REGISTRY.register("incomplete_universal",Basic::new);
	public static final DeferredItem<Item> ELECTRUM_DESTROYER=REGISTRY.register("electrum_destroyer",ElectrumDestroyer::new);
	public static final DeferredItem<Item> BLUESTONE=REGISTRY.register("bluestone",Basic::new);
	public static final DeferredItem<Item> BLUE_PLATE=REGISTRY.register("blue_plate",Basic::new);
	public static final DeferredItem<Item> HEAVY_PLATE=REGISTRY.register("heavy_plate",Basic::new);

	public static final DeferredItem<Item> MITHRIL=REGISTRY.register("mithril",Basic::new);
	public static final DeferredItem<Item> MITHRIL_PLATE=REGISTRY.register("mithril_plate",Basic::new);
	public static final DeferredItem<Item> INCOMPLETE_MITHRIL_PLATE=REGISTRY.register("incomplete_mithril_plate",Basic::new);

	public static final DeferredItem<Item> CPU_SINGULARITY=REGISTRY.register("cpu_singularity",Basic::new);
	public static final DeferredItem<Item> INCOMPLETE_CPU_SINGULARITY=REGISTRY.register("incomplete_cpu_singularity",Basic::new);


	//Main:
	public static final DeferredItem<Item> THE_DIFFERENTIAL=block(DifModBlocks.THE_DIFFERENTIAL);
	public static final DeferredItem<Item> EVENT_BUS=block(DifModBlocks.EVENT_BUS);
	public static final DeferredItem<Item> VENT=block(DifModBlocks.VENT);

	public static final DeferredItem<Item> SINGULARITATOR=block(DifModBlocks.SINGULARITATOR);


	public static final DeferredItem<Item> CINDER_FLOUR_BLOCK=block(DifModBlocks.CINDER_FLOUR_BLOCK);
	public static final DeferredItem<Item> PEDROCK=block(DifModBlocks.PEDROCK);
	public static final DeferredItem<Item> GLITCH_BLOCK=block(DifModBlocks.GLITCH_BLOCK);

	public static final DeferredItem<Item> BROKEN_TRACK00=block(DifModBlocks.BROKEN_TRACK00);
	public static final DeferredItem<Item> BROKEN_TRACK01=block(DifModBlocks.BROKEN_TRACK01);
	public static final DeferredItem<Item> BROKEN_TRACK02=block(DifModBlocks.BROKEN_TRACK02);

	public static final DeferredItem<Item> ANDESITE_LATTICE=block(DifModBlocks.ANDESITE_LATTICE);
	public static final DeferredItem<Item> ANDESITE_WINDOW=block(DifModBlocks.ANDESITE_WINDOW);

	public static final DeferredItem<Item> BURNING_GENERATOR=block(DifModBlocks.BURNING_GENERATOR);
	public static final DeferredItem<Item> FLUID_HATCH=block(DifModBlocks.FLUID_HATCH);

	public static final DeferredItem<Item> SUPER_BOX=block(DifModBlocks.SUPER_BOX);
	public static final DeferredItem<Item> OLD_CHEST=block(DifModBlocks.OLD_CHEST);

	public static final DeferredItem<Item> ANDESITE_BARREL=block(DifModBlocks.ANDESITE_BARREL);
	public static final DeferredItem<Item> COPPER_BARREL=block(DifModBlocks.COPPER_BARREL);
	public static final DeferredItem<Item> BRASS_BARREL=block(DifModBlocks.BRASS_BARREL);

	public static final DeferredItem<Item> RUBY=REGISTRY.register("ruby",Basic::new);
	public static final DeferredItem<Item> RUBY_ORE=block(DifModBlocks.RUBY_ORE);
	public static final DeferredItem<Item> RUBY_BLOCK=block(DifModBlocks.RUBY_BLOCK);



	public static final DeferredItem<Item> SOLAR_PANEL_INC=REGISTRY.register("solar_panel_inc",Basic::new);
	public static final DeferredItem<Item> SOLAR_PANEL_00=block(DifModBlocks.SOLAR_PANEL_00);
	public static final DeferredItem<Item> SOLAR_PANEL_01=block(DifModBlocks.SOLAR_PANEL_01);
	public static final DeferredItem<Item> SOLAR_PANEL_02=block(DifModBlocks.SOLAR_PANEL_02);
	public static final DeferredItem<Item> SOLAR_PANEL_03=block(DifModBlocks.SOLAR_PANEL_03);
	public static final DeferredItem<Item> SOLAR_PANEL_04=block(DifModBlocks.SOLAR_PANEL_04);
	public static final DeferredItem<Item> SOLAR_PANEL_00_W=block(DifModBlocks.SOLAR_PANEL_00_W);
	public static final DeferredItem<Item> SOLAR_PANEL_01_W=block(DifModBlocks.SOLAR_PANEL_01_W);
	public static final DeferredItem<Item> SOLAR_PANEL_02_W=block(DifModBlocks.SOLAR_PANEL_02_W);
	public static final DeferredItem<Item> SOLAR_PANEL_03_W=block(DifModBlocks.SOLAR_PANEL_03_W);
	public static final DeferredItem<Item> SOLAR_PANEL_04_W=block(DifModBlocks.SOLAR_PANEL_04_W);

	public static final DeferredItem<Item> DEEPSLATED_ARROW=block(DifModBlocks.DEEPSLATED_ARROW);
	public static final DeferredItem<Item> STONED_ARROW=block(DifModBlocks.STONED_ARROW);
	public static final DeferredItem<Item> WOODED_ARROW=block(DifModBlocks.WOODED_ARROW);

	public static final DeferredItem<Item> BAUXITE_ORE=block(DifModBlocks.BAUXITE_ORE);
	public static final DeferredItem<Item> DEEPSLATE_BAUXITE_ORE=block(DifModBlocks.DEEPSLATE_BAUXITE_ORE);
	public static final DeferredItem<Item> RAW_BAUXITE=REGISTRY.register("raw_bauxite",Basic::new);
	public static final DeferredItem<Item> CRUSHED_RAW_BAUXITE=REGISTRY.register("crushed_raw_bauxite",Basic::new);
	public static final DeferredItem<Item> ALUMINUM_INGOT=REGISTRY.register("aluminum_ingot",Basic::new);
	public static final DeferredItem<Item> ALUMINUM_NUGGET=REGISTRY.register("aluminum_nugget",Basic::new);
	public static final DeferredItem<Item> ALUMINUM_BLOCK=block(DifModBlocks.ALUMINUM_BLOCK);
	public static final DeferredItem<Item> ALUMINUM_PROFILE=block(DifModBlocks.ALUMINUM_PROFILE);

	public static final DeferredItem<Item> SMOOTH_STONE_DOUBLE_SLAB=block(DifModBlocks.SMOOTH_STONE_DOUBLE_SLAB);
	public static final DeferredItem<Item> IRON_BARS_BLOCK=block(DifModBlocks.IRON_BARS_BLOCK);

	public static final DeferredItem<Item> TREE_BARK_BLOCK=block(DifModBlocks.TREE_BARK_BLOCK);
	public static final DeferredItem<Item> FRYING_TABLE=block(DifModBlocks.FRYING_TABLE);

	//Energy:
	public static final DeferredItem<Item> ENERGY_BLOCK=block(DifModBlocks.ENERGY_BLOCK);
	public static final DeferredItem<Item> BUDDING_ENERGY=block(DifModBlocks.BUDDING_ENERGY);
	public static final DeferredItem<Item> ENERGY_CLUSTER=block(DifModBlocks.ENERGY_CLUSTER);
	public static final DeferredItem<Item> LARGE_ENERGY_BUD=block(DifModBlocks.LARGE_ENERGY_BUD);
	public static final DeferredItem<Item> MEDIUM_ENERGY_BUD=block(DifModBlocks.MEDIUM_ENERGY_BUD);
	public static final DeferredItem<Item> SMALL_ENERGY_BUD=block(DifModBlocks.SMALL_ENERGY_BUD);
	public static final DeferredItem<Item> ENERGY_SHARD=REGISTRY.register("energy_shard",Basic::new);



	public static final DeferredItem<Item>FORMULA_ITEM=REGISTRY.register("formula",()->new FormulaItem(new Item.Properties().stacksTo(1)));


	//Weapons:
	public static final DeferredItem<Item> WOODEN_BATTLE_AXE=REGISTRY.register("wooden_battle_axe",()->new BattleAxeItem(Tiers.WOOD,6,-3.4F,new Item.Properties()));
	public static final DeferredItem<Item> GOLDEN_BATTLE_AXE=REGISTRY.register("golden_battle_axe",()->new BattleAxeItem(Tiers.GOLD,8,-3.4F,new Item.Properties()));
	public static final DeferredItem<Item> STONE_BATTLE_AXE=REGISTRY.register("stone_battle_axe",()->new BattleAxeItem(Tiers.STONE,7,-3.4F,new Item.Properties()));
	public static final DeferredItem<Item> IRON_BATTLE_AXE=REGISTRY.register("iron_battle_axe",()->new BattleAxeItem(Tiers.IRON,7,-3.3F,new Item.Properties()));
	public static final DeferredItem<Item> DIAMOND_BATTLE_AXE=REGISTRY.register("diamond_battle_axe",()->new BattleAxeItem(Tiers.DIAMOND,7,-3.3F,new Item.Properties()));
	public static final DeferredItem<Item> NETHERITE_BATTLE_AXE=REGISTRY.register("netherite_battle_axe",()->new BattleAxeItem(Tiers.NETHERITE,7,-3.3F,new Item.Properties().fireResistant()));
	public static final DeferredItem<Item> COPPER_BATTLE_AXE=REGISTRY.register("copper_battle_axe",()->new BattleAxeItem(DifModTiers.COPPER,6,-3.3F,new Item.Properties()));

	public static final DeferredItem<Item> WOODEN_KATANA=REGISTRY.register("wooden_katana",()->new SwordItem(Tiers.WOOD, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.WOOD, 1, -1F))));
	public static final DeferredItem<Item> GOLDEN_KATANA=REGISTRY.register("golden_katana",()->new SwordItem(Tiers.GOLD, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.GOLD, 2, -1F))));
	public static final DeferredItem<Item> STONE_KATANA=REGISTRY.register("stone_katana",()->new SwordItem(Tiers.STONE, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.STONE, 1, -1F))));
	public static final DeferredItem<Item> IRON_KATANA=REGISTRY.register("iron_katana",()->new SwordItem(Tiers.IRON, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.IRON, 1, -1F))));
	public static final DeferredItem<Item> DIAMOND_KATANA=REGISTRY.register("diamond_katana",()->new SwordItem(Tiers.DIAMOND, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.DIAMOND, 1, -1F))));
	public static final DeferredItem<Item> NETHERITE_KATANA=REGISTRY.register("netherite_katana",()->new SwordItem(Tiers.NETHERITE, new Item.Properties().fireResistant().attributes(SwordItem.createAttributes(Tiers.NETHERITE, 1, -1F))));
	public static final DeferredItem<Item> COPPER_KATANA=REGISTRY.register("copper_katana",()->new SwordItem(DifModTiers.COPPER, new Item.Properties().attributes(SwordItem.createAttributes(DifModTiers.COPPER, 0, -1F))));

	//Copper Tools:
	public static final DeferredItem<Item> COPPER_SHOVEL=REGISTRY.register("copper_shovel",()->new ShovelItem(DifModTiers.COPPER, new Item.Properties().attributes(ShovelItem.createAttributes(DifModTiers.COPPER, 0.5F, -3.0F))));
	public static final DeferredItem<Item> COPPER_PICKAXE=REGISTRY.register("copper_pickaxe",()->new PickaxeItem(DifModTiers.COPPER, new Item.Properties().attributes(PickaxeItem.createAttributes(DifModTiers.COPPER, 0, -2.8F))));
	public static final DeferredItem<Item> COPPER_SWORD=REGISTRY.register("copper_sword",()->new SwordItem(DifModTiers.COPPER, new Item.Properties().attributes(SwordItem.createAttributes(DifModTiers.COPPER, 2, -2.4F))));
	public static final DeferredItem<Item> COPPER_AXE=REGISTRY.register("copper_axe",()->new AxeItem(DifModTiers.COPPER, new Item.Properties().attributes(AxeItem.createAttributes(DifModTiers.COPPER, 6, -3.0F))));
	public static final DeferredItem<Item> COPPER_HOE=REGISTRY.register("copper_hoe",()->new HoeItem(DifModTiers.COPPER, new Item.Properties().attributes(HoeItem.createAttributes(DifModTiers.COPPER, -2, 0.0F))));
	public static final DeferredItem<Item> COPPER_HELMET=REGISTRY.register("copper_helmet",CopperArmor.Helmet::new);
	public static final DeferredItem<Item> COPPER_CHESTPLATE=REGISTRY.register("copper_chestplate",CopperArmor.Chestplate::new);
	public static final DeferredItem<Item> COPPER_LEGGINGS=REGISTRY.register("copper_leggings",CopperArmor.Leggings::new);
	public static final DeferredItem<Item> COPPER_BOOTS=REGISTRY.register("copper_boots",CopperArmor.Boots::new);

	//Space:
	public static final DeferredItem<Item> AURORA_CASING=block(DifModBlocks.AURORA_CASING);
	public static final DeferredItem<Item> AURORA_INGOT=REGISTRY.register("aurora_ingot",Basic::new);

	public static final DeferredItem<Item> ROCKET_FUEL=REGISTRY.register("rocket_fuel",()->new StackSize(16));
	public static final DeferredItem<Item> EMPTY_ROCKET_FUEL=REGISTRY.register("empty_rocket_fuel",()->new StackSize(16));

	public static final DeferredItem<Item> SPACE_SUIT_HELMET=REGISTRY.register("space_suit_helmet",SpaceSuit.Helmet::new);
	public static final DeferredItem<Item> SPACE_SUIT_CHESTPLATE=REGISTRY.register("space_suit_chestplate",SpaceSuit.Chestplate::new);
	public static final DeferredItem<Item> SPACE_SUIT_LEGGINGS=REGISTRY.register("space_suit_leggings",SpaceSuit.Leggings::new);
	public static final DeferredItem<Item> SPACE_SUIT_BOOTS=REGISTRY.register("space_suit_boots",SpaceSuit.Boots::new);

	public static final DeferredItem<Item> CARBON_SUIT_HELMET=REGISTRY.register("carbon_suit_helmet",CarbonSuit.Helmet::new);
	public static final DeferredItem<Item> CARBON_SUIT_CHESTPLATE=REGISTRY.register("carbon_suit_chestplate",CarbonSuit.Chestplate::new);
	public static final DeferredItem<Item> CARBON_SUIT_LEGGINGS=REGISTRY.register("carbon_suit_leggings",CarbonSuit.Leggings::new);
	public static final DeferredItem<Item> CARBON_SUIT_BOOTS=REGISTRY.register("carbon_suit_boots",CarbonSuit.Boots::new);

	public static final DeferredItem<Item> SPACESHIP=block(DifModBlocks.SPACESHIP);
	public static final DeferredItem<Item> SPACE_ENGINE=block(DifModBlocks.SPACE_ENGINE);
	public static final DeferredItem<Item> SPACE_SCAFFOLDING=block(DifModBlocks.SPACE_SCAFFOLDING);

	public static final DeferredItem<Item> SPACE_CASING=block(DifModBlocks.SPACE_CASING);
	public static final DeferredItem<Item> SPACE_CASING_REINFORCED=block(DifModBlocks.SPACE_CASING_REINFORCED);
	public static final DeferredItem<Item> SPACE_CASING_METAL=block(DifModBlocks.SPACE_CASING_METAL);

	public static final DeferredItem<Item> SPACE_DOOR=doubleBlock(DifModBlocks.SPACE_DOOR);
	public static final DeferredItem<Item> SPACE_CORRIDOR=block(DifModBlocks.SPACE_CORRIDOR);
	public static final DeferredItem<Item> SPACE_CRATE=block(DifModBlocks.SPACE_CRATE);
	public static final DeferredItem<Item> SOLAR_PANEL_BLOCK=block(DifModBlocks.SOLAR_PANEL_BLOCK);

	public static final DeferredItem<Item> MOON_STONE=block(DifModBlocks.MOON_STONE);
	public static final DeferredItem<Item> MARS_STONE=block(DifModBlocks.MARS_STONE);

	//nuke
	public static final DeferredItem<NuclearBombItem> NUCLEAR_BOMB = REGISTRY.register("nuclear_bomb", NuclearBombItem::new);

	public static final DeferredItem<Item>STEAM_GENERATOR=block(DifModBlocks.STEAM_GENERATOR);
}
