package cz.maxtechnik.dif.entity;

import cz.maxtechnik.dif.init.other.DifModParticles;
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
public class NuclearMushroomEntity extends Entity{
	// ── Konfigurace HugeSmoke mraku ───────────────────────────────────────────
	private static final double SMOKE_RADIUS=18.0;
	private static final int SMOKE_PARTICLES=2500;
	private static final float SMOKE_RISE_SPEED=5.0f;
	private static final int SMOKE_SPAWN_TICKS=10;
	/**
	 * Kdy entita přestane existovat kvůli smoke (ticky).
	 */
	private static final int SMOKE_LIFETIME=600;
	// ── Konfigurace Fireball mraku ────────────────────────────────────────────
	private static final double FIREBALL_RADIUS=20.0;
	private static final int FIREBALL_PARTICLES=3000;
	private static final float FIREBALL_RISE_SPEED=5F;
	private static final int FIREBALL_SPAWN_TICKS=10;
	/**
	 * Kdy entita přestane existovat kvůli fireballu (ticky).
	 */
	private static final int FIREBALL_LIFETIME=200;
	// ── Společná konfigurace ──────────────────────────────────────────────────
	/**
	 * Entita zanikne až když oba mraky dosáhnou svého lifetime.
	 */
	private static final int LIFETIME_TICKS=Math.max(SMOKE_LIFETIME,FIREBALL_LIFETIME);
	private static final double SEND_RADIUS=512.0;
	// ── Interní stav ─────────────────────────────────────────────────────────
	private int age=0;
	public NuclearMushroomEntity(EntityType<?> type,Level level){
		super(type,level);
		this.noPhysics=true;
	}
	// ── Tick logika ───────────────────────────────────────────────────────────
	@Override
	public void tick(){
		super.tick();
		if(this.level() instanceof ServerLevel serverLevel){
			if(age<SMOKE_SPAWN_TICKS){
				spawnSphereParticles(serverLevel,
						SMOKE_PARTICLES/SMOKE_SPAWN_TICKS,
						SMOKE_RADIUS,
						DifModParticles.HUGE_SMOKE.get(),
						SMOKE_RISE_SPEED);
			}
			if(age<FIREBALL_SPAWN_TICKS){
				spawnSphereParticles(serverLevel,
						FIREBALL_PARTICLES/FIREBALL_SPAWN_TICKS,
						FIREBALL_RADIUS,
						DifModParticles.FIREBALL.get(),
						FIREBALL_RISE_SPEED);
			}
		}
		age++;
		if(age>=LIFETIME_TICKS) this.discard();
	}
	// ── Spawn metody ──────────────────────────────────────────────────────────
	private void spawnSphereParticles(ServerLevel serverLevel,int count,double radius,SimpleParticleType particleType,float riseSpeed){
		for(int i=0;i<count;i++){
			// Rejection sampling — rovnoměrné náhodné rozložení v kouli
			double dx, dy, dz, len;
			do{
				dx=Math.random()*2.0-1.0;
				dy=Math.random()*2.0-1.0;
				dz=Math.random()*2.0-1.0;
				len=Math.sqrt(dx*dx+dy*dy+dz*dz);
			}while(len>1.0||len==0.0);
			// Normalizuj směr a škáluj na rádius (cbrt = rovnoměrné vyplnění objemu)
			double r=radius*Math.cbrt(Math.random());
			dx=(dx/len)*r;
			dy=(dy/len)*r;
			dz=(dz/len)*r;
			double spawnX=this.getX()+dx;
			double spawnY=this.getY()+dy;
			double spawnZ=this.getZ()+dz;
			sendParticle(serverLevel,particleType,spawnX,spawnY,spawnZ,riseSpeed);
		}
	}
	private void sendParticle(ServerLevel serverLevel,SimpleParticleType particleType,double x,double y,double z,float riseSpeed){
		for(ServerPlayer player: serverLevel.getPlayers(p->p.distanceToSqr(x,y,z)<SEND_RADIUS*SEND_RADIUS))
			player.connection.send(
					new ClientboundLevelParticlesPacket(particleType,true,x,y,z,0F,0.2F,0F,riseSpeed,0)
			);
	}
	// ── Povinné přepisy ───────────────────────────────────────────────────────
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