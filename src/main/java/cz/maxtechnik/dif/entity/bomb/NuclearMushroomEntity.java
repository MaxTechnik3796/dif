package cz.maxtechnik.dif.entity.bomb;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static cz.maxtechnik.dif.init.other.DifModParticles.FIREBALL;
import static cz.maxtechnik.dif.init.other.DifModParticles.HUGE_SMOKE;
public class NuclearMushroomEntity extends Entity{
	private static final int SMOKE_PARTICLES=200, SMOKE_SPAWN_TICKS=10, SMOKE_STEM_PARTICLES=100, FIREBALL_PARTICLES=200, FIREBALL_SPAWN_TICKS=10, FIREBALL_STEM_PARTICLES=100, LIFETIME_TICKS=800;
	private static final double SEND_RADIUS=512, FIREBALL_STEM_LENGTH=80, FIREBALL_STEM_RADIUS=1.5, FIREBALL_RADIUS=28, SMOKE_STEM_LENGTH=80, SMOKE_STEM_RADIUS=1, SMOKE_RADIUS=24;
	private static final float SMOKE_RISE_SPEED=5F, FIREBALL_RISE_SPEED=5F;
	private int age=0;
	public NuclearMushroomEntity(EntityType<?> type,Level level){
		super(type,level);
		this.noPhysics=true;
	}
	@Override
	public void tick(){
		super.tick();
		if(this.level() instanceof ServerLevel serverLevel){
			if(age<SMOKE_SPAWN_TICKS){
				spawnSphereParticles(serverLevel,SMOKE_PARTICLES/SMOKE_SPAWN_TICKS,SMOKE_RADIUS,HUGE_SMOKE.get(),SMOKE_RISE_SPEED);
				spawnStemParticles(serverLevel,SMOKE_STEM_PARTICLES/SMOKE_SPAWN_TICKS,SMOKE_STEM_LENGTH,SMOKE_STEM_RADIUS,HUGE_SMOKE.get(),SMOKE_RISE_SPEED);
			}
			if(age<FIREBALL_SPAWN_TICKS){
				spawnSphereParticles(serverLevel,FIREBALL_PARTICLES/FIREBALL_SPAWN_TICKS,FIREBALL_RADIUS,FIREBALL.get(),FIREBALL_RISE_SPEED);
				spawnStemParticles(serverLevel,FIREBALL_STEM_PARTICLES/FIREBALL_SPAWN_TICKS,FIREBALL_STEM_LENGTH,FIREBALL_STEM_RADIUS,FIREBALL.get(),FIREBALL_RISE_SPEED);
			}
		}
		age++;
		if(age>=LIFETIME_TICKS) this.discard();
	}
	private void spawnSphereParticles(ServerLevel serverLevel,int count,double radius,SimpleParticleType particleType,float riseSpeed){
		for(int i=0;i<count;i++){
			double dx, dy, dz, len;
			do{
				dx=Math.random()*2-1;
				dy=Math.random()*2-1;
				dz=Math.random()*2-1;
				len=Math.sqrt(dx*dx+dy*dy+dz*dz);
			}
			while(len>1||len==0);
			double r=radius*Math.cbrt(Math.random());
			dx=(dx/len)*r;
			dy=(dy/len)*r;
			dz=(dz/len)*r;
			double spawnX=this.getX()+dx, spawnY=this.getY()+dy+radius, spawnZ=this.getZ()+dz;
			sendParticle(serverLevel,particleType,spawnX,spawnY,spawnZ,riseSpeed);
		}
	}
	private void spawnStemParticles(ServerLevel serverLevel,int count,double length,double radius,SimpleParticleType particleType,float riseSpeed){
		for(int i=0;i<count;i++){
			double dy=-(Math.random()*length), dx, dz, len;
			do{
				dx=Math.random()*2-1;
				dz=Math.random()*2-1;
				len=Math.sqrt(dx*dx+dz*dz);
			}
			while(len>1||len==0);
			double r=radius*Math.sqrt(Math.random());
			dx=(dx/len)*r;
			dz=(dz/len)*r;
			double spawnX=this.getX()+dx, spawnY=this.getY()+dy, spawnZ=this.getZ()+dz;
			sendParticle(serverLevel,particleType,spawnX,spawnY,spawnZ,riseSpeed);
		}
	}
	private void sendParticle(ServerLevel serverLevel,SimpleParticleType particleType,double x,double y,double z,float riseSpeed){
		for(ServerPlayer player: serverLevel.getPlayers(p->p.distanceToSqr(x,y,z)<SEND_RADIUS*SEND_RADIUS))
			player.connection.send(new ClientboundLevelParticlesPacket(particleType,true,x,y,z,0F,0.2F,0F,riseSpeed,0));
	}
	@Override
	protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder){
	}
	@Override
	protected void readAdditionalSaveData(CompoundTag tag){
		age=tag.getInt("MushroomAge");
	}
	@Override
	protected void addAdditionalSaveData(CompoundTag tag){
		tag.putInt("MushroomAge",age);
	}
}