package cz.maxtechnik.dif.item.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class Tresnovice extends Item {
    public Tresnovice(){
        super(new Properties().rarity(Rarity.UNCOMMON).food(new FoodProperties.Builder().nutrition(5).saturationMod(1.3F).alwaysEat().build()));
    }
    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemstack){
        return UseAnim.DRINK;
    }
    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemstack,@NotNull Level world,@NotNull LivingEntity entity){
        ItemStack retval=new ItemStack(Items.GLASS_BOTTLE);
        super.finishUsingItem(itemstack,world,entity);
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION,200,0));
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION,200,0));
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
