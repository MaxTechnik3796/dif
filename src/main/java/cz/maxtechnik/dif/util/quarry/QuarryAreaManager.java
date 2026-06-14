package cz.maxtechnik.dif.util.quarry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
/**
 * Pomocná třída, která řeší veškerou matematiku ohledně rozměrů lomu,
 * hranic těžby, iterování po blocích a chunkloadingu.
 */
public class QuarryAreaManager{
	public static final int DEFAULT_RANGE=5;
	private static final int FRAME_HEIGHT=3;
	private QuarryArea area;
	private QuarryArea miningBounds;
	private BlockPos miningPos;
	private List<BlockPos> cachedFramePos=null;
	private BlockPos cachedCenter=null;
	private final List<ChunkPos> forcedChunks=new ArrayList<>();
	public QuarryAreaManager(){
	}
	// ── Správa oblasti ──────────────────────────────────────────────────
	public void setArea(QuarryArea newArea){
		this.area=newArea;
		this.miningBounds=newArea.miningBounds();
		this.cachedFramePos=null;
		this.cachedCenter=null;
	}
	public QuarryArea getArea(){
		return area;
	}
	public boolean hasArea(){
		return area!=null;
	}
	// ── Matematika rámu ─────────────────────────────────────────────────
	/**
	 * Vypočítá (a zacachuje) všechny pozice bloků, kde má stát rám lomu.
	 */
	public List<BlockPos> computeFramePositions(int yBase){
		if(cachedFramePos!=null) return cachedFramePos;
		if(area==null) return List.of();
		cachedCenter=new BlockPos((area.minX()+area.maxX())/2,yBase,(area.minZ()+area.maxZ())/2);
		List<BlockPos> result=new ArrayList<>();
		for(int x=area.minX();x<=area.maxX();x++){
			for(int z=area.minZ();z<=area.maxZ();z++){
				boolean edgeX=(x==area.minX()||x==area.maxX());
				boolean edgeZ=(z==area.minZ()||z==area.maxZ());
				if(!edgeX&&!edgeZ) continue;
				// Spodní a vrchní hrana rámu
				result.add(new BlockPos(x,yBase,z));
				result.add(new BlockPos(x,yBase+FRAME_HEIGHT,z));
				// Svislé rohové pilíře
				if(edgeX&&edgeZ){
					result.add(new BlockPos(x,yBase+1,z));
					result.add(new BlockPos(x,yBase+2,z));
				}
			}
		}
		cachedFramePos=result;
		return result;
	}
	public BlockPos getAreaCenter(int yBase){
		computeFramePositions(yBase);
		return cachedCenter;
	}
	// ── Iterace těžební pozice ──────────────────────────────────────────
	public BlockPos getMiningPos(){
		return miningPos;
	}
	public void setMiningPos(BlockPos pos){
		this.miningPos=pos;
	}
	/**
	 * Zresetuje těžební pozici na začátek (úplně nahoře, v rohu těžební oblasti).
	 */
	public void resetMiningPos(int yBase){
		if(miningBounds!=null){
			this.miningPos=new BlockPos(miningBounds.minX(),yBase-1,miningBounds.minZ());
		}
	}
	/**
	 * Posune těžební pozici na další blok.
	 * Vrátí true, pokud lze pokračovat (ještě jsme nedosáhli dna).
	 * Vrátí false, pokud je těžba hotova.
	 */
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
	// ── Chunkloading ────────────────────────────────────────────────────
	/**
	 * Načte a "zamkne" všechny chunky, které quarry aktuálně těží.
	 */
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
	/**
	 * Uvolní všechny načtené chunky zpět.
	 */
	public void unloadForcedChunks(ServerLevel sl){
		for(ChunkPos cp: forcedChunks){
			sl.setChunkForced(cp.x,cp.z,false);
		}
		forcedChunks.clear();
	}
}
