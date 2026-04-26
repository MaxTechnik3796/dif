package cz.maxtechnik.dif.init.fluid;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.fluid.*;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
public class DifModFluidTypes{
	public static final DeferredRegister<FluidType>REGISTRY=DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES,DifMod.MODID);
	public static final RegistryObject<FluidType>BEER_TYPE=REGISTRY.register("beer_fluid",Beer.FluidType::new);
	public static final RegistryObject<FluidType>XP_TYPE=REGISTRY.register("xp_fluid",Xp.FluidType::new);
	public static final RegistryObject<FluidType>FUEL_TYPE=REGISTRY.register("fuel_fluid",Fuel.FluidType::new);
	public static final RegistryObject<FluidType>CIDER_TYPE=REGISTRY.register("cider_fluid",Cider.FluidType::new);
	public static final RegistryObject<FluidType>CRUDE_OIL_TYPE=REGISTRY.register("crude_oil_fluid",CrudeOil.FluidType::new);
	public static final RegistryObject<FluidType>JETPACK_FUEL_TYPE=REGISTRY.register("jetpack_fuel_fluid",JetpackFuel.FluidType::new);
	public static final RegistryObject<FluidType>JETPACK_TURBO_FUEL_TYPE=REGISTRY.register("jetpack_turbo_fuel_fluid",JetpackTurboFuel.FluidType::new);
	public static final RegistryObject<FluidType>SUNFLOWER_OIL_TYPE=REGISTRY.register("sunflower_oil_fluid",SunflowerOil.FluidType::new);
}
