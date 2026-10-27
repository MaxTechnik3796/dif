package cz.maxtechnik.dif.init.event.nuke;

import cz.maxtechnik.dif.network.NukeDetonationMessage;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;

public class NukeVisualHandler {
	public static void triggerMainDetonation(ServerLevel level, double x, double y, double z) {
		for (ServerPlayer player : level.getPlayers(p -> p.distanceToSqr(x, y, z) < 600 * 600)) {
			PacketDistributor.sendToPlayer(player, new NukeDetonationMessage(x, y, z, 1.0F));
		}
		level.playSound(null, x, y, z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS, 35.0F, 0.5F);
		level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 35.0F, 0.5F);
		level.playSound(null, x, y, z, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 30.0F, 0.7F);
		level.playSound(null, x, y, z, SoundEvents.WITHER_DEATH, SoundSource.BLOCKS, 25.0F, 0.55F);
		level.playSound(null, x, y, z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 35.0F, 0.6F);
		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(x, y, z) < 512 * 512) {
				level.sendParticles(player, ParticleTypes.FLASH, true, x, y + 1.5, z, 2, 0, 0, 0, 0);
				level.sendParticles(player, ParticleTypes.EXPLOSION_EMITTER, true, x, y + 1.5, z, 1, 0, 0, 0, 0);
			}
		}
	}
}