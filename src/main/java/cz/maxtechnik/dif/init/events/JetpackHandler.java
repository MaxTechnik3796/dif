package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.armor.JetpackItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = DifMod.MODID)
public class JetpackHandler {
	private static Field jumpingField;

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		// Logiku provádíme pouze na konci ticku a pouze na serveru (kvůli manipulaci s itemy)
		if (event.phase != TickEvent.Phase.END) return;

		Player player = event.player;
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);

		if (!(chest.getItem() instanceof JetpackItem)) return;

		// Načteme hodnoty
		int main = JetpackItem.getMainFuel(chest);
		int thrust = JetpackItem.getThrustFuel(chest);

		// --- 1. DOPLNĚNÍ MAINU Z HOTBARU ---
		// Pokud je v mainu 0, zkusíme najít uhlí v hotbaru
		if (main <= 0) {
			for (int i = 0; i < 9; i++) {
				ItemStack fuelStack = player.getInventory().getItem(i);
				if (JetpackItem.isFuel(fuelStack)) {
					if (!player.level().isClientSide) {
						fuelStack.shrink(1);
						JetpackItem.setMainFuel(chest, JetpackItem.MAX_MAIN);
						player.displayClientMessage(Component.literal("Jetpack refueled!").withStyle(ChatFormatting.GREEN), true);
					}
					return; // V tomto ticku už nic víc neděláme, aby se data stačila zapsat
				}
			}
		}

		// --- 2. DOBÍJENÍ THRUSTU Z MAINU (Na zemi) ---
		if (player.onGround()) {
			if (thrust < JetpackItem.MAX_THRUST && main > 0) {
				// Přeléváme 2 fuel za tick
				int toAdd = Math.min(2, Math.min(main, JetpackItem.MAX_THRUST - thrust));
				JetpackItem.setMainFuel(chest, main - toAdd);
				JetpackItem.setThrustFuel(chest, thrust + toAdd);
				showOverlay(player, thrust + toAdd, true);
			}
		}
		// --- 3. LET (Ve vzduchu) ---
		else {
			boolean isJumping = false;
			try {
				if (jumpingField == null) {
					try { jumpingField = LivingEntity.class.getDeclaredField("jumping"); }
					catch (NoSuchFieldException e) { jumpingField = LivingEntity.class.getDeclaredField("f_20899_"); }
					jumpingField.setAccessible(true);
				}
				isJumping = jumpingField.getBoolean(player);
			} catch (Exception ignored) {}

			if (isJumping && thrust > 0 && !player.getAbilities().flying) {
				player.setDeltaMovement(player.getDeltaMovement().x, 0.42, player.getDeltaMovement().z);
				player.fallDistance = 0;
				JetpackItem.setThrustFuel(chest, thrust - 1);
				showOverlay(player, thrust - 1, false);
			}
		}
	}

	private static void showOverlay(Player player, int thrust, boolean charging) {
		// Zobrazujeme pouze pokud se něco děje (let nebo nabíjení)
		String icon = charging ? "⚡ " : "🚀 ";
		ChatFormatting color = charging ? ChatFormatting.YELLOW : ChatFormatting.AQUA;
		int filled = thrust / 2;
		String bar = "■".repeat(Math.max(0, filled)) + "□".repeat(Math.max(0, 10 - filled));

		player.displayClientMessage(
				Component.literal(icon + "THRUST: [" + bar + "] " + (thrust * 5) + "%").withStyle(color),
				true
		);
	}
}