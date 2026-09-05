package cz.maxtechnik.dif.entity.bomb;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class NukeCraterHandler{
	private static final int BLOCKS_PER_TICK=36_000;
	private static final float MAX_DESTROYABLE_RESISTANCE=1500F;
	// 2x větší exploze: Průměr kráteru cca 128 bloků (poloměr 64), zóna sežehnutí poloměr 100 bloků (průměr 200 bloků)
	private static final double HOR_R_FULL=48.0, HOR_R_TOTAL=64.0;
	private static final double UP_R_FULL=32.0, UP_R_TOTAL=42.0;
	private static final double DOWN_R_FULL=18.0, DOWN_R_TOTAL=24.0;
	private static final double HOR_FULL_SQ=HOR_R_FULL*HOR_R_FULL, HOR_TOTAL_SQ=HOR_R_TOTAL*HOR_R_TOTAL;
	private static final double UP_FULL_SQ=UP_R_FULL*UP_R_FULL, UP_TOTAL_SQ=UP_R_TOTAL*UP_R_TOTAL;
	private static final double DN_FULL_SQ=DOWN_R_FULL*DOWN_R_FULL, DN_TOTAL_SQ=DOWN_R_TOTAL*DOWN_R_TOTAL;
	private static final double SCORCH_RADIUS=100.0, SCORCH_RADIUS_SQ=SCORCH_RADIUS*SCORCH_RADIUS;
	private static final BlockState AIR=Blocks.AIR.defaultBlockState();

	private int currentShell=0, maxShell=(int)Math.ceil(SCORCH_RADIUS), shellFace=0, shellU=0, shellV=0;
	private boolean debrisSpawned=false;
	private final BlockPos.MutableBlockPos mutablePos=new BlockPos.MutableBlockPos();

	public boolean tick(Level level,BlockPos center,double groundWaveRadius,RandomSource random){
		if(level.isClientSide) return true;

		if(!debrisSpawned){
			debrisSpawned=true;
			spawnDebris(level,center,random);
		}

		int targetShell=(int)Math.floor(groundWaveRadius);
		int cx=center.getX(), cy=center.getY(), cz=center.getZ(), processed=0;
		while(processed<BLOCKS_PER_TICK){
			if(currentShell>maxShell){
				return true; // Kráter je kompletně hotový
			}
			if(currentShell>targetShell){
				// Počkáme, až rázová vlna postoupí k další vrstvě
				return false;
			}
			int r=currentShell;
			if(r==0){
				destroyAt(level,cx,cy,cz);
				currentShell=1;
				shellFace=shellU=shellV=0;
				processed++;
				continue;
			}
			int dx, dy, dz, uSize, vSize;
			switch(shellFace){
				case 0 -> { dy=-r+shellU; dz=-r+shellV; dx=r; uSize=2*r+1; vSize=2*r+1; }
				case 1 -> { dy=-r+shellU; dz=-r+shellV; dx=-r; uSize=2*r+1; vSize=2*r+1; }
				case 2 -> { dx=-(r-1)+shellU; dz=-r+shellV; dy=r; uSize=2*(r-1)+1; vSize=2*r+1; }
				case 3 -> { dx=-(r-1)+shellU; dz=-r+shellV; dy=-r; uSize=2*(r-1)+1; vSize=2*r+1; }
				case 4 -> { dx=-(r-1)+shellU; dy=-(r-1)+shellV; dz=r; uSize=2*(r-1)+1; vSize=2*(r-1)+1; }
				case 5 -> { dx=-(r-1)+shellU; dy=-(r-1)+shellV; dz=-r; uSize=2*(r-1)+1; vSize=2*(r-1)+1; }
				default -> { dx=dy=dz=0; uSize=vSize=0; }
			}
			shellV++;
			if(shellV>=vSize){
				shellV=0;
				shellU++;
				if(shellU>=uSize){
					shellU=0;
					shellFace++;
					if(shellFace>5){
						shellFace=0;
						currentShell++;
					}
				}
			}

			double dxSq=(double)dx*dx, dzSq=(double)dz*dz;
			double horizDistSq=dxSq+dzSq;
			if(horizDistSq>SCORCH_RADIUS_SQ) continue;

			double verFullSq, verTotalSq, dyEff;
			if(dy>=0){
				verFullSq=UP_FULL_SQ;
				verTotalSq=UP_TOTAL_SQ;
				dyEff=dy;
			}else{
				double horFrac=Math.sqrt(horizDistSq)/HOR_R_TOTAL;
				dyEff=dy+horFrac*2.0;
				verFullSq=DN_FULL_SQ;
				verTotalSq=DN_TOTAL_SQ;
			}
			double dyEffSq=dyEff*dyEff;
			double nTotal=dxSq/HOR_TOTAL_SQ+dyEffSq/verTotalSq+dzSq/HOR_TOTAL_SQ;

			if(nTotal<=1.0){
				double nFull=dxSq/HOR_FULL_SQ+dyEffSq/verFullSq+dzSq/HOR_FULL_SQ;
				boolean destroy;
				if(nFull<=1.0) destroy=true;
				else{
					double scaleSq=1.0/nTotal;
					double maxNFull=(dxSq*scaleSq)/HOR_FULL_SQ+(dyEffSq*scaleSq)/verFullSq+(dzSq*scaleSq)/HOR_FULL_SQ;
					double t=Math.clamp((nFull-1.0)/(maxNFull-1.0),0.0,1.0);
					double chance=1.0-t*0.95;
					destroy=chance>=1.0||random.nextDouble()<chance;
				}
				if(destroy){
					destroyAt(level,cx+dx,cy+dy,cz+dz);
					if(dy<-8){
						mutateFloorAt(level,cx+dx,cy+dy-1,cz+dz,random);
					}
				}else{
					thermalScorchAt(level,cx+dx,cy+dy,cz+dz,random);
				}
			}else if(dy>=-16&&dy<=28&&horizDistSq<=SCORCH_RADIUS_SQ){
				thermalScorchAt(level,cx+dx,cy+dy,cz+dz,random);
			}
			processed++;
		}
		return false;
	}

	private void spawnDebris(Level level,BlockPos center,RandomSource random){
		if(!(level instanceof ServerLevel sl)) return;
		for(int i=0;i<45;i++){
			BlockState debrisState = (random.nextFloat()<0.40F) ? Blocks.COBBLED_DEEPSLATE.defaultBlockState()
					: (random.nextFloat()<0.40F) ? Blocks.BLACKSTONE.defaultBlockState()
					: (random.nextFloat()<0.50F) ? Blocks.COBBLESTONE.defaultBlockState()
					: Blocks.DIRT.defaultBlockState();
			FallingBlockEntity falling=FallingBlockEntity.fall(sl,center.above(3),debrisState);
			falling.time=1;
			falling.dropItem=false;
			double angle=random.nextDouble()*Math.PI*2.0;
			double speed=0.5+random.nextDouble()*0.9;
			double vy=0.65+random.nextDouble()*0.80;
			falling.setDeltaMovement(Math.cos(angle)*speed,vy,Math.sin(angle)*speed);
		}
	}

	private void destroyAt(Level level,int x,int y,int z){
		if(y<level.getMinBuildHeight()||y>=level.getMaxBuildHeight()) return;
		mutablePos.set(x,y,z);
		BlockState state=level.getBlockState(mutablePos);
		if(state.isAir()) return;
		if(state.getBlock().getExplosionResistance()>MAX_DESTROYABLE_RESISTANCE) return;
		level.setBlock(mutablePos,AIR,2|16|64);
	}

	private void mutateFloorAt(Level level,int x,int y,int z,RandomSource random){
		if(y<level.getMinBuildHeight()||y>=level.getMaxBuildHeight()) return;
		mutablePos.set(x,y,z);
		BlockState state=level.getBlockState(mutablePos);
		if(state.isAir()||state.getBlock().getExplosionResistance()>MAX_DESTROYABLE_RESISTANCE) return;
		if(state.isSolid()){
			// Dno kráteru: kombinace deepslatu (2 typy) a blackstone (2 typy) s trochou magmy, žádný čedič ani obsidián
			float roll=random.nextFloat();
			BlockState melted = roll<0.38F ? Blocks.COBBLED_DEEPSLATE.defaultBlockState()
					: roll<0.68F ? Blocks.DEEPSLATE.defaultBlockState()
					: roll<0.84F ? Blocks.BLACKSTONE.defaultBlockState()
					: roll<0.94F ? Blocks.POLISHED_BLACKSTONE.defaultBlockState()
					: Blocks.MAGMA_BLOCK.defaultBlockState(); // pouze 6 % magma
			level.setBlock(mutablePos,melted,2|16|64);
		}
	}

	private void thermalScorchAt(Level level,int x,int y,int z,RandomSource random){
		if(y<level.getMinBuildHeight()||y>=level.getMaxBuildHeight()) return;
		mutablePos.set(x,y,z);
		BlockState state=level.getBlockState(mutablePos);
		if(state.isAir()) return;

		// Voda v sežehnuté oblasti zmizí (vypaří se)
		if(state.is(Blocks.WATER)||state.getFluidState().is(net.minecraft.tags.FluidTags.WATER)){
			level.setBlock(mutablePos,AIR,2|16|64);
			return;
		}

		// Vegetace, vodní rostliny, sníh, led a liány / bloky nahraditelné stromy se sežehnou a zmizí
		if(state.is(BlockTags.REPLACEABLE_BY_TREES)||state.is(BlockTags.LEAVES)||state.is(BlockTags.FLOWERS)
				||state.is(Blocks.SHORT_GRASS)||state.is(Blocks.TALL_GRASS)||state.is(Blocks.VINE)
				||state.is(Blocks.SEAGRASS)||state.is(Blocks.TALL_SEAGRASS)||state.is(Blocks.KELP)||state.is(Blocks.KELP_PLANT)
				||state.is(Blocks.SNOW)||state.is(Blocks.SNOW_BLOCK)||state.is(Blocks.ICE)){
			level.setBlock(mutablePos,AIR,2|16|64);
			return;
		}

		// Stromy: Kmeny se promění na leštěný / obyčejný čedič (žádné bloky uhlí)
		if(state.is(BlockTags.LOGS)){
			BlockState charred=(random.nextFloat()<0.70F)?Blocks.POLISHED_BASALT.defaultBlockState():Blocks.BASALT.defaultBlockState();
			level.setBlock(mutablePos,charred,2|16|64);
			return;
		}

		// Písek, štěrk, jíl a tráva se sežehnou na hlínu a hrubou hlínu
		if(state.is(Blocks.GRASS_BLOCK)||state.is(Blocks.SAND)||state.is(Blocks.RED_SAND)
				||state.is(Blocks.GRAVEL)||state.is(Blocks.CLAY)){
			float r=random.nextFloat();
			BlockState dirt=(r<0.60F)?Blocks.COARSE_DIRT.defaultBlockState()
					:(r<0.90F)?Blocks.DIRT.defaultBlockState()
					:Blocks.ROOTED_DIRT.defaultBlockState();
			level.setBlock(mutablePos,dirt,2|16|64);
			mutablePos.set(x,y+1,z);
			if(level.getBlockState(mutablePos).isAir()&&random.nextFloat()<0.16F){
				level.setBlock(mutablePos,Blocks.FIRE.defaultBlockState(),2|16|64);
			}
			return;
		}

		// Příležitostné ohoření pevných kamenných povrchů na povrchu
		if(state.is(Blocks.STONE)){
			if(random.nextFloat()<0.10F){
				level.setBlock(mutablePos,Blocks.COBBLESTONE.defaultBlockState(),2|16|64);
			}
		}

		// Příležitostné zapálení pevných povrchů
		if(random.nextFloat()<0.08F&&state.isSolid()){
			mutablePos.set(x,y+1,z);
			if(level.getBlockState(mutablePos).isAir()){
				level.setBlock(mutablePos,Blocks.FIRE.defaultBlockState(),2|16|64);
			}
		}
	}

	public void writeNbt(CompoundTag tag){
		tag.putInt("CurrentShell",currentShell);
		tag.putInt("MaxShell",maxShell);
		tag.putInt("ShellFace",shellFace);
		tag.putInt("ShellU",shellU);
		tag.putInt("ShellV",shellV);
		tag.putBoolean("DebrisSpawned",debrisSpawned);
	}

	public void readNbt(CompoundTag tag){
		currentShell=tag.getInt("CurrentShell");
		maxShell=tag.getInt("MaxShell");
		shellFace=tag.getInt("ShellFace");
		shellU=tag.getInt("ShellU");
		shellV=tag.getInt("ShellV");
		debrisSpawned=tag.getBoolean("DebrisSpawned");
	}
}
