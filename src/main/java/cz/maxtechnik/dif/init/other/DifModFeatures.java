package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.feature.OilWellFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DifModFeatures{
    public static final DeferredRegister<Feature<?>>REGISTRY=DeferredRegister.create(ForgeRegistries.FEATURES,DifMod.MODID);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>>OIL_WELL=REGISTRY.register("oil_well",()->new OilWellFeature(NoneFeatureConfiguration.CODEC));

}