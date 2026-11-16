package cz.maxtechnik.dif.init.event.nuke;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
@SuppressWarnings("deprecation")
public class NukeScorchHandler{
	private static final int RING_STEP=4;
	private static final int FLAGS=NukeCraterHandler.BLOCK_UPDATE_FLAGS;
	public static int getStartRadius(){
		return 12;
	}
	public static int getMaxScorchRadius(){
		return Math.max(NukeCraterHandler.getRadius()+110,(int)NukeEffectHandler.getDamageRadius());
	}
	public static int tickScorchRing(ServerLevel level,BlockPos center,int currentRadius,BlockPos.MutableBlockPos mutablePos){
		int craterRadius=NukeCraterHandler.getRadius();
		int maxRadius=getMaxScorchRadius();
		if(currentRadius>maxRadius) return currentRadius;
		int nextRadius=Math.min(maxRadius+1,currentRadius+RING_STEP);
		int rMinSq=currentRadius*currentRadius;
		int rMaxSq=nextRadius*nextRadius;
		int cx=center.getX(), cz=center.getZ();
		RandomSource random=level.getRandom();
		for(int dx=-nextRadius;dx<=nextRadius;dx++){
			int dxSq=dx*dx;
			if(dxSq>rMaxSq) continue;
			int minDz=dxSq<rMinSq?(int)Math.floor(Math.sqrt(rMinSq-dxSq)):0;
			int maxDz=(int)Math.ceil(Math.sqrt(rMaxSq-dxSq));
			for(int dz=minDz;dz<=maxDz;dz++){
				double dist=Math.sqrt(dxSq+dz*dz);
				scorchColumn(level,cx+dx,cz+dz,dist,craterRadius,maxRadius,random,mutablePos);
				if(dz>0)
					scorchColumn(level,cx+dx,cz-dz,dist,craterRadius,maxRadius,random,mutablePos);
			}
		}
		return nextRadius;
	}
	private static void scorchColumn(ServerLevel level,int x,int z,double dist,int craterRadius,int maxRadius,RandomSource random,BlockPos.MutableBlockPos pos){
		if(!level.hasChunk(x>>4,z>>4)) return;
		double norm=Math.max(0.0,(dist-craterRadius)/Math.max(1.0,maxRadius-craterRadius));
		if(norm>0.65&&random.nextDouble()>(1.0-norm)/0.35) return;
		int topY=level.getHeight(Heightmap.Types.MOTION_BLOCKING,x,z)-1;
		int minY=Math.max(level.getMinBuildHeight(),topY-18);
		for(int y=topY+1;y>=minY;y--){
			pos.set(x,y,z);
			BlockState state=level.getBlockState(pos);
			if(state.isAir()) continue;
			if(!state.getFluidState().isEmpty()) break;
			if(isLeavesOrDryPlant(state)){
				level.setBlock(pos,Blocks.AIR.defaultBlockState(),FLAGS);
				continue;
			}
			if(state.is(BlockTags.LOGS)){
				BlockState charredLog=Blocks.BASALT.defaultBlockState();
				if(state.hasProperty(RotatedPillarBlock.AXIS))
					charredLog=charredLog.setValue(RotatedPillarBlock.AXIS,state.getValue(RotatedPillarBlock.AXIS));
				level.setBlock(pos,charredLog,FLAGS);
				continue;
			}
			if(isScorchableGrass(state)){
				level.setBlock(pos,getScorchedDirt(random),FLAGS);
			}
			break;
		}
	}
	private static boolean isLeavesOrDryPlant(BlockState state){
		return state.is(BlockTags.LEAVES)||state.is(BlockTags.FLOWERS)||state.is(BlockTags.SAPLINGS)
				||state.is(BlockTags.CROPS)||state.is(Blocks.SHORT_GRASS)||state.is(Blocks.TALL_GRASS)
				||state.is(Blocks.FERN)||state.is(Blocks.LARGE_FERN)||state.is(Blocks.DEAD_BUSH)
				||state.is(Blocks.VINE)||state.is(Blocks.MOSS_CARPET)||state.is(Blocks.SNOW);
	}
	private static boolean isScorchableGrass(BlockState state){
		return state.is(Blocks.GRASS_BLOCK)||state.is(Blocks.PODZOL)||state.is(Blocks.MYCELIUM)
				||state.is(Blocks.MOSS_BLOCK)||state.is(Blocks.DIRT_PATH)||state.is(Blocks.FARMLAND);
	}
	private static BlockState getScorchedDirt(RandomSource random){
		float roll=random.nextFloat();
		if(roll<0.35F) return Blocks.PODZOL.defaultBlockState();
		if(roll<0.80F) return Blocks.COARSE_DIRT.defaultBlockState();
		return Blocks.DIRT.defaultBlockState();
	}
}
