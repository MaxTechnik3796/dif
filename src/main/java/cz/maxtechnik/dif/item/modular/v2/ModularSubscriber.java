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
		switch(reforge){
			case VAMPIRIC,DRAIN -> {
				if(attacker.getHealth()>=attacker.getMaxHealth()) return;
				float[] chances={0F,0F,0.05F,0.10F,0.15F};
				if(chances[tier]==0F||DifMod.rouletteBoolean(chances[tier])) return;
				attacker.heal(Math.clamp(dmg/2F,0.5F,target.getHealth()));
			}
			case PHANTOM -> {
				float[] chances={0F,0F,0.1F,0.15F,0.2F};
				if(chances[tier]==0F||DifMod.rouletteBoolean(chances[tier])) return;
				event.setAmount(dmg+Math.max(0.5F,dmg/2F));
			}
		}
	}
}