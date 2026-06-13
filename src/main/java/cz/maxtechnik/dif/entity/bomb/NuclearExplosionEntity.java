package cz.maxtechnik.dif.entity.bomb;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

@SuppressWarnings("deprecation")
public class NuclearExplosionEntity extends Entity{
	// KONSTANTY ────────────────────────────────────────────────────────────
	private static final int BLOCKS_PER_TICK=16_000, PHASE_INIT=0, PHASE_CRATER=1, PHASE_DONE=2;
	private static final float MAX_DESTROYABLE_RESISTANCE=1500F;
	private static final double HOR_R_FULL=96, HOR_R_TOTAL=128, UP_R_FULL=52, UP_R_TOTAL=56, DOWN_R_FULL=28, DOWN_R_TOTAL=32, HOR_FULL_SQ=HOR_R_FULL*HOR_R_FULL, HOR_TOTAL_SQ=HOR_R_TOTAL*HOR_R_TOTAL, UP_FULL_SQ=UP_R_FULL*UP_R_FULL, UP_TOTAL_SQ=UP_R_TOTAL*UP_R_TOTAL, DN_FULL_SQ=DOWN_R_FULL*DOWN_R_FULL, DN_TOTAL_SQ=DOWN_R_TOTAL*DOWN_R_TOTAL;
	private static final BlockState AIR=Blocks.AIR.defaultBlockState();
	private static final EntityDataAccessor<Integer> DATA_PHASE=SynchedEntityData.defineId(NuclearExplosionEntity.class,EntityDataSerializers.INT);
	// Shell iterátor (kráter – od středu ven)
	private int currentShell, maxShell, shellFace, shellU, shellV, radius=(int)HOR_R_TOTAL;
	private boolean entitiesHit=false;
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
				maxShell=(int)Math.ceil(HOR_R_TOTAL);
				currentShell=shellFace=shellU=shellV=0;
				hitEntities();
				setPhase(PHASE_CRATER);
			}
			case PHASE_CRATER -> tickCrater();
			case PHASE_DONE -> discard();
		}
	}
	// ── Kráter – shell-based od středu ven ───────────────────────────────
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
			// Pro dolní směr posuneme osu o horFrac*2 – na okraji (r=92) sahá dolů o 2 bloky
			double verFullSq, verTotalSq, dyEff;
			if(dy>=0){
				verFullSq=UP_FULL_SQ;
				verTotalSq=UP_TOTAL_SQ;
				dyEff=dy;
			}else{
				double horFrac=Math.sqrt(dxSq+dzSq)/HOR_R_TOTAL; // 0.0 u středu, 1.0 na okraji
				dyEff=dy+horFrac*2.0;                             // posun: na okraji -0 místo -2
				verFullSq=DN_FULL_SQ;
				verTotalSq=DN_TOTAL_SQ;
			}
			double dyEffSq=dyEff*dyEff;
			double nTotal=dxSq/HOR_TOTAL_SQ+dyEffSq/verTotalSq+dzSq/HOR_TOTAL_SQ;
			if(nTotal>1.0) continue;
			double nFull=dxSq/HOR_FULL_SQ+dyEffSq/verFullSq+dzSq/HOR_FULL_SQ;
			boolean destroy;
			if(nFull<=1.0) destroy=true;
			else{
				double scaleSq=1.0/nTotal;
				double maxNFull=(dxSq*scaleSq)/HOR_FULL_SQ+(dyEffSq*scaleSq)/verFullSq+(dzSq*scaleSq)/HOR_FULL_SQ;
				double t=Math.clamp((nFull-1.0)/(maxNFull-1.0),0.0,1.0);
				double chance=1.0-t*0.99;
				destroy=chance>=1.0||random.nextDouble()<chance;
			}
			if(destroy) destroyAt(cx+dx,cy+dy,cz+dz);
			processed++;
		}
	}
	// ── Helpers ───────────────────────────────────────────────────────────
	private void destroyAt(int x,int y,int z){
		mutablePos.set(x,y,z);
		if(!level().isLoaded(mutablePos)) return;
		BlockState state=level().getBlockState(mutablePos);
		if(state.isAir()) return;
		if(state.getBlock().getExplosionResistance()>MAX_DESTROYABLE_RESISTANCE) return;
		level().setBlock(mutablePos,AIR,2|16|64);
	}
	private void hitEntities() {
		if (entitiesHit) return;
		entitiesHit = true;

		level().explode(null, getX(), getY() + 2.0, getZ(), 30, Level.ExplosionInteraction.NONE);

		AABB area = new AABB(getX() - 320, getY() - 320, getZ() - 320, getX() + 320, getY() + 320, getZ() + 320);
		for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, area)) {
			if (entity.isSpectator()) continue;
			if (entity instanceof Player player && player.isCreative()) continue;

			double dist = entity.distanceTo(this);

			// Wither efekt
			if (dist <= 220.0) {
				entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 2400, 1));
			}

			// Damage do 128 bloků
			if (dist <= 128.0) {
				float damage = (float)(200.0 * (1.0 - dist / 128.0));
				entity.invulnerableTime = 0;
				entity.setHealth(entity.getHealth() - damage);
				if (entity.getHealth() <= 0) entity.kill();
			}
		}
	}
	// ── NBT ───────────────────────────────────────────────────────────────
	@Override
	protected void readAdditionalSaveData(CompoundTag tag){
		radius=tag.getInt("Radius");
		entitiesHit=tag.getBoolean("EntitiesHit");
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
		tag.putBoolean("EntitiesHit",entitiesHit);
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