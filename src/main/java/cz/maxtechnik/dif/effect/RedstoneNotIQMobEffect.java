package cz.maxtechnik.dif.effect;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.other.DifModMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.NotNull;
public class RedstoneNotIQMobEffect extends MobEffect{
	public RedstoneNotIQMobEffect(){
		super(MobEffectCategory.HARMFUL,-16776961);
	}
	@Override
	public boolean applyEffectTick(@NotNull LivingEntity entity,int amplifier){
		if(entity.hasEffect(DifModMobEffects.REDSTONE_IQ)){
			entity.removeEffect(DifModMobEffects.REDSTONE_IQ);
			entity.removeEffect(DifModMobEffects.REDSTONE_NOT_IQ);
			if(DifMod.rouletteBoolean(2)){
				entity.igniteForSeconds(10);
			}else{
				Level world=entity.level();
				if(!world.isClientSide()){
					world.explode(null,entity.getX(),entity.getY(),entity.getZ(),3.0F,Level.ExplosionInteraction.MOB);
				}
			}
		}
		return true;
	}
	@Override
	public boolean shouldApplyEffectTickThisTick(int duration,int amplifier){
		return true;
	}
}
