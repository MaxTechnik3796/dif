package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.armor.ElectroRunners;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
@EventBusSubscriber(modid=DifMod.MODID)
public class ElectroRunnersHandler{
	@SubscribeEvent
	public static void onJump(LivingEvent.LivingJumpEvent event){
		if(event.getEntity() instanceof Player player){
			ItemStack boots=player.getItemBySlot(EquipmentSlot.FEET);
			if(boots.getItem() instanceof ElectroRunners&&ElectroRunners.Boots.getEnergy(boots)>0){
				// Skok na 2 bloky bez spotřeby energie
				player.setDeltaMovement(player.getDeltaMovement().x,0.6D,player.getDeltaMovement().z);
			}
		}
	}
	@SubscribeEvent
	public static void onFall(LivingFallEvent event){
		if(event.getEntity() instanceof Player player){
			ItemStack boots=player.getItemBySlot(EquipmentSlot.FEET);
			if(boots.getItem() instanceof ElectroRunners&&ElectroRunners.Boots.getEnergy(boots)>0){
				// Pokud je pád z 5 a méně bloků, zrušíme poškození
				if(event.getDistance()<=5.0F){
					event.setDistance(0);
					event.setCanceled(true);
				}
			}
		}
	}
	@SubscribeEvent
	public static void onLivingDamage(LivingIncomingDamageEvent event){
		if(event.getEntity() instanceof Player player){
			ItemStack boots=player.getItemBySlot(EquipmentSlot.FEET);
			if(boots.getItem() instanceof ElectroRunners){
				ElectroRunners.Boots.extract(boots,10);
			}
		}
	}
}