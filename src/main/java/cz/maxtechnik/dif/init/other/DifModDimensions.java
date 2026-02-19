package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
public class DifModDimensions{
	public static final ResourceKey<DimensionType> MOON_TYPE=ResourceKey.create(
			Registries.DIMENSION_TYPE,
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"moon_type")
	);
	public static final ResourceKey<DimensionType> ORBIT_TYPE=ResourceKey.create(
			Registries.DIMENSION_TYPE,
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"orbit_type")
	);
	public static final ResourceKey<Level> MOON=ResourceKey.create(
			Registries.DIMENSION,
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"moon")
	);
	public static final ResourceKey<Level> ORBIT=ResourceKey.create(
			Registries.DIMENSION,
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"orbit")
	);
	public static void register(){
	}
}