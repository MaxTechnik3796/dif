package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.item.armor.ElectroRunnersItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "dif") // Zkontroluj své modid!
public class ElectroRunnersHandler {

	@SubscribeEvent
	public static void onJump(LivingEvent.LivingJumpEvent event) {
		if (event.getEntity() instanceof Player player) {
			ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
			if (boots.getItem() instanceof ElectroRunnersItem && ElectroRunnersItem.getEnergy(boots) > 0) {
				// Skok na 2 bloky bez spotřeby energie
				player.setDeltaMovement(player.getDeltaMovement().x, 0.6D, player.getDeltaMovement().z);
			}
		}
	}
	@SubscribeEvent
	public static void onFall(LivingFallEvent event) {
		if (event.getEntity() instanceof Player player) {
			ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
			if (boots.getItem() instanceof ElectroRunnersItem && ElectroRunnersItem.getEnergy(boots) > 0) {
				// Pokud je pád z 5 a méně bloků, zrušíme poškození
				if (event.getDistance() <= 5.0F) {
					event.setDistance(0);
					event.setCanceled(true);
				}
			}
		}
	}
	@SubscribeEvent
	public static void onHurt(LivingHurtEvent event) {
		if (event.getEntity() instanceof Player player) {
			ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
			if (boots.getItem() instanceof ElectroRunnersItem) {
				// Jakýkoliv damage (včetně pádu nad 5 bloků) ubere 10 EU
				ElectroRunnersItem.extract(boots, 10);
			}
		}
	}
}