package cz.maxtechnik.dif.item.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class JetpackItem extends ArmorItem {
	public static final int MAX_MAIN = 10000;
	public static final int MAX_TANK = 200;

	public JetpackItem(ArmorMaterial mat, Properties props) {
		super(mat, Type.CHESTPLATE, props.defaultDurability(0).setNoRepair());
	}

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

	@Override
	public void appendHoverText(ItemStack s, @Nullable Level l, List<Component> t, TooltipFlag f) {
		t.add(Component.literal("Hlavní nádrž: ").withStyle(ChatFormatting.GOLD)
				.append(Component.literal(getMainFuel(s) + " / " + MAX_MAIN).withStyle(ChatFormatting.WHITE)));
		t.add(Component.literal("Operativní nádrž: ").withStyle(ChatFormatting.AQUA)
				.append(Component.literal(getTankFuel(s) + " / " + MAX_TANK).withStyle(ChatFormatting.WHITE)));
	}
}