package cz.maxtechnik.dif.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
public class ExperienceMobEffect extends MobEffect{
	public ExperienceMobEffect(){
		super(MobEffectCategory.BENEFICIAL,0x80FF20);
	}
	@Override
	public boolean applyEffectTick(@NotNull LivingEntity entity,int amplifier){
		return true;
	}
	@Override
	public boolean shouldApplyEffectTickThisTick(int duration,int amplifier){
		return true;
	}
}
