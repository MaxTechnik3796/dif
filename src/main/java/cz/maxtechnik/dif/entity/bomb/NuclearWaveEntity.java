package cz.maxtechnik.dif.entity.bomb;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NuclearWaveEntity extends Entity{
	private static final double WAVE_SPEED=2.0, SEND_RADIUS=384.0;
	private static final int WAVE_MAX_RADIUS=64, PARTICLES_PER_TICK=36;
	private static final EntityDataAccessor<Integer> DATA_TICK=SynchedEntityData.defineId(NuclearWaveEntity.class,EntityDataSerializers.INT);
	private double waveRadius=0;

	public NuclearWaveEntity(EntityType<?> type,Level level){
		super(type,level);
		this.noPhysics=true;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder){
		builder.define(DATA_TICK,0);
	}

	@Override
	public void tick(){
		super.tick();
		if(level().isClientSide) return;
		if(!(level() instanceof ServerLevel sl)) return;

		waveRadius+=WAVE_SPEED;
		if(waveRadius>WAVE_MAX_RADIUS){
			this.discard();
			return;
		}

		double ox=getX(), oy=getY(), oz=getZ();
		double angleStep=(Math.PI*2.0)/PARTICLES_PER_TICK;
		double offset=random.nextDouble()*angleStep;

		// 1. Hustý prstenec rázové vlny
		for(int i=0;i<PARTICLES_PER_TICK;i++){
			double angle=i*angleStep+offset;
			double cos=Math.cos(angle);
			double sin=Math.sin(angle);
			double px=ox+cos*waveRadius;
			double pz=oz+sin*waveRadius;

			ParticleOptions particle=(i%2==0)?ParticleTypes.CAMPFIRE_COSY_SMOKE:ParticleTypes.POOF;
			sendParticle(sl,particle,px,oy+0.5,pz,(float)(cos*0.25),0.04F,(float)(sin*0.25),1.0F);
		}

		// 2. Kinetické odhození entit zasažených rázovou vlnou
		AABB waveBox=new AABB(ox-waveRadius-3,oy-4,oz-waveRadius-3,ox+waveRadius+3,oy+8,oz+waveRadius+3);
		double rMinSq=(waveRadius-3.0)*(waveRadius-3.0);
		double rMaxSq=(waveRadius+3.0)*(waveRadius+3.0);

		for(LivingEntity entity: sl.getEntitiesOfClass(LivingEntity.class,waveBox)){
			if(entity.isSpectator()) continue;
			double dx=entity.getX()-ox;
			double dz=entity.getZ()-oz;
			double dSq=dx*dx+dz*dz;
			if(dSq>=rMinSq&&dSq<=rMaxSq){
				double dist=Math.sqrt(dSq);
				if(dist>0.01){
					double pushFactor=Math.max(0.3,1.0-(dist/WAVE_MAX_RADIUS))*1.6;
					Vec3 motion=new Vec3((dx/dist)*pushFactor,0.35,(dz/dist)*pushFactor);
					entity.setDeltaMovement(entity.getDeltaMovement().add(motion));
					entity.hurtMarked=true;
				}
			}
		}
	}

	private void sendParticle(ServerLevel serverLevel,ParticleOptions particleType,double x,double y,double z,float vx,float vy,float vz,float speed){
		ClientboundLevelParticlesPacket packet=new ClientboundLevelParticlesPacket(particleType,true,x,y,z,vx,vy,vz,speed,0);
		for(ServerPlayer player: serverLevel.getPlayers(p->p.distanceToSqr(x,y,z)<SEND_RADIUS*SEND_RADIUS)){
			player.connection.send(packet);
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag){
		waveRadius=tag.getDouble("WaveRadius");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag){
		tag.putDouble("WaveRadius",waveRadius);
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