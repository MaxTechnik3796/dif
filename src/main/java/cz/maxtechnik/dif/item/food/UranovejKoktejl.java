package cz.maxtechnik.dif.item.food;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class UranovejKoktejl extends Item{
	public UranovejKoktejl(){
		super(new Properties().food((DifModFoods.BOTTLE_OF_URANOVEJ_KOKTEJL)));
	}
	@Override
	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemstack){
		return UseAnim.DRINK;
	}
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(@NotNull ItemStack itemStack,Item.@NotNull TooltipContext context,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		super.appendHoverText(itemStack,context,list,flag);
		list.add(Component.literal("§l§6!!!POZOR!!!"));
		list.add(Component.literal("§8- §aZvýšené množství radiace!"));
	}
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemStack,@NotNull Level level,@NotNull LivingEntity entity){
		ItemStack itemstack=super.finishUsingItem(itemStack,level,entity);
		return entity instanceof Player&&((Player)entity).getAbilities().instabuild?itemstack:new ItemStack(Items.GLASS_BOTTLE);
	}
}
