package cz.maxtechnik.dif.item.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
public class Bad extends Item{
	public Bad(){
		super(new Properties().rarity(Rarity.UNCOMMON).food((new FoodProperties.Builder()).nutrition(4).saturationMod(0.3f).alwaysEat().build()));
	}
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemstack,@NotNull Level world,@NotNull LivingEntity entity){
		ItemStack retval=super.finishUsingItem(itemstack,world,entity);
		entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION,1200,0));
		entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,1200,0));
		entity.addEffect(new MobEffectInstance(MobEffects.HUNGER,1200,4));
		entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,1200,3));
		entity.addEffect(new MobEffectInstance(MobEffects.POISON,1200,1));
		entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,1200,0));
		return retval;
	}
}
