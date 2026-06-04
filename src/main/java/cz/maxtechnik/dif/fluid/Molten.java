package cz.maxtechnik.dif.fluid;

import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.fluids.FluidType;
public class Molten{
	public static final FluidType.Properties PROPERTIES=FluidType.Properties.create()
			.fallDistanceModifier(0F).canExtinguish(false).supportsBoating(true)
			.canHydrate(false).canDrown(true).motionScale(0.007D).pathType(PathType.LAVA);
	public static final int TICK_RATE=30;
}