package cz.maxtechnik.dif.init.basic;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import cz.maxtechnik.dif.init.other.DifModEntities;
import cz.maxtechnik.dif.init.other.DifModFoods;
import cz.maxtechnik.dif.init.other.DifModTiers;
import cz.maxtechnik.dif.item.*;
import cz.maxtechnik.dif.item.food.*;
import cz.maxtechnik.dif.item.modular.v2.ModularPart;
import cz.maxtechnik.dif.item.modular.v2.ModularTemplate;
import cz.maxtechnik.dif.item.modular.v2.ModularTool;
import cz.maxtechnik.dif.item.modular.v2.ModularWikiBook;
import cz.maxtechnik.dif.item.quarry.DrillHeadItem;
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
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("unused")
public class DifModItems{
	public static final DeferredRegister.Items REGISTRY=DeferredRegister.createItems(DifMod.MODID);
	public static final DeferredRegister.Items V_REGISTRY= DeferredRegister.createItems("minecraft");
	private static DeferredItem<Item> block(DeferredBlock<Block> block){
		return block(block,new Item.Properties());
	}
	private static DeferredItem<Item> block(DeferredBlock<Block> block,Item.Properties properties){
		return REGISTRY.register(block.getId().getPath(),()->new BlockItem(block.get(),properties));
	}
	private static DeferredItem<Item> doubleBlock(DeferredBlock<Block> block){
		return REGISTRY.register(block.getId().getPath(),()->new DoubleHighBlockItem(block.get(),new Item.Properties()));
	}
	public static final DeferredItem<Item>SLEEPING_BAG=block(DifModBlocks.SLEEPING_BAG);
	public static final DeferredItem<Item>LAP_TIMER=block(DifModBlocks.LAP_TIMER);

	public static final DeferredItem<Item>QUARRY=block(DifModBlocks.QUARRY);
	public static final DeferredItem<Item>QUARRY_FRAME=block(DifModBlocks.QUARRY_FRAME);
	public static final DeferredItem<Item>QUARRY_LANDMARK=block(DifModBlocks.QUARRY_LANDMARK);
	

