package cz.maxtechnik.dif.init;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.entity.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DifModBlockEntities{
	public static final DeferredRegister<BlockEntityType<?>>REGISTRY=DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,DifMod.MODID);
	public static final RegistryObject<BlockEntityType<?>>SUPER_BOX=register("super_box",DifModBlocks.SUPER_BOX,SuperBox::new);

	private static RegistryObject<BlockEntityType<?>>register(String registryname,RegistryObject<Block> block,BlockEntityType.BlockEntitySupplier<?>supplier){
		return REGISTRY.register(registryname,()->BlockEntityType.Builder.of(supplier,block.get()).build(null));
	}
}
