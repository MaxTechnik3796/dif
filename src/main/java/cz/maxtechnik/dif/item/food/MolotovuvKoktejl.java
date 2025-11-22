
package cz.maxtechnik.dif.item.food;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class MolotovuvKoktejl extends Item{
	public MolotovuvKoktejl(){
		super(new Properties().food((new FoodProperties.Builder()).nutrition(4).saturationMod(3f).alwaysEat().build()));
	}
	@Override
	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemstack){
		return UseAnim.DRINK;
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemstack,Level level,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		super.appendHoverText(itemstack,level,list,flag);
		list.add(Component.literal("§l§6!!!POZOR!!!"));
		list.add(Component.literal("§8- §cPijte na vlastní nebezpečí!"));
	}
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemstack,@NotNull Level world,@NotNull LivingEntity entity){
		ItemStack retval=new ItemStack(Items.GLASS_BOTTLE);
		super.finishUsingItem(itemstack,world,entity);
		if(!world.isClientSide()){
			if(DifMod.rouletteBoolean(2)){
				world.explode(null,entity.getX(),entity.getY(),entity.getZ(),15,Level.ExplosionInteraction.TNT);
			}else{
				entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION,100,0));
			}
		}
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
