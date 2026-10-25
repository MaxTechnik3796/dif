package cz.maxtechnik.dif.entity.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public class PortalData extends SavedData{
	private final Map<UUID,BlockPos> bluePortals=new HashMap<>();
	private final Map<UUID,BlockPos> orangePortals=new HashMap<>();
	public static PortalData get(ServerLevel serverLevel){
		return serverLevel.getDataStorage().computeIfAbsent(new SavedData.Factory<>(PortalData::new,PortalData::load),"dif_portals");
	}
	public static PortalData load(CompoundTag tag,HolderLookup.Provider provider){
		PortalData data=new PortalData();
		for(String key: tag.getAllKeys()){
			UUID id=UUID.fromString(key);
			CompoundTag pt=tag.getCompound(key);
			NbtUtils.readBlockPos(pt,"b").ifPresent(pos->data.bluePortals.put(id,pos));
			NbtUtils.readBlockPos(pt,"o").ifPresent(pos->data.orangePortals.put(id,pos));
		}
		return data;
	}
	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		for(UUID id: bluePortals.keySet()){
			CompoundTag pt=new CompoundTag();
			pt.put("b",NbtUtils.writeBlockPos(bluePortals.get(id)));
			if(orangePortals.containsKey(id)) pt.put("o",NbtUtils.writeBlockPos(orangePortals.get(id)));
			tag.put(id.toString(),pt);
		}
		for(UUID id: orangePortals.keySet()){
			if(bluePortals.containsKey(id)) continue;
			CompoundTag pt=new CompoundTag();
			pt.put("o",NbtUtils.writeBlockPos(orangePortals.get(id)));
			tag.put(id.toString(),pt);
		}
		return tag;
	}
	public void set(UUID uuid,boolean isBlue,BlockPos pos){
		(isBlue?bluePortals:orangePortals).put(uuid,pos);
		setDirty();
	}
	public BlockPos getPos(UUID uuid,boolean isBlue){
		return (isBlue?bluePortals:orangePortals).get(uuid);
	}
	public void remove(UUID uuid,boolean isBlue){
		if((isBlue?bluePortals:orangePortals).remove(uuid)!=null) setDirty();
	}
	public PortalEntity findEntity(ServerLevel sl,UUID owner,boolean isBlue){
		BlockPos pos=getPos(owner,isBlue);
		if(pos==null||!sl.isLoaded(pos)) return null;
		List<PortalEntity> list=sl.getEntitiesOfClass(PortalEntity.class,new AABB(pos).inflate(2),p->owner.equals(p.getOwner())&&p.isBlue()==isBlue);
		return list.isEmpty()?null:list.getFirst();
	}
	public void removeOldPortal(ServerLevel sl,UUID owner,boolean isBlue){
		PortalEntity existing=findEntity(sl,owner,isBlue);
		if(existing!=null) existing.discard();
		remove(owner,isBlue);
	}
	public void updateLinks(ServerLevel sl,UUID owner){
		PortalEntity blue=findEntity(sl,owner,true);
		PortalEntity orange=findEntity(sl,owner,false);
		boolean linked=blue!=null&&orange!=null;
		if(blue!=null) blue.setIsLinked(linked);
		if(orange!=null) orange.setIsLinked(linked);
	}
}
