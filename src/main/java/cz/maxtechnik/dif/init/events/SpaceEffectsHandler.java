package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;
@Mod.EventBusSubscriber(modid=DifMod.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class SpaceEffectsHandler{
	private static final Set<ResourceKey<Level>> LOW_GRAVITY_DIMENSIONS=Set.of(
			ResourceKey.create(Registries.DIMENSION,ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"orbit")),
			ResourceKey.create(Registries.DIMENSION,ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"moon"))
	);
	@SubscribeEvent
	public static void onEntityTick(LivingEvent.LivingTickEvent event){
		LivingEntity entity=event.getEntity();
		if(event.getEntity().tickCount%20!=0) return;
		if(LOW_GRAVITY_DIMENSIONS.contains(entity.level().dimension())){
			entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,40,1,true,false));
			entity.addEffect(new MobEffectInstance(MobEffects.JUMP,40,1,true,false));
		}
	}
}