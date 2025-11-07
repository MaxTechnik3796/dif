package cz.maxtechnik.dif.item.food;

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

public class Create extends Item{
	public Create(){
		super(new Properties().stacksTo(64).rarity(Rarity.COMMON).food((new FoodProperties.Builder()).nutrition(18).saturationMod(1.2f).alwaysEat().build()));
	}
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemstack,@NotNull Level world,@NotNull LivingEntity entity){
		ItemStack retval=new ItemStack(Items.BOWL);
		super.finishUsingItem(itemstack,world,entity);
		if(!world.isClientSide()){
			Item item=itemstack.getItem();
			if(item.equals(DifModItems.CREATE_CAN.get())){
				retval=new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("create","andesite_alloy"))));
				entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,600,1));
				entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,1500,0));
				entity.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST,1200,0));
			}else if(item.equals(DifModItems.CREATE_BOWL.get())){
				entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,1200,2));
				entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,3000,0));
				entity.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST,2400,0));
			}else if(item.equals(DifModItems.SUPER_HEATED_CREATE_BOWL.get())){
				entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,2400,2));
				entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,6000,0));
				entity.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST,4800,1));
				entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,6000,0));
			}
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
