package cz.maxtechnik.dif.fluid;

import net.neoforged.neoforge.fluids.FluidType;
public class CreosoteOil{
	public static final FluidType.Properties PROPERTIES=FluidType.Properties.create()
			.fallDistanceModifier(0F).canExtinguish(false).supportsBoating(true)
			.canHydrate(false).canDrown(true).motionScale(0.007D);
	public static final int TICK_RATE=7;
}