package cz.maxtechnik.dif.init.events;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TorchSavedData extends SavedData {
	private final Set<BlockPos> torches = ConcurrentHashMap.newKeySet();

	// Načtení z NBT při startu světa
	public static TorchSavedData load(CompoundTag tag) {
		TorchSavedData data = new TorchSavedData();
		long[] array = tag.getLongArray("Torches");
		for (long posLong : array) {
			data.torches.add(BlockPos.of(posLong));
		}
		return data;
	}

	// Uložení do NBT na disk
	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
		long[] array = new long[torches.size()];
		int i = 0;
		for (BlockPos pos : torches) {
			array[i++] = pos.asLong();
		}
		tag.putLongArray("Torches", array);
		return tag;
	}

	public void addTorch(BlockPos pos) {
		torches.add(pos);
		setDirty(); // Značí, že data byla změněna a musí se při nejbližší příležitosti uložit
	}

	public void removeTorch(BlockPos pos) {
		torches.remove(pos);
		setDirty();
	}

	public Set<BlockPos> getTorches() {
		return torches;
	}

	// Získání nebo vytvoření dat pro daný ServerLevel
	public static TorchSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(
				TorchSavedData::load,
				TorchSavedData::new,
				"dif_mega_torches"
		);
	}
}