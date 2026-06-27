package cz.maxtechnik.dif.item.modular.v2;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
@EventBusSubscriber
public class ModularSubscriber{
	@SubscribeEvent
	public static void onEntityAttacked(LivingIncomingDamageEvent event){
		if(!(event.getSource().getEntity() instanceof Player attacker)) return;
		LivingEntity target=event.getEntity();
		float dmg=event.getAmount();
		ItemStack tool=attacker.getMainHandItem();
		if(!(tool.getItem() instanceof ModularTool)) return;
		ModularToolProperties props=ModularTool.getProps(tool);
		if(props==null) return;
		ModularReforge reforge=ModularReforge.byName(props.reforge());
		int tier=ModularTier.byName(props.tier()).getReforgeIndex();
		int[] chances={0,0,9,6,4};
		switch(reforge){
			case VAMPIRIC,DRAIN -> {
				if(attacker.getHealth()>=attacker.getMaxHealth()) return;
				if(!(chances[tier]==0)&&DifMod.rouletteBoolean(chances[tier]))
					attacker.heal(Math.clamp(dmg/2F,0.5F,target.getHealth()));
			}
			case PHANTOM -> {
				if(!(chances[tier]==0)&&DifMod.rouletteBoolean(chances[tier]))
					event.setAmount(dmg+Math.max(0.5F,dmg/2F));
			}
		}
	}
}