package cz.maxtechnik.dif.entity.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
public final class PortalPlacement{
	private PortalPlacement(){}
	public static double snapToGrid(double v){
		return Math.round(v*16.0)/16.0;
	}
	public static Set<BlockPos> getFootprint(Vec3 pos,Direction upDir,Direction facing){
		Vec3 up= PortalEntity.dirVec(upDir);
		Vec3 right=PortalEntity.dirVec(facing).cross(up);
		Vec3 lo=pos.subtract(up).subtract(right.scale(0.5));
		Vec3 hi=pos.add(up).add(right.scale(0.5));
		Set<BlockPos> set=new HashSet<>();
		int sx=Mth.floor(Math.min(lo.x,hi.x)+1e-4),ex=Mth.floor(Math.max(lo.x,hi.x)-1e-4);
		int sy=Mth.floor(Math.min(lo.y,hi.y)+1e-4),ey=Mth.floor(Math.max(lo.y,hi.y)-1e-4);
		int sz=Mth.floor(Math.min(lo.z,hi.z)+1e-4),ez=Mth.floor(Math.max(lo.z,hi.z)-1e-4);
		for(int x=sx;x<=ex;x++)
			for(int y=sy;y<=ey;y++)
				for(int z=sz;z<=ez;z++)
					set.add(new BlockPos(x,y,z));
		return set;
	}
	public static boolean isValidPosition(ServerLevel world,Vec3 pos,Direction upDir,Direction face){
		Set<BlockPos> blocks=getFootprint(pos,upDir,face);
		if(blocks.isEmpty()) return false;
		for(BlockPos p:blocks){
			BlockPos behind=p.relative(face.getOpposite());
			if(!world.getBlockState(behind).isFaceSturdy(world,behind,face)) return false;
			if(!world.isEmptyBlock(p)&&!world.getBlockState(p).canBeReplaced()) return false;
		}
		return true;
	}
	public static Vec3 align(ServerLevel world,BlockPos hitPos,Direction face,Direction extDir,Vec3 hitLoc){
		Vec3 center=Vec3.atCenterOf(hitPos);
		Vec3 normal=Vec3.atLowerCornerOf(face.getNormal());
		Vec3 up=Vec3.atLowerCornerOf(extDir.getNormal());
		Vec3 right=normal.cross(up);
		double cU=center.dot(up),cR=center.dot(right);
		double nVal=center.dot(normal)+0.5;
		double offU=snapToGrid(Math.clamp(hitLoc.dot(up),cU-0.5,cU+0.5));
		double offR=snapToGrid(Math.clamp(hitLoc.dot(right),cR-0.5,cR+0.5));
		for(double u:new double[]{offU,cU+0.5,cU-0.5,cU}){
			for(double r:new double[]{offR,cR}){
				Vec3 pos=normal.scale(nVal+0.005).add(up.scale(u)).add(right.scale(r));
				if(isValidPosition(world,pos,extDir,face)) return pos;
			}
		}
		return null;
	}
	public static boolean hasOverlap(ServerLevel world,AABB box,UUID owner,boolean isBlue){
		for(PortalEntity o:world.getEntitiesOfClass(PortalEntity.class,box.inflate(0.05))){
			if(owner.equals(o.getOwner())&&o.isBlue()==isBlue) continue;
			if(box.intersects(o.getBoundingBox())) return true;
		}
		return false;
	}
}
