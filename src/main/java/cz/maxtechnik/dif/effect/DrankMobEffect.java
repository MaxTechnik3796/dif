package cz.maxtechnik.dif.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class DrankMobEffect extends MobEffect{
	public DrankMobEffect(){
		super(MobEffectCategory.BENEFICIAL,-3407668);
	}
	@Override
	public void applyEffectTick(@NotNull LivingEntity entity,int amplifier){
		super.applyEffectTick(entity,amplifier);
		if(entity.hasEffect(MobEffects.CONFUSION))return;
		entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION,200,amplifier));
	}
	@Override
	public boolean isDurationEffectTick(int duration,int amplifier){
		return true;
	}
}