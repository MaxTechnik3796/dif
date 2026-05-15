package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifModCommonConfig;
import cz.maxtechnik.dif.block.MegaTorch;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

import java.util.Set;

import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = cz.maxtechnik.dif.DifMod.MODID)
public class SpawnEventHandler {
	@SubscribeEvent
	public static void onCheckSpawn(MobSpawnEvent.PositionCheck event) {
		if (!event.getEntity().getType().is(MegaTorch.BLOCKED_MOBS)) return;

		MobSpawnType spawnType = event.getSpawnType();
		if (spawnType != MobSpawnType.NATURAL && spawnType != MobSpawnType.CHUNK_GENERATION &&
				spawnType != MobSpawnType.PATROL && spawnType != MobSpawnType.STRUCTURE)
			return;

		ServerLevel serverLevel = event.getLevel().getLevel();
		BlockPos spawnPos = BlockPos.containing(event.getX(), event.getY(), event.getZ());

		Set<BlockPos> torches = TorchSavedData.get(serverLevel).getTorches();
		for (BlockPos torchPos : torches) {
			if (Math.abs(torchPos.getX() - spawnPos.getX()) <= DifModCommonConfig.MEGA_TORCH_RADIUS.get() &&
					Math.abs(torchPos.getZ() - spawnPos.getZ()) <= DifModCommonConfig.MEGA_TORCH_RADIUS.get()) {

				// OPRAVA PRO NEOFORGE 1.21.1
				event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
				return;
			}
		}
	}
}