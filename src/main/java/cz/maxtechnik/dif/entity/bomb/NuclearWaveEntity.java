package cz.maxtechnik.dif.entity.bomb;

import cz.maxtechnik.dif.init.other.DifModParticles;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
public class NuclearWaveEntity extends Entity{
	private static final double WAVE_SPEED=3, SEND_RADIUS=512;
	private static final int WAVE_MAX_RADIUS=128, PARTICLES_PER_TICK=6; // cca 6 * 42 tick
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
		for(int i=0;i<PARTICLES_PER_TICK;i++){
			double angle=Math.random()*Math.PI*2, px=ox+Math.cos(angle)*waveRadius, pz=oz+Math.sin(angle)*waveRadius;
			sendParticle(sl,DifModParticles.HUGE_SMOKE.get(),px,oy,pz);
		}
	}
	private void sendParticle(ServerLevel serverLevel,SimpleParticleType particleType,double x,double y,double z){
		for(ServerPlayer player: serverLevel.getPlayers(p->p.distanceToSqr(x,y,z)<SEND_RADIUS*SEND_RADIUS))
			player.connection.send(new ClientboundLevelParticlesPacket(particleType,true,x,y,z,0F,0F,0F,0F,0));
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