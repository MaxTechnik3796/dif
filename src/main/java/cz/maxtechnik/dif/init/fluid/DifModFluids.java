package cz.maxtechnik.dif.init.fluid;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.fluid.*;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Supplier;

@SuppressWarnings({"removal","unused"})
public class DifModFluids {
	public static final DeferredRegister<Fluid> REGISTRY = DeferredRegister.create(Registries.FLUID, DifMod.MODID);

	// BEER
	public static final DeferredHolder<Fluid, FlowingFluid> BEER = REGISTRY.register("beer_fluid",
			() -> new BaseFluidBlocks.Source(createProperties(DifModFluidTypes.BEER_TYPE, fluidSupplier("beer_fluid"), fluidSupplier("flowing_beer_fluid"), DifModItems.BEER_BUCKET, () -> (LiquidBlock) DifModBlocks.BEER_FLUID.get(), Beer.TICK_RATE)));
	public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_BEER = REGISTRY.register("flowing_beer_fluid",
			() -> new BaseFluidBlocks.Flowing(createProperties(DifModFluidTypes.BEER_TYPE, fluidSupplier("beer_fluid"), fluidSupplier("flowing_beer_fluid"), DifModItems.BEER_BUCKET, () -> (LiquidBlock) DifModBlocks.BEER_FLUID.get(), Beer.TICK_RATE)));

	// FUEL
	public static final DeferredHolder<Fluid, FlowingFluid> FUEL = REGISTRY.register("fuel_fluid",
			() -> new BaseFluidBlocks.Source(createProperties(DifModFluidTypes.FUEL_TYPE, fluidSupplier("fuel_fluid"), fluidSupplier("flowing_fuel_fluid"), DifModItems.FUEL_BUCKET, () -> (LiquidBlock) DifModBlocks.FUEL_FLUID.get(), Fuel.TICK_RATE)));
	public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_FUEL = REGISTRY.register("flowing_fuel_fluid",
			() -> new BaseFluidBlocks.Flowing(createProperties(DifModFluidTypes.FUEL_TYPE, fluidSupplier("fuel_fluid"), fluidSupplier("flowing_fuel_fluid"), DifModItems.FUEL_BUCKET, () -> (LiquidBlock) DifModBlocks.FUEL_FLUID.get(), Fuel.TICK_RATE)));

	// JETPACK FUEL
	public static final DeferredHolder<Fluid, FlowingFluid> JETPACK_FUEL = REGISTRY.register("jetpack_fuel_fluid",
			() -> new BaseFluidBlocks.Source(createProperties(DifModFluidTypes.JETPACK_FUEL_TYPE, fluidSupplier("jetpack_fuel_fluid"), fluidSupplier("flowing_jetpack_fuel_fluid"), DifModItems.JETPACK_FUEL_BUCKET, () -> (LiquidBlock) DifModBlocks.JETPACK_FUEL_FLUID.get(), JetpackFuel.TICK_RATE)));
	public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_JETPACK_FUEL = REGISTRY.register("flowing_jetpack_fuel_fluid",
			() -> new BaseFluidBlocks.Flowing(createProperties(DifModFluidTypes.JETPACK_FUEL_TYPE, fluidSupplier("jetpack_fuel_fluid"), fluidSupplier("flowing_jetpack_fuel_fluid"), DifModItems.JETPACK_FUEL_BUCKET, () -> (LiquidBlock) DifModBlocks.JETPACK_FUEL_FLUID.get(), JetpackFuel.TICK_RATE)));

	// JETPACK TURBO FUEL
	public static final DeferredHolder<Fluid, FlowingFluid> JETPACK_TURBO_FUEL = REGISTRY.register("jetpack_turbo_fuel_fluid",
			() -> new BaseFluidBlocks.Source(createProperties(DifModFluidTypes.JETPACK_TURBO_FUEL_TYPE, fluidSupplier("jetpack_turbo_fuel_fluid"), fluidSupplier("flowing_jetpack_turbo_fuel_fluid"), DifModItems.JETPACK_TURBO_FUEL_BUCKET, () -> (LiquidBlock) DifModBlocks.JETPACK_TURBO_FUEL_FLUID.get(), JetpackTurboFuel.TICK_RATE)));
	public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_JETPACK_TURBO_FUEL = REGISTRY.register("flowing_jetpack_turbo_fuel_fluid",
			() -> new BaseFluidBlocks.Flowing(createProperties(DifModFluidTypes.JETPACK_TURBO_FUEL_TYPE, fluidSupplier("jetpack_turbo_fuel_fluid"), fluidSupplier("flowing_jetpack_turbo_fuel_fluid"), DifModItems.JETPACK_TURBO_FUEL_BUCKET, () -> (LiquidBlock) DifModBlocks.JETPACK_TURBO_FUEL_FLUID.get(), JetpackTurboFuel.TICK_RATE)));

