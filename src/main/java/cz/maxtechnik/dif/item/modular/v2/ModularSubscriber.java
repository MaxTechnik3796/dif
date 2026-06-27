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
		float damageAmount=event.getAmount();
		ItemStack tool=attacker.getMainHandItem();
		if(!(tool.getItem() instanceof ModularTool)) return;
		ModularToolProperties props=ModularTool.getProps(tool);
		if(props==null) return;
		ModularReforge reforge=ModularReforge.byName(props.reforge());
		int tierIndex=ModularTier.byName(props.tier()).getReforgeIndex();
		switch(reforge){
			case VAMPIRIC,DRAIN -> {
				if(attacker.getHealth()>=attacker.getMaxHealth()) return;
				float[] chanceList={0F,0F,0.05F,0.1F,0.15F};
				float chance=chanceList[tierIndex];
				if(chance==0F) return;
				if(DifMod.rouletteBoolean(chance)) return;
				float heal=Math.clamp(damageAmount,1F,target.getHealth());
				attacker.heal(heal);
			}
			case PHANTOM -> {
				float[] chanceList={0F,0F,0.10F,0.15F,0.2F};
				float chance=chanceList[tierIndex];
				if(chance==0F) return;
				if(DifMod.rouletteBoolean(chance)) return;
				event.setAmount(damageAmount*2);
			}
		}
	}
}