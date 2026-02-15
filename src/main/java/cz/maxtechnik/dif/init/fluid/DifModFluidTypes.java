package cz.maxtechnik.dif.init.fluid;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.fluid.types.*;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
public class DifModFluidTypes{
	public static final DeferredRegister<FluidType>REGISTRY=DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES,DifMod.MODID);
	public static final RegistryObject<FluidType>BEER_TYPE=REGISTRY.register("beer_fluid",BeerFluidType::new);
	public static final RegistryObject<FluidType>XP_TYPE=REGISTRY.register("xp_fluid",XpFluidType::new);
	public static final RegistryObject<FluidType>FUEL_TYPE=REGISTRY.register("fuel_fluid",FuelFluidType::new);
}
