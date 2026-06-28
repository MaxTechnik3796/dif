package cz.maxtechnik.dif.item.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
public class Canola extends Item{
	public Canola(){
		super(new Item.Properties().rarity(Rarity.RARE).food(DifModFoods.CANOLA_SEEDS));
	}
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemstack,@NotNull Level world,@NotNull LivingEntity entity){
		ItemStack retval=super.finishUsingItem(itemstack,world,entity);
		if(!world.isClientSide()){
			if(entity.hasEffect(MobEffects.CONFUSION)){
				entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION,400+Objects.requireNonNull(entity.getEffect(MobEffects.CONFUSION)).getDuration(),0,false,false));
			}else{
				entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION,400,0,false,false));
			}
		}
		return retval;
	}
}
