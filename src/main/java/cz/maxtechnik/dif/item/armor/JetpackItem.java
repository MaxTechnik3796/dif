package cz.maxtechnik.dif.item.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class JetpackItem extends ArmorItem {
	public static final int MAX_MAIN = 100;
	public static final int MAX_THRUST = 20;

	public JetpackItem(ArmorMaterial mat, Properties props) {
		super(mat, Type.CHESTPLATE, props.stacksTo(1).setNoRepair());
	}

	public static int getMainFuel(ItemStack s) {
		if (!s.hasTag() || !s.getTag().contains("MainFuel")) return 0;
		return s.getTag().getInt("MainFuel");
	}

	public static void setMainFuel(ItemStack s, int v) {
		s.getOrCreateTag().putInt("MainFuel", Mth.clamp(v, 0, MAX_MAIN));
	}

	public static int getThrustFuel(ItemStack s) {
		if (!s.hasTag() || !s.getTag().contains("ThrustFuel")) return 0;
		return s.getTag().getInt("ThrustFuel");
	}

	public static void setThrustFuel(ItemStack s, int v) {
		s.getOrCreateTag().putInt("ThrustFuel", Mth.clamp(v, 0, MAX_THRUST));
	}

	public static boolean isFuel(ItemStack stack) {
		return !stack.isEmpty() && stack.hasTag() && stack.getTag().getBoolean("JetpackFuel");
	}

	@Override public boolean isEnchantable(ItemStack s) { return false; }
	@Override public boolean isBookEnchantable(ItemStack s, ItemStack b) { return false; }

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || oldStack.getItem() != newStack.getItem();
	}

	@Override
	public boolean isBarVisible(ItemStack s) { return true; }

	@Override
	public int getBarWidth(ItemStack s) {
		return Math.round(13.0F * (float) getMainFuel(s) / (float) MAX_MAIN);
	}

	@Override
	public int getBarColor(ItemStack s) {
		float f = Math.max(0.0F, (float) getMainFuel(s) / (float) MAX_MAIN);
		return Mth.hsvToRgb(f * 0.33F, 1.0F, 1.0F);
	}

	@Override
	public void appendHoverText(ItemStack s, @Nullable Level l, List<Component> t, TooltipFlag f) {
		t.add(Component.literal("Main Storage: " + getMainFuel(s) + " / " + MAX_MAIN).withStyle(ChatFormatting.GRAY));
		t.add(Component.literal("Thrust Tank: " + getThrustFuel(s) + " / " + MAX_THRUST).withStyle(ChatFormatting.AQUA));
	}
}