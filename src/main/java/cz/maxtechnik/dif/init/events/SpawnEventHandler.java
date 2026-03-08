package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.MegaTorchRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber(modid = DifMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpawnEventHandler {

	@SubscribeEvent
	public static void onCheckSpawn(MobSpawnEvent.PositionCheck event) {
		if (!event.getEntity().getType().is(MegaTorchRegistry.BLOCKED_MOBS)) {
			return;
		}
		MobSpawnType spawnType = event.getSpawnType();
		if (spawnType != MobSpawnType.NATURAL
				&& spawnType != MobSpawnType.CHUNK_GENERATION
				&& spawnType != MobSpawnType.PATROL
				&& spawnType != MobSpawnType.STRUCTURE) {
			return;
		}
		BlockPos spawnPos = BlockPos.containing(event.getX(), event.getY(), event.getZ());

		int radius = 128;

		Set<BlockPos> torches = MegaTorchRegistry.getTorches(event.getLevel().getLevel());
		for (BlockPos torchPos : torches) {
			int dx = Math.abs(torchPos.getX() - spawnPos.getX());
			int dz = Math.abs(torchPos.getZ() - spawnPos.getZ());
			if (dx <= radius && dz <= radius) {
				event.setResult(Event.Result.DENY);
				return;
			}
		}
	}
}