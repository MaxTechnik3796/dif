package cz.maxtechnik.dif.init.fluid;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.fluid.fluid.*;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DifModFluids{
	public static final DeferredRegister<Fluid>REGISTRY=DeferredRegister.create(ForgeRegistries.FLUIDS,DifMod.MODID);

	public static final RegistryObject<FlowingFluid>BEER=REGISTRY.register("beer_fluid",BeerFluid.Source::new);
	public static final RegistryObject<FlowingFluid>FLOWING_BEER=REGISTRY.register("flowing_beer_fluid",BeerFluid.Flowing::new);

	public static final RegistryObject<FlowingFluid>FUEL=REGISTRY.register("fuel_fluid",FuelFluid.Source::new);
	public static final RegistryObject<FlowingFluid>FLOWING_FUEL=REGISTRY.register("flowing_fuel_fluid",FuelFluid.Flowing::new);

	public static final RegistryObject<FlowingFluid>JETPACK_FUEL=REGISTRY.register("jetpack_fuel_fluid",JetpackFuelFluid.Source::new);
	public static final RegistryObject<FlowingFluid>FLOWING_JETPACK_FUEL=REGISTRY.register("flowing_jetpack_fuel_fluid",JetpackFuelFluid.Flowing::new);

	public static final RegistryObject<FlowingFluid>JETPACK_TURBO_FUEL=REGISTRY.register("jetpack_turbo_fuel_fluid",JetpackTurboFuelFluid.Source::new);
	public static final RegistryObject<FlowingFluid>FLOWING_JETPACK_TURBO_FUEL=REGISTRY.register("flowing_jetpack_turbo_fuel_fluid",JetpackTurboFuelFluid.Flowing::new);

	public static final RegistryObject<FlowingFluid>CIDER=REGISTRY.register("cider_fluid",CiderFluid.Source::new);
	public static final RegistryObject<FlowingFluid>FLOWING_CIDER=REGISTRY.register("flowing_cider_fluid",CiderFluid.Flowing::new);

	public static final RegistryObject<FlowingFluid>XP=REGISTRY.register("xp_fluid",XpFluid.Source::new);
	public static final RegistryObject<FlowingFluid>FLOWING_XP=REGISTRY.register("flowing_xp_fluid",XpFluid.Flowing::new);

	@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
	public static class FluidsClientSideHandler{
		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event) {
			ItemBlockRenderTypes.setRenderLayer(CIDER.get(),RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(FLOWING_CIDER.get(),RenderType.translucent());
		}
	}
}
