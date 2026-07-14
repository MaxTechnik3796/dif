package cz.maxtechnik.dif.item.modular.v2;

import cz.maxtechnik.dif.init.other.DifModComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class ModularPart extends Item{
	public ModularPart(){
		super(new Properties().fireResistant());
	}
	public static ModularPartProperties getProps(ItemStack itemStack){
		ModularPartProperties props=itemStack.get(DifModComponents.MODULAR_PART_PROPERTIES.get());
		return props!=null?props:ModularPartProperties.DEFAULT;
	}
	public static boolean isModularPart(ItemStack itemStack){
		return itemStack.getItem() instanceof ModularPart;
	}
	public static ModularParts getPart(ItemStack itemStack){
		return ModularParts.byName(getProps(itemStack).partType());
	}
	public static ModularMaterial getMaterial(ItemStack itemStack){
		return ModularMaterial.byName(getProps(itemStack).material());
	}
	public static boolean isCast(ItemStack itemStack){
		ModularPartProperties props=itemStack.get(DifModComponents.MODULAR_PART_PROPERTIES.get());
		return props!=null&&props.castMold();
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemStack,@NotNull TooltipContext context,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		ModularPartProperties props=getProps(itemStack);
		if(props.partType().equals("none")) return;
		ModularMaterial material=getMaterial(itemStack);
		ModularModifier modifier=material.getModifier();
		int durability=switch(ModularPartType.getPartType(getPart(itemStack))){
			case HEAD -> material.getHeadDurability();
			case BINDING -> material.getBindingDurability();
			case HANDLE -> material.getHandleDurability();
			default -> 0;
		};
		list.add(Component.literal("───── Stats ─────").withStyle(Style.EMPTY.withColor(0x6644BB)));
		list.add(Component.literal("Material: ").withStyle(Style.EMPTY.withColor(0x888888)).append(Component.translatable("dif.material."+material.getName()).withStyle(Style.EMPTY.withColor(material.getColor()))));
		list.add(Component.literal("Modifier: ").withStyle(Style.EMPTY.withColor(0x888888)).append(Component.translatable("dif.modifier."+modifier.getName()).withStyle(Style.EMPTY.withColor(material.getColor()))));
		list.add(Component.literal("Durability: ").withStyle(Style.EMPTY.withColor(0x888888)).append(Component.literal(String.valueOf(durability)).withColor(0xFFAA00)));
	}
	@Override
	public @NotNull String getDescriptionId(@NotNull ItemStack itemStack){
		String type=getProps(itemStack).partType();
		if(!type.isEmpty()&&!type.equals("none")) return getProps(itemStack).castMold()?(super.getDescriptionId(itemStack)+"."+type+".cast_mold"):(super.getDescriptionId(itemStack)+"."+type);
		return super.getDescriptionId(itemStack);
	}
	@Override
	public @NotNull Component getName(@NotNull ItemStack itemStack){
		int rarityColor=getMaterial(itemStack).getColor();
		return Component.translatable(getDescriptionId(itemStack)).withStyle(Style.EMPTY.withColor(rarityColor).withItalic(false));
	}
}
