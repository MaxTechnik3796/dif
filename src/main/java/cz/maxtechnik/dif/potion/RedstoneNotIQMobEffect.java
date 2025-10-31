package cz.maxtechnik.dif.potion;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.DifModMobEffects;
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
	public void applyEffectTick(@NotNull LivingEntity entity,int amplifier) {
		if(entity.hasEffect(DifModMobEffects.REDSTONE_IQ.get())){
			entity.removeEffect(DifModMobEffects.REDSTONE_IQ.get());
			entity.removeEffect(DifModMobEffects.REDSTONE_NOT_IQ.get());
			if(DifMod.rouletteBoolean(2)){
				entity.setSecondsOnFire(10);
			}else{
				LevelAccessor world=entity.level();
				if(world instanceof Level level&&!level.isClientSide()){
					level.explode(null,entity.getX(),entity.getY(),entity.getZ(),3,Level.ExplosionInteraction.MOB);
				}
			}
		}
	}
	@Override
	public boolean isDurationEffectTick(int duration,int amplifier){
		return true;
	}
}
