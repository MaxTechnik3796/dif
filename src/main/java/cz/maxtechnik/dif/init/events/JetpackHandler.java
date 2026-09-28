package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.other.DifModKeys;
import cz.maxtechnik.dif.item.armor.Jetpack;
import cz.maxtechnik.dif.network.JetpackSyncMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@EventBusSubscriber(modid = DifMod.MODID)
public class JetpackHandler {
	private static final float FLY_SPEED = 0.5F;
	private static final int FLY_COST = 1;
	private static final int HOVER_COST = 1;
	private static final int HOVER_INTERVAL = 8; // V hover módu spotřebovává palivo každých 8 ticků (mnohem méně)
	private static final Map<UUID, Integer> lastFlyTick = new HashMap<>();
	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		lastFlyTick.remove(event.getEntity().getUUID());
	}
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!(chest.getItem() instanceof Jetpack)) return;
		tickHover(player, chest);
		if (player.level().isClientSide() && player.isLocalPlayer()) {
			showOverlay(player, chest);
		}
	}
	/**
	 * Aktivní let mezerníkem:
	 * - Mezerník: okamžitý vzlet konstantní rychlostí
	 * - Shift + Mezerník: stání na místě ve vzduchu
	 * - Pokaždé nuluje pádové zranění
	 */
	public static void fly(Player player) {
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!(chest.getItem() instanceof Jetpack) || Jetpack.Chestplate.isOff(chest)) return;
		int fuel = Jetpack.Chestplate.getThrust(chest);
		if (fuel <= 0) return;
		lastFlyTick.put(player.getUUID(), player.tickCount);
		Vec3 motion = player.getDeltaMovement();
		// Shift + Space = stání na místě, samotný Space = stoupání nahoru
		double yMotion = player.isShiftKeyDown() ? 0 : FLY_SPEED;
		player.setDeltaMovement(motion.x, yMotion, motion.z);
		resetFall(player);

		boolean hovering = Jetpack.Chestplate.isHovering(chest);
		if (!hovering || player.tickCount % HOVER_INTERVAL == 0) {
			consumeFuel(player, chest, hovering ? HOVER_COST : FLY_COST);
		}
		spawnParticles(player);
	}
	/**
	 * Přepínání módů: 0 = Let (Hover OFF), 1 = Hover (Hover ON), 2 = Vypnuto
	 */
	public static void toggleHover(Player player) {
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!(chest.getItem() instanceof Jetpack)) return;
		int next = (Jetpack.Chestplate.getMode(chest) + 1) % 3;
		if (next != 2 && Jetpack.Chestplate.getThrust(chest) <= 0) next = 2;
		Jetpack.Chestplate.setMode(chest, next);
	}
	/**
	 * Logika vznášení (Hover mód):
	 * - Na zemi je neaktivní
	 * - Pokud hráč stoupá mezerníkem, hover ustoupí aktivnímu letu
	 * - Shift = pomalé klesání, jinak visení na místě (lze létat s WASD dopředu/do stran)
	 * - Žere mnohem méně paliva než stoupání
	 */
	public static void tickHover(Player player, ItemStack chest) {
		if (!Jetpack.Chestplate.isHovering(chest) || player.onGround()) return;
		int fuel = Jetpack.Chestplate.getThrust(chest);
		if (fuel <= 0) {
			Jetpack.Chestplate.setMode(chest, 2); // Vypnout při vyčerpání paliva
			return;
		}
		// Pokud hráč zrovna mačká let (stoupá), hover nezasahuje
		if (isFlying(player)) return;
		Vec3 motion = player.getDeltaMovement();
		double newY = player.isShiftKeyDown() ? -0.25 : 0;
		player.setDeltaMovement(motion.x, newY, motion.z);
		resetFall(player);
		// V hoveru žere výrazně méně paliva
		if (player.tickCount % HOVER_INTERVAL == 0) {
			consumeFuel(player, chest, HOVER_COST);
		}
		spawnParticles(player);
	}
	private static boolean isFlying(Player player) {
		if (player.level().isClientSide()) {
			return DifModKeys.JETPACK_FLY.isDown();
		}
		return player.tickCount - lastFlyTick.getOrDefault(player.getUUID(), -99) <= 1;
	}
	private static void resetFall(Player player) {
		player.fallDistance = 0;
		player.resetFallDistance();
	}
	private static void consumeFuel(Player player, ItemStack chest, int amount) {
		if (!player.level().isClientSide()) {
			int current = Jetpack.Chestplate.getThrust(chest);
			Jetpack.Chestplate.setThrust(chest, Math.max(0, current - amount));
			syncFuel(player, chest);
		}
	}
	private static void syncFuel(Player player, ItemStack chest) {
		if (player instanceof ServerPlayer sp) {
			PacketDistributor.sendToPlayer(sp, new JetpackSyncMessage(Jetpack.Chestplate.getThrust(chest)));
		}
	}
	private static void spawnParticles(Player player) {
		double angle = Math.toRadians(player.getYRot());
		double bx = player.getX() + Math.sin(angle) * 0.3;
		double by = player.getY() + 0.8;
		double bz = player.getZ() - Math.cos(angle) * 0.3;
		double vx = Math.sin(angle) * 0.05;
		double vy = -0.1;
		double vz = -Math.cos(angle) * 0.05;
		if (player.level().isClientSide()) {
			player.level().addParticle(ParticleTypes.FLAME, bx, by, bz, vx, vy, vz);
		} else if (player.level() instanceof ServerLevel sl && player.tickCount % 4 == 0) {
			sl.sendParticles(ParticleTypes.FLAME, bx, by, bz, 1, vx, vy, vz, 0.02);
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static void showOverlay(Player player, ItemStack chest) {
		int thrust = Jetpack.Chestplate.getThrust(chest);
		int max = Jetpack.Chestplate.getMax();
		int pct = max > 0 ? (thrust * 100) / max : 0;
		boolean off = Jetpack.Chestplate.isOff(chest);
		boolean hovering = Jetpack.Chestplate.isHovering(chest);
		String status = off ? "OFF" : (hovering ? "Hover: ON" : "Hover: OFF");
		ChatFormatting color = thrust <= 0 ? ChatFormatting.RED : (off ? ChatFormatting.GRAY : (hovering ? ChatFormatting.GREEN : ChatFormatting.AQUA));
		Component msg = Component.literal(status + " | Thrust: " + thrust + " mB (" + pct + "%)").withStyle(color);
		player.displayClientMessage(msg, true);
	}
}