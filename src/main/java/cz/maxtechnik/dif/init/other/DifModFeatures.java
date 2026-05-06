package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.feature.OilWellFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DifModFeatures{
    public static final DeferredRegister<Feature<?>> REGISTRY = DeferredRegister.create(Registries.FEATURE, DifMod.MODID);
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> OIL_WELL = REGISTRY.register("oil_well", () -> new OilWellFeature(NoneFeatureConfiguration.CODEC));
}