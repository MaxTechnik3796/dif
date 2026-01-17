package cz.maxtechnik.dif.item.modular;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.stringtemplate.v4.ST;

import java.util.List;
public class ModularPart extends Item{
	protected String defaultMaterial="Wood";
	protected int defaultDurability=3;
	public ModularPart(){
		super(new Properties());
	}
	public boolean isHead(ItemStack itemStack){
		return ModularRepairRecipe.isTagged(itemStack,"dif","modular_tools_parts/head");
	}
	public boolean isBinding(ItemStack itemStack){
		return ModularRepairRecipe.isTagged(itemStack,"dif","modular_tools_parts/binding");
	}
	public boolean isHandle(ItemStack itemStack){
		return ModularRepairRecipe.isTagged(itemStack,"dif","modular_tools_parts/handle");
	}
	@Override
	public void inventoryTick(@NotNull ItemStack itemStack,@NotNull Level world,@NotNull Entity entity,int slot,boolean selected){
		if(!world.isClientSide()){
			CompoundTag tag=itemStack.getOrCreateTag();
			if(!tag.contains("Material")) tag.putString("Material","Wood");
			if(!tag.contains("Durability")) tag.putInt("Durability",defaultDurability);
			if(isHead(itemStack)&&!tag.contains("HeadColor"))tag.putInt("HeadColor",0xFFFFFF);
			if(isBinding(itemStack)&&!tag.contains("HandleColor"))tag.putInt("HandleColor",0x915A2D);
			if(isHandle(itemStack)&&!tag.contains("BindingColor"))tag.putInt("BindingColor",0xFFFF00);



		}
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemStack,Level world,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		if(!itemStack.hasTag()) return;
		assert itemStack.getTag()!=null;
		CompoundTag tag=itemStack.getTag();
		if(!tag.contains("Durability")||!tag.contains("Material"))return;
		ChatFormatting dColor=ChatFormatting.DARK_AQUA;
		if(tag.getInt("Durability")>0){
			dColor=ChatFormatting.GREEN;
		}else if(tag.getInt("Durability")<0){
			dColor=ChatFormatting.RED;
		}
		String mColor="#FFFFFF";
		switch(tag.getString("Material")){
			case "Wood"->mColor="#915A2D";
			case "Stone"->mColor="##555555";
			case "Iron"->mColor="#C6C6C6";
			case "Gold"->mColor="#D6C400";
			case "Diamond"->mColor="#55FFFF";
			case "Netherite"->mColor="#301100";
		}
		list.add(Component.literal("Material: ").append(Component.literal(defaultMaterial).withStyle(Style.EMPTY.withColor(TextColor.parseColor(mColor)))));
		list.add(Component.literal("Durability: ").append(Component.literal(String.valueOf(tag.getInt("Durability"))).withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(dColor)))));

	}
}