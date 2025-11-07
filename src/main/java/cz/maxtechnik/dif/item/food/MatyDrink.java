package cz.maxtechnik.dif.item.food;

import cz.maxtechnik.dif.init.special.DifModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MatyDrink extends Item{
	public MatyDrink(){
		super(new Item.Properties().rarity(Rarity.UNCOMMON).food((new FoodProperties.Builder()).nutrition(13).saturationMod(2f).alwaysEat().build()));
	}
	@Override
	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemstack){
		return UseAnim.DRINK;
	}
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemstack,@NotNull Level world,@NotNull LivingEntity entity){
		ItemStack retval=new ItemStack(Items.GLASS_BOTTLE);
		super.finishUsingItem(itemstack,world,entity);
		entity.addEffect(new MobEffectInstance(DifModMobEffects.REDSTONE_IQ.get(),1200,0));
		entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,0));
		entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,100,2));
		if(itemstack.isEmpty()){
			return retval;
		}else{
			if(entity instanceof Player player &&!player.getAbilities().instabuild){
				if(!player.getInventory().add(retval)){
					player.drop(retval,false);
				}
			}
			return itemstack;
		}
	}
}
