package cz.maxtechnik.dif.init;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DifModBlocks{
	public static final DeferredRegister<Block>REGISTRY=DeferredRegister.create(ForgeRegistries.BLOCKS,DifMod.MODID);
	public static final RegistryObject<Block>EXAMPLE_BLOCK=REGISTRY.register("example_block",Basic::new);

	public static final RegistryObject<Block>THE_DIFFERENTIAL=REGISTRY.register("the_differential",()->new CustomWaterloggedHorizontalRotation(SoundType.STONE,5F,6F,true));
	public static final RegistryObject<Block>EVENT_BUS=REGISTRY.register("event_bus",()->new CustomWaterloggedHorizontalRotation(SoundType.NETHERITE_BLOCK,5F,6F,true));
	public static final RegistryObject<Block>GENERATOR=REGISTRY.register("generator",()->new CustomWaterloggedHorizontalRotation(SoundType.STONE,5F,6F,true));
	public static final RegistryObject<Block>HOSPITAL_HANDLE=REGISTRY.register("hospital_handle",HospitalHandle::new);
	public static final RegistryObject<Block>SINGULARITATOR=REGISTRY.register("singularitator",()->new CustomWaterlogged(SoundType.METAL,5F,6F,true));

	public static final RegistryObject<Block>PEDROCK=REGISTRY.register("pedrock",()->new Custom(SoundType.STONE,1000F,999999999F,true));
	public static final RegistryObject<Block>ANDESITE_LATTICE=REGISTRY.register("andesite_lattice",AndesiteLattice::new);
	public static final RegistryObject<Block>ANDESITE_WINDOW=REGISTRY.register("andesite_window",AndesiteWindow::new);

	public static final RegistryObject<Block>DEEPSLATED_ARROW=REGISTRY.register("deepslated_arrow",()->new CustomHorizontalRotation(SoundType.DEEPSLATE,2.5F,16F,true));
	public static final RegistryObject<Block>STONED_ARROW=REGISTRY.register("stoned_arrow",()->new CustomHorizontalRotation(SoundType.STONE,1.5F,6F,true));
	public static final RegistryObject<Block>WOODED_ARROW=REGISTRY.register("wooded_arrow",()->new CustomHorizontalRotation(SoundType.WOOD,2F,3F,false));


	public static final RegistryObject<Block>MATA_PLANT=REGISTRY.register("mata_plant",MataPlant::new);
	public static final RegistryObject<Block>MATY_BLOCK=REGISTRY.register("maty_block",MatyBlock::new);
}
