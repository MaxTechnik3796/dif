package cz.maxtechnik.dif.util.quarry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
public class QuarryAreaManager{
	public static final int DEFAULT_RANGE=5;
	private static final int FRAME_HEIGHT=3;
	private QuarryArea area;
	private QuarryArea miningBounds;
	private BlockPos miningPos;
	private List<BlockPos> cachedFramePos=null;
	private final List<ChunkPos> forcedChunks=new ArrayList<>();
	public QuarryAreaManager(){
	}
	// Správa oblasti
	public void setArea(@org.jetbrains.annotations.Nullable QuarryArea newArea){
		this.area=newArea;
		this.miningBounds=newArea!=null?newArea.miningBounds():null;
		this.cachedFramePos=null;
	}
	public QuarryArea getArea(){
		return area;
	}
	public boolean hasArea(){
		return area!=null;
	}
	// Matematika framu
	public List<BlockPos> computeFramePositions(int yBase){
		if(cachedFramePos!=null) return cachedFramePos;
		if(area==null) return List.of();
		List<BlockPos> result=new ArrayList<>();
		for(int x=area.minX();x<=area.maxX();x++){
			for(int z=area.minZ();z<=area.maxZ();z++){
				boolean edgeX=(x==area.minX()||x==area.maxX());
				boolean edgeZ=(z==area.minZ()||z==area.maxZ());
				if(!edgeX&&!edgeZ) continue;
				// Spodní a vrchní hrana framu
				result.add(new BlockPos(x,yBase,z));
				result.add(new BlockPos(x,yBase+FRAME_HEIGHT,z));
				if(edgeX&&edgeZ){
					result.add(new BlockPos(x,yBase+1,z));
					result.add(new BlockPos(x,yBase+2,z));
				}
			}
		}
		cachedFramePos=result;
		return result;
	}
	// Iterace těžební pozice
	public BlockPos getMiningPos(){
		return miningPos;
	}
	public void setMiningPos(BlockPos pos){
		this.miningPos=pos;
	}
	public void resetMiningPos(int yBase){
		if(miningBounds!=null){
			this.miningPos=new BlockPos(miningBounds.minX(),yBase-1,miningBounds.minZ());
		}
	}
	public boolean advanceMiningPos(Level level){
		if(miningPos==null||level==null||miningBounds==null) return true;
		int nx=miningPos.getX()+1;
		int nz=miningPos.getZ();
		int ny=miningPos.getY();
		if(nx>miningBounds.maxX()){
			nx=miningBounds.minX();
			nz++;
		}
		if(nz>miningBounds.maxZ()){
			nz=miningBounds.minZ();
			ny--;
		}
		miningPos=new BlockPos(nx,ny,nz);
		return ny<=level.getMinBuildHeight();
	}
	// Chunkloading
	public void loadMiningChunks(ServerLevel sl){
		unloadForcedChunks(sl);
		if(miningBounds==null) return;
		int mnCx=miningBounds.minX()>>4;
		int mxCx=miningBounds.maxX()>>4;
		int mnCz=miningBounds.minZ()>>4;
		int mxCz=miningBounds.maxZ()>>4;
		for(int cx=mnCx;cx<=mxCx;cx++){
			for(int cz=mnCz;cz<=mxCz;cz++){
				sl.setChunkForced(cx,cz,true);
				forcedChunks.add(new ChunkPos(cx,cz));
			}
		}
	}
	public void unloadForcedChunks(ServerLevel sl){
		for(ChunkPos cp: forcedChunks){
			sl.setChunkForced(cp.x,cp.z,false);
		}
		forcedChunks.clear();
	}
}
