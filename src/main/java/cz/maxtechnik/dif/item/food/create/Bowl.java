package cz.maxtechnik.dif.item.food.create;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
public class Bowl extends Item{
	public Bowl(){
		super(new Properties().food((new FoodProperties.Builder()).nutrition(18).saturationMod(1.2F).meat().alwaysEat().build()));
	}
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemstack,@NotNull Level world,@NotNull LivingEntity entity){
		ItemStack retval=new ItemStack(Items.BOWL);
		super.finishUsingItem(itemstack,world,entity);
		if(!world.isClientSide()){
			entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,1800,1));
			entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,3000,0));
			entity.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST,2400,0));
		}
		if(itemstack.isEmpty()){
			return retval;
		}else{
			if(entity instanceof Player player&&!player.getAbilities().instabuild){
				if(!player.getInventory().add(retval)){
					player.drop(retval,false);
				}
			}
			return itemstack;
		}
	}
}
