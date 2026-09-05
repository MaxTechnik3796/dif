package cz.maxtechnik.dif.entity.bomb;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class NuclearExplosionEntity extends Entity{
	// -------------------- KONSTANTY --------------------
	private static final int BLOCKS_PER_TICK=8_000, PHASE_INIT=0, PHASE_CRATER=1, PHASE_DONE=2;
	private static final float MAX_DESTROYABLE_RESISTANCE=1500F;
	// Průměr cca 64 bloků (poloměr 32)
	private static final double HOR_R_FULL=24.0, HOR_R_TOTAL=32.0;
	private static final double UP_R_FULL=20.0, UP_R_TOTAL=24.0;
	private static final double DOWN_R_FULL=12.0, DOWN_R_TOTAL=16.0;
	private static final double HOR_FULL_SQ=HOR_R_FULL*HOR_R_FULL, HOR_TOTAL_SQ=HOR_R_TOTAL*HOR_R_TOTAL;
	private static final double UP_FULL_SQ=UP_R_FULL*UP_R_FULL, UP_TOTAL_SQ=UP_R_TOTAL*UP_R_TOTAL;
	private static final double DN_FULL_SQ=DOWN_R_FULL*DOWN_R_FULL, DN_TOTAL_SQ=DOWN_R_TOTAL*DOWN_R_TOTAL;
	private static final double SCORCH_RADIUS=48.0, SCORCH_RADIUS_SQ=SCORCH_RADIUS*SCORCH_RADIUS;
	private static final BlockState AIR=Blocks.AIR.defaultBlockState();
	private static final EntityDataAccessor<Integer> DATA_PHASE=SynchedEntityData.defineId(NuclearExplosionEntity.class,EntityDataSerializers.INT);

	// Shell iterátor (kráter – od středu ven)
	private int currentShell, maxShell, shellFace, shellU, shellV, radius=(int)HOR_R_TOTAL;
	private final BlockPos.MutableBlockPos mutablePos=new BlockPos.MutableBlockPos();

	public NuclearExplosionEntity(EntityType<?> type,Level level){
		super(type,level);
		this.noPhysics=true;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder){
		builder.define(DATA_PHASE,PHASE_INIT);
	}

	public void setRadius(int radius){
		this.radius=radius;
	}

	private void setPhase(int phase){
		entityData.set(DATA_PHASE,phase);
	}

	private int getPhase(){
		return entityData.get(DATA_PHASE);
	}

	@Override
	public void tick(){
		super.tick();
		if(level().isClientSide) return;
		switch(getPhase()){
			case PHASE_INIT -> {
				maxShell=(int)Math.ceil(SCORCH_RADIUS);
				currentShell=shellFace=shellU=shellV=0;
				spawnDebris();
				setPhase(PHASE_CRATER);
			}
			case PHASE_CRATER -> tickCrater();
			case PHASE_DONE -> discard();
		}
	}

	private void spawnDebris(){
		if(!(level() instanceof ServerLevel sl)) return;
		BlockPos center=blockPosition();
		for(int i=0;i<35;i++){
			BlockState debrisState = (random.nextFloat()<0.35F) ? Blocks.MAGMA_BLOCK.defaultBlockState()
					: (random.nextFloat()<0.45F) ? Blocks.COBBLESTONE.defaultBlockState()
					: Blocks.DIRT.defaultBlockState();
			FallingBlockEntity falling=FallingBlockEntity.fall(sl,center.above(2),debrisState);
			falling.time=1;
			falling.dropItem=false;
			double angle=random.nextDouble()*Math.PI*2.0;
			double speed=0.4+random.nextDouble()*0.7;
			double vy=0.55+random.nextDouble()*0.65;
			falling.setDeltaMovement(Math.cos(angle)*speed,vy,Math.sin(angle)*speed);
		}
	}

	// -------------------- Kráter – shell-based od středu ven --------------------
	private void tickCrater(){
		BlockPos center=blockPosition();
		int cx=center.getX(), cy=center.getY(), cz=center.getZ(), processed=0;
		while(processed<BLOCKS_PER_TICK){
			if(currentShell>maxShell){
				setPhase(PHASE_DONE);
				return;
			}
			int r=currentShell;
			if(r==0){
				destroyAt(cx,cy,cz);
				currentShell=1;
				shellFace=shellU=shellV=0;
				processed++;
				continue;
			}
			int dx, dy, dz, uSize, vSize;
			switch(shellFace){
				case 0 -> {
					dy=-r+shellU;
					dz=-r+shellV;
					dx=r;
					uSize=2*r+1;
					vSize=2*r+1;
				}
				case 1 -> {
					dy=-r+shellU;
					dz=-r+shellV;
					dx=-r;
					uSize=2*r+1;
					vSize=2*r+1;
				}
				case 2 -> {
					dx=-(r-1)+shellU;
					dz=-r+shellV;
					dy=r;
					uSize=2*(r-1)+1;
					vSize=2*r+1;
				}
				case 3 -> {
					dx=-(r-1)+shellU;
					dz=-r+shellV;
					dy=-r;
					uSize=2*(r-1)+1;
					vSize=2*r+1;
				}
				case 4 -> {
					dx=-(r-1)+shellU;
					dy=-(r-1)+shellV;
					dz=r;
					uSize=2*(r-1)+1;
					vSize=2*(r-1)+1;
				}
				case 5 -> {
					dx=-(r-1)+shellU;
					dy=-(r-1)+shellV;
					dz=-r;
					uSize=2*(r-1)+1;
					vSize=2*(r-1)+1;
				}
				default -> {
					dx=dy=dz=0;
					uSize=vSize=0;
				}
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
					destroyAt(cx+dx,cy+dy,cz+dz);
					if(dy<-6){
						mutateFloorAt(cx+dx,cy+dy-1,cz+dz);
					}
				}else{
					thermalScorchAt(cx+dx,cy+dy,cz+dz);
				}
			}else if(dy>=-4&&dy<=12&&horizDistSq<=SCORCH_RADIUS_SQ){
				thermalScorchAt(cx+dx,cy+dy,cz+dz);
			}
			processed++;
		}
	}

	// -------------------- Helpers --------------------
	private void destroyAt(int x,int y,int z){
		if(y<level().getMinBuildHeight()||y>=level().getMaxBuildHeight()) return;
		mutablePos.set(x,y,z);
		BlockState state=level().getBlockState(mutablePos);
		if(state.isAir()) return;
		if(state.getBlock().getExplosionResistance()>MAX_DESTROYABLE_RESISTANCE) return;
		level().setBlock(mutablePos,AIR,2|16|64);
	}

	private void mutateFloorAt(int x,int y,int z){
		if(y<level().getMinBuildHeight()||y>=level().getMaxBuildHeight()) return;
		mutablePos.set(x,y,z);
		BlockState state=level().getBlockState(mutablePos);
		if(state.isAir()||state.getBlock().getExplosionResistance()>MAX_DESTROYABLE_RESISTANCE) return;
		if(state.isSolid()){
			float roll=random.nextFloat();
			BlockState melted = roll<0.35F ? Blocks.MAGMA_BLOCK.defaultBlockState()
					: roll<0.55F ? Blocks.BASALT.defaultBlockState()
					: roll<0.70F ? Blocks.OBSIDIAN.defaultBlockState()
					: roll<0.80F ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
					: null;
			if(melted!=null){
				level().setBlock(mutablePos,melted,2|16|64);
			}
		}
	}

	private void thermalScorchAt(int x,int y,int z){
		if(y<level().getMinBuildHeight()||y>=level().getMaxBuildHeight()) return;
		mutablePos.set(x,y,z);
		BlockState state=level().getBlockState(mutablePos);
		if(state.isAir()) return;

		if(state.is(BlockTags.LEAVES)||state.is(BlockTags.FLOWERS)||state.is(Blocks.SHORT_GRASS)||state.is(Blocks.TALL_GRASS)){
			level().setBlock(mutablePos,AIR,2|16|64);
			return;
		}
		if(state.is(BlockTags.LOGS)){
			BlockState charred=(random.nextFloat()<0.7F)?Blocks.COAL_BLOCK.defaultBlockState():Blocks.BASALT.defaultBlockState();
			level().setBlock(mutablePos,charred,2|16|64);
			return;
		}
		if(state.is(Blocks.SAND)||state.is(Blocks.RED_SAND)){
			level().setBlock(mutablePos,Blocks.GLASS.defaultBlockState(),2|16|64);
			return;
		}
		if(state.is(Blocks.GRASS_BLOCK)){
			BlockState dirt=(random.nextFloat()<0.6F)?Blocks.COARSE_DIRT.defaultBlockState():Blocks.DIRT.defaultBlockState();
			level().setBlock(mutablePos,dirt,2|16|64);
			mutablePos.set(x,y+1,z);
			if(level().getBlockState(mutablePos).isAir()&&random.nextFloat()<0.15F){
				level().setBlock(mutablePos,Blocks.FIRE.defaultBlockState(),2|16|64);
			}
			return;
		}
		if(random.nextFloat()<0.08F&&state.isSolid()){
			mutablePos.set(x,y+1,z);
			if(level().getBlockState(mutablePos).isAir()){
				level().setBlock(mutablePos,Blocks.FIRE.defaultBlockState(),2|16|64);
			}
		}
	}

	// -------------------- NBT --------------------
	@Override
	protected void readAdditionalSaveData(CompoundTag tag){
		radius=tag.getInt("Radius");
		currentShell=tag.getInt("CurrentShell");
		maxShell=tag.getInt("MaxShell");
		shellFace=tag.getInt("ShellFace");
		shellU=tag.getInt("ShellU");
		shellV=tag.getInt("ShellV");
		setPhase(tag.getInt("Phase"));
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag){
		tag.putInt("Radius",radius);
		tag.putInt("Phase",getPhase());
		tag.putInt("CurrentShell",currentShell);
		tag.putInt("MaxShell",maxShell);
		tag.putInt("ShellFace",shellFace);
		tag.putInt("ShellU",shellU);
		tag.putInt("ShellV",shellV);
	}

	@Override
	public boolean isAttackable(){
		return false;
	}

	@Override
	public boolean isPickable(){
		return false;
	}
}