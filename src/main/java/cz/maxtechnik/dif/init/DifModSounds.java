package cz.maxtechnik.dif.init;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DifModSounds{
    public static final DeferredRegister<SoundEvent>REGISTRY=DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,DifMod.MODID);
    public static final RegistryObject<SoundEvent>CLAIRDELUNE=REGISTRY.register("clairdelune",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("dif","clairdelune")));
    public static final RegistryObject<SoundEvent>CREMEKA=REGISTRY.register("cremeka",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("dif","cremeka")));
    public static final RegistryObject<SoundEvent>FURT_TA_STEJNA_HRA=REGISTRY.register("furt_ta_stejna_hra",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mcc","furt_ta_stejna_hra")));
    public static final RegistryObject<SoundEvent>MATY_CREATE=REGISTRY.register("maty_create",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("dif","maty_create")));
    public static final RegistryObject<SoundEvent>MATY_PADA_STREAM=REGISTRY.register("maty_pada_stream",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("dif","maty_pada_stream")));
    public static final RegistryObject<SoundEvent>MAYONNAISE=REGISTRY.register("mayonnaise",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("dif","mayonnaise")));
    public static final RegistryObject<SoundEvent>REDSTONE=REGISTRY.register("redstone",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("dif","redstone")));
}
