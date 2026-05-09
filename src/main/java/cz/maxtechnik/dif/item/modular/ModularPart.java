package cz.maxtechnik.dif.item.modular;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static cz.maxtechnik.dif.item.modular.ModularBase.*;
public class ModularPart extends Item{
	public String defaultMaterial="Wood";
	public ModularPart(){
		super(new Properties().rarity(Rarity.EPIC).fireResistant());
	}
	@Override
	public void inventoryTick(@NotNull ItemStack itemStack,@NotNull Level world,@NotNull Entity entity,int slot,boolean selected){
		if(!world.isClientSide()){
			CompoundTag tag=Objects.requireNonNull(itemStack.get(D)).copyTag();
			if(isHead(itemStack)){
				if(!tag.contains("HeadMaterial")) tag.putString("HeadMaterial",defaultMaterial);
				if(!tag.contains("HeadDurability"))
					tag.putInt("HeadDurability",durabilityFromMaterial("Head",defaultMaterial));
				if(!tag.contains("HeadColor"))
					tag.putInt("HeadColor",colorIntFromMaterial(tag.getString("HeadMaterial")));
			}
			if(isBinding(itemStack)){
				if(!tag.contains("BindingMaterial")) tag.putString("BindingMaterial",defaultMaterial);
				if(!tag.contains("BindingDurability"))
					tag.putInt("BindingDurability",durabilityFromMaterial("Binding",defaultMaterial));
				if(!tag.contains("BindingColor"))
					tag.putInt("BindingColor",colorIntFromMaterial(tag.getString("BindingMaterial")));
			}
			if(isHandle(itemStack)){
				if(!tag.contains("HandleMaterial")) tag.putString("HandleMaterial",defaultMaterial);
				if(!tag.contains("HandleDurability"))
					tag.putInt("HandleDurability",durabilityFromMaterial("Handle",defaultMaterial));
				if(!tag.contains("HandleColor"))
					tag.putInt("HandleColor",colorIntFromMaterial(tag.getString("HandleMaterial")));
			}
		}
	}
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(@NotNull ItemStack itemStack, Item.@NotNull TooltipContext context, @NotNull List<Component> list, @NotNull TooltipFlag flag) {
		super.appendHoverText(itemStack, context, list, flag);
		var data = itemStack.get(D);
		if(data == null || data.copyTag().isEmpty()) return;
		CompoundTag tag = data.copyTag();
		if(!isHead(itemStack) && !isBinding(itemStack) && !isHandle(itemStack)) return;
		if(isHead(itemStack) && (!tag.contains("HeadDurability") || !tag.contains("HeadMaterial"))) return;
		if(isBinding(itemStack) && (!tag.contains("BindingDurability") || !tag.contains("BindingMaterial"))) return;
		if(isHandle(itemStack) && (!tag.contains("HandleDurability") || !tag.contains("HandleMaterial"))) return;
		String partType = getPartType(itemStack);
		list.add(Component.literal("Material:").append(CommonComponents.space()).append(Component.literal(tag.getString(partType + "Material")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(colorIntFromMaterial(tag.getString(partType + "Material")))))));
		list.add(Component.literal("Durability:").append(CommonComponents.space()).append(Component.literal(String.valueOf(tag.getInt(partType + "Durability"))).withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(durabilityColor(partType, tag))))));
		if(isHead(itemStack))
			list.add(Component.literal("Efficiency:").append(CommonComponents.space()).append(Component.literal(String.valueOf(ModularBase.efficiencyFromMaterial(tag.getString("HeadMaterial")))).withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.GREEN)))));
		list.add(CommonComponents.EMPTY);
		list.add(modifierTipFormMaterial(tag.getString(partType + "Material")));
	}
}