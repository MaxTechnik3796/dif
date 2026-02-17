package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber
public class OrbitVoidTeleport{
	@SubscribeEvent
	public static void onEntityTick(LivingEvent.LivingTickEvent event){
		LivingEntity entity=event.getEntity();
		ResourceLocation currentDim=entity.level().dimension().location();
		if(currentDim.equals(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"orbit"))){
			if(entity.getY()<=-90){
				entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,100,0,true,false));
				entity.setSecondsOnFire(10);
				entity.fallDistance=0;
				entity.teleportTo(entity.getX(),500,entity.getZ());
				if(entity instanceof ServerPlayer serverPlayer)serverPlayer.displayClientMessage(Component.literal("§6You are falling back to Overworld!"),true);

			}
		}
	}
}