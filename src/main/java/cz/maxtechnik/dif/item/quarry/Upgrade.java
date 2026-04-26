package cz.maxtechnik.dif.item.quarry;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
public class Upgrade extends Item{
	public Upgrade(Properties properties) {
		super(properties);
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemStack,@Nullable Level level,List<Component> tooltips,@NotNull TooltipFlag flag){
		tooltips.add(Component.translatable("item.dif."+itemStack.getItem()+".desc").withStyle(ChatFormatting.GRAY));
		super.appendHoverText(itemStack,level,tooltips,flag);
	}
}
