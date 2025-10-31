package cz.maxtechnik.dif.item.food;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class UranovejKoktejl extends Item {
	public UranovejKoktejl() {
		super(new Properties().food((new FoodProperties.Builder()).nutrition(4).saturationMod(3f).alwaysEat().build()));
	}
	@Override
	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemstack) {
		return UseAnim.DRINK;
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemstack,Level level,@NotNull List<Component>list,@NotNull TooltipFlag flag){
		super.appendHoverText(itemstack,level,list,flag);
		list.add(Component.literal("§l§6!!!POZOR!!!"));
		list.add(Component.literal("§8- §aZvýšené množství radiace!"));
	}
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemstack,@NotNull Level world,@NotNull LivingEntity entity){
		ItemStack retval=new ItemStack(Items.GLASS_BOTTLE);
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
