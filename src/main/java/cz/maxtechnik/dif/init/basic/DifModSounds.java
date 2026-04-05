package cz.maxtechnik.dif.init.basic;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DifModSounds{
    public static final DeferredRegister<SoundEvent>REGISTRY=DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,DifMod.MODID);
    public static final RegistryObject<SoundEvent>FURT_TA_STEJNA_HRA=REGISTRY.register("furt_ta_stejna_hra",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"furt_ta_stejna_hra")));
    public static final RegistryObject<SoundEvent>MATY_CREATE=REGISTRY.register("maty_create",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"maty_create")));
    public static final RegistryObject<SoundEvent>REDSTONE=REGISTRY.register("redstone",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"redstone")));
	public static final RegistryObject<SoundEvent>DOG=REGISTRY.register("dog",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"dog")));
}
