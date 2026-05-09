package cz.maxtechnik.dif.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
public class DrankMobEffect extends MobEffect{
	public DrankMobEffect(){
		super(MobEffectCategory.BENEFICIAL,-3407668);
	}
	@Override
	public boolean applyEffectTick(@NotNull LivingEntity entity,int amplifier){
		if(entity.hasEffect(MobEffects.CONFUSION)) return true;
		entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION,200,amplifier));
		return true;
	}
	@Override
	public boolean shouldApplyEffectTickThisTick(int duration,int amplifier){
		return true;
	}
}