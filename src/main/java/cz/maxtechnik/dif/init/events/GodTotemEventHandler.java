package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=DifMod.MODID)
public class GodTotemEventHandler{
	public static boolean isHoldingGodTotem(LivingEntity entity){
		if(!(entity instanceof Player player)) return false;
		return player.getMainHandItem().getItem().equals(DifModItems.GOD_TOTEM.get())||
				player.getOffhandItem().getItem().equals(DifModItems.GOD_TOTEM.get());
	}
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public static void onLivingAttack(LivingAttackEvent event){
		if(event.getEntity() instanceof Player target&&isHoldingGodTotem(target)){
			// Kontrola našich zbraní
			if(event.getSource().getEntity() instanceof LivingEntity attacker){
				var item=attacker.getMainHandItem().getItem();
				if(item.equals(DifModItems.BAN_HAMMER.get())) return;
				// ODRAZ (Thorns)
				if(attacker!=target) attacker.hurt(target.damageSources().thorns(target),event.getAmount()*2F);
			}
			// TOTÁLNÍ IMUNITA (Zruší poškození od čehokoliv jiného)
			event.setCanceled(true);
		}
	}
}