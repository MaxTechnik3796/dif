package cz.maxtechnik.dif.item.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
public class JetpackItem extends ArmorItem{
	public static final int MAX_MAIN=10000;
	public static final int MAX_TANK=200;
	public JetpackItem(ArmorMaterial mat,Properties props){
		super(mat,Type.CHESTPLATE,props.defaultDurability(0).setNoRepair());
	}
	public static int getMainFuel(ItemStack itemStack){
		CompoundTag tag=itemStack.getTag();
		return tag!=null?tag.getInt("MainFuel"):0;
	}
	public static void setMainFuel(ItemStack itemStack,int v){
		itemStack.getOrCreateTag().putInt("MainFuel",Math.max(0,Math.min(v,MAX_MAIN)));
	}
	public static int getTankFuel(ItemStack itemStack){
		CompoundTag tag=itemStack.getTag();
		return tag!=null?tag.getInt("TankFuel"):0;
	}
	public static void setTankFuel(ItemStack itemStack,int v){
		itemStack.getOrCreateTag().putInt("TankFuel",Math.max(0,Math.min(v,MAX_TANK)));
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemStack,@Nullable Level world,List<Component> list,@NotNull TooltipFlag flag){
		list.add(Component.literal("Hlavní nádrž: ").withStyle(ChatFormatting.GOLD).append(Component.literal(getMainFuel(itemStack)+" / "+MAX_MAIN).withStyle(ChatFormatting.WHITE)));
		list.add(Component.literal("Operativní nádrž: ").withStyle(ChatFormatting.AQUA).append(Component.literal(getTankFuel(itemStack)+" / "+MAX_TANK).withStyle(ChatFormatting.WHITE)));
	}
}