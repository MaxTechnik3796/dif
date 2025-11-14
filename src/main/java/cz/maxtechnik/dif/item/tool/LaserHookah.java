package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class LaserHookah extends Item {
	private static final int USE_DURATION=1200;
	public LaserHookah(){
		super(new Properties().stacksTo(1));
	}
	@Override
	public int getUseDuration(@NotNull ItemStack itemStack){
		return USE_DURATION;
	}
	@Override
	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemstack){
		return UseAnim.TOOT_HORN;
	}
	@Override
	public @NotNull InteractionResultHolder<ItemStack>use(@NotNull Level world,Player player,@NotNull InteractionHand hand){
		player.awardStat(Stats.ITEM_USED.get(this));
		player.getItemInHand(hand).getOrCreateTag().putBoolean("active",true);
		return ItemUtils.startUsingInstantly(world,player,hand);
	}
	@Override
	public void inventoryTick(@NotNull ItemStack itemstack,@NotNull Level world,@NotNull Entity entity,int slot,boolean selected){
		super.inventoryTick(itemstack,world,entity,slot,selected);
		if(entity instanceof Player player){
			if(selected){
				//DifMod.LOGGER.debug(itemstack.);
			}
		}
	}

}