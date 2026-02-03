package cz.maxtechnik.dif.item.modular;

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
import org.jetbrains.annotations.NotNull;
import java.util.List;
import static cz.maxtechnik.dif.item.modular.ModularBase.*;
public class ModularPart extends Item{
	public String defaultMaterial="Wood";
	public ModularPart(){
		super(new Properties().rarity(Rarity.EPIC));
	}
	@Override
	public void inventoryTick(@NotNull ItemStack itemStack,@NotNull Level world,@NotNull Entity entity,int slot,boolean selected){
		if(!world.isClientSide()){
			CompoundTag tag=itemStack.getOrCreateTag();
			if(isHead(itemStack)){
				if(!tag.contains("HeadMaterial"))tag.putString("HeadMaterial",defaultMaterial);
				if(!tag.contains("HeadDurability"))tag.putInt("HeadDurability",durabilityFromMaterial("Head",tag));
				if(!tag.contains("HeadColor"))tag.putInt("HeadColor",colorFromMaterial(tag.getString("HeadMaterial")));
			}
			if(isBinding(itemStack)){
				if(!tag.contains("BindingMaterial"))tag.putString("BindingMaterial",defaultMaterial);
				if(!tag.contains("BindingDurability"))tag.putInt("BindingDurability",durabilityFromMaterial("Binding",tag));
				if(!tag.contains("BindingColor")) tag.putInt("BindingColor",colorFromMaterial(tag.getString("BindingMaterial")));
			}
			if(isHandle(itemStack)){
				if(!tag.contains("HandleMaterial"))tag.putString("HandleMaterial",defaultMaterial);
				if(!tag.contains("HandleDurability"))tag.putInt("HandleDurability",durabilityFromMaterial("Handle",tag));
				if(!tag.contains("HandleColor"))tag.putInt("HandleColor",colorFromMaterial(tag.getString("HandleMaterial")));
			}
		}
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemStack,Level world,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		if(!itemStack.hasTag()) return;
		assert itemStack.getTag()!=null;
		CompoundTag tag=itemStack.getTag();
		if(!isHead(itemStack)&&!isBinding(itemStack)&&!isHandle(itemStack))return;
		if(isHead(itemStack)&&(!tag.contains("HeadDurability")||!tag.contains("HeadMaterial")))return;
		if(isBinding(itemStack)&&(!tag.contains("BindingDurability")||!tag.contains("BindingMaterial")))return;
		if(isHandle(itemStack)&&(!tag.contains("HandleDurability")||!tag.contains("HandleMaterial")))return;
		String partType=getPartType(itemStack);
		list.add(Component.literal("Material:").append(CommonComponents.space()).append(Component.literal(tag.getString(partType+"Material")).withStyle(Style.EMPTY.withColor(TextColor.parseColor(materialColor(partType,tag))))));
		list.add(Component.literal("Durability:").append(CommonComponents.space()).append(Component.literal(String.valueOf(tag.getInt(partType+"Durability"))).withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(durabilityColor(partType,tag))))));

	}
}