package cz.maxtechnik.dif.init.fluid;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.fluid.*;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;

@SuppressWarnings("removal")
public class DifModFluids{
	public static final DeferredRegister<Fluid> REGISTRY = DeferredRegister.create(Registries.FLUID, DifMod.MODID);

	public static final DeferredHolder<Fluid, FlowingFluid> BEER = REGISTRY.register("beer_fluid", Beer.Fluid.Source::new);
	public static final DeferredHolder<Fluid, FlowingFluid>FLOWING_BEER=REGISTRY.register("flowing_beer_fluid",Beer.Fluid.Flowing::new);

	public static final DeferredHolder<Fluid, FlowingFluid>FUEL=REGISTRY.register("fuel_fluid",Fuel.Fluid.Source::new);
	public static final DeferredHolder<Fluid, FlowingFluid>FLOWING_FUEL=REGISTRY.register("flowing_fuel_fluid",Fuel.Fluid.Flowing::new);

	public static final DeferredHolder<Fluid, FlowingFluid>JETPACK_FUEL=REGISTRY.register("jetpack_fuel_fluid",JetpackFuel.Fluid.Source::new);
	public static final DeferredHolder<Fluid, FlowingFluid>FLOWING_JETPACK_FUEL=REGISTRY.register("flowing_jetpack_fuel_fluid",JetpackFuel.Fluid.Flowing::new);

	public static final DeferredHolder<Fluid, FlowingFluid>JETPACK_TURBO_FUEL=REGISTRY.register("jetpack_turbo_fuel_fluid",JetpackTurboFuel.Fluid.Source::new);
	public static final DeferredHolder<Fluid, FlowingFluid>FLOWING_JETPACK_TURBO_FUEL=REGISTRY.register("flowing_jetpack_turbo_fuel_fluid",JetpackTurboFuel.Fluid.Flowing::new);

	public static final DeferredHolder<Fluid, FlowingFluid>CIDER=REGISTRY.register("cider_fluid",Cider.Fluid.Source::new);
	public static final DeferredHolder<Fluid, FlowingFluid>FLOWING_CIDER=REGISTRY.register("flowing_cider_fluid",Cider.Fluid.Flowing::new);

	public static final DeferredHolder<Fluid, FlowingFluid>CRUDE_OIL=REGISTRY.register("crude_oil_fluid",CrudeOil.Fluid.Source::new);
	public static final DeferredHolder<Fluid, FlowingFluid>FLOWING_CRUDE_OIL=REGISTRY.register("flowing_crude_oil_fluid",CrudeOil.Fluid.Flowing::new);

	public static final DeferredHolder<Fluid, FlowingFluid>XP=REGISTRY.register("xp_fluid",Xp.Fluid.Source::new);
	public static final DeferredHolder<Fluid, FlowingFluid>FLOWING_XP=REGISTRY.register("flowing_xp_fluid",Xp.Fluid.Flowing::new);

	public static final DeferredHolder<Fluid, FlowingFluid>SUNFLOWER_OIL=REGISTRY.register("sunflower_oil_fluid",SunflowerOil.Fluid.Source::new);
	public static final DeferredHolder<Fluid, FlowingFluid>FLOWING_SUNFLOWER_OIL=REGISTRY.register("flowing_sunflower_oil_fluid",SunflowerOil.Fluid.Flowing::new);

	@EventBusSubscriber(bus= EventBusSubscriber.Bus.MOD,value= Dist.CLIENT)
	public static class FluidsClientSideHandler{
		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event) {
			ItemBlockRenderTypes.setRenderLayer(CIDER.get(),RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(FLOWING_CIDER.get(),RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(SUNFLOWER_OIL.get(),RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(FLOWING_SUNFLOWER_OIL.get(),RenderType.translucent());
		}
	}
}
