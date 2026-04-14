package cz.maxtechnik.dif.init.basic;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.*;
import cz.maxtechnik.dif.block.barrel.AndesiteBarrel;
import cz.maxtechnik.dif.block.barrel.BrassBarrel;
import cz.maxtechnik.dif.block.barrel.CopperBarrel;
import cz.maxtechnik.dif.block.dev.Test;
import cz.maxtechnik.dif.block.dev.XpStorage;
import cz.maxtechnik.dif.block.mata.MataPlant;
import cz.maxtechnik.dif.block.mata.MatyBlock;
import cz.maxtechnik.dif.block.rails.FastPoweredRailBlock;
import cz.maxtechnik.dif.block.rails.FastRailBlock;
import cz.maxtechnik.dif.block.space.*;
import cz.maxtechnik.dif.block.template.*;
import cz.maxtechnik.dif.fluid.block.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static net.minecraft.world.level.block.Blocks.DEEPSLATE;
public class DifModBlocks{
	public static final DeferredRegister<Block>REGISTRY=DeferredRegister.create(ForgeRegistries.BLOCKS,DifMod.MODID);

	public static final RegistryObject<Block> CHUNK_LOADER_1X1 = REGISTRY.register("chunk_loader_1x1",ChunkLoader::new);
	public static final RegistryObject<Block> CHUNK_LOADER_3X3 = REGISTRY.register("chunk_loader_3x3",ChunkLoader::new);
	public static final RegistryObject<Block>FAST_POWERED_RAIL=REGISTRY.register("fast_powered_rail",()->new FastPoweredRailBlock(BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL)));
	public static final RegistryObject<Block>FAST_RAIL=REGISTRY.register("fast_rail",()->new FastRailBlock(BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL)));
	public static final RegistryObject<Block>EXAMPLE_BLOCK=REGISTRY.register("example_block",Test::new);
	public static final RegistryObject<Block>REMOTE_MINECART_BLOCK=REGISTRY.register("remote_minecart_block",RemoteMinecartBlock::new);

	public static final RegistryObject<Block>SLEEPING_BAG=REGISTRY.register("sleeping_bag",SleepingBagBlock::new);
	public static final RegistryObject<Block>QUARRY=REGISTRY.register("quarry",()->new Quarry(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(5F,6F).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block>QUARRY_FRAME=REGISTRY.register("quarry_frame",QuarryFrame::new);

	public static final RegistryObject<Block>CAMERA_MONITOR=REGISTRY.register("camera_monitor",()->new CameraMonitor(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(5F,6F).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block>CAMERA=REGISTRY.register("camera",()->new Camera(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(3F,3F).requiresCorrectToolForDrops()));

	public static final RegistryObject<Block>FUEL_FLUID=REGISTRY.register("fuel_fluid",FuelBlock::new);
	public static final RegistryObject<Block>BEER_FLUID=REGISTRY.register("beer_fluid",BeerBlock::new);
	public static final RegistryObject<Block>XP_FLUID=REGISTRY.register("xp_fluid",XpBlock::new);
	public static final RegistryObject<Block>CIDER_FLUID=REGISTRY.register("cider_fluid",CiderBlock::new);
	public static final RegistryObject<Block>JETPACK_FUEL_FLUID=REGISTRY.register("jetpack_fuel_fluid",JetpackFuelBlock::new);
	public static final RegistryObject<Block>JETPACK_TURBO_FUEL_FLUID=REGISTRY.register("jetpack_turbo_fuel_fluid",JetpackTurboFuelBlock::new);
	public static final RegistryObject<Block>SUNFLOWER_OIL_FLUID=REGISTRY.register("sunflower_oil_fluid",SunflowerOilBlock::new);

	public static final RegistryObject<Block> MEGA_TORCH = REGISTRY.register("mega_torch", () -> new MegaTorch(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2F,3F).sound(SoundType.WOOD).lightLevel(state->15)));

	public static final RegistryObject<Block>BEER=REGISTRY.register("beer",Beer::new);
	public static final RegistryObject<Block>THE_DIFFERENTIAL=REGISTRY.register("the_differential",()->new CustomWaterloggedHorizontalRotation(SoundType.STONE,5F,6F,true));
	public static final RegistryObject<Block>WASHING_MACHINE=REGISTRY.register("washing_machine",WashingMachine::new);
	public static final RegistryObject<Block>AIR_CONDITIONING=REGISTRY.register("air_conditioning",()->new CustomFullRotation(SoundType.STONE,5F,6F,true));
	public static final RegistryObject<Block>EVENT_BUS=REGISTRY.register("event_bus",()->new CustomWaterloggedHorizontalRotation(SoundType.NETHERITE_BLOCK,5F,6F,true));
	public static final RegistryObject<Block>VENT=REGISTRY.register("vent",()->new CustomWaterloggedHorizontalRotation(SoundType.NETHERITE_BLOCK,5F,6F,true));
	public static final RegistryObject<Block>BURNING_GENERATOR=REGISTRY.register("burning_generator",BurningGenerator::new);
	public static final RegistryObject<Block>FLUID_HATCH=REGISTRY.register("fluid_hatch",FluidHatch::new);
	public static final RegistryObject<Block>SINGULARITATOR=REGISTRY.register("singularitator",()->new CustomWaterlogged(SoundType.METAL,5F,6F,true));
	public static final RegistryObject<Block>SUPER_BOX=REGISTRY.register("super_box",SuperBox::new);
	public static final RegistryObject<Block>OLD_CHEST=REGISTRY.register("old_chest",OldChest::new);

	public static final RegistryObject<Block>SOLAR_PANEL_00=REGISTRY.register("solar_panel_00",SolarPanel::new);
	public static final RegistryObject<Block>SOLAR_PANEL_01=REGISTRY.register("solar_panel_01",SolarPanel::new);
	public static final RegistryObject<Block>SOLAR_PANEL_02=REGISTRY.register("solar_panel_02",SolarPanel::new);
	public static final RegistryObject<Block>SOLAR_PANEL_03=REGISTRY.register("solar_panel_03",SolarPanel::new);
	public static final RegistryObject<Block>SOLAR_PANEL_04=REGISTRY.register("solar_panel_04",SolarPanel::new);

	public static final RegistryObject<Block>SOLAR_PANEL_00_W=REGISTRY.register("solar_panel_00_w",SolarPanel::new);
	public static final RegistryObject<Block>SOLAR_PANEL_01_W=REGISTRY.register("solar_panel_01_w",SolarPanel::new);
	public static final RegistryObject<Block>SOLAR_PANEL_02_W=REGISTRY.register("solar_panel_02_w",SolarPanel::new);
	public static final RegistryObject<Block>SOLAR_PANEL_03_W=REGISTRY.register("solar_panel_03_w",SolarPanel::new);
	public static final RegistryObject<Block>SOLAR_PANEL_04_W=REGISTRY.register("solar_panel_04_w",SolarPanel::new);

	public static final RegistryObject<Block>SOLANA_BLOCK=REGISTRY.register("solana_block",Crypto::new);
	public static final RegistryObject<Block>BITCOIN_BLOCK=REGISTRY.register("bitcoin_block", Crypto::new);
	public static final RegistryObject<Block>CINDER_FLOUR_BLOCK=REGISTRY.register("cinder_flour_block",()->new Custom(SoundType.WART_BLOCK,0.4F,0.6F,false));
	public static final RegistryObject<Block>PEDROCK=REGISTRY.register("pedrock",()->new Custom(SoundType.STONE,1000F,999999999F,true));
	public static final RegistryObject<Block>ANDESITE_LATTICE=REGISTRY.register("andesite_lattice",AndesiteLattice::new);
	public static final RegistryObject<Block>ANDESITE_WINDOW=REGISTRY.register("andesite_window",AndesiteWindow::new);
	public static final RegistryObject<Block>SMOOTH_STONE_DOUBLE_SLAB=REGISTRY.register("smooth_stone_double_slab",()->new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2F,6F)));
	public static final RegistryObject<Block>IRON_BARS_BLOCK=REGISTRY.register("iron_bars_block",()->new CustomWaterlogged(SoundType.METAL,5F,6F,true));
	public static final RegistryObject<Block>GLITCH_BLOCK=REGISTRY.register("glitch_block",()->new Custom(SoundType.STONE,1.8F,3F,true));
	public static final RegistryObject<Block>TREE_BARK_BLOCK=REGISTRY.register("tree_bark_block",()->new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.8F,3F).sound(SoundType.MANGROVE_ROOTS).ignitedByLava()));

	public static final RegistryObject<Block>DEEPSLATED_ARROW=REGISTRY.register("deepslated_arrow",()->new CustomHorizontalRotation(SoundType.DEEPSLATE,2.5F,16F,true));
	public static final RegistryObject<Block>STONED_ARROW=REGISTRY.register("stoned_arrow",()->new CustomHorizontalRotation(SoundType.STONE,1.5F,6F,true));
	public static final RegistryObject<Block>WOODED_ARROW=REGISTRY.register("wooded_arrow",()->new CustomHorizontalRotation(SoundType.WOOD,2F,3F,false));

	public static final RegistryObject<Block>BAUXITE_ORE=REGISTRY.register("bauxite_ore",()->new Custom(SoundType.STONE,3F,3F,true));
	public static final RegistryObject<Block>DEEPSLATE_BAUXITE_ORE=REGISTRY.register("deepslate_bauxite_ore",()->new Custom(SoundType.DEEPSLATE,4.5F,3F,true));
	public static final RegistryObject<Block>ALUMINUM_BLOCK=REGISTRY.register("aluminum_block",()->new Custom(SoundType.METAL,5F,6F,true));
	public static final RegistryObject<Block>ALUMINUM_PROFILE=REGISTRY.register("aluminum_profile",AluminumProfile::new);

	public static final RegistryObject<Block>CANOLA_PLANT=REGISTRY.register("canola_plant",CanolaPlant::new);
	public static final RegistryObject<Block>MATA_PLANT=REGISTRY.register("mata_plant",MataPlant::new);
	public static final RegistryObject<Block>MATY_BLOCK=REGISTRY.register("maty_block",MatyBlock::new);
	public static final RegistryObject<Block>RUBY_ORE=REGISTRY.register("ruby_ore",()->new Custom(SoundType.STONE,3F,3F,true));
	public static final RegistryObject<Block>RUBY_BLOCK=REGISTRY.register("ruby_block",()->new Custom(SoundType.STONE,5F,6F,true));

	public static final RegistryObject<Block>ENERGY_BLOCK=REGISTRY.register("energy_block",()->new AmethystBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(1.5F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block>BUDDING_ENERGY=REGISTRY.register("budding_energy",()->new BuddingEnergyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).randomTicks().strength(1.5F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops().pushReaction(PushReaction.DESTROY)));
	public static final RegistryObject<Block>ENERGY_CLUSTER=REGISTRY.register("energy_cluster",()->new AmethystClusterBlock(7,3,BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).forceSolidOn().noOcclusion().randomTicks().sound(SoundType.AMETHYST_CLUSTER).strength(1.5F).lightLevel((p_152632_)->5).pushReaction(PushReaction.DESTROY)));
	public static final RegistryObject<Block>LARGE_ENERGY_BUD=REGISTRY.register("large_energy_bud",()->new AmethystClusterBlock(5,3,BlockBehaviour.Properties.copy(ENERGY_CLUSTER.get()).sound(SoundType.MEDIUM_AMETHYST_BUD).forceSolidOn().lightLevel((p_152629_)->4).pushReaction(PushReaction.DESTROY)));
	public static final RegistryObject<Block>MEDIUM_ENERGY_BUD=REGISTRY.register("medium_energy_bud",()->new AmethystClusterBlock(4,3,BlockBehaviour.Properties.copy(ENERGY_CLUSTER.get()).sound(SoundType.LARGE_AMETHYST_BUD).forceSolidOn().lightLevel((p_152617_)->2).pushReaction(PushReaction.DESTROY)));
	public static final RegistryObject<Block>SMALL_ENERGY_BUD=REGISTRY.register("small_energy_bud",()->new AmethystClusterBlock(3,4,BlockBehaviour.Properties.copy(ENERGY_CLUSTER.get()).sound(SoundType.SMALL_AMETHYST_BUD).forceSolidOn().lightLevel((p_187409_)->1).pushReaction(PushReaction.DESTROY)));

	public static final RegistryObject<Block>ANDESITE_BARREL=REGISTRY.register("andesite_barrel",AndesiteBarrel::new);
	public static final RegistryObject<Block>COPPER_BARREL=REGISTRY.register("copper_barrel",CopperBarrel::new);
	public static final RegistryObject<Block>BRASS_BARREL=REGISTRY.register("brass_barrel",BrassBarrel::new);

	public static final RegistryObject<Block>XP_STORAGE=REGISTRY.register("xp_storage",XpStorage::new);
	public static final RegistryObject<Block>PORTAL_BLOCK=REGISTRY.register("portal_block",()->new PortalBlock(BlockBehaviour.Properties.of().noCollission().noOcclusion().pushReaction(PushReaction.BLOCK)));
	public static final RegistryObject<Block>FRYING_TABLE=REGISTRY.register("frying_table",FryingTable::new);

	static BlockBehaviour.Properties c_cobblestone_props=BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F);
	public static final RegistryObject<Block>C1_COBBLESTONE=REGISTRY.register("c1_cobblestone",()->new Block(c_cobblestone_props));
	public static final RegistryObject<Block>C2_COBBLESTONE=REGISTRY.register("c2_cobblestone",()->new Block(c_cobblestone_props));
	public static final RegistryObject<Block>C3_COBBLESTONE=REGISTRY.register("c3_cobblestone",()->new Block(c_cobblestone_props));
	public static final RegistryObject<Block>C4_COBBLESTONE=REGISTRY.register("c4_cobblestone",()->new Block(c_cobblestone_props));
	public static final RegistryObject<Block>C5_COBBLESTONE=REGISTRY.register("c5_cobblestone",()->new Block(c_cobblestone_props));
	public static final RegistryObject<Block>C6_COBBLESTONE=REGISTRY.register("c6_cobblestone",()->new Block(c_cobblestone_props));
	public static final RegistryObject<Block>C7_COBBLESTONE=REGISTRY.register("c7_cobblestone",()->new Block(c_cobblestone_props));
	public static final RegistryObject<Block>C8_COBBLESTONE=REGISTRY.register("c8_cobblestone",()->new Block(c_cobblestone_props));
	public static final RegistryObject<Block>C9_COBBLESTONE=REGISTRY.register("c9_cobblestone",()->new Block(c_cobblestone_props));

	static BlockBehaviour.Properties c_dirt_props=BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL);
	public static final RegistryObject<Block>C1_DIRT=REGISTRY.register("c1_dirt",()->new Block(c_dirt_props));
	public static final RegistryObject<Block>C2_DIRT=REGISTRY.register("c2_dirt",()->new Block(c_dirt_props));
	public static final RegistryObject<Block>C3_DIRT=REGISTRY.register("c3_dirt",()->new Block(c_dirt_props));
	public static final RegistryObject<Block>C4_DIRT=REGISTRY.register("c4_dirt",()->new Block(c_dirt_props));
	public static final RegistryObject<Block>C5_DIRT=REGISTRY.register("c5_dirt",()->new Block(c_dirt_props));
	public static final RegistryObject<Block>C6_DIRT=REGISTRY.register("c6_dirt",()->new Block(c_dirt_props));
	public static final RegistryObject<Block>C7_DIRT=REGISTRY.register("c7_dirt",()->new Block(c_dirt_props));
	public static final RegistryObject<Block>C8_DIRT=REGISTRY.register("c8_dirt",()->new Block(c_dirt_props));
	public static final RegistryObject<Block>C9_DIRT=REGISTRY.register("c9_dirt",()->new Block(c_dirt_props));


	static BlockBehaviour.Properties c_gravel_props=BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.SNARE).strength(0.6F).sound(SoundType.GRAVEL);
	public static final RegistryObject<Block>C1_GRAVEL=REGISTRY.register("c1_gravel",()->new Block(c_gravel_props));
	public static final RegistryObject<Block>C2_GRAVEL=REGISTRY.register("c2_gravel",()->new Block(c_gravel_props));
	public static final RegistryObject<Block>C3_GRAVEL=REGISTRY.register("c3_gravel",()->new Block(c_gravel_props));
	public static final RegistryObject<Block>C4_GRAVEL=REGISTRY.register("c4_gravel",()->new Block(c_gravel_props));
	public static final RegistryObject<Block>C5_GRAVEL=REGISTRY.register("c5_gravel",()->new Block(c_gravel_props));
	public static final RegistryObject<Block>C6_GRAVEL=REGISTRY.register("c6_gravel",()->new Block(c_gravel_props));
	public static final RegistryObject<Block>C7_GRAVEL=REGISTRY.register("c7_gravel",()->new Block(c_gravel_props));
	public static final RegistryObject<Block>C8_GRAVEL=REGISTRY.register("c8_gravel",()->new Block(c_gravel_props));
	public static final RegistryObject<Block>C9_GRAVEL=REGISTRY.register("c9_gravel",()->new Block(c_gravel_props));


	static BlockBehaviour.Properties c_deepslate_props=BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.DEEPSLATE);
	public static final RegistryObject<Block>C1_DEEPSLATE=REGISTRY.register("c1_deepslate",()->new RotatedPillarBlock(c_deepslate_props));
	public static final RegistryObject<Block>C2_DEEPSLATE=REGISTRY.register("c2_deepslate",()->new RotatedPillarBlock(c_deepslate_props));
	public static final RegistryObject<Block>C3_DEEPSLATE=REGISTRY.register("c3_deepslate",()->new RotatedPillarBlock(c_deepslate_props));
	public static final RegistryObject<Block>C4_DEEPSLATE=REGISTRY.register("c4_deepslate",()->new RotatedPillarBlock(c_deepslate_props));
	public static final RegistryObject<Block>C5_DEEPSLATE=REGISTRY.register("c5_deepslate",()->new RotatedPillarBlock(c_deepslate_props));
	public static final RegistryObject<Block>C6_DEEPSLATE=REGISTRY.register("c6_deepslate",()->new RotatedPillarBlock(c_deepslate_props));
	public static final RegistryObject<Block>C7_DEEPSLATE=REGISTRY.register("c7_deepslate",()->new RotatedPillarBlock(c_deepslate_props));
	public static final RegistryObject<Block>C8_DEEPSLATE=REGISTRY.register("c8_deepslate",()->new RotatedPillarBlock(c_deepslate_props));
	public static final RegistryObject<Block>C9_DEEPSLATE=REGISTRY.register("c9_deepslate",()->new RotatedPillarBlock(c_deepslate_props));


	static BlockBehaviour.Properties c_cobbled_deepslate_props=BlockBehaviour.Properties.copy(DEEPSLATE).strength(3.5F, 6.0F);
	public static final RegistryObject<Block>C1_COBBLED_DEEPSLATE=REGISTRY.register("c1_cobbled_deepslate",()->new Block(c_cobbled_deepslate_props));
	public static final RegistryObject<Block>C2_COBBLED_DEEPSLATE=REGISTRY.register("c2_cobbled_deepslate",()->new Block(c_cobbled_deepslate_props));
	public static final RegistryObject<Block>C3_COBBLED_DEEPSLATE=REGISTRY.register("c3_cobbled_deepslate",()->new Block(c_cobbled_deepslate_props));
	public static final RegistryObject<Block>C4_COBBLED_DEEPSLATE=REGISTRY.register("c4_cobbled_deepslate",()->new Block(c_cobbled_deepslate_props));
	public static final RegistryObject<Block>C5_COBBLED_DEEPSLATE=REGISTRY.register("c5_cobbled_deepslate",()->new Block(c_cobbled_deepslate_props));
	public static final RegistryObject<Block>C6_COBBLED_DEEPSLATE=REGISTRY.register("c6_cobbled_deepslate",()->new Block(c_cobbled_deepslate_props));
	public static final RegistryObject<Block>C7_COBBLED_DEEPSLATE=REGISTRY.register("c7_cobbled_deepslate",()->new Block(c_cobbled_deepslate_props));
	public static final RegistryObject<Block>C8_COBBLED_DEEPSLATE=REGISTRY.register("c8_cobbled_deepslate",()->new Block(c_cobbled_deepslate_props));
	public static final RegistryObject<Block>C9_COBBLED_DEEPSLATE=REGISTRY.register("c9_cobbled_deepslate",()->new Block(c_cobbled_deepslate_props));


	public static final RegistryObject<Block>AURORA_CASING=REGISTRY.register("aurora_casing",()->new Custom(SoundType.NETHERITE_BLOCK,4F,4F,true));
	public static final RegistryObject<Block>SPACESHIP=REGISTRY.register("spaceship",Spaceship::new);
	public static final RegistryObject<Block>SPACESHIP_GHOST_BLOCK=REGISTRY.register("spaceship_ghost_block",SpaceshipGhostBlock::new);
	public static final RegistryObject<Block>SPACE_ENGINE=REGISTRY.register("space_engine",SpaceEngine::new);


	public static final RegistryObject<Block>SPACE_SCAFFOLDING=REGISTRY.register("space_scaffolding",SpaceScaffolding::new);
	public static final RegistryObject<Block>SPACE_CASING=REGISTRY.register("space_casing",()->new Custom(SoundType.NETHERITE_BLOCK,5F,6F,true));
	public static final RegistryObject<Block>SPACE_CASING_REINFORCED=REGISTRY.register("space_casing_reinforced",()->new Custom(SoundType.NETHERITE_BLOCK,5F,6F,true));
	public static final RegistryObject<Block>SPACE_CASING_METAL=REGISTRY.register("space_casing_metal",()->new Custom(SoundType.NETHERITE_BLOCK,5F,6F,true));
	public static final RegistryObject<Block>SPACE_DOOR=REGISTRY.register("space_door",SpaceDoor::new);
	public static final RegistryObject<Block>SPACE_CORRIDOR=REGISTRY.register("space_corridor",()->new Custom(SoundType.GLASS,5F,6F,true));
	public static final RegistryObject<Block>SPACE_CRATE=REGISTRY.register("space_crate",()->new BarrelBlock(BlockBehaviour.Properties.of().strength(5F,6F).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block>SOLAR_PANEL_BLOCK=REGISTRY.register("solar_panel_block",()->new Custom(SoundType.STONE,5F,6F,true));

	public static final RegistryObject<Block>MOON_STONE=REGISTRY.register("moon_stone",()->new Custom(SoundType.STONE,3F,4F,true));
	public static final RegistryObject<Block>MARS_STONE=REGISTRY.register("mars_stone",()->new Custom(SoundType.STONE,3F,4F,true));

	public static final RegistryObject<Block>BROKEN_TRACK00=REGISTRY.register("broken_track00",BrokenTrack::new);
	public static final RegistryObject<Block>BROKEN_TRACK01=REGISTRY.register("broken_track01",BrokenTrack::new);
	public static final RegistryObject<Block>BROKEN_TRACK02=REGISTRY.register("broken_track02",BrokenTrack::new);
	
	public static final RegistryObject<Block>LAP_TIMER=REGISTRY.register("lap_timer",()->new Block(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHERITE_BLOCK)));
}
