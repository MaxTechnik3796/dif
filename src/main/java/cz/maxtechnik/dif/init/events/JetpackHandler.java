package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
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
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = DifMod.MODID)
public class JetpackHandler {
	private static final Map<UUID, Float> verticalVelocity = new HashMap<>();
	private static final float MAX_VELOCITY = 0.5F;
	private static final float ACCEL_TICKS = 20F;
	private static final float DECEL_TICKS = 20F;

	// Spotřeba paliva v mB za tick
	private static final int FLY_COST = 1;   // normální let
	private static final int HOVER_COST = 1;  // hover = 5x levnější

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!(chest.getItem() instanceof Jetpack)) return;

		// Hover drží výšku / spotřebovává palivo (server i klient pro plynulost)
		tickHover(player, chest);

		// Overlay na klientu
		if (player.level().isClientSide()) {
			showOverlay(player, chest);
		}
	}

	public static void fly(Player player) {
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!(chest.getItem() instanceof Jetpack)) return;

		if (Jetpack.Chestplate.isOff(chest)) return;
		int fuel = Jetpack.Chestplate.getThrust(chest);
		if (fuel <= 0) return;

		UUID uid = player.getUUID();
		float accel = MAX_VELOCITY / ACCEL_TICKS;
		float curVel = verticalVelocity.getOrDefault(uid, 0F);
		curVel = Math.min(curVel + accel, MAX_VELOCITY);
		verticalVelocity.put(uid, curVel);

		Vec3 motion = player.getDeltaMovement();
		player.setDeltaMovement(motion.x, curVel, motion.z);
		player.fallDistance = 0;

		// Spotřeba paliva pouze na serveru
		if (!player.level().isClientSide()) {
			Jetpack.Chestplate.setThrust(chest, fuel - FLY_COST);
			syncFuel(player, chest);
		}

		spawnParticles(player);
	}

	public static void decelerate(Player player) {
		UUID uid = player.getUUID();
		float curVel = verticalVelocity.getOrDefault(uid, 0F);
		if (curVel <= 0) {
			verticalVelocity.remove(uid);
			return;
		}
		float decel = MAX_VELOCITY / DECEL_TICKS;
		curVel = Math.max(0, curVel - decel);
		verticalVelocity.put(uid, curVel);
		if (curVel > 0) {
			Vec3 motion = player.getDeltaMovement();
			player.setDeltaMovement(motion.x, curVel, motion.z);
			player.fallDistance = 0;
		}
	}

	public static void toggleHover(Player player) {
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!(chest.getItem() instanceof Jetpack)) return;

		int current = Jetpack.Chestplate.getMode(chest);
		int next = (current + 1) % 3; // 0→1→2→0
		// Nelze přejít do let/hover bez paliva
		if (next != 2 && Jetpack.Chestplate.getThrust(chest) <= 0) next = 2;
		Jetpack.Chestplate.setMode(chest, next);
		verticalVelocity.remove(player.getUUID());
		hoverTick.remove(player.getUUID());
	}

	// Hover: drží výšku ve vzduchu, pohyb do stran zůstává, particles, levnější spotřeba.
	// Funguje i po aktivaci na zemi (Mekanism styl) – na zemi nic nedělá ani nespotřebovává.
	private static final java.util.Map<UUID, Integer> hoverTick = new HashMap<>();

	public static void tickHover(Player player, ItemStack chest) {
		if (!Jetpack.Chestplate.isHovering(chest)) return;
		if (player.onGround()) return;

		int fuel = Jetpack.Chestplate.getThrust(chest);
		if (fuel <= 0) {
			Jetpack.Chestplate.setMode(chest, 2); // vypni při prázdné nádrži
			return;
		}

		// Drží výšku
		Vec3 motion = player.getDeltaMovement();
		double newY = 0;
		// Shift = klesání v hover módu
		if (player.isShiftKeyDown()) newY = -0.25;
		player.setDeltaMovement(motion.x, newY, motion.z);
		player.fallDistance = 0;

		// Spotřeba jen na serveru, 1 mB každých 5 ticků (5x levnější)
		if (!player.level().isClientSide()) {
			UUID uid = player.getUUID();
			int t = hoverTick.getOrDefault(uid, 0) + 1;
			if (t >= 5) {
				hoverTick.put(uid, 0);
				Jetpack.Chestplate.setThrust(chest, fuel - HOVER_COST);
				syncFuel(player, chest);
			} else {
				hoverTick.put(uid, t);
			}
		}
		spawnParticles(player);
	}

	private static void spawnParticles(Player player) {
		if (player.level() instanceof ServerLevel sl) {
			double angle = Math.toRadians(player.getYRot());
			double bx = player.getX() - Math.sin(angle) * 0.3;
			double by = player.getY() + 0.8;
			double bz = player.getZ() + Math.cos(angle) * 0.3;
			sl.sendParticles(ParticleTypes.FLAME, bx, by, bz, 2, -Math.sin(angle) * 0.05, -0.1, Math.cos(angle) * 0.05, 0.02);
		}
	}

	private static void syncFuel(Player player, ItemStack chest) {
		if (player instanceof ServerPlayer sp) {
			PacketDistributor.sendToPlayer(sp, new JetpackSyncMessage(Jetpack.Chestplate.getThrust(chest)));
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static void showOverlay(Player player, ItemStack chest) {
		int thrust = Jetpack.Chestplate.getThrust(chest);
		int max = Jetpack.Chestplate.getMax();
		int pct = max > 0 ? (thrust * 100) / max : 0;
		boolean hovering = Jetpack.Chestplate.isHovering(chest);

		int filled = (thrust * 10) / Math.max(1, max);
		StringBuilder bar = new StringBuilder("[");
		for (int i = 0; i < 10; i++) bar.append(i < filled ? "■" : "□");
		bar.append("]");

		boolean off = Jetpack.Chestplate.isOff(chest);
		ChatFormatting barColor = thrust <= 0 ? ChatFormatting.RED : (off ? ChatFormatting.GRAY : (hovering ? ChatFormatting.GREEN : ChatFormatting.AQUA));
		String icon = off ? "❌ " : (hovering ? "\uD83D\uDD12 " : "\uD83D\uDE80 "); // ❌ vypnuto / 🔒 hover / 🚀 let

		Component msg = Component.literal(icon)
				.append(Component.literal(bar + " ").withStyle(barColor))
				.append(Component.literal(pct + "%").withStyle(barColor));

		player.displayClientMessage(msg, true);
	}
}