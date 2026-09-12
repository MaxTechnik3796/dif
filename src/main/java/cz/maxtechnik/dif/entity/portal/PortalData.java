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
	private final Map<UUID,Map<Boolean,BlockPos>> map=new HashMap<>();
	public static PortalData get(ServerLevel serverLevel){
		return serverLevel.getDataStorage().computeIfAbsent(new SavedData.Factory<>(PortalData::new,PortalData::load),"dif_portals");
	}
	public static PortalData load(CompoundTag t,HolderLookup.Provider provider){
		PortalData d=new PortalData();
		t.getAllKeys().forEach(k->{
			CompoundTag pt=t.getCompound(k);
			Map<Boolean,BlockPos> m=new HashMap<>();
			if(pt.contains("b")) NbtUtils.readBlockPos(pt,"b").ifPresent(pos->m.put(true,pos));
			if(pt.contains("o")) NbtUtils.readBlockPos(pt,"o").ifPresent(pos->m.put(false,pos));
			d.map.put(UUID.fromString(k),m);
		});
		return d;
	}
	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		map.forEach((k,v)->{
			CompoundTag pt=new CompoundTag();
			if(v.containsKey(true)) pt.put("b",NbtUtils.writeBlockPos(v.get(true)));
			if(v.containsKey(false)) pt.put("o",NbtUtils.writeBlockPos(v.get(false)));
			tag.put(k.toString(),pt);
		});
		return tag;
	}
	public void set(UUID uuid,boolean b,BlockPos pos){
		map.computeIfAbsent(uuid,k->new HashMap<>()).put(b,pos);
		setDirty();
	}
	public BlockPos getPos(UUID uuid,boolean b){
		return map.getOrDefault(uuid,Map.of()).get(b);
	}
	public void remove(UUID id,boolean b){
		Map<Boolean,BlockPos> m=map.get(id);
		if(m!=null){
			m.remove(b);
			if(m.isEmpty()) map.remove(id);
			setDirty();
		}
	}
	// Správa portálů – sjednocený lookup a lifecycle
	/** Najde portál entity ve světě podle uloženého BlockPos. Sjednocuje původní findPortal i findLinkedPortal. */
	public PortalEntity findEntity(ServerLevel sl,UUID owner,boolean isBlue){
		BlockPos pos=getPos(owner,isBlue);
		if(pos==null||!sl.isLoaded(pos)) return null;
		List<PortalEntity> list=sl.getEntitiesOfClass(PortalEntity.class,new AABB(pos).inflate(2),
				p->owner.equals(p.getOwner())&&p.isBlue()==isBlue);
		return list.isEmpty()?null:list.getFirst();
	}
	/** Smaže existující portál entity a jeho data. */
	public void removeOldPortal(ServerLevel sl,UUID owner,boolean isBlue){
		PortalEntity existing=findEntity(sl,owner,isBlue);
		if(existing!=null) existing.discard();
		remove(owner,isBlue);
	}
	/** Aktualizuje isLinked stav obou portálů daného hráče. */
	public void updateLinks(ServerLevel sl,UUID owner){
		PortalEntity blue=findEntity(sl,owner,true);
		PortalEntity orange=findEntity(sl,owner,false);
		boolean linked=blue!=null&&orange!=null;
		if(blue!=null) blue.setIsLinked(linked);
		if(orange!=null) orange.setIsLinked(linked);
	}
}
