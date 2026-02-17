package cz.maxtechnik.dif.item.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
public class JetpackItem extends ArmorItem{
	public static final int MAX_MAIN=100;
	public static final int MAX_THRUST=20;
	public JetpackItem(){
		super(ArmorMaterials.IRON,Type.CHESTPLATE,new Item.Properties().stacksTo(1));
	}
	public static int getMainFuel(ItemStack itemStack){
		if(!itemStack.hasTag()||!Objects.requireNonNull(itemStack.getTag()).contains("MainFuel")) return 0;
		return itemStack.getTag().getInt("MainFuel");
	}
	public static void setMainFuel(ItemStack itemStack,int value){
		assert itemStack.getTag()!=null;
		itemStack.getTag().putInt("MainFuel",Mth.clamp(value,0,MAX_MAIN));
	}
	public static int getThrustFuel(ItemStack itemStack){
		if(!itemStack.hasTag()||!Objects.requireNonNull(itemStack.getTag()).contains("ThrustFuel")) return 0;
		return itemStack.getTag().getInt("ThrustFuel");
	}
	public static void setThrustFuel(ItemStack itemStack,int value){
		assert itemStack.getTag()!=null;
		itemStack.getTag().putInt("ThrustFuel",Mth.clamp(value,0,MAX_THRUST));
	}
	public static boolean isFuel(ItemStack stack){
		if(stack.isEmpty()||!stack.hasTag()) return false;
		assert stack.getTag()!=null;
		return stack.getTag().getBoolean("JetpackFuel");
	}
	@Override
	public boolean isEnchantable(@NotNull ItemStack itemStack){
		return false;
	}
	@Override
	public boolean isBookEnchantable(ItemStack s,ItemStack b){
		return false;
	}
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack,ItemStack newStack,boolean slotChanged){
		return slotChanged||oldStack.getItem()!=newStack.getItem();
	}
	@Override
	public boolean isBarVisible(@NotNull ItemStack itemStack){
		return true;
	}
	@Override
	public int getBarWidth(@NotNull ItemStack itemStack){
		return Math.round(13.0F*(float)getMainFuel(itemStack)/(float)MAX_MAIN);
	}
	@Override
	public int getBarColor(@NotNull ItemStack itemStack){
		float f=Math.max(0.0F,(float)getMainFuel(itemStack)/(float)MAX_MAIN);
		return Mth.hsvToRgb(f*0.33F,1.0F,1.0F);
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemStack,@Nullable Level world,List<Component> list,@NotNull TooltipFlag flag){
		list.add(Component.literal("Main Storage: "+getMainFuel(itemStack)+" / "+MAX_MAIN).withStyle(ChatFormatting.GRAY));
		list.add(Component.literal("Thrust Tank: "+getThrustFuel(itemStack)+" / "+MAX_THRUST).withStyle(ChatFormatting.AQUA));
	}
}