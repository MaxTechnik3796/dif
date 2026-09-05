package cz.maxtechnik.dif.init.events.nuke;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public class NukeShockwaveHandler{
	private static final double MAX_GROUND_RADIUS=85.0;
	private static final double MAX_AIR_RADIUS=155.0;

	public static double getGroundWaveRadius(int age){
		return age*1.15;
	}

	public static void tick(ServerLevel level,double bx,double by,double bz,int age){
		// Dolní pozemní kružnice
		if(age<=75){
			tickGroundWave(level,bx,by,bz,age);
		}

		// Horní vzdušná kružnice
		if(age>=6&&age<=80){
			tickAirWave(level,bx,by+34.0,bz,age);
		}
	}

	private static void tickGroundWave(ServerLevel level,double bx,double by,double bz,int age){
		double r=getGroundWaveRadius(age);
		if(r<1.0||r>MAX_GROUND_RADIUS) return;

		int points=Math.clamp((int)(r*2.5),24,130);
		double step=(Math.PI*2.0)/points;
		boolean insideCrater=(r<=34.0);

		for(int i=0;i<points;i++){
			double angle=i*step;
			double wx=bx+Math.cos(angle)*r;
			double wz=bz+Math.sin(angle)*r;

			if(insideCrater){
				NukeParticleHandler.spawnSmoke(level,wx,by+0.35,wz,1.0F,0.70F,0.15F,1.2F,2);
				if(i%4==0){
					NukeParticleHandler.sendVanilla(level,ParticleTypes.FLAME,wx,by+0.25,wz,1,0,0,0,0.0);
				}
			}else{
				NukeParticleHandler.spawnSmoke(level,wx,by+0.35,wz,0.65F,0.60F,0.50F,1.2F,2);
				if(i%4==0){
					NukeParticleHandler.sendVanilla(level,ParticleTypes.POOF,wx,by+0.25,wz,1,0,0,0,0.0);
				}
			}
		}

		// Kinetické odhození entit
		AABB waveBox=new AABB(bx-r-2.5,by-3.0,bz-r-2.5,bx+r+2.5,by+6.0,bz+r+2.5);
		for(LivingEntity entity: level.getEntitiesOfClass(LivingEntity.class,waveBox)){
			if(entity.isSpectator()) continue;
			double dx=entity.getX()-bx;
			double dz=entity.getZ()-bz;
			double dist=Math.sqrt(dx*dx+dz*dz);
			if(Math.abs(dist-r)<=2.0&&dist>0.01){
				double pushFactor=Math.max(0.35,1.0-(dist/MAX_GROUND_RADIUS))*1.8;
				entity.setDeltaMovement(entity.getDeltaMovement().add((dx/dist)*pushFactor,0.40,(dz/dist)*pushFactor));
				entity.hurtMarked=true;
			}
		}
	}

	private static void tickAirWave(ServerLevel level,double bx,double airY,double bz,int age){
		double airRadius=4.0+(age-6)*2.0;
		if(airRadius>MAX_AIR_RADIUS) return;

		int points=Math.clamp((int)(airRadius*2.4),28,160);
		double step=(Math.PI*2.0)/points;

		for(int i=0;i<points;i++){
			double angle=i*step;
			double rx=bx+Math.cos(angle)*airRadius;
			double rz=bz+Math.sin(angle)*airRadius;

			NukeParticleHandler.spawnSmoke(level,rx,airY,rz,0.85F,0.95F,1.0F,1.1F,2);

			if(i%5==0){
				NukeParticleHandler.sendVanilla(level,ParticleTypes.ELECTRIC_SPARK,rx,airY,rz,1,0,0,0,0.0);
			}
		}
	}
}