	// CIDER
	public static final DeferredHolder<Fluid, FlowingFluid> CIDER = REGISTRY.register("cider_fluid",
			() -> new BaseFluidBlocks.Source(createProperties(DifModFluidTypes.CIDER_TYPE, fluidSupplier("cider_fluid"), fluidSupplier("flowing_cider_fluid"), DifModItems.CIDER_BUCKET, () -> (LiquidBlock) DifModBlocks.CIDER_FLUID.get(), Cider.TICK_RATE)));
	public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_CIDER = REGISTRY.register("flowing_cider_fluid",
			() -> new BaseFluidBlocks.Flowing(createProperties(DifModFluidTypes.CIDER_TYPE, fluidSupplier("cider_fluid"), fluidSupplier("flowing_cider_fluid"), DifModItems.CIDER_BUCKET, () -> (LiquidBlock) DifModBlocks.CIDER_FLUID.get(), Cider.TICK_RATE)));

	// CRUDE OIL
	public static final DeferredHolder<Fluid, FlowingFluid> CRUDE_OIL = REGISTRY.register("crude_oil_fluid",
			() -> new BaseFluidBlocks.Source(createProperties(DifModFluidTypes.CRUDE_OIL_TYPE, fluidSupplier("crude_oil_fluid"), fluidSupplier("flowing_crude_oil_fluid"), DifModItems.CRUDE_OIL_BUCKET, () -> (LiquidBlock) DifModBlocks.CRUDE_OIL_FLUID.get(), CrudeOil.TICK_RATE)));
	public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_CRUDE_OIL = REGISTRY.register("flowing_crude_oil_fluid",
			() -> new BaseFluidBlocks.Flowing(createProperties(DifModFluidTypes.CRUDE_OIL_TYPE, fluidSupplier("crude_oil_fluid"), fluidSupplier("flowing_crude_oil_fluid"), DifModItems.CRUDE_OIL_BUCKET, () -> (LiquidBlock) DifModBlocks.CRUDE_OIL_FLUID.get(), CrudeOil.TICK_RATE)));

	// XP
	public static final DeferredHolder<Fluid, FlowingFluid> XP = REGISTRY.register("xp_fluid",
			() -> new BaseFluidBlocks.Source(createProperties(DifModFluidTypes.XP_TYPE, fluidSupplier("xp_fluid"), fluidSupplier("flowing_xp_fluid"), DifModItems.XP_BUCKET, () -> (LiquidBlock) DifModBlocks.XP_FLUID.get(), Xp.TICK_RATE)));
	public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_XP = REGISTRY.register("flowing_xp_fluid",
			() -> new BaseFluidBlocks.Flowing(createProperties(DifModFluidTypes.XP_TYPE, fluidSupplier("xp_fluid"), fluidSupplier("flowing_xp_fluid"), DifModItems.XP_BUCKET, () -> (LiquidBlock) DifModBlocks.XP_FLUID.get(), Xp.TICK_RATE)));

	// SUNFLOWER OIL
	public static final DeferredHolder<Fluid, FlowingFluid> SUNFLOWER_OIL = REGISTRY.register("sunflower_oil_fluid",
			() -> new BaseFluidBlocks.Source(createProperties(DifModFluidTypes.SUNFLOWER_OIL_TYPE, fluidSupplier("sunflower_oil_fluid"), fluidSupplier("flowing_sunflower_oil_fluid"), DifModItems.SUNFLOWER_OIL_BUCKET, () -> (LiquidBlock) DifModBlocks.SUNFLOWER_OIL_FLUID.get(), SunflowerOil.TICK_RATE)));
	public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_SUNFLOWER_OIL = REGISTRY.register("flowing_sunflower_oil_fluid",
			() -> new BaseFluidBlocks.Flowing(createProperties(DifModFluidTypes.SUNFLOWER_OIL_TYPE, fluidSupplier("sunflower_oil_fluid"), fluidSupplier("flowing_sunflower_oil_fluid"), DifModItems.SUNFLOWER_OIL_BUCKET, () -> (LiquidBlock) DifModBlocks.SUNFLOWER_OIL_FLUID.get(), SunflowerOil.TICK_RATE)));

	// Pomocná metoda pro bezpečné vyhledání tekutiny podle textového ID bez cyklických chyb
	private static Supplier<? extends Fluid> fluidSupplier(String name) {
		return () -> BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, name));
	}

	// Pomocná metoda pro čisté generování vlastností tekutin bez spamu kódu
	private static BaseFlowingFluid.Properties createProperties(
			Supplier<? extends FluidType> type,
			Supplier<? extends Fluid> source,
			Supplier<? extends Fluid> flowing,
			Supplier<? extends net.minecraft.world.item.Item> bucket,
			Supplier<? extends LiquidBlock> block,
			int tickRate) {

		return new BaseFlowingFluid.Properties(type, source, flowing)
				.explosionResistance(100F)
				.bucket(bucket)
				.block(block)
				.tickRate(tickRate);
	}

	@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class FluidsClientSideHandler {
		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event) {
			ItemBlockRenderTypes.setRenderLayer(CIDER.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(FLOWING_CIDER.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(SUNFLOWER_OIL.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(FLOWING_SUNFLOWER_OIL.get(), RenderType.translucent());
		}
	}
}