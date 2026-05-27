package cz.maxtechnik.dif.init.basic;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.*;
import cz.maxtechnik.dif.block.barrel.AndesiteBarrel;
import cz.maxtechnik.dif.block.barrel.BrassBarrel;
import cz.maxtechnik.dif.block.barrel.CopperBarrel;
import cz.maxtechnik.dif.block.SteamGeneratorBlock;
import cz.maxtechnik.dif.block.mata.MataPlant;
import cz.maxtechnik.dif.block.mata.MatyBlock;
import cz.maxtechnik.dif.block.rails.FastPoweredRailBlock;
import cz.maxtechnik.dif.block.rails.FastRailBlock;
import cz.maxtechnik.dif.block.space.*;
import cz.maxtechnik.dif.block.template.*;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
@SuppressWarnings("unused")
public class DifModBlocks{
	public static final DeferredRegister.Blocks REGISTRY=DeferredRegister.createBlocks(DifMod.MODID);

	//Random:
	public static final DeferredBlock<Block>CHUNK_LOADER_1X1=REGISTRY.register("chunk_loader_1x1",ChunkLoader::new);
	public static final DeferredBlock<Block>CHUNK_LOADER_3X3=REGISTRY.register("chunk_loader_3x3",ChunkLoader::new);
	public static final DeferredBlock<Block>FAST_POWERED_RAIL=REGISTRY.register("fast_powered_rail",()->new FastPoweredRailBlock(BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL)));
	public static final DeferredBlock<Block>FAST_RAIL=REGISTRY.register("fast_rail",()->new FastRailBlock(BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL)));
	public static final DeferredBlock<Block>REMOTE_MINECART_BLOCK=REGISTRY.register("remote_minecart_block",RemoteMinecartBlock::new);

	public static final DeferredBlock<Block>SLEEPING_BAG=REGISTRY.register("sleeping_bag",SleepingBagBlock::new);

	//Quarry & Stuff:
	public static final DeferredBlock<Block>QUARRY=REGISTRY.register("quarry",()->new Quarry(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(5F,6F).pushReaction(PushReaction.BLOCK).requiresCorrectToolForDrops()));
	public static final DeferredBlock<Block>QUARRY_FRAME=REGISTRY.register("quarry_frame",QuarryFrame::new);
	public static final DeferredBlock<Block>QUARRY_LANDMARK=REGISTRY.register("quarry_landmark",QuarryLandmark::new);

	//Camera Stuff:
	public static final DeferredBlock<Block>CAMERA_MONITOR=REGISTRY.register("camera_monitor",()->new CameraMonitor(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(5F,6F).pushReaction(PushReaction.BLOCK).requiresCorrectToolForDrops()));
	public static final DeferredBlock<Block>CAMERA=REGISTRY.register("camera",()->new Camera(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(3F,3F).pushReaction(PushReaction.BLOCK).requiresCorrectToolForDrops()));


	//Fluids:
	public static final DeferredBlock<Block>FUEL_FLUID=REGISTRY.register("fuel_fluid",()->new LiquidBlock(DifModFluids.FUEL.get(),BlockBehaviour.Properties.of().strength(100F).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable()));
	public static final DeferredBlock<Block>BEER_FLUID=REGISTRY.register("beer_fluid",()->new LiquidBlock(DifModFluids.BEER.get(),BlockBehaviour.Properties.of().strength(100F).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable()));
	public static final DeferredBlock<Block>XP_FLUID=REGISTRY.register("xp_fluid",()->new LiquidBlock(DifModFluids.XP.get(),BlockBehaviour.Properties.of().strength(100F).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable().lightLevel(s->15)));
	public static final DeferredBlock<Block>CIDER_FLUID=REGISTRY.register("cider_fluid",()->new LiquidBlock(DifModFluids.CIDER.get(),BlockBehaviour.Properties.of().strength(100F).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable()));
	public static final DeferredBlock<Block>CRUDE_OIL_FLUID=REGISTRY.register("crude_oil_fluid",()->new LiquidBlock(DifModFluids.CRUDE_OIL.get(),BlockBehaviour.Properties.of().strength(100F).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable()));
	public static final DeferredBlock<Block>JETPACK_FUEL_FLUID=REGISTRY.register("jetpack_fuel_fluid",()->new LiquidBlock(DifModFluids.JETPACK_FUEL.get(),BlockBehaviour.Properties.of().strength(100F).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable()));
	public static final DeferredBlock<Block>JETPACK_TURBO_FUEL_FLUID=REGISTRY.register("jetpack_turbo_fuel_fluid",()->new LiquidBlock(DifModFluids.JETPACK_TURBO_FUEL.get(),BlockBehaviour.Properties.of().strength(100F).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable()));
	public static final DeferredBlock<Block>SUNFLOWER_OIL_FLUID=REGISTRY.register("sunflower_oil_fluid",()->new LiquidBlock(DifModFluids.SUNFLOWER_OIL.get(),BlockBehaviour.Properties.of().strength(100F).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable()));

	//Random (0):
	public static final DeferredBlock<Block> MEGA_TORCH = REGISTRY.register("mega_torch",()->new MegaTorch(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2F,3F).sound(SoundType.WOOD).pushReaction(PushReaction.BLOCK).lightLevel(state->15)));

	public static final DeferredBlock<Block>BEER=REGISTRY.register("beer",Beer::new);
	public static final DeferredBlock<Block>THE_DIFFERENTIAL=REGISTRY.register("the_differential",()->new CustomWaterloggedHorizontalRotation(SoundType.STONE,5F,6F,true));

	public static final DeferredBlock<Block>EVENT_BUS=REGISTRY.register("event_bus",()->new CustomWaterloggedHorizontalRotation(SoundType.NETHERITE_BLOCK,5F,6F,true));
	public static final DeferredBlock<Block>VENT=REGISTRY.register("vent",()->new CustomWaterloggedHorizontalRotation(SoundType.NETHERITE_BLOCK,5F,6F,true));
	public static final DeferredBlock<Block>BURNING_GENERATOR=REGISTRY.register("burning_generator",BurningGenerator::new);
	public static final DeferredBlock<Block>FLUID_HATCH=REGISTRY.register("fluid_hatch",FluidHatch::new);
	public static final DeferredBlock<Block>SINGULARITATOR=REGISTRY.register("singularitator",()->new CustomWaterlogged(SoundType.METAL,5F,6F,true));
	public static final DeferredBlock<Block>SUPER_BOX=REGISTRY.register("super_box",SuperBox::new);
	public static final DeferredBlock<Block>OLD_CHEST=REGISTRY.register("old_chest",OldChest::new);

	//Solar Panels:
	public static final DeferredBlock<Block>SOLAR_PANEL_00=REGISTRY.register("solar_panel_00",SolarPanel::new);
	public static final DeferredBlock<Block>SOLAR_PANEL_01=REGISTRY.register("solar_panel_01",SolarPanel::new);
	public static final DeferredBlock<Block>SOLAR_PANEL_02=REGISTRY.register("solar_panel_02",SolarPanel::new);
	public static final DeferredBlock<Block>SOLAR_PANEL_03=REGISTRY.register("solar_panel_03",SolarPanel::new);
	public static final DeferredBlock<Block>SOLAR_PANEL_04=REGISTRY.register("solar_panel_04",SolarPanel::new);

	public static final DeferredBlock<Block>SOLAR_PANEL_00_W=REGISTRY.register("solar_panel_00_w",SolarPanel::new);
	public static final DeferredBlock<Block>SOLAR_PANEL_01_W=REGISTRY.register("solar_panel_01_w",SolarPanel::new);
	public static final DeferredBlock<Block>SOLAR_PANEL_02_W=REGISTRY.register("solar_panel_02_w",SolarPanel::new);
	public static final DeferredBlock<Block>SOLAR_PANEL_03_W=REGISTRY.register("solar_panel_03_w",SolarPanel::new);
	public static final DeferredBlock<Block>SOLAR_PANEL_04_W=REGISTRY.register("solar_panel_04_w",SolarPanel::new);

	//Random (2):
	public static final DeferredBlock<Block>CINDER_FLOUR_BLOCK=REGISTRY.register("cinder_flour_block",()->new Custom(SoundType.WART_BLOCK,0.4F,0.6F,false));
	public static final DeferredBlock<Block>PEDROCK=REGISTRY.register("pedrock",()->new Custom(SoundType.STONE,1000F,999999999F,true));
	public static final DeferredBlock<Block>ANDESITE_LATTICE=REGISTRY.register("andesite_lattice",AndesiteLattice::new);
	public static final DeferredBlock<Block>ANDESITE_WINDOW=REGISTRY.register("andesite_window",AndesiteWindow::new);
	public static final DeferredBlock<Block>SMOOTH_STONE_DOUBLE_SLAB=REGISTRY.register("smooth_stone_double_slab",()->new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2F,6F)));
	public static final DeferredBlock<Block>IRON_BARS_BLOCK=REGISTRY.register("iron_bars_block",()->new CustomWaterlogged(SoundType.METAL,5F,6F,true));
	public static final DeferredBlock<Block>GLITCH_BLOCK=REGISTRY.register("glitch_block",()->new Custom(SoundType.STONE,1.8F,3F,true));
	public static final DeferredBlock<Block>TREE_BARK_BLOCK=REGISTRY.register("tree_bark_block",()->new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.8F,3F).sound(SoundType.MANGROVE_ROOTS).ignitedByLava()));

	//Arrows:
	public static final DeferredBlock<Block>DEEPSLATED_ARROW=REGISTRY.register("deepslated_arrow",()->new CustomHorizontalRotation(SoundType.DEEPSLATE,2.5F,16F,true));
	public static final DeferredBlock<Block>STONED_ARROW=REGISTRY.register("stoned_arrow",()->new CustomHorizontalRotation(SoundType.STONE,1.5F,6F,true));
	public static final DeferredBlock<Block>WOODED_ARROW=REGISTRY.register("wooded_arrow",()->new CustomHorizontalRotation(SoundType.WOOD,2F,3F,false));

	//Bauxite:
	public static final DeferredBlock<Block>BAUXITE_ORE=REGISTRY.register("bauxite_ore",()->new Custom(SoundType.STONE,3F,3F,true));
	public static final DeferredBlock<Block>DEEPSLATE_BAUXITE_ORE=REGISTRY.register("deepslate_bauxite_ore",()->new Custom(SoundType.DEEPSLATE,4.5F,3F,true));
	public static final DeferredBlock<Block>ALUMINUM_BLOCK=REGISTRY.register("aluminum_block",()->new Custom(SoundType.METAL,5F,6F,true));
	public static final DeferredBlock<Block>ALUMINUM_PROFILE=REGISTRY.register("aluminum_profile",AluminumProfile::new);

	//Canola:
	public static final DeferredBlock<Block>CANOLA_PLANT=REGISTRY.register("canola_plant",CanolaPlant::new);
	public static final DeferredBlock<Block>MATA_PLANT=REGISTRY.register("mata_plant",MataPlant::new);
	public static final DeferredBlock<Block>MATY_BLOCK=REGISTRY.register("maty_block",MatyBlock::new);

	//Ruby:
	public static final DeferredBlock<Block>RUBY_ORE=REGISTRY.register("ruby_ore",()->new Custom(SoundType.STONE,3F,3F,true));
	public static final DeferredBlock<Block>RUBY_BLOCK=REGISTRY.register("ruby_block",()->new Custom(SoundType.STONE,5F,6F,true));

	//Energy:
	public static final DeferredBlock<Block>ENERGY_BLOCK=REGISTRY.register("energy_block",()->new AmethystBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(1.5F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops()));
	public static final DeferredBlock<Block>BUDDING_ENERGY=REGISTRY.register("budding_energy",()->new BuddingEnergyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).randomTicks().strength(1.5F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops().pushReaction(PushReaction.DESTROY)));
	public static final DeferredBlock<Block>ENERGY_CLUSTER=REGISTRY.register("energy_cluster",()->new AmethystClusterBlock(7,3,BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).forceSolidOn().noOcclusion().randomTicks().sound(SoundType.AMETHYST_CLUSTER).strength(1.5F).lightLevel((p_152632_)->5).pushReaction(PushReaction.DESTROY)));
	public static final DeferredBlock<Block>LARGE_ENERGY_BUD=REGISTRY.register("large_energy_bud",()->new AmethystClusterBlock(5,3,BlockBehaviour.Properties.ofFullCopy(ENERGY_CLUSTER.get()).sound(SoundType.MEDIUM_AMETHYST_BUD).forceSolidOn().lightLevel((p_152629_)->4).pushReaction(PushReaction.DESTROY)));
	public static final DeferredBlock<Block>MEDIUM_ENERGY_BUD=REGISTRY.register("medium_energy_bud",()->new AmethystClusterBlock(4,3,BlockBehaviour.Properties.ofFullCopy(ENERGY_CLUSTER.get()).sound(SoundType.LARGE_AMETHYST_BUD).forceSolidOn().lightLevel((p_152617_)->2).pushReaction(PushReaction.DESTROY)));
	public static final DeferredBlock<Block>SMALL_ENERGY_BUD=REGISTRY.register("small_energy_bud",()->new AmethystClusterBlock(3,4,BlockBehaviour.Properties.ofFullCopy(ENERGY_CLUSTER.get()).sound(SoundType.SMALL_AMETHYST_BUD).forceSolidOn().lightLevel((p_187409_)->1).pushReaction(PushReaction.DESTROY)));

	//Barrels:
	public static final DeferredBlock<Block>ANDESITE_BARREL=REGISTRY.register("andesite_barrel",AndesiteBarrel::new);
	public static final DeferredBlock<Block>COPPER_BARREL=REGISTRY.register("copper_barrel",CopperBarrel::new);
	public static final DeferredBlock<Block>BRASS_BARREL=REGISTRY.register("brass_barrel",BrassBarrel::new);

	//Random (3):
	public static final DeferredBlock<Block>PORTAL_BLOCK=REGISTRY.register("portal_block",()->new PortalBlock(BlockBehaviour.Properties.of().noCollission().noOcclusion().pushReaction(PushReaction.BLOCK)));
	public static final DeferredBlock<Block>FRYING_TABLE=REGISTRY.register("frying_table",FryingTable::new);

	public static final DeferredBlock<Block> DISTILLATION_TANK=REGISTRY.register("distillation_tank", DistillationTank::new);

	//Space:
	public static final DeferredBlock<Block>AURORA_CASING=REGISTRY.register("aurora_casing",()->new Block(BlockBehaviour.Properties.of().sound(SoundType.NETHERITE_BLOCK).strength(4F,4F).requiresCorrectToolForDrops()));
	public static final DeferredBlock<Block>SPACESHIP=REGISTRY.register("spaceship",Spaceship::new);
	public static final DeferredBlock<Block>SPACESHIP_GHOST_BLOCK=REGISTRY.register("spaceship_ghost_block",SpaceshipGhostBlock::new);
	public static final DeferredBlock<Block>SPACE_ENGINE=REGISTRY.register("space_engine",SpaceEngine::new);


	public static final DeferredBlock<Block>SPACE_SCAFFOLDING=REGISTRY.register("space_scaffolding",SpaceScaffolding::new);
	public static final DeferredBlock<Block>SPACE_CASING=REGISTRY.register("space_casing",()->new Custom(SoundType.NETHERITE_BLOCK,5F,6F,true));
	public static final DeferredBlock<Block>SPACE_CASING_REINFORCED=REGISTRY.register("space_casing_reinforced",()->new Custom(SoundType.NETHERITE_BLOCK,5F,6F,true));
	public static final DeferredBlock<Block>SPACE_CASING_METAL=REGISTRY.register("space_casing_metal",()->new Custom(SoundType.NETHERITE_BLOCK,5F,6F,true));
	public static final DeferredBlock<Block>SPACE_DOOR=REGISTRY.register("space_door",SpaceDoor::new);
	public static final DeferredBlock<Block>SPACE_CORRIDOR=REGISTRY.register("space_corridor",()->new Custom(SoundType.GLASS,5F,6F,true));
	public static final DeferredBlock<Block>SPACE_CRATE=REGISTRY.register("space_crate",()->new SpaceCrateBlock(BlockBehaviour.Properties.of().strength(5F,6F).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops()));
	public static final DeferredBlock<Block>SOLAR_PANEL_BLOCK=REGISTRY.register("solar_panel_block",()->new Custom(SoundType.STONE,5F,6F,true));

	public static final DeferredBlock<Block>MOON_STONE=REGISTRY.register("moon_stone",()->new Custom(SoundType.STONE,3F,4F,true));
	public static final DeferredBlock<Block>MARS_STONE=REGISTRY.register("mars_stone",()->new Custom(SoundType.STONE,3F,4F,true));

	//Tracks:
	public static final DeferredBlock<Block>BROKEN_TRACK00=REGISTRY.register("broken_track00",BrokenTrack::new);
	public static final DeferredBlock<Block>BROKEN_TRACK01=REGISTRY.register("broken_track01",BrokenTrack::new);
	public static final DeferredBlock<Block>BROKEN_TRACK02=REGISTRY.register("broken_track02",BrokenTrack::new);

	//Race:
	public static final DeferredBlock<Block>LAP_TIMER=REGISTRY.register("lap_timer",()->new Block(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.NETHERITE_BLOCK)));

	//nuke
	public static final DeferredBlock<Block> NUCLEAR_BOMB = REGISTRY.register("nuclear_bomb", NuclearBombBlock::new);

	public static final DeferredBlock<Block>BIG_GIRDER=REGISTRY.register("big_girder",()->new RotatedPillarBlock(BlockBehaviour.Properties.of().sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops().strength(4F,5F)));


	public static final DeferredBlock<Block>ZINC_CASING=REGISTRY.register("zinc_casing",()->new Block(BlockBehaviour.Properties.of().strength(1.5F,6F).sound(SoundType.WOOD).requiresCorrectToolForDrops()));
	public static final DeferredBlock<Block>STEEL_CASING=REGISTRY.register("steel_casing",()->new Block(BlockBehaviour.Properties.of().strength(5F,6F).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops()));


	public static final DeferredBlock<Block>ZINC_SUPPORT=REGISTRY.register("zinc_support",()->new SupportBase(BlockBehaviour.Properties.of().strength(4F,5F).sound(SoundType.METAL).requiresCorrectToolForDrops()));
	public static final DeferredBlock<Block>BRASS_SUPPORT=REGISTRY.register("brass_support",()->new SupportBase(BlockBehaviour.Properties.of().strength(4F,5F).sound(SoundType.METAL).requiresCorrectToolForDrops()));
	public static final DeferredBlock<Block>COPPER_SUPPORT=REGISTRY.register("copper_support",()->new SupportBase(BlockBehaviour.Properties.of().strength(4F,5F).sound(SoundType.COPPER).requiresCorrectToolForDrops()));
	public static final DeferredBlock<Block>STEEL_SUPPORT=REGISTRY.register("steel_support",()->new SupportBase(BlockBehaviour.Properties.of().strength(4F,5F).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops()));

	public static final DeferredBlock<Block>COKE_OVEN=REGISTRY.register("coke_oven",()->new CokeOven(BlockBehaviour.Properties.of()));
	public static final DeferredBlock<Block>COKE_OVEN_CONTROLLER=REGISTRY.register("coke_oven_controller",()->new CokeOvenController(BlockBehaviour.Properties.of()));



	// ── Generátory ────────────────────────────────────────────────────────────

	/** Parní generátor – hřídel vystupuje nahoru a dolů (osa Y). */
	public static final DeferredBlock<Block> STEAM_GENERATOR = REGISTRY.register("steam_generator",() -> new SteamGeneratorBlock(BlockBehaviour.Properties.of().noOcclusion())
	);
}
