package cz.maxtechnik.dif.init.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
public class ChunkLoaderData extends SavedData{
	public final List<LoaderRecord> loaders=new ArrayList<>();
	public record LoaderRecord(BlockPos pos,UUID uuid,String name,boolean active,boolean is3x3){
	}
	// Definice factory pro jednodušší volání v computeIfAbsent
	private static final SavedData.Factory<ChunkLoaderData> FACTORY=new SavedData.Factory<>(
			ChunkLoaderData::new,
			ChunkLoaderData::load,
			null // DataFixTypes (pro jednoduchá data obvykle null)
	);
	public static ChunkLoaderData get(ServerLevel level){
		// V 1.21 se doporučuje používat overworld storage pro globální data, nebo level storage pro data specifická pro dimenzi
		return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY,"dif_loaders");
	}
	public void updateRecord(BlockPos pos,UUID uuid,String name,boolean active,boolean is3x3){
		loaders.removeIf(r->r.pos.equals(pos));
		loaders.add(new LoaderRecord(pos,uuid,name,active,is3x3));
		setDirty(); // Označí data pro uložení
	}
	// Nově vyžaduje HolderLookup.Provider registries
	public static ChunkLoaderData load(CompoundTag tag,HolderLookup.Provider registries){
		ChunkLoaderData data=new ChunkLoaderData();
		ListTag list=tag.getList("loaders",Tag.TAG_COMPOUND);
		for(int i=0;i<list.size();i++){
			CompoundTag entry=list.getCompound(i);
			data.loaders.add(new LoaderRecord(
					BlockPos.of(entry.getLong("p")),
					entry.getUUID("u"),
					entry.getString("n"),
					entry.getBoolean("a"),
					entry.getBoolean("s")
			));
		}
		return data;
	}
	// Metoda save má nyní v parametrech HolderLookup.Provider
	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider registries){
		ListTag list=new ListTag();
		for(LoaderRecord r: loaders){
			CompoundTag entry=new CompoundTag();
			entry.putLong("p",r.pos.asLong());
			entry.putUUID("u",r.uuid);
			entry.putString("n",r.name);
			entry.putBoolean("a",r.active);
			entry.putBoolean("s",r.is3x3);
			list.add(entry);
		}
		tag.put("loaders",list);
		return tag;
	}
}