
package cz.maxtechnik.dif.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class DrankMobEffect extends MobEffect {
	public DrankMobEffect() {
		super(MobEffectCategory.NEUTRAL, -3407668);
	}

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {

    }

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}
}
