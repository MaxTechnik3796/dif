package cz.maxtechnik.dif.item.modular;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModularPart extends Item {

	public ModularPart(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC).fireResistant());
	}

	// Vytvoří část s daným materiálem
	public static ItemStack ofMaterial(Item partItem, ToolMaterial material) {
		ItemStack stack = new ItemStack(partItem);
		stack.set(ToolComponents.TOOL_DATA.get(),
				ModularToolData.of(material, material, material));
		return stack;
	}

	// Vrátí materiál části podle toho jaký typ části to je
	public static ToolMaterial getPartMaterial(ItemStack stack) {
		ModularToolData data = stack.get(ToolComponents.TOOL_DATA.get());
		if (data == null) return ToolMaterial.WOOD;
		if (ModularBase.isHead(stack)) return data.head();
		if (ModularBase.isBinding(stack)) return data.binding();
		if (ModularBase.isHandle(stack)) return data.handle();
		return data.head();
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
	                            @NotNull List<Component> list, @NotNull TooltipFlag flag) {
		ModularToolData data = stack.get(ToolComponents.TOOL_DATA.get());
		if (data == null) return;

		ToolMaterial material = getPartMaterial(stack);

		list.add(Component.literal("Material: ").withStyle(ChatFormatting.WHITE)
				.append(Component.literal(material.name)
						.withStyle(Style.EMPTY.withColor(material.color))));

		int durability;
		if (ModularBase.isHead(stack)) durability = data.headDurability();
		else if (ModularBase.isBinding(stack)) durability = data.bindingDurability();
		else durability = data.handleDurability();

		list.add(Component.literal("Durability: ").withStyle(ChatFormatting.WHITE)
				.append(Component.literal(String.valueOf(durability)).withStyle(ChatFormatting.GREEN)));

		if (ModularBase.isHead(stack))
			list.add(Component.literal("Efficiency: ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(String.valueOf(material.efficiency)).withStyle(ChatFormatting.GREEN)));

		list.add(CommonComponents.EMPTY);

		if (!material.passiveKey().isEmpty())
			list.add(Component.literal(capitalize(material.passiveKey()))
					.withStyle(Style.EMPTY.withColor(material.color)));
	}

	private static String capitalize(String s) {
		if (s == null || s.isEmpty()) return s;
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
}