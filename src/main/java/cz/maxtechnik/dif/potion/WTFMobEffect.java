package cz.maxtechnik.dif.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
public class WTFMobEffect extends MobEffect{
	public WTFMobEffect(){
		super(MobEffectCategory.BENEFICIAL,0x1A4D2E);
	}
	@Override
	public void applyEffectTick(@NotNull LivingEntity entity,int amplifier){
		super.applyEffectTick(entity,amplifier);
	}
	@Override
	public boolean isDurationEffectTick(int duration,int amplifier){
		return true;
	}
}