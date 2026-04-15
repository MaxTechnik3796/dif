package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.entity.*;
import cz.maxtechnik.dif.block.entity.barrel.AndesiteBarrelBlockEntity;
import cz.maxtechnik.dif.block.entity.barrel.BrassBarrelBlockEntity;
import cz.maxtechnik.dif.block.entity.barrel.CopperBarrelBlockEntity;
import cz.maxtechnik.dif.block.entity.dev.SpecialCraftingBlockEntity;
import cz.maxtechnik.dif.block.entity.dev.XpStorageBlockEntity;
import cz.maxtechnik.dif.block.industrial.entity.*;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
public class DifModBlockEntities{
	public static final DeferredRegister<BlockEntityType<?>>REGISTRY=DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,DifMod.MODID);
	public static final RegistryObject<BlockEntityType<?>>SUPER_BOX=register("super_box",DifModBlocks.SUPER_BOX,SuperBoxBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>>ANDESITE_BARREL=register("andesite_barrel",DifModBlocks.ANDESITE_BARREL,AndesiteBarrelBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>>COPPER_BARREL=register("copper_barrel",DifModBlocks.COPPER_BARREL,CopperBarrelBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>>BRASS_BARREL=register("brass_barrel",DifModBlocks.BRASS_BARREL,BrassBarrelBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>>OLD_CHEST=register("old_chest",DifModBlocks.OLD_CHEST,OldChestBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>>PORTAL=register("portal",DifModBlocks.PORTAL_BLOCK,PortalBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>>SPACESHIP=register("spaceship",DifModBlocks.SPACESHIP,SpaceshipBlockEntity::new);
	public static final RegistryObject<BlockEntityType<ChunkLoaderBlockEntity>> CHUNK_LOADER_BE = REGISTRY.register("chunk_loader_be", () -> BlockEntityType.Builder.of(ChunkLoaderBlockEntity::new, DifModBlocks.CHUNK_LOADER_1X1.get(), DifModBlocks.CHUNK_LOADER_3X3.get()).build(null));

	public static final RegistryObject<BlockEntityType<?>>SPACE_SCAFFOLDING=REGISTRY.register("space_scaffolding",()->BlockEntityType.Builder.of(SpaceScaffoldingBlockEntity::new,DifModBlocks.SPACE_CASING.get()).build(null));
	public static final RegistryObject<BlockEntityType<?>>XP_STORAGE=REGISTRY.register("xp_storage",()->BlockEntityType.Builder.of(XpStorageBlockEntity::new,DifModBlocks.XP_STORAGE.get()).build(null));
	public static final RegistryObject<BlockEntityType<FryingTableBlockEntity>>FRYING_TABLE=REGISTRY.register("frying_table",()->BlockEntityType.Builder.of(FryingTableBlockEntity::new,DifModBlocks.FRYING_TABLE.get()).build(null));

	public static final RegistryObject<BlockEntityType<CameraMonitorBlockEntity>>CAMERA_MONITOR=REGISTRY.register("monitor",()->BlockEntityType.Builder.of(CameraMonitorBlockEntity::new,DifModBlocks.CAMERA_MONITOR.get()).build(null));
	public static final RegistryObject<BlockEntityType<CameraBlockEntity>>CAMERA=REGISTRY.register("camera",()->BlockEntityType.Builder.of(CameraBlockEntity::new,DifModBlocks.CAMERA.get()).build(null));
	public static final RegistryObject<BlockEntityType<QuarryBlockEntity>>QUARRY=REGISTRY.register("quarry",()->BlockEntityType.Builder.of(QuarryBlockEntity::new,DifModBlocks.QUARRY.get()).build(null));

	// V souboru DifModBlockEntities.java
	public static final RegistryObject<BlockEntityType<ReinforcedShaftBlockEntity>> REINFORCED_SHAFT = 	REGISTRY.register("reinforced_shaft", () -> BlockEntityType.Builder.of(ReinforcedShaftBlockEntity::new, DifModBlocks.REINFORCED_SHAFT.get()).build(null));

	public static final RegistryObject<BlockEntityType<BurningGeneratorBlockEntity>> BURNING_GENERATOR=REGISTRY.register("burning_generator",()->BlockEntityType.Builder.of(BurningGeneratorBlockEntity::new,DifModBlocks.BURNING_GENERATOR.get()).build(null));
	public static final RegistryObject<BlockEntityType<?>>SPECIAL_CRAFTING=REGISTRY.register("special_crafting",()->BlockEntityType.Builder.of(SpecialCraftingBlockEntity::new,DifModBlocks.XP_STORAGE.get()).build(null));

	private static RegistryObject<BlockEntityType<?>>register(String registryname,RegistryObject<Block> block,BlockEntityType.BlockEntitySupplier<?> supplier){
		return REGISTRY.register(registryname,()->BlockEntityType.Builder.of(supplier,block.get()).build(null));
	}
}
