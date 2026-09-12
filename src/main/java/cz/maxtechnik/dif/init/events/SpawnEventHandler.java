package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.block.MegaTorch;
import cz.maxtechnik.dif.config.DifModServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent;

import java.util.Set;
@EventBusSubscriber(modid=cz.maxtechnik.dif.DifMod.MODID)
public class SpawnEventHandler{
	public static boolean isNearTorch(ServerLevel serverLevel,BlockPos pos){
		Set<BlockPos> torches=TorchSavedData.get(serverLevel).getTorches();
		if(torches.isEmpty()) return false;
		int radius=DifModServerConfig.MEGA_TORCH_RADIUS.get();
		int x=pos.getX();
		int z=pos.getZ();
		for(BlockPos torchPos: torches){
			if(Math.abs(torchPos.getX()-x)<=radius&&Math.abs(torchPos.getZ()-z)<=radius){
				return true;
			}
		}
		return false;
	}
	public static boolean isSpawnAllowed(EntityType<?> type,MobSpawnType spawnType){
		if(MobSpawnType.isSpawner(spawnType)) return true;
		if(spawnType==MobSpawnType.SPAWN_EGG||
				spawnType==MobSpawnType.COMMAND||
				spawnType==MobSpawnType.BREEDING||
				spawnType==MobSpawnType.BUCKET||
				spawnType==MobSpawnType.DISPENSER||
				spawnType==MobSpawnType.CONVERSION){
			return true;
		}
		return !type.is(MegaTorch.BLOCKED_MOBS);
	}
	@SubscribeEvent
	public static void onCheckSpawn(MobSpawnEvent.PositionCheck event){
		if(isSpawnAllowed(event.getEntity().getType(),event.getSpawnType())) return;
		ServerLevel serverLevel=event.getLevel().getLevel();
		BlockPos spawnPos=BlockPos.containing(event.getX(),event.getY(),event.getZ());
		if(isNearTorch(serverLevel,spawnPos)){
			event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
		}
	}
	@SubscribeEvent
	public static void onFinalizeSpawn(FinalizeSpawnEvent event){
		if(isSpawnAllowed(event.getEntity().getType(),event.getSpawnType())) return;
		ServerLevel serverLevel=event.getLevel().getLevel();
		BlockPos spawnPos=BlockPos.containing(event.getX(),event.getY(),event.getZ());
		if(isNearTorch(serverLevel,spawnPos)){
			event.setSpawnCancelled(true);
		}
	}
	@SubscribeEvent
	public static void onPlayerSpawnPhantoms(PlayerSpawnPhantomsEvent event){
		if(!EntityType.PHANTOM.is(MegaTorch.BLOCKED_MOBS)) return;
		Player player=event.getEntity();
		if(player.level() instanceof ServerLevel serverLevel){
			if(isNearTorch(serverLevel,player.blockPosition())){
				event.setResult(PlayerSpawnPhantomsEvent.Result.DENY);
			}
		}
	}
}