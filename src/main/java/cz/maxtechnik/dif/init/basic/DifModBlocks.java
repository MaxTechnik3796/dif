package cz.maxtechnik.dif.init.basic;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.*;
import cz.maxtechnik.dif.block.CopperChestBlock;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DifModBlocks{
	public static final DeferredRegister<Block>REGISTRY=DeferredRegister.create(ForgeRegistries.BLOCKS,DifMod.MODID);
		public static final RegistryObject<Block> COPPER_CHEST = REGISTRY.register("copper_chest", CopperChestBlock::new);
	public static final RegistryObject<Block>EXAMPLE_BLOCK=REGISTRY.register("example_block",Test::new);

	public static final RegistryObject<Block>THE_DIFFERENTIAL=REGISTRY.register("the_differential",()->new CustomWaterloggedHorizontalRotation(SoundType.STONE,5F,6F,true));
	public static final RegistryObject<Block>EVENT_BUS=REGISTRY.register("event_bus",()->new CustomWaterloggedHorizontalRotation(SoundType.NETHERITE_BLOCK,5F,6F,true));
	public static final RegistryObject<Block>BURNING_GENERATOR=REGISTRY.register("burning_generator",BurningGenerator::new);
	public static final RegistryObject<Block>HOSPITAL_HANDLE=REGISTRY.register("hospital_handle",HospitalHandle::new);
	public static final RegistryObject<Block>SINGULARITATOR=REGISTRY.register("singularitator",()->new CustomWaterlogged(SoundType.METAL,5F,6F,true));
	public static final RegistryObject<Block>SUPER_BOX=REGISTRY.register("super_box",SuperBox::new);

	public static final RegistryObject<Block>SOLAR_PANEL_00=REGISTRY.register("solar_panel_00",SolarPanel::new);
	public static final RegistryObject<Block>SOLAR_PANEL_01=REGISTRY.register("solar_panel_01",SolarPanel::new);
	public static final RegistryObject<Block>SOLAR_PANEL_02=REGISTRY.register("solar_panel_02",SolarPanel::new);
	public static final RegistryObject<Block>SOLAR_PANEL_03=REGISTRY.register("solar_panel_03",SolarPanel::new);
	public static final RegistryObject<Block>SOLAR_PANEL_04=REGISTRY.register("solar_panel_04",SolarPanel::new);

	public static final RegistryObject<Block>SOLANA_BLOCK=REGISTRY.register("solana_block",Crypto::new);
	public static final RegistryObject<Block>BITCOIN_BLOCK=REGISTRY.register("bitcoin_block", Crypto::new);
	public static final RegistryObject<Block>CINDER_FLOUR_BLOCK=REGISTRY.register("cinder_flour_block",()->new Custom(SoundType.WART_BLOCK,0.4F,0.6F,false));
	public static final RegistryObject<Block>PEDROCK=REGISTRY.register("pedrock",()->new Custom(SoundType.STONE,1000F,999999999F,true));
	public static final RegistryObject<Block>ANDESITE_LATTICE=REGISTRY.register("andesite_lattice",AndesiteLattice::new);
	public static final RegistryObject<Block>ANDESITE_WINDOW=REGISTRY.register("andesite_window",AndesiteWindow::new);

	public static final RegistryObject<Block>DEEPSLATED_ARROW=REGISTRY.register("deepslated_arrow",()->new CustomHorizontalRotation(SoundType.DEEPSLATE,2.5F,16F,true));
	public static final RegistryObject<Block>STONED_ARROW=REGISTRY.register("stoned_arrow",()->new CustomHorizontalRotation(SoundType.STONE,1.5F,6F,true));
	public static final RegistryObject<Block>WOODED_ARROW=REGISTRY.register("wooded_arrow",()->new CustomHorizontalRotation(SoundType.WOOD,2F,3F,false));

	public static final RegistryObject<Block>BAUXITE_ORE=REGISTRY.register("bauxite_ore",()->new Custom(SoundType.STONE,3F,3F,true));
	public static final RegistryObject<Block>DEEPSLATE_BAUXITE_ORE=REGISTRY.register("deepslate_bauxite_ore",()->new Custom(SoundType.DEEPSLATE,4.5F,3F,true));
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


}
