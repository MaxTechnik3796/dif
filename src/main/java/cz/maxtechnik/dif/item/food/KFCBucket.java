package cz.maxtechnik.dif.item.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
public class KFCBucket extends Item{
	public KFCBucket(){
		super(new Properties().food(new FoodProperties.Builder().nutrition(9).saturationMod(1.25F).alwaysEat().build()));
	}
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemstack,@NotNull Level world,@NotNull LivingEntity entity){
		ItemStack retval=new ItemStack(Items.BUCKET);
		super.finishUsingItem(itemstack,world,entity);
		if(itemstack.isEmpty()){
			return retval;
		}else{
			if(entity instanceof Player player&&!player.getAbilities().instabuild){
				if(!player.getInventory().add(retval))
					player.drop(retval,false);
			}
			return itemstack;
		}
	}
}
