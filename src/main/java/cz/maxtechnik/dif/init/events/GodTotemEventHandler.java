package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
@EventBusSubscriber(modid=DifMod.MODID)
public class GodTotemEventHandler{
	public static boolean isHoldingGodTotem(LivingEntity entity){
		if(!(entity instanceof Player player)) return false;
		return player.getMainHandItem().is(DifModItems.GOD_TOTEM.get())||
				player.getOffhandItem().is(DifModItems.GOD_TOTEM.get());
	}
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public static void onLivingIncomingDamage(LivingIncomingDamageEvent event){
		if(event.getEntity() instanceof Player target&&isHoldingGodTotem(target)){
			if(event.getSource().getEntity() instanceof LivingEntity attacker){
				var item=attacker.getMainHandItem();
				if(item.is(DifModItems.BAN_HAMMER.get())) return;
				if(attacker!=target){
					attacker.hurt(target.damageSources().thorns(target),event.getAmount()*2.0F);
				}
			}
			event.setCanceled(true);
		}
	}
}