package cz.maxtechnik.dif.init.basic;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
public class DifModSounds{
	public static final DeferredRegister<SoundEvent> REGISTRY=DeferredRegister.create(Registries.SOUND_EVENT,DifMod.MODID);
	public static final DeferredHolder<SoundEvent,SoundEvent> FORMULA_ENGINE=REGISTRY.register("formula_engine",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"formula_engine")));
	public static final DeferredHolder<SoundEvent,SoundEvent> NANO_GLASS=REGISTRY.register("nano_glass",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"nano_glass")));
}
