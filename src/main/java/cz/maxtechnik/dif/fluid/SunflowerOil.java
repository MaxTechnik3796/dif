package cz.maxtechnik.dif.fluid;

import net.neoforged.neoforge.fluids.FluidType;
public class SunflowerOil{
	public static final FluidType.Properties PROPERTIES=FluidType.Properties.create()
			.fallDistanceModifier(0F).canExtinguish(true).supportsBoating(true)
			.canHydrate(false).canDrown(true).motionScale(0.007D);
	public static final int TICK_RATE=3;
}