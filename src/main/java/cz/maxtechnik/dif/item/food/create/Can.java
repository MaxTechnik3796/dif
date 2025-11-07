package cz.maxtechnik.dif.item.food.create;

import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Can extends Item{
	public Can(){
		super(new Properties().food((new FoodProperties.Builder()).nutrition(9).saturationMod(1.2F).alwaysEat().build()));
	}
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemstack,@NotNull Level world,@NotNull LivingEntity entity){
		ItemStack retval=new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("create","andesite_alloy"))));
		super.finishUsingItem(itemstack,world,entity);
		if(!world.isClientSide()){
			entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,600,1));
			entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,1500,0));
			entity.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST,1200,0));
		}
		if(itemstack.isEmpty()){
			return retval;
		}else{
			if(entity instanceof Player player &&!player.getAbilities().instabuild){
				if(!player.getInventory().add(retval)){
					player.drop(retval, false);
				}
			}
			return itemstack;
		}
	}
}