	// Quarry Upgrades & Components:
	public static final DeferredItem<Item>QUARRY_DRILL_IRON=REGISTRY.register("quarry_drill_iron",()->new DrillHeadItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<Item>QUARRY_DRILL_DIAMOND=REGISTRY.register("quarry_drill_diamond",()->new DrillHeadItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<Item>QUARRY_ENGINE_IRON=REGISTRY.register("quarry_engine_iron",()->new EngineItem(new Item.Properties().stacksTo(1),QuarryStats.IRON_ENGINE_QP_GEN,QuarryStats.IRON_ENGINE_FE_COST));
	public static final DeferredItem<Item>QUARRY_ENGINE_GOLD=REGISTRY.register("quarry_engine_gold",()->new EngineItem(new Item.Properties().stacksTo(1),QuarryStats.GOLD_ENGINE_QP_GEN,QuarryStats.GOLD_ENGINE_FE_COST));
	public static final DeferredItem<Item>QUARRY_ENGINE_DIAMOND=REGISTRY.register("quarry_engine_diamond",()->new EngineItem(new Item.Properties().stacksTo(1),QuarryStats.DIAMOND_ENGINE_QP_GEN,QuarryStats.DIAMOND_ENGINE_FE_COST));

	public static final DeferredItem<Item> DISTILLATION_TANK = REGISTRY.register("distillation_tank", () -> new cz.maxtechnik.dif.item.DistillationTankItem(DifModBlocks.DISTILLATION_TANK.get(), new Item.Properties()));

	public static final DeferredItem<Item>CAMERA_MONITOR=block(DifModBlocks.CAMERA_MONITOR);
	public static final DeferredItem<Item>CAMERA=block(DifModBlocks.CAMERA);
	public static final DeferredItem<Item>CAMERA_LINK=REGISTRY.register("camera_link",()->new CameraLink(new Item.Properties().stacksTo(1)));

	public static final DeferredItem<Item>MEGA_TORCH=block(DifModBlocks.MEGA_TORCH);

	public static final DeferredItem<Item>PHANTOM_RING=REGISTRY.register("phantom_ring",PhantomRing::new);

	public static final DeferredItem<Item>GOD_TOTEM=REGISTRY.register("god_totem",()->new GodTotemItem(new Item.Properties()));
	public static final DeferredItem<Item>BAN_HAMMER=REGISTRY.register("ban_hammer",BanHammer::new);

	public static final DeferredItem<Item>FAST_POWERED_RAIL=block(DifModBlocks.FAST_POWERED_RAIL);
	public static final DeferredItem<Item>FAST_RAIL=block(DifModBlocks.FAST_RAIL);

	public static final DeferredItem<Item>CHUNK_LOADER_1X1=REGISTRY.register("chunk_loader_1x1",()->new BlockItem(DifModBlocks.CHUNK_LOADER_1X1.get(),new Item.Properties()){@Override public boolean isFoil(@NotNull ItemStack stack){return true;}});
	public static final DeferredItem<Item>CHUNK_LOADER_3X3=REGISTRY.register("chunk_loader_3x3",()->new BlockItem(DifModBlocks.CHUNK_LOADER_3X3.get(),new Item.Properties()){@Override public boolean isFoil(@NotNull ItemStack stack){return true;}});

	//Modular Stuff:
	public static final DeferredItem<Item>MODULAR_TOOL=REGISTRY.register("modular_tool",ModularTool::new);
	public static final DeferredItem<Item>MODULAR_PART=REGISTRY.register("modular_part",ModularPart::new);
	public static final DeferredItem<Item>SILK=REGISTRY.register("silk",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>MODULAR_REFORGE_TABLE=block(DifModBlocks.MODULAR_REFORGE_TABLE);
	public static final DeferredItem<Item>MODULAR_REFORGE_STONE=REGISTRY.register("modular_reforge_stone",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>MODULAR_TEMPLATE_NORMAL=REGISTRY.register("modular_template_normal",()->new ModularTemplate(new Item.Properties()));
	public static final DeferredItem<Item>MODULAR_TEMPLATE_HYPER=REGISTRY.register("modular_template_hyper",()->new ModularTemplate(new Item.Properties().rarity(Rarity.EPIC)));

	public static final DeferredItem<Item>CASTING_MOLD=REGISTRY.register("casting_mold",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>CASTING_MOLD_HANDLE=REGISTRY.register("casting_mold_handle",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>CASTING_MOLD_BINDING=REGISTRY.register("casting_mold_binding",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>CASTING_MOLD_AXE_HEAD=REGISTRY.register("casting_mold_axe_head",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>CASTING_MOLD_PICKAXE_HEAD=REGISTRY.register("casting_mold_pickaxe_head",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>CASTING_MOLD_SHOVEL_HEAD=REGISTRY.register("casting_mold_shovel_head",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>CASTING_MOLD_SWORD_HEAD=REGISTRY.register("casting_mold_sword_head",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>CASTING_MOLD_SWORD_BINDING=REGISTRY.register("casting_mold_sword_binding",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>CASTING_MOLD_HOE_HEAD=REGISTRY.register("casting_mold_hoe_head",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>CASTING_MOLD_BATTLE_AXE_HEAD=REGISTRY.register("casting_mold_battle_axe_head",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>CASTING_MOLD_KATANA_HEAD=REGISTRY.register("casting_mold_katana_head",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>CASTING_MOLD_TIMBER_AXE_HEAD=REGISTRY.register("casting_mold_timber_axe_head",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>CASTING_MOLD_HAMMER_HEAD=REGISTRY.register("casting_mold_hammer_head",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>CASTING_MOLD_EXCAVATOR_HEAD=REGISTRY.register("casting_mold_excavator_head",()->new Item(new Item.Properties()));

	public static final DeferredItem<Item>MODULAR_WIKI_BOOK=REGISTRY.register("modular_wiki_book",()->new ModularWikiBook(new Item.Properties()));

	public static final DeferredItem<Item> PORTAL_GUN=REGISTRY.register("portal_gun",PortalGun::new);

	public static final DeferredItem<Item> ELECTRO_RUNNERS=REGISTRY.register("electro_runners",ElectroRunners.Boots::new);

	//Jetpack
	public static final DeferredItem<Item> JETPACK=REGISTRY.register("jetpack",Jetpack.Chestplate::new);

	//Fluid:
	public static final Item.Properties BUCKET_PROPERTIES=new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1);
	public static final DeferredItem<Item> BEER_BUCKET=REGISTRY.register("beer_bucket",()->new BucketItem(DifModFluids.BEER.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item> XP_BUCKET=REGISTRY.register("xp_bucket",()->new BucketItem(DifModFluids.XP.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item> FUEL_BUCKET=REGISTRY.register("fuel_bucket",()->new BucketItem(DifModFluids.FUEL.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>CIDER_BUCKET=REGISTRY.register("cider_bucket",()->new BucketItem(DifModFluids.CIDER.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>CRUDE_OIL_BUCKET=REGISTRY.register("crude_oil_bucket",()->new BucketItem(DifModFluids.CRUDE_OIL.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>JETPACK_FUEL_BUCKET=REGISTRY.register("jetpack_fuel_bucket",()->new BucketItem(DifModFluids.JETPACK_FUEL.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>SUNFLOWER_OIL_BUCKET=REGISTRY.register("sunflower_oil_bucket",()->new BucketItem(DifModFluids.SUNFLOWER_OIL.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>CREOSOTE_OIL_BUCKET=REGISTRY.register("creosote_oil_bucket",()->new BucketItem(DifModFluids.CREOSOTE_OIL.get(),BUCKET_PROPERTIES));


	public static final DeferredItem<Item>LPG_BUCKET=REGISTRY.register("lpg_bucket",()->new BucketItem(DifModFluids.LPG.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>GASOLINE_BUCKET=REGISTRY.register("gasoline_bucket",()->new BucketItem(DifModFluids.GASOLINE.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>DIESEL_BUCKET=REGISTRY.register("diesel_bucket",()->new BucketItem(DifModFluids.DIESEL.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>LUBRICATING_OIL_BUCKET=REGISTRY.register("lubricating_oil_bucket",()->new BucketItem(DifModFluids.LUBRICATING_OIL.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>HEAVY_FUEL_OIL_BUCKET=REGISTRY.register("heavy_fuel_oil_bucket",()->new BucketItem(DifModFluids.HEAVY_FUEL_OIL.get(),BUCKET_PROPERTIES));

	public static final DeferredItem<Item>MOLTEN_IRON_BUCKET=REGISTRY.register("molten_iron_bucket",()->new BucketItem(DifModFluids.MOLTEN_IRON.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>MOLTEN_COPPER_BUCKET=REGISTRY.register("molten_copper_bucket",()->new BucketItem(DifModFluids.MOLTEN_COPPER.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>MOLTEN_GOLD_BUCKET=REGISTRY.register("molten_gold_bucket",()->new BucketItem(DifModFluids.MOLTEN_GOLD.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>MOLTEN_STEEL_BUCKET=REGISTRY.register("molten_steel_bucket",()->new BucketItem(DifModFluids.MOLTEN_STEEL.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>MOLTEN_OBSIDIAN_BUCKET=REGISTRY.register("molten_obsidian_bucket",()->new BucketItem(DifModFluids.MOLTEN_OBSIDIAN.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>MOLTEN_ZINC_BUCKET=REGISTRY.register("molten_zinc_bucket",()->new BucketItem(DifModFluids.MOLTEN_ZINC.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>MOLTEN_BRASS_BUCKET=REGISTRY.register("molten_brass_bucket",()->new BucketItem(DifModFluids.MOLTEN_BRASS.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>MOLTEN_NICKEL_BUCKET=REGISTRY.register("molten_nickel_bucket",()->new BucketItem(DifModFluids.MOLTEN_NICKEL.get(),BUCKET_PROPERTIES));
	public static final DeferredItem<Item>MOLTEN_MITHRIL_BUCKET=REGISTRY.register("molten_mithril_bucket",()->new BucketItem(DifModFluids.MOLTEN_MITHRIL.get(),BUCKET_PROPERTIES));

	//Vanilla + :
	public static final DeferredItem<Item> END_PORTAL=V_REGISTRY.register("end_portal",()->new BlockItem(Blocks.END_PORTAL,new Item.Properties()));
	public static final DeferredItem<Item> END_GATEWAY=V_REGISTRY.register("end_gateway",()->new BlockItem(Blocks.END_GATEWAY,new Item.Properties()));
	public static final DeferredItem<Item> NETHER_PORTAL=V_REGISTRY.register("nether_portal",()->new BlockItem(Blocks.NETHER_PORTAL,new Item.Properties()));
	public static final DeferredItem<Item> WATER=V_REGISTRY.register("water",()->new BlockItem(Blocks.WATER,new Item.Properties()));
	public static final DeferredItem<Item> LAVA=V_REGISTRY.register("lava",()->new BlockItem(Blocks.LAVA,new Item.Properties().fireResistant()));
	public static final DeferredItem<Item> FIRE=V_REGISTRY.register("fire",()->new BlockItem(Blocks.FIRE,new Item.Properties().fireResistant()));

	//Food:
	public static final DeferredItem<Item> BEER=REGISTRY.register("beer",()->new Beer(DifModBlocks.BEER.get(),new Item.Properties().food(DifModFoods.BEER)));

	public static final DeferredItem<Item> BERRY_BOTTLE=REGISTRY.register("berry_bottle",()->new RetvalFoods(new Item.Properties().rarity(Rarity.UNCOMMON).food(DifModFoods.BERRY_BOTTLE),Items.GLASS_BOTTLE,UseAnim.DRINK));

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

	//Tech & Stuff:
	public static final DeferredItem<Item> INCOMPLETE_UNIVERSAL=REGISTRY.register("incomplete_universal",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item> ELECTRUM_DESTROYER=REGISTRY.register("electrum_destroyer",ElectrumDestroyer::new);
	public static final DeferredItem<Item> BLUESTONE=REGISTRY.register("bluestone",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item> BLUE_PLATE=REGISTRY.register("blue_plate",()->new Item(new Item.Properties()));


	public static final DeferredItem<Item> MITHRIL=REGISTRY.register("mithril",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item> MITHRIL_PLATE=REGISTRY.register("mithril_plate",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item> INCOMPLETE_MITHRIL_PLATE=REGISTRY.register("incomplete_mithril_plate",()->new Item(new Item.Properties()));




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

	public static final DeferredItem<Item> RUBY=REGISTRY.register("ruby",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item> RUBY_ORE=block(DifModBlocks.RUBY_ORE);
	public static final DeferredItem<Item> RUBY_BLOCK=block(DifModBlocks.RUBY_BLOCK);



	public static final DeferredItem<Item> SOLAR_PANEL_INC=REGISTRY.register("solar_panel_inc",()->new Item(new Item.Properties()));
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
	public static final DeferredItem<Item> RAW_BAUXITE=REGISTRY.register("raw_bauxite",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item> CRUSHED_RAW_BAUXITE=REGISTRY.register("crushed_raw_bauxite",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item> ALUMINUM_INGOT=REGISTRY.register("aluminum_ingot",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item> ALUMINUM_NUGGET=REGISTRY.register("aluminum_nugget",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item> ALUMINUM_BLOCK=block(DifModBlocks.ALUMINUM_BLOCK);
	public static final DeferredItem<Item> ALUMINUM_PROFILE=block(DifModBlocks.ALUMINUM_PROFILE);

	public static final DeferredItem<Item> SMOOTH_STONE_DOUBLE_SLAB=block(DifModBlocks.SMOOTH_STONE_DOUBLE_SLAB);
	public static final DeferredItem<Item> IRON_BARS_BLOCK=block(DifModBlocks.IRON_BARS_BLOCK);

	public static final DeferredItem<Item> TREE_BARK_BLOCK=block(DifModBlocks.TREE_BARK_BLOCK);
	public static final DeferredItem<Item> FRYING_TABLE=block(DifModBlocks.FRYING_TABLE);

	public static final DeferredItem<Item> FORMULA=REGISTRY.register("formula",()->new FormulaItem(new Item.Properties().stacksTo(1)));


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
	public static final DeferredItem<Item> AURORA_INGOT=REGISTRY.register("aurora_ingot",()->new Item(new Item.Properties()));

	public static final DeferredItem<Item> ROCKET_FUEL=REGISTRY.register("rocket_fuel",()->new Item(new Item.Properties().stacksTo(16)));
	public static final DeferredItem<Item> EMPTY_ROCKET_FUEL=REGISTRY.register("empty_rocket_fuel",()->new Item(new Item.Properties().stacksTo(16)));

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


	public static final DeferredItem<Item>ZINC_CASING=block(DifModBlocks.ZINC_CASING);
	public static final DeferredItem<Item>STEEL_CASING=block(DifModBlocks.STEEL_CASING);

	public static final DeferredItem<Item>ZINC_SUPPORT=block(DifModBlocks.ZINC_SUPPORT);
	public static final DeferredItem<Item>BRASS_SUPPORT=block(DifModBlocks.BRASS_SUPPORT);
	public static final DeferredItem<Item>COPPER_SUPPORT=block(DifModBlocks.COPPER_SUPPORT);
	public static final DeferredItem<Item>STEEL_SUPPORT=block(DifModBlocks.STEEL_SUPPORT);

	public static final DeferredItem<Item>BIG_GIRDER=block(DifModBlocks.BIG_GIRDER);

	public static final DeferredItem<Item>NUKE=block(DifModBlocks.NUKE,new Item.Properties().stacksTo(16));
	public static final DeferredItem<Item>NUKE_SAFE=block(DifModBlocks.NUKE_SAFE,new Item.Properties().stacksTo(16));

	public static final DeferredItem<Item>COKE_OVEN=block(DifModBlocks.COKE_OVEN);
	public static final DeferredItem<Item>COKE_OVEN_CONTROLLER=block(DifModBlocks.COKE_OVEN_CONTROLLER);
	public static final DeferredItem<Item>COKE=REGISTRY.register("coke",()->new Item(new Item.Properties()));

	public static final DeferredItem<Item>FORGE_BRICK=block(DifModBlocks.FORGE_BRICK);
	public static final DeferredItem<Item>FORGE_GLASS=block(DifModBlocks.FORGE_GLASS);
	public static final DeferredItem<Item>FORGE_FURNACE_CONTROLLER=block(DifModBlocks.FORGE_FURNACE_CONTROLLER);

	public static final DeferredItem<Item>ENGINE_BASE=block(DifModBlocks.ENGINE_BASE);

	public static final DeferredItem<Item>ENGINE_PORTABLE_DIESEL=block(DifModBlocks.ENGINE_PORTABLE_DIESEL);
	public static final DeferredItem<Item>ENGINE_PORTABLE_GASOLINE=block(DifModBlocks.ENGINE_PORTABLE_GASOLINE);
	public static final DeferredItem<Item>ENGINE_PORTABLE_LPG=block(DifModBlocks.ENGINE_PORTABLE_LPG);

	public static final DeferredItem<Item>ENGINE_EXTENDER_DIESEL=block(DifModBlocks.ENGINE_EXTENDER_DIESEL);
	public static final DeferredItem<Item>ENGINE_EXTENDER_GASOLINE=block(DifModBlocks.ENGINE_EXTENDER_GASOLINE);
	public static final DeferredItem<Item>ENGINE_EXTENDER_LPG=block(DifModBlocks.ENGINE_EXTENDER_LPG);
	public static final DeferredItem<Item>ENGINE_EXTENDER_HEAVY_FUEL_OIL=block(DifModBlocks.ENGINE_EXTENDER_HEAVY_FUEL_OIL);

	public static final DeferredItem<Item>WOODEN_FRAME=block(DifModBlocks.WOODEN_FRAME);
	public static final DeferredItem<Item>DEEPSLATE_MITHRIL_ORE=block(DifModBlocks.DEEPSLATE_MITHRIL_ORE);

	public static final DeferredItem<Item>NICKEL_NUGGET=REGISTRY.register("nickel_nugget",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>NICKEL_INGOT=REGISTRY.register("nickel_ingot",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>RAW_NICKEL=REGISTRY.register("raw_nickel",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>NICKEL_BLOCK=block(DifModBlocks.NICKEL_BLOCK);
	public static final DeferredItem<Item>RAW_NICKEL_BLOCK=block(DifModBlocks.RAW_NICKEL_BLOCK);
	public static final DeferredItem<Item>NICKEL_ORE=block(DifModBlocks.NICKEL_ORE);
	public static final DeferredItem<Item>DEEPSLATE_NICKEL_ORE=block(DifModBlocks.DEEPSLATE_NICKEL_ORE);
	public static final DeferredItem<Item>NICKEL_SHEET=REGISTRY.register("nickel_sheet",()->new Item(new Item.Properties()));

	public static final DeferredItem<Item>STEEL_SHEET=REGISTRY.register("steel_sheet",()->new Item(new Item.Properties()));
	public static final DeferredItem<Item>STEEL_INGOT=REGISTRY.register("steel_ingot",()->new Item(new Item.Properties()));

	public static final DeferredItem<Item>NANO_GLASS=block(DifModBlocks.NANO_GLASS);


	public static final DeferredItem<Item>SILKWORM_MOTH_SPAWN_EGG=REGISTRY.register("silkworm_moth_spawn_egg",()->new DeferredSpawnEggItem(DifModEntities.SILKWORM_MOTH,0xFFFFFF,0x898989,new Item.Properties()));

}
