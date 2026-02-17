package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.armor.Jetpack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = DifMod.MODID)
public class JetpackHandler {
	private static Field jumpingField;
	// Mapa pro sledování, zda byl hráč v minulém ticku na zemi
	private static final Map<UUID, Boolean> wasOnGround = new HashMap<>();

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		Player player = event.player;
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);

		// Uložíme si stav 'na zemi' z minulého ticku (default true, pokud neexistuje)
		boolean previousOnGround = wasOnGround.getOrDefault(player.getUUID(), true);

		// Na konci metody aktualizujeme mapu pro příští tick
		wasOnGround.put(player.getUUID(), player.onGround());

		if (!(chest.getItem() instanceof Jetpack)) return;

		// --- DETEKCE SKOKU (Reflection) ---
		boolean isJumping;
		try {
			if (jumpingField == null) {
				try {
					jumpingField = LivingEntity.class.getDeclaredField("jumping");
				} catch (NoSuchFieldException e) {
					jumpingField = LivingEntity.class.getDeclaredField("f_20899_");
				}
				jumpingField.setAccessible(true);
			}
			isJumping = jumpingField.getBoolean(player);
		} catch (Exception e) {
			return;
		}

		int tank = Jetpack.getTankFuel(chest);
		int main = Jetpack.getMainFuel(chest);

		// --- LOGIKA LETU ---
		// Podmínka: Hráč skáče, není na zemi, neletí creativně...
		// A HLAVNĚ: V minulém ticku NESMĚL být na zemi.
		// To zajistí, že první stisk mezerníku je jen výskok, a let začne až ve vzduchu.
		if (isJumping && !player.onGround() && !player.getAbilities().flying && !previousOnGround) {
			if (tank > 0) {
				Vec3 v = player.getDeltaMovement();
				player.setDeltaMovement(v.x, 0.4, v.z);
				player.fallDistance = 0;

				Jetpack.setTankFuel(chest, tank - 1);
				showOverlay(player, tank - 1, false);
			}
		}
		// --- LOGIKA DOPLŇOVÁNÍ (Na zemi) ---
		else if (player.onGround()) {

			// 1. Doplnění operační nádrže z hlavní nádrže
			if (tank < Jetpack.MAX_TANK && main > 0) {
				int toAdd = Math.min(1, Jetpack.MAX_TANK - tank);
				toAdd = Math.min(toAdd, main);

				if (toAdd > 0) {
					if (!player.level().isClientSide()) {
						// SERVER: Skutečná změna dat
						Jetpack.setMainFuel(chest, main - toAdd);
						Jetpack.setTankFuel(chest, tank + toAdd);
						player.getInventory().setChanged();
					} else {
						// KLIENT: Vizuální simulace (aby se odečítal i Main Bar plynule)
						// Tím se opraví ten bug, že to vypadá, že se nedoplňuje
						Jetpack.setMainFuel(chest, main - toAdd);
						Jetpack.setTankFuel(chest, tank + toAdd);
					}
					showOverlay(player, tank + toAdd, true);
				}
			}

			// 2. Automatické doplnění hlavní nádrže z hotbaru (každé 2 vteřiny)
			if (main == 0) {
				if (player.level().getGameTime() % 40 == 0) {
					for (int i = 0; i < 9; i++) {
						ItemStack fuelStack = player.getInventory().getItem(i);
						if (Jetpack.isFuel(fuelStack)) {
							if (!player.level().isClientSide()) {
								fuelStack.shrink(1);
								Jetpack.setMainFuel(chest, 100);
								player.getInventory().setChanged();
								player.displayClientMessage(Component.literal("§aPalivo doplněno z hotbaru!"), true);
							}
							break;
						}
					}
				}
			}
		}
	}

	private static void showOverlay(Player player, int tank, boolean recharging) {
		if (tank < Jetpack.MAX_TANK || recharging) {
			String icon = recharging ? "⚡ " : "🚀 ";
			ChatFormatting color = recharging ? ChatFormatting.YELLOW : ChatFormatting.AQUA;

			int percent = (int) ((tank / (float) Jetpack.MAX_TANK) * 100);
			int filledBlocks = tank / 2;

			String bar = "■".repeat(Math.max(0, filledBlocks)) + "□".repeat(Math.max(0, 10 - filledBlocks));
			player.displayClientMessage(
					Component.literal(icon + "THRUST: [" + bar + "] " + percent + "%").withStyle(color),
					true
			);
		}
	}
}