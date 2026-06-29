package cz.maxtechnik.dif.init.fluid;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.fluid.*;
import cz.maxtechnik.dif.fluid.template.BaseFluidType;
import cz.maxtechnik.dif.fluid.template.BaseMoltenFluidType;
import cz.maxtechnik.dif.item.modular.v2.ModularMaterial;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
public class DifModFluidTypes{
	public static final DeferredRegister<FluidType> REGISTRY=DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES,DifMod.MODID);
	public static final DeferredHolder<FluidType,FluidType>BEER_TYPE=REGISTRY.register("beer_fluid",()->new BaseFluidType(Beer.PROPERTIES,"beer"));
	public static final DeferredHolder<FluidType,FluidType>XP_TYPE=REGISTRY.register("xp_fluid",()->new BaseFluidType(Xp.PROPERTIES,"xp"));
	public static final DeferredHolder<FluidType,FluidType>FUEL_TYPE=REGISTRY.register("fuel_fluid",()->new BaseFluidType(Fuel.PROPERTIES,"fuel"));
	public static final DeferredHolder<FluidType,FluidType>CIDER_TYPE=REGISTRY.register("cider_fluid",()->new BaseFluidType(Cider.PROPERTIES,"cider"));
	public static final DeferredHolder<FluidType,FluidType>CRUDE_OIL_TYPE=REGISTRY.register("crude_oil_fluid",()->new BaseFluidType(CrudeOil.PROPERTIES,"crude_oil"));
	public static final DeferredHolder<FluidType,FluidType>JETPACK_FUEL_TYPE=REGISTRY.register("jetpack_fuel_fluid",()->new BaseFluidType(JetpackFuel.PROPERTIES,"jetpack_fuel"));
	public static final DeferredHolder<FluidType,FluidType>SUNFLOWER_OIL_TYPE=REGISTRY.register("sunflower_oil_fluid",()->new BaseFluidType(SunflowerOil.PROPERTIES,"sunflower_oil"));

	public static final DeferredHolder<FluidType,FluidType>LPG_TYPE=REGISTRY.register("lpg_fluid",()->new BaseFluidType(Lpg.PROPERTIES,"lpg"));
	public static final DeferredHolder<FluidType,FluidType>GASOLINE_TYPE=REGISTRY.register("gasoline_fluid",()->new BaseFluidType(Gasoline.PROPERTIES,"gasoline"));
	public static final DeferredHolder<FluidType,FluidType>DIESEL_TYPE=REGISTRY.register("diesel_fluid",()->new BaseFluidType(Diesel.PROPERTIES,"diesel"));
	public static final DeferredHolder<FluidType,FluidType>LUBRICATING_OIL_TYPE=REGISTRY.register("lubricating_oil_fluid",()->new BaseFluidType(LubricatingOil.PROPERTIES,"lubricating_oil"));
	public static final DeferredHolder<FluidType,FluidType>HEAVY_FUEL_OIL_TYPE=REGISTRY.register("heavy_fuel_oil_fluid",()->new BaseFluidType(HeavyFuelOil.PROPERTIES,"heavy_fuel_oil"));
	public static final DeferredHolder<FluidType,FluidType>CREOSOTE_OIL_TYPE=REGISTRY.register("creosote_oil_fluid",()->new BaseFluidType(CreosoteOil.PROPERTIES,"creosote_oil"));

	public static final DeferredHolder<FluidType,FluidType>MOLTEN_IRON_TYPE=REGISTRY.register("molten_iron_fluid",()->new BaseMoltenFluidType(Molten.PROPERTIES,"molten_iron",ModularMaterial.IRON.getColor()));
	public static final DeferredHolder<FluidType,FluidType>MOLTEN_COPPER_TYPE=REGISTRY.register("molten_copper_fluid",()->new BaseMoltenFluidType(Molten.PROPERTIES,"molten_copper",ModularMaterial.COPPER.getColor()));
	public static final DeferredHolder<FluidType,FluidType>MOLTEN_GOLD_TYPE=REGISTRY.register("molten_gold_fluid",()->new BaseMoltenFluidType(Molten.PROPERTIES,"molten_gold",ModularMaterial.GOLD.getColor()));
	public static final DeferredHolder<FluidType,FluidType>MOLTEN_STEEL_TYPE=REGISTRY.register("molten_steel_fluid",()->new BaseMoltenFluidType(Molten.PROPERTIES,"molten_steel",ModularMaterial.STEEL.getColor()));
	public static final DeferredHolder<FluidType,FluidType>MOLTEN_OBSIDIAN_TYPE=REGISTRY.register("molten_obsidian_fluid",()->new BaseMoltenFluidType(Molten.PROPERTIES,"molten_obsidian",ModularMaterial.OBSIDIAN.getColor()));
	public static final DeferredHolder<FluidType,FluidType>MOLTEN_ZINC_TYPE=REGISTRY.register("molten_zinc_fluid",()->new BaseMoltenFluidType(Molten.PROPERTIES,"molten_zinc",ModularMaterial.ZINC.getColor()));
	public static final DeferredHolder<FluidType,FluidType>MOLTEN_BRASS_TYPE=REGISTRY.register("molten_brass_fluid",()->new BaseMoltenFluidType(Molten.PROPERTIES,"molten_brass",ModularMaterial.BRASS.getColor()));
	public static final DeferredHolder<FluidType,FluidType>MOLTEN_NICKEL_TYPE=REGISTRY.register("molten_nickel_fluid",()->new BaseMoltenFluidType(Molten.PROPERTIES,"molten_nickel",ModularMaterial.NICKEL.getColor()));
	public static final DeferredHolder<FluidType,FluidType>MOLTEN_MITHRIL_TYPE=REGISTRY.register("molten_mithril_fluid",()->new BaseMoltenFluidType(Molten.PROPERTIES,"molten_mithril",ModularMaterial.MITHRIL.getColor()));



}