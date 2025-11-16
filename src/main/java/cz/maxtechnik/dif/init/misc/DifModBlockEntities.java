package cz.maxtechnik.dif.init.misc;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.entity.*;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DifModBlockEntities{
	public static final DeferredRegister<BlockEntityType<?>>REGISTRY=DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,DifMod.MODID);
	public static final RegistryObject<BlockEntityType<?>>SUPER_BOX=register("super_box",DifModBlocks.SUPER_BOX,SuperBox::new);
	public static final RegistryObject<BlockEntityType<BurningGenerator>>BURNING_GENERATOR=REGISTRY.register("generator_be",()->BlockEntityType.Builder.of(BurningGenerator::new,DifModBlocks.BURNING_GENERATOR.get()).build(null));
	public static final RegistryObject<BlockEntityType<?>>SPECIAL_CRAFTING=register("special_crafting",DifModBlocks.EXAMPLE_BLOCK,SpecialCrafting::new);
	private static RegistryObject<BlockEntityType<?>>register(String registryname,RegistryObject<Block> block,BlockEntityType.BlockEntitySupplier<?>supplier){
		return REGISTRY.register(registryname,()->BlockEntityType.Builder.of(supplier,block.get()).build(null));
	}
}
