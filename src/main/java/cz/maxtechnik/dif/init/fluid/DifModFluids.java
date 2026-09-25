package cz.maxtechnik.dif.init.fluid;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
public class DifModFluids{
	public static final DeferredRegister<Fluid> FLUIDS=DeferredRegister.create(Registries.FLUID,DifMod.MODID);
	public static final DeferredRegister<FluidType> TYPES=DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES,DifMod.MODID);
	public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(DifMod.MODID);
	public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(DifMod.MODID);
	public static final FluidEntry BEER=new FluidEntry("beer",true,false,
			type->type.density(1100).viscosity(1500).canHydrate(true).canDrown(true).canExtinguish(true).supportsBoating(true).fallDistanceModifier(0F).motionScale(0D),
			fluid->fluid.tickRate(20).levelDecreasePerBlock(1).slopeFindDistance(4),FLUIDS,TYPES,ITEMS,BLOCKS
	);
	public static final FluidEntry JETPACK_FUEL=new FluidEntry("jetpack_fuel",false,false,
			type->type.density(1100).viscosity(1500).canHydrate(false).canDrown(true).canExtinguish(true).supportsBoating(true).fallDistanceModifier(0F).motionScale(0.007D),
			fluid->fluid.tickRate(7).levelDecreasePerBlock(1).slopeFindDistance(4),FLUIDS,TYPES,ITEMS,BLOCKS
	);
	public static final FluidEntry CRUDE_OIL=new FluidEntry("crude_oil",false,true,
			type->type.density(800).viscosity(1500).canHydrate(false).canDrown(true).canExtinguish(true).supportsBoating(true).fallDistanceModifier(0F).motionScale(0.0002D),
			fluid->fluid.tickRate(30).levelDecreasePerBlock(1).slopeFindDistance(4),FLUIDS,TYPES,ITEMS,BLOCKS
	);
	public static final FluidEntry XP=new FluidEntry("xp",false,false,
			type->type.density(100).viscosity(1500).canHydrate(true).canDrown(true).canExtinguish(true).supportsBoating(true).fallDistanceModifier(0F).motionScale(0.007D),
			fluid->fluid.tickRate(20).levelDecreasePerBlock(1).slopeFindDistance(4),FLUIDS,TYPES,ITEMS,BLOCKS
	);
	public static final FluidEntry SUNFLOWER_OIL=new FluidEntry("sunflower_oil",true,true,
			type->type.density(700).viscosity(1500).canHydrate(false).canDrown(true).canExtinguish(true).supportsBoating(true).fallDistanceModifier(0F).motionScale(0.007D),
			fluid->fluid.tickRate(3).levelDecreasePerBlock(1).slopeFindDistance(4),FLUIDS,TYPES,ITEMS,BLOCKS
	);
	public static final FluidEntry LPG=new FluidEntry("lpg",false,false,
			type->type.density(800).viscosity(1500).canHydrate(false).canDrown(true).canExtinguish(true).supportsBoating(true).fallDistanceModifier(0F).motionScale(0.007D),
			fluid->fluid.tickRate(7).levelDecreasePerBlock(1).slopeFindDistance(4),FLUIDS,TYPES,ITEMS,BLOCKS
	);
	public static final FluidEntry GASOLINE=new FluidEntry("gasoline",false,true,
			type->type.density(800).viscosity(1500).canHydrate(false).canDrown(true).canExtinguish(true).supportsBoating(true).fallDistanceModifier(0F).motionScale(0.007D),
			fluid->fluid.tickRate(7).levelDecreasePerBlock(1).slopeFindDistance(4),FLUIDS,TYPES,ITEMS,BLOCKS
	);
	public static final FluidEntry DIESEL=new FluidEntry("diesel",false,true,
			type->type.density(800).viscosity(1500).canHydrate(false).canDrown(true).canExtinguish(true).supportsBoating(true).fallDistanceModifier(0F).motionScale(0.007D),
			fluid->fluid.tickRate(7).levelDecreasePerBlock(1).slopeFindDistance(4),FLUIDS,TYPES,ITEMS,BLOCKS
	);
	public static final FluidEntry LUBRICATING_OIL=new FluidEntry("lubricating_oil",false,false,
			type->type.density(700).viscosity(1500).canHydrate(false).canDrown(true).canExtinguish(true).supportsBoating(true).fallDistanceModifier(0F).motionScale(0.007D),
			fluid->fluid.tickRate(1).levelDecreasePerBlock(1).slopeFindDistance(4),FLUIDS,TYPES,ITEMS,BLOCKS
	);
	public static final FluidEntry HEAVY_FUEL_OIL=new FluidEntry("heavy_fuel_oil",false,false,
			type->type.density(900).viscosity(1500).canHydrate(false).canDrown(true).canExtinguish(true).supportsBoating(true).fallDistanceModifier(0F).motionScale(0.0002D),
			fluid->fluid.tickRate(7).levelDecreasePerBlock(1).slopeFindDistance(4),FLUIDS,TYPES,ITEMS,BLOCKS
	);
}