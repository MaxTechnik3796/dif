
package cz.maxtechnik.dif.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
public class RedstoneIQMobEffect extends MobEffect{
	public RedstoneIQMobEffect(){
		super(MobEffectCategory.BENEFICIAL,-1531648);
	}
	@Override
	public boolean isDurationEffectTick(int duration,int amplifier){
		return true;
	}
}
