package cz.maxtechnik.dif.util.quarry;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
public record QuarryArea(int minX,int maxX,int minZ,int maxZ){
	public int sizeX(){
		return maxX-minX+1;
	}
	public int sizeZ(){
		return maxZ-minZ+1;
	}
	public QuarryArea miningBounds(){
		return new QuarryArea(minX+1,maxX-1,minZ+1,maxZ-1);
	}
	public void save(CompoundTag tag){
		tag.putInt("AMnX",minX);
		tag.putInt("AMxX",maxX);
		tag.putInt("AMnZ",minZ);
		tag.putInt("AMxZ",maxZ);
	}
	@Nullable
	public static QuarryArea load(CompoundTag tag){
		if(!tag.contains("AMnX")) return null;
		return new QuarryArea(
				tag.getInt("AMnX"),tag.getInt("AMxX"),
				tag.getInt("AMnZ"),tag.getInt("AMxZ")
		);
	}
}
