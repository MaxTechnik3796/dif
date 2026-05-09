package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.DifModCommonConfig;
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

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!(chest.getItem() instanceof Jetpack)) return;

		// Na serveru: doplňování Thrust z Main když stojí na zemi
		if (!player.level().isClientSide() && player.onGround()) {
			refillThrust(player, chest);
		}

		// Overlay na klientu
		if (player.level().isClientSide()) {
			showOverlay(player, chest);
		}
	}

	// Doplní Thrust z Main (nebo z inventáře) když hráč stojí na zemi
	private static void refillThrust(Player player, ItemStack chest) {
		int max = Jetpack.Chestplate.getMax();
		int thrust = Jetpack.Chestplate.getThrust(chest);
		if (thrust >= max) return;

		int main = Jetpack.Chestplate.getMain(chest);

		// Pokud Main > 0, přesuň vše do Thrust
		if (main > 0) {
			int toMove = Math.max(1, (int)(max * 0.02f)); // 2% za tick
			toMove = Math.min(toMove, main);
			toMove = Math.min(toMove, max - thrust);
			Jetpack.Chestplate.setMain(chest, main - toMove);
			Jetpack.Chestplate.setThrust(chest, thrust + toMove);
		} else {
			// Main je prázdný → vezmi 1 palivo z inventáře a naplň Main
			for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
				ItemStack s = player.getInventory().getItem(i);
				if (Jetpack.Chestplate.isFuel(s)) {
					s.shrink(1);
					Jetpack.Chestplate.setMain(chest, max);
					break;
				}
			}
		}

		// Synchronizuj klientovi
		if (player instanceof ServerPlayer sp) {
			PacketDistributor.sendToPlayer(sp, new JetpackSyncMessage(
					Jetpack.Chestplate.getMain(chest),
					Jetpack.Chestplate.getThrust(chest)
			));
		}
	}

	public static void fly(Player player) {
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!(chest.getItem() instanceof Jetpack)) return;

		int thrust = Jetpack.Chestplate.getThrust(chest);
		if (thrust <= 0) return;

		UUID uid = player.getUUID();
		float accel = MAX_VELOCITY / ACCEL_TICKS;
		float curVel = verticalVelocity.getOrDefault(uid, 0F);
		curVel = Math.min(curVel + accel, MAX_VELOCITY);
		verticalVelocity.put(uid, curVel);

		Vec3 motion = player.getDeltaMovement();
		player.setDeltaMovement(motion.x, curVel, motion.z);
		player.fallDistance = 0;

		// Spotřeba Thrust pouze na serveru
		if (!player.level().isClientSide()) {
			Jetpack.Chestplate.setThrust(chest, thrust - 1);

			// Synchronizuj klientovi
			if (player instanceof ServerPlayer sp) {
				PacketDistributor.sendToPlayer(sp, new JetpackSyncMessage(
						Jetpack.Chestplate.getMain(chest),
						Jetpack.Chestplate.getThrust(chest)
				));
			}
		}

		// Particles
		if (player.level() instanceof ServerLevel sl) {
			double angle = Math.toRadians(player.getYRot());
			double bx = player.getX() - Math.sin(angle) * 0.3;
			double by = player.getY() + 0.8;
			double bz = player.getZ() + Math.cos(angle) * 0.3;
			sl.sendParticles(ParticleTypes.FLAME, bx, by, bz, 2, -Math.sin(angle) * 0.05, -0.1, Math.cos(angle) * 0.05, 0.02);
		}
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

	@OnlyIn(Dist.CLIENT)
	private static void showOverlay(Player player, ItemStack chest) {
		int main = Jetpack.Chestplate.getMain(chest);
		int thrust = Jetpack.Chestplate.getThrust(chest);
		int max = Jetpack.Chestplate.getMax();
		int pct = max > 0 ? (thrust * 100) / max : 0;
		int invCount = Jetpack.Chestplate.countFuelInInventory(player);
		String invStr = invCount > 99 ? "99+" : String.valueOf(invCount);

		int filled = (thrust * 10) / Math.max(1, max);
		StringBuilder bar = new StringBuilder("[");
		for (int i = 0; i < 10; i++) bar.append(i < filled ? "■" : "□");
		bar.append("]");

		Component msg = Component.literal("🚀 ")
				.append(Component.literal(invStr + " ").withStyle(thrust <= 0 ? ChatFormatting.RED : ChatFormatting.YELLOW))
				.append(Component.literal(bar + " ").withStyle(ChatFormatting.AQUA))
				.append(Component.literal(pct + "%").withStyle(ChatFormatting.AQUA));

		player.displayClientMessage(msg, true);
	}
}