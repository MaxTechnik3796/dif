package cz.maxtechnik.dif.item.modular.v2;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber
public class ModularSubscriber{
	@SubscribeEvent
	public static void onEntityAttacked(LivingIncomingDamageEvent event) {
		LivingEntity attacker=(LivingEntity)event.getSource().getEntity();
		LivingEntity target=event.getEntity();
		Level level=target.level();
		float damageAmount=event.getAmount();
		if(attacker==null) return;
		if(!(attacker instanceof Player)) return;
		ItemStack tool=attacker.getUseItem();
		if(!(tool.getItem() instanceof ModularTool)) return;
		ModularToolProperties props=ModularTool.getProps(tool);
		if(props==null) return;
		if(!DifMod.rouletteBoolean(4)) return;
		if(ModularReforge.byName(props.reforge()).equals(ModularReforge.VAMPIRIC)||ModularReforge.byName(props.reforge()).equals(ModularReforge.DRAIN)){
			if(!(attacker.getHealth()<attacker.getMaxHealth())) return;
			if(target.getHealth()<damageAmount){
				attacker.heal(damageAmount-target.getHealth());
				return;
			}
			Float[] amountList=new Float[]{1F,1F,1.05F,1.1F,1.15F};
			float amount=amountList[ModularTier.byName(props.tier()).getReforgeIndex()];
			if(amount==1F) return;
			float lifeSteal=damageAmount*amount;
			if(lifeSteal<1F) lifeSteal=1F;
			target.hurt(new DamageSource(level.holderOrThrow(DamageTypes.GENERIC)),lifeSteal);
			attacker.heal(lifeSteal);
		}else if(ModularReforge.byName(props.reforge()).equals(ModularReforge.PHANTOM)){
			Float[] amountList=new Float[]{1F,1F,1.1F,1.15F,1.2F};
			float amount=amountList[ModularTier.byName(props.tier()).getReforgeIndex()];
			float strike=damageAmount*amount;
			target.hurt(new DamageSource(level.holderOrThrow(DamageTypes.GENERIC)),strike);
		}
	}
}
