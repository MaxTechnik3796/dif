package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashSet;

@EventBusSubscriber(modid = DifMod.MODID)
public class OrbitVoidTeleport {

	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent event) { // Změna na EntityTickEvent
		if (!(event.getEntity() instanceof LivingEntity entity)) return;
		ResourceLocation currentDim = entity.level().dimension().location();
		ResourceLocation orbitDim = ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "orbit");
		if (currentDim.equals(orbitDim)) {
			if (entity.getY() <= -90) {
				if (!entity.level().isClientSide() && entity.getServer() != null) {
					ServerLevel overworld = entity.getServer().getLevel(Level.OVERWORLD);
					if (overworld != null) {
						entity.fallDistance = 0;
						// Teleportace
						entity.teleportTo(overworld, entity.getX(), 500, entity.getZ(), new HashSet<>(), entity.getYRot(), entity.getXRot());
						// Efekty
						entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, true, false));
						entity.setRemainingFireTicks(200); // 10 sekund = 200 ticků
						if (entity instanceof ServerPlayer serverPlayer) {
							serverPlayer.displayClientMessage(Component.literal("§6You are falling back to Overworld!"), true);
						}
					}
				}
			}
		}
	}
}