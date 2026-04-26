package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.init.events.OilWellFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = 
        DeferredRegister.create(ForgeRegistries.FEATURES, "dif");

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> OIL_WELL = 
        FEATURES.register("oil_well", () -> new OilWellFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}