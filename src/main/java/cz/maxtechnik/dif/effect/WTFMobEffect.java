package cz.maxtechnik.dif.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
public class WTFMobEffect extends MobEffect{
	public WTFMobEffect(){
		super(MobEffectCategory.BENEFICIAL,0x1A4D2E);
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