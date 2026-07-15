package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.effect.DrankMobEffect;
import cz.maxtechnik.dif.effect.WTFMobEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
public class DifModMobEffects{
	public static final DeferredRegister<MobEffect> REGISTRY=DeferredRegister.create(Registries.MOB_EFFECT,DifMod.MODID);
	public static final DeferredHolder<MobEffect,MobEffect> DRANK=REGISTRY.register("drank",DrankMobEffect::new);
	public static final DeferredHolder<MobEffect,MobEffect> WTF=REGISTRY.register("wtf",WTFMobEffect::new);
}
