package cz.maxtechnik.dif.init.events;

import net.minecraft.core.BlockPos;
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
	public static ChunkLoaderData get(ServerLevel level){
		return level.getServer().overworld().getDataStorage().computeIfAbsent(ChunkLoaderData::load,ChunkLoaderData::new,"dif_loaders");
	}
	public void updateRecord(BlockPos pos,UUID uuid,String name,boolean active,boolean is3x3){
		loaders.removeIf(r->r.pos.equals(pos));
		loaders.add(new LoaderRecord(pos,uuid,name,active,is3x3));
		setDirty();
	}
	public static ChunkLoaderData load(CompoundTag tag){
		ChunkLoaderData data=new ChunkLoaderData();
		ListTag list=tag.getList("loaders",Tag.TAG_COMPOUND);
		for(int i=0;i<list.size();i++){
			CompoundTag entry=list.getCompound(i);
			data.loaders.add(new LoaderRecord(
					BlockPos.of(entry.getLong("p")),entry.getUUID("u"),
					entry.getString("n"),entry.getBoolean("a"),entry.getBoolean("s")
			));
		}
		return data;
	}
	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag){
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