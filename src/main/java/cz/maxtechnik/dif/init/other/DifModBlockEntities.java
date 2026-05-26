package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.entity.*;
import cz.maxtechnik.dif.block.entity.barrel.AndesiteBarrelBlockEntity;
import cz.maxtechnik.dif.block.entity.barrel.BrassBarrelBlockEntity;
import cz.maxtechnik.dif.block.entity.barrel.CopperBarrelBlockEntity;
import cz.maxtechnik.dif.block.entity.SteamGeneratorBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DifModBlockEntities{
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,DifMod.MODID);

	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SuperBoxBlockEntity>> SUPER_BOX=register("super_box",DifModBlocks.SUPER_BOX,SuperBoxBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<AndesiteBarrelBlockEntity>> ANDESITE_BARREL=register("andesite_barrel",DifModBlocks.ANDESITE_BARREL,AndesiteBarrelBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<CopperBarrelBlockEntity>> COPPER_BARREL=register("copper_barrel",DifModBlocks.COPPER_BARREL,CopperBarrelBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<BrassBarrelBlockEntity>> BRASS_BARREL=register("brass_barrel",DifModBlocks.BRASS_BARREL,BrassBarrelBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<OldChestBlockEntity>> OLD_CHEST=register("old_chest",DifModBlocks.OLD_CHEST,OldChestBlockEntity::new);

	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<PortalBlockEntity>> PORTAL=REGISTRY.register("portal",()->BlockEntityType.Builder.of(PortalBlockEntity::new,DifModBlocks.PORTAL_BLOCK.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SpaceshipBlockEntity>> SPACESHIP=register("spaceship",DifModBlocks.SPACESHIP,SpaceshipBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ChunkLoaderBlockEntity>> CHUNK_LOADER_BE=REGISTRY.register("chunk_loader_be",()->BlockEntityType.Builder.of(ChunkLoaderBlockEntity::new,DifModBlocks.CHUNK_LOADER_1X1.get(),DifModBlocks.CHUNK_LOADER_3X3.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SpaceScaffoldingBlockEntity>> SPACE_SCAFFOLDING=REGISTRY.register("space_scaffolding",()->BlockEntityType.Builder.of(SpaceScaffoldingBlockEntity::new,DifModBlocks.SPACE_SCAFFOLDING.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<FryingTableBlockEntity>> FRYING_TABLE=REGISTRY.register("frying_table",()->BlockEntityType.Builder.of(FryingTableBlockEntity::new,DifModBlocks.FRYING_TABLE.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<CameraMonitorBlockEntity>> CAMERA_MONITOR=REGISTRY.register("monitor",()->BlockEntityType.Builder.of(CameraMonitorBlockEntity::new,DifModBlocks.CAMERA_MONITOR.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<CameraBlockEntity>> CAMERA=REGISTRY.register("camera",()->BlockEntityType.Builder.of(CameraBlockEntity::new,DifModBlocks.CAMERA.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<QuarryBlockEntity>> QUARRY=REGISTRY.register("quarry",()->BlockEntityType.Builder.of(QuarryBlockEntity::new,DifModBlocks.QUARRY.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<QuarryFrameBlockEntity>> QUARRY_FRAME=REGISTRY.register("quarry_frame",()->BlockEntityType.Builder.of(QuarryFrameBlockEntity::new,DifModBlocks.QUARRY_FRAME.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<QuarryLandmarkBlockEntity>> QUARRY_LANDMARK=REGISTRY.register("quarry_landmark",()->BlockEntityType.Builder.of(QuarryLandmarkBlockEntity::new,DifModBlocks.QUARRY_LANDMARK.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SleepingBagBlockEntity>> SLEEPING_BAG=REGISTRY.register("sleeping_bag",()->BlockEntityType.Builder.of(SleepingBagBlockEntity::new,DifModBlocks.SLEEPING_BAG.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SpaceCrateBlockEntity>> SPACE_CRATE=REGISTRY.register("space_crate",()->BlockEntityType.Builder.of(SpaceCrateBlockEntity::new,DifModBlocks.SPACE_CRATE.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<BurningGeneratorBlockEntity>> BURNING_GENERATOR=REGISTRY.register("burning_generator",()->BlockEntityType.Builder.of(BurningGeneratorBlockEntity::new,DifModBlocks.BURNING_GENERATOR.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<DistillationTankBlockEntity>>DISTILLATION_TANK=REGISTRY.register("distillation_tank",()->BlockEntityType.Builder.of((pos,state)->new DistillationTankBlockEntity(DifModBlockEntities.DISTILLATION_TANK.get(),pos,state), DifModBlocks.DISTILLATION_TANK.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SteamGeneratorBlockEntity>> STEAM_GENERATOR=register("steam_generator",DifModBlocks.STEAM_GENERATOR,SteamGeneratorBlockEntity::new);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CokeOvenBlockEntity>> COKE_OVEN = REGISTRY.register("coke_oven", () -> BlockEntityType.Builder.of(CokeOvenBlockEntity::new, DifModBlocks.COKE_OVEN_CONTROLLER.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CokeOvenPortBlockEntity>> COKE_OVEN_PORT = REGISTRY.register("coke_oven_port", () -> BlockEntityType.Builder.of(CokeOvenPortBlockEntity::new, DifModBlocks.COKE_OVEN_PORT.get()).build(null));


	private static <T extends net.minecraft.world.level.block.entity.BlockEntity> DeferredHolder<BlockEntityType<?>,BlockEntityType<T>> register(String name,Supplier<? extends Block> block,BlockEntityType.BlockEntitySupplier<T> supplier){
		return REGISTRY.register(name,()->BlockEntityType.Builder.of(supplier,block.get()).build(null));
	}
}