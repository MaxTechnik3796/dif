package cz.maxtechnik.dif.item.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class Jetpack extends ArmorItem {
	public static final int MAX_MAIN = 100;
	public static final int MAX_TANK = 20;

	public Jetpack(ArmorMaterial mat,Properties props) {
		super(mat, Type.CHESTPLATE, props.defaultDurability(MAX_MAIN).setNoRepair());
	}

	// --- NBT POMOCNÉ METODY ---
	public static int getMainFuel(ItemStack s) {
		CompoundTag tag = s.getTag();
		return tag != null ? tag.getInt("MainFuel") : 0;
	}

	public static void setMainFuel(ItemStack s, int v) {
		s.getOrCreateTag().putInt("MainFuel", Math.max(0, Math.min(v, MAX_MAIN)));
	}

	public static int getTankFuel(ItemStack s) {
		CompoundTag tag = s.getTag();
		return tag != null ? tag.getInt("TankFuel") : 0;
	}

	public static void setTankFuel(ItemStack s, int v) {
		s.getOrCreateTag().putInt("TankFuel", Math.max(0, Math.min(v, MAX_TANK)));
	}

	// --- LOGIKA PALIVA (HOTBAR REFILL) ---
	public static boolean isFuel(ItemStack stack) {
		return !stack.isEmpty() && stack.hasTag() && stack.getTag().getBoolean("JetpackFuel");
	}

	// --- ZÁKAZ ENCHANTU ---
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return false;
	}

	// --- DAMAGE BAR (Main Storage) ---
	@Override
	public boolean isBarVisible(ItemStack stack) {
		return getMainFuel(stack) < MAX_MAIN;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13.0F * (float) getMainFuel(stack) / (float) MAX_MAIN);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		float f = (float) getMainFuel(stack) / (float) MAX_MAIN;
		return Mth.hsvToRgb(f * 0.6F, 1.0F, 1.0F);
	}

	@Override
	public void appendHoverText(ItemStack s, @Nullable Level l, List<Component> t, TooltipFlag f) {
		t.add(Component.literal("Main Tank: " + getMainFuel(s) + " / " + MAX_MAIN).withStyle(ChatFormatting.GRAY));
		t.add(Component.literal("Thruster: " + getTankFuel(s) + " / " + MAX_TANK).withStyle(ChatFormatting.AQUA));
	}
}