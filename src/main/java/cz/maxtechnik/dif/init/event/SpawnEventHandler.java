package cz.maxtechnik.dif.init.event;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.MegaTorch;
import cz.maxtechnik.dif.config.DifModServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
@EventBusSubscriber(modid=DifMod.MODID)
public class SpawnEventHandler extends SavedData{
	private final Set<BlockPos> torches=new HashSet<>();
	private static final Factory<SpawnEventHandler> FACTORY=new Factory<>(SpawnEventHandler::new,SpawnEventHandler::load,null);
	public static SpawnEventHandler get(ServerLevel level){
		return level.getDataStorage().computeIfAbsent(FACTORY,"dif_mega_torches");
	}
	public static SpawnEventHandler load(CompoundTag tag,HolderLookup.Provider registries){
		SpawnEventHandler data=new SpawnEventHandler();
		for(long posLong: tag.getLongArray("Torches")){
			data.torches.add(BlockPos.of(posLong));
		}
		return data;
	}
	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider registries){
		long[] array=new long[torches.size()];
		int i=0;
		for(BlockPos pos: torches) array[i++]=pos.asLong();
		tag.putLongArray("Torches",array);
		return tag;
	}
	public void addTorch(BlockPos pos){
		if(torches.add(pos)) setDirty();
	}
	public void removeTorch(BlockPos pos){
		if(torches.remove(pos)) setDirty();
	}
	public static boolean isNearTorch(ServerLevel level,BlockPos pos){
		Set<BlockPos> torches=get(level).torches;
		if(torches.isEmpty()) return false;
		int radius=DifModServerConfig.MEGA_TORCH_RADIUS.get();
		for(BlockPos torch: torches){
			if(Math.abs(torch.getX()-pos.getX())<=radius&&Math.abs(torch.getZ()-pos.getZ())<=radius){
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
		ServerLevel level=event.getLevel().getLevel();
		BlockPos pos=BlockPos.containing(event.getX(),event.getY(),event.getZ());
		if(isNearTorch(level,pos)){
			event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
		}
	}
	@SubscribeEvent
	public static void onPlayerSpawnPhantoms(PlayerSpawnPhantomsEvent event){
		if(!EntityType.PHANTOM.is(MegaTorch.BLOCKED_MOBS)) return;
		if(event.getEntity().level() instanceof ServerLevel level){
			if(isNearTorch(level,event.getEntity().blockPosition())){
				event.setResult(PlayerSpawnPhantomsEvent.Result.DENY);
			}
		}
	}
}