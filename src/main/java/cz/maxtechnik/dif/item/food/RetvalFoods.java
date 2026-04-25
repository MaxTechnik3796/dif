package cz.maxtechnik.dif.item.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
public class RetvalFoods extends Item{
	Item retval;
	UseAnim useAnim;
	public RetvalFoods(Item.Properties properties,Item retval,UseAnim useAnim){
		super(properties);
		this.retval=retval;
		this.useAnim=useAnim;
	}
	@Override
	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemstack){
		return useAnim;
	}
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemStack,@NotNull Level level,@NotNull LivingEntity entity){
		ItemStack itemstack=super.finishUsingItem(itemStack,level,entity);
		return entity instanceof Player&&((Player)entity).getAbilities().instabuild?itemstack:new ItemStack(retval);
	}
}