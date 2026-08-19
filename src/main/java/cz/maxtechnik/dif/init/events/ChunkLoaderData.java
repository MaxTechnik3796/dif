package cz.maxtechnik.dif.init.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChunkLoaderData extends SavedData {
	public final List<LoaderRecord> loaders = new ArrayList<>();

	public record LoaderRecord(BlockPos pos, UUID uuid, String name, boolean active, int radius, ResourceLocation dimension) {}

	private static final SavedData.Factory<ChunkLoaderData> FACTORY = new SavedData.Factory<>(
			ChunkLoaderData::new,
			ChunkLoaderData::load,
			null
	);

	public static ChunkLoaderData get(ServerLevel level) {
		return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, "dif_loaders");
	}

	// --- Records ---

	public void updateRecord(BlockPos pos, UUID uuid, String name, boolean active, int radius, ResourceLocation dimension) {
		loaders.removeIf(r -> r.pos.equals(pos) && r.dimension.equals(dimension));
		loaders.add(new LoaderRecord(pos, uuid, name, active, radius, dimension));
		setDirty();
	}

	public List<LoaderRecord> getLoadersForDimension(ResourceKey<Level> dimKey) {
		ResourceLocation dimId = dimKey.location();
		return loaders.stream().filter(r -> r.dimension.equals(dimId)).toList();
	}

	// --- NBT ---

	public static ChunkLoaderData load(CompoundTag tag, HolderLookup.Provider registries) {
		ChunkLoaderData data = new ChunkLoaderData();
		ListTag list = tag.getList("loaders", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			int radius = 0;
			if (entry.contains("r")) radius = entry.getInt("r");
			else if (entry.contains("s")) radius = entry.getBoolean("s") ? 1 : 0;
			ResourceLocation dimension = entry.contains("d")
					? ResourceLocation.parse(entry.getString("d"))
					: Level.OVERWORLD.location();
			data.loaders.add(new LoaderRecord(
					BlockPos.of(entry.getLong("p")),
					entry.getUUID("u"),
					entry.getString("n"),
					entry.getBoolean("a"),
					radius,
					dimension
			));
		}
		return data;
	}

	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
		ListTag list = new ListTag();
		for (LoaderRecord r : loaders) {
			CompoundTag entry = new CompoundTag();
			entry.putLong("p", r.pos.asLong());
			entry.putUUID("u", r.uuid);
			entry.putString("n", r.name);
			entry.putBoolean("a", r.active);
			entry.putInt("r", r.radius);
			entry.putString("d", r.dimension.toString());
			list.add(entry);
		}
		tag.put("loaders", list);
		return tag;
	}
}