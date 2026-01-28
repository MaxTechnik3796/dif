package cz.maxtechnik.dif.init.other;

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
	public static final RegistryObject<BlockEntityType<?>>ANDESITE_BARREL=register("andesite_barrel",DifModBlocks.ANDESITE_BARREL,AndesiteBarrel::new);
	public static final RegistryObject<BlockEntityType<?>>COPPER_BARREL=register("copper_barrel",DifModBlocks.COPPER_BARREL,CopperBarrel::new);
	public static final RegistryObject<BlockEntityType<?>>BRASS_BARREL=register("brass_barrel",DifModBlocks.BRASS_BARREL,BrassBarrel::new);
	public static final RegistryObject<BlockEntityType<?>>OLD_CHEST=register("old_chest",DifModBlocks.OLD_CHEST,OldChest::new);
	public static final RegistryObject<BlockEntityType<?>> PORTAL = register("portal", DifModBlocks.PORTAL_BLOCK, PortalBlockEntity::new);

	public static final RegistryObject<BlockEntityType<?>>XP_STORAGE=REGISTRY.register("xp_storage",()->BlockEntityType.Builder.of(XpStorageBlockEntity::new,DifModBlocks.XP_STORAGE.get()).build(null));

	public static final RegistryObject<BlockEntityType<BurningGenerator>> BURNING_GENERATOR=REGISTRY.register("generator_be",()->BlockEntityType.Builder.of(BurningGenerator::new,DifModBlocks.BURNING_GENERATOR.get()).build(null));
	public static final RegistryObject<BlockEntityType<?>>SPECIAL_CRAFTING=register("special_crafting",DifModBlocks.XP_STORAGE,SpecialCrafting::new);
	private static RegistryObject<BlockEntityType<?>>register(String registryname,RegistryObject<Block> block,BlockEntityType.BlockEntitySupplier<?> supplier){
		return REGISTRY.register(registryname,()->BlockEntityType.Builder.of(supplier,block.get()).build(null));
	}
}
