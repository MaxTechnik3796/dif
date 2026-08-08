package cz.maxtechnik.dif.item.food;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.other.DifModFoods;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class MolotovuvKoktejl extends Item{
	public MolotovuvKoktejl(){
		super(new Properties().food((DifModFoods.BOTTLE_OF_MOLOTOVUV_KOKTEJL)));
	}
	@Override
	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemstack){
		return UseAnim.DRINK;
	}
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(@NotNull ItemStack itemStack,Item.@NotNull TooltipContext context,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		super.appendHoverText(itemStack,context,list,flag);
		list.add(Component.literal("§l§6!!!WARNING!!!"));
		list.add(Component.literal("§8- §cDrink at your own risk!"));
	}
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemstack,@NotNull Level world,@NotNull LivingEntity entity){
		ItemStack itemStackResult=super.finishUsingItem(itemstack,world,entity);
		if(!world.isClientSide()){
			if(DifMod.rouletteBoolean(2)){
				world.explode(null,entity.getX(),entity.getY(),entity.getZ(),15.0F,Level.ExplosionInteraction.TNT);
			}else{
				entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION,100,0));
			}
		}
		if(entity instanceof Player player&&!player.getAbilities().instabuild){
			ItemStack container=new ItemStack(Items.GLASS_BOTTLE);
			if(itemStackResult.isEmpty()){
				return container;
			}
			if(!player.getInventory().add(container)){
				player.drop(container,false);
			}
		}
		return itemStackResult;
	}
}
