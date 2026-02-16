package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.effect.DrankMobEffect;
import cz.maxtechnik.dif.effect.RedstoneIQMobEffect;
import cz.maxtechnik.dif.effect.RedstoneNotIQMobEffect;
import cz.maxtechnik.dif.effect.WTFMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
public class DifModMobEffects{
	public static final DeferredRegister<MobEffect>REGISTRY=DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,DifMod.MODID);
	public static final RegistryObject<MobEffect>REDSTONE_IQ=REGISTRY.register("redstone_iq",RedstoneIQMobEffect::new);
	public static final RegistryObject<MobEffect>REDSTONE_NOT_IQ=REGISTRY.register("redstone_not_iq",RedstoneNotIQMobEffect::new);
	public static final RegistryObject<MobEffect>DRANK=REGISTRY.register("drank",DrankMobEffect::new);
    public static final RegistryObject<MobEffect>WTF=REGISTRY.register("wtf",WTFMobEffect::new);
}
