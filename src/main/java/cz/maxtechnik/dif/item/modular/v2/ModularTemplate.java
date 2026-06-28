package cz.maxtechnik.dif.item.modular.v2;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class ModularTemplate extends Item{
	public ModularTemplate(Item.Properties properties){
		super(properties);
	}
	@Override
	public @NotNull String getDescriptionId(@NotNull ItemStack itemStack){
		return "item.dif.modular_template";
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemStack,@NotNull TooltipContext context,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		list.add(Component.literal("Modifier:").withStyle(ChatFormatting.GRAY));
		list.add(Component.literal(" ").append(Component.translatable("item.dif.modular_template_efficiency").withStyle(ChatFormatting.BLUE)));
	}
}
