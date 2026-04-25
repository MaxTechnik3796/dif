package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.DifModCommonConfig;
import cz.maxtechnik.dif.block.MegaTorch;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;
@Mod.EventBusSubscriber(modid=DifMod.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class SpawnEventHandler{
	@SubscribeEvent
	public static void onCheckSpawn(MobSpawnEvent.PositionCheck event){
		if(!event.getEntity().getType().is(MegaTorch.BLOCKED_MOBS)) return;
		MobSpawnType spawnType=event.getSpawnType();
		if(spawnType!=MobSpawnType.NATURAL&&spawnType!=MobSpawnType.CHUNK_GENERATION&&spawnType!=MobSpawnType.PATROL&&spawnType!=MobSpawnType.STRUCTURE)
			return;
		// Získáme rovnou ServerLevel, na Forge eventech spawnu už to garantovaně běží na serverové straně
		ServerLevel serverLevel=event.getLevel().getLevel();
		BlockPos spawnPos=BlockPos.containing(event.getX(),event.getY(),event.getZ());
		// Načteme pozice našich torčí ze SavedData
		Set<BlockPos> torches=TorchSavedData.get(serverLevel).getTorches();
		for(BlockPos torchPos: torches){
			if(Math.abs(torchPos.getX()-spawnPos.getX())<=DifModCommonConfig.megaTorchRadius&&Math.abs(torchPos.getZ()-spawnPos.getZ())<=DifModCommonConfig.megaTorchRadius){
				event.setResult(Event.Result.DENY);
				return;
			}
		}
	}
}