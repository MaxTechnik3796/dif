package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Set;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.GAME)
public class SpaceEffectsHandler{
	private static final Set<ResourceKey<Level>> LOW_GRAVITY_DIMENSIONS=Set.of(
			ResourceKey.create(Registries.DIMENSION,ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"orbit")),
			ResourceKey.create(Registries.DIMENSION,ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"moon"))
	);
	@SubscribeEvent
	public static void onEntityTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event){
		// V 1.21.1 se používá .getEntity() nebo .entity()
		if(event.getEntity() instanceof LivingEntity livingEntity){
			// Kontrola každou sekundu
			if(livingEntity.tickCount%20!=0) return;
			if(LOW_GRAVITY_DIMENSIONS.contains(livingEntity.level().dimension())){
				livingEntity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,40,1,true,false));
				livingEntity.addEffect(new MobEffectInstance(MobEffects.JUMP,40,1,true,false));
			}
		}
	}
}