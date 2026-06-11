package cz.maxtechnik.dif.item.modular.v2;

import cz.maxtechnik.dif.init.other.DifModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
public class ModularPart extends Item{
	public ModularPart(){
		super(new Properties());
	}
	private ModularPartProperties getProps(ItemStack itemStack){
		ModularPartProperties props=itemStack.get(DifModComponents.MODULAR_PART_PROPERTIES.get());
		return props!=null?props:ModularPartProperties.DEFAULT;
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemStack,@NotNull TooltipContext context,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		ModularPartProperties props=getProps(itemStack);
		if(props.partType().equals("none")) return;
		ModularMaterial material=ModularMaterial.byName(props.material());
		ModularTier tier=material.getTier();
		ModularModifier modifier=material.getModifier();
		list.add(
				Component.literal("───── Stats ─────")
						.withStyle(Style.EMPTY.withColor(0x6644BB))
		);
		list.add(
				Component.literal("Tier: ").withStyle(ChatFormatting.GRAY)
						.append(Component.translatable("dif.tier."+tier.name).withColor(ModularTier.byName(tier.name).getColor()))
		);
		list.add(
				Component.literal("Material: ").withStyle(Style.EMPTY.withColor(0x888888))
						.append(Component.translatable("dif.material."+material.getName())
								.withStyle(Style.EMPTY.withColor(material.getColor())))
		);
		list.add(
				Component.literal("Modifier: ").withStyle(Style.EMPTY.withColor(0x888888))
						.append(Component.translatable("dif.modifier."+modifier.getName())
								.withStyle(Style.EMPTY.withColor(material.getColor())))
		);
	}
	@Override
	public @NotNull String getDescriptionId(@NotNull ItemStack itemStack){
		String type=getProps(itemStack).partType().toLowerCase(Locale.ROOT);
		if(!type.isEmpty()&&!type.equals("none"))
			return super.getDescriptionId(itemStack)+"."+type;
		return super.getDescriptionId(itemStack);
	}
	@Override
	public @NotNull Component getName(@NotNull ItemStack itemStack){
		int rarityColor=ModularMaterial.byName(getProps(itemStack).material()).getTier().getColor();
		return Component.translatable(getDescriptionId(itemStack)).withStyle(Style.EMPTY.withColor(rarityColor).withItalic(false));
	}
}
