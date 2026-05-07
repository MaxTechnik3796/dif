package cz.maxtechnik.dif.init.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TorchSavedData extends SavedData {
	private final Set<BlockPos> torches = ConcurrentHashMap.newKeySet();

	// Factory definice pro NeoForge 1.21.1
	private static final SavedData.Factory<TorchSavedData> FACTORY = new SavedData.Factory<>(
			TorchSavedData::new,
			TorchSavedData::load,
			null
	);

	public TorchSavedData() {
	}

	// Načtení z NBT - nyní vyžaduje HolderLookup.Provider
	public static TorchSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
		TorchSavedData data = new TorchSavedData();
		long[] array = tag.getLongArray("Torches");
		for (long posLong : array) {
			data.torches.add(BlockPos.of(posLong));
		}
		return data;
	}

	// Uložení do NBT - nyní vyžaduje HolderLookup.Provider
	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
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
		setDirty();
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
		// level.getDataStorage() ukládá data pro každou dimenzi zvlášť
		return level.getDataStorage().computeIfAbsent(FACTORY, "dif_mega_torches");
	}
}