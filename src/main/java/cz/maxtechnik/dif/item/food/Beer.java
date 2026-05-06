package cz.maxtechnik.dif.item.food;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;

@SuppressWarnings("deprecation")
public class Beer extends BlockItem{
	public Beer(Block block,Properties properties){
		super(block,properties);
	}
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemStack,@NotNull Level level,@NotNull LivingEntity entity){
		ItemStack itemstack=super.finishUsingItem(itemStack,level,entity);
		return entity instanceof Player&&((Player)entity).getAbilities().instabuild?itemstack:new ItemStack(Items.GLASS_BOTTLE);
	}
	@Override
	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemstack){
		return UseAnim.DRINK;
	}
	@Override
	public @NotNull InteractionResult useOn(@NotNull UseOnContext context){
		Player player=context.getPlayer();
		if(player!=null&&player.isShiftKeyDown()){
			return super.useOn(context);
		}
		return InteractionResult.PASS;
	}
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level,@NotNull Player player,@NotNull InteractionHand hand){
		ItemStack itemstack=player.getItemInHand(hand);
		if(player.isShiftKeyDown()){
			return InteractionResultHolder.pass(itemstack);
		}
		FoodProperties food = itemstack.get(DataComponents.FOOD);
		if(food != null && player.canEat(food.canAlwaysEat())){
			player.startUsingItem(hand);
			return InteractionResultHolder.consume(itemstack);
		}else{
			return InteractionResultHolder.fail(itemstack);
		}
	}
}