package cz.maxtechnik.dif.init.event.nuke;

import cz.maxtechnik.dif.config.DifModServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
@SuppressWarnings("deprecation")
public class NukeCraterHandler{
	public static final int BLOCK_UPDATE_FLAGS=2|16|64;
	private static final double CRATER_UP_RATIO=0.55;
	private static final double CRATER_DOWN_RATIO=0.55;
	private static final double INNER_CLEAN_NORM_SQ=0.86;
	public static int getRadius(){
		return DifModServerConfig.NUKE_RADIUS.get();
	}
	public static int getBlocksPerTick(){
		return DifModServerConfig.NUKE_BLOCKS_PER_TICK.get();
	}
	public static double getMaxResistance(){
		return DifModServerConfig.NUKE_MAX_DESTROYABLE_RESISTANCE.get();
	}
	public static int getCraterUpRadius(int radius){
		return Math.max(8,(int)Math.round(radius*CRATER_UP_RATIO));
	}
	public static int getCraterDownRadius(int radius){
		return Math.max(8,(int)Math.round(radius*CRATER_DOWN_RATIO));
	}
	public static void clearBlockEntity(ServerLevel level,BlockPos pos,BlockState state){
		if(state.hasBlockEntity()){
			BlockEntity be=level.getBlockEntity(pos);
			if(be instanceof RandomizableContainer loot) loot.setLootTable(null);
			Clearable.tryClear(be);
		}
	}
	public static int tickCrater(ServerLevel level,BlockPos center,int currentY,BlockPos.MutableBlockPos mutablePos){
		int radius=getRadius();
		int upRadius=getCraterUpRadius(radius);
		int downRadius=getCraterDownRadius(radius);
		int blocksPerTick=getBlocksPerTick();
		int maxChecksPerTick=blocksPerTick*6;
		double maxResistance=getMaxResistance();
		int cx=center.getX(), cy=center.getY(), cz=center.getZ();
		int processed=0;
		int checked=0;
		RandomSource random=level.getRandom();
		double invRxSq=1.0/((double)radius*radius);
		while(currentY>=-downRadius&&processed<blocksPerTick&&checked<maxChecksPerTick){
			int y=cy+currentY;
			int vRadius=currentY>=0?upRadius:downRadius;
			if(y>=level.getMinBuildHeight()&&y<level.getMaxBuildHeight()&&vRadius>0){
				double yNorm=(double)currentY/vRadius;
				double yNormSq=yNorm*yNorm;
				if(yNormSq<=1.0){
					int layerRadius=(int)Math.ceil(radius*Math.sqrt(1.0-yNormSq))+4;
					for(int dx=-layerRadius;dx<=layerRadius;dx++){
						for(int dz=-layerRadius;dz<=layerRadius;dz++){
							checked++;
							double normDistSq=(dx*dx+dz*dz)*invRxSq+yNormSq;
							if(normDistSq>1.12) continue;
							if(normDistSq>0.78){
								double angle=Math.atan2(dz,dx);
								double wave=1.0+Math.sin(angle*5.0)*0.05+Math.cos(angle*3.0)*0.03;
								if(normDistSq>wave*wave) continue;
							}
							mutablePos.set(cx+dx,y,cz+dz);
							BlockState state=level.getBlockState(mutablePos);
							if(state.isAir()||state.getBlock().getExplosionResistance()>maxResistance) continue;
							clearBlockEntity(level,mutablePos,state);
							if(normDistSq<=INNER_CLEAN_NORM_SQ||random.nextFloat()<0.75F){
								level.setBlock(mutablePos,Blocks.AIR.defaultBlockState(),BLOCK_UPDATE_FLAGS);
							}else{
								BlockState replacement=getCrustBlock(state,random);
								level.setBlock(mutablePos,replacement,BLOCK_UPDATE_FLAGS);
								if(!replacement.isAir()&&random.nextFloat()<0.08F&&y+1<level.getMaxBuildHeight()){
									mutablePos.set(cx+dx,y+1,cz+dz);
									if(level.getBlockState(mutablePos).isAir())
										level.setBlock(mutablePos,Blocks.SOUL_FIRE.defaultBlockState(),BLOCK_UPDATE_FLAGS);
								}
							}
							processed++;
						}
					}
				}
			}
			currentY--;
		}
		return currentY;
	}
	private static BlockState getCrustBlock(BlockState original,RandomSource random){
		if(!original.getFluidState().isEmpty()) return Blocks.AIR.defaultBlockState();
		if(original.is(BlockTags.SAND))
			return random.nextFloat()<0.30F?Blocks.GLASS.defaultBlockState():Blocks.SOUL_SAND.defaultBlockState();
		if(original.is(BlockTags.DIRT))
			return random.nextBoolean()?Blocks.SOUL_SAND.defaultBlockState():Blocks.COARSE_DIRT.defaultBlockState();
		float roll=random.nextFloat();
		if(roll<0.50F) return Blocks.BLACKSTONE.defaultBlockState();
		if(roll<0.75F) return Blocks.DEEPSLATE.defaultBlockState();
		return Blocks.BASALT.defaultBlockState();
	}
}
