package cz.maxtechnik.dif.item.food;

import cz.maxtechnik.dif.init.other.DifModFoods;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ExperienceDrink extends Item{
	public ExperienceDrink(){
		super(new Properties().food(DifModFoods.EXPERIENCE_DRINK));
	}
	@Override
	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemstack){
		return UseAnim.DRINK;
	}
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(@NotNull ItemStack itemStack,Item.@NotNull TooltipContext context,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		super.appendHoverText(itemStack,context,list,flag);
		list.add(Component.literal("§eProvides an intense experience trip!"));
	}
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemStack,@NotNull Level level,@NotNull LivingEntity entity){
		ItemStack itemstack=super.finishUsingItem(itemStack,level,entity);
		if(entity instanceof Player player&&!player.getAbilities().instabuild){
			ItemStack container=new ItemStack(Items.GLASS_BOTTLE);
			if(itemstack.isEmpty()){
				return container;
			}
			if(!player.getInventory().add(container)){
				player.drop(container,false);
			}
		}
		return itemstack;
	}
}
