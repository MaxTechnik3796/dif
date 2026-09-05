package cz.maxtechnik.dif.entity.bomb;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NukeShockwaveHandler{
	private static final double MAX_GROUND_RADIUS=85.0;
	private static final double MAX_AIR_RADIUS=155.0;

	public static double getGroundWaveRadius(int age){
		return age*1.15;
	}

	public static void tick(ServerLevel level,double bx,double by,double bz,int age){
		// 1. Dolní pozemní kružnice (letí do 85 bloků a ihned zmizí)
		if(age<=75){
			tickGroundWave(level,bx,by,bz,age);
		}

		// 2. Horní vzdušná kružnice (letí vysoko na obloze a po 155 blocích ihned zmizí)
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

		// Kinetické odhození entit přesně na čele kružnice
		AABB waveBox=new AABB(bx-r-2.5,by-3.0,bz-r-2.5,bx+r+2.5,by+6.0,bz+r+2.5);
		double rMinSq=(r-2.0)*(r-2.0);
		double rMaxSq=(r+2.0)*(r+2.0);

		for(LivingEntity entity: level.getEntitiesOfClass(LivingEntity.class,waveBox)){
			if(entity.isSpectator()) continue;
			double dx=entity.getX()-bx;
			double dz=entity.getZ()-bz;
			double dSq=dx*dx+dz*dz;
			if(dSq>=rMinSq&&dSq<=rMaxSq){
				double dist=Math.sqrt(dSq);
				if(dist>0.01){
					double pushFactor=Math.max(0.35,1.0-(dist/MAX_GROUND_RADIUS))*1.8;
					Vec3 motion=new Vec3((dx/dist)*pushFactor,0.40,(dz/dist)*pushFactor);
					entity.setDeltaMovement(entity.getDeltaMovement().add(motion));
					entity.hurtMarked=true;
				}
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

			// Životnost 2 ticky: zobrazí se jako ostrá letící kružnice a nezanechává za sebou kouř
			NukeParticleHandler.spawnSmoke(level,rx,airY,rz,0.85F,0.95F,1.0F,1.1F,2);

			if(i%5==0){
				NukeParticleHandler.sendVanilla(level,ParticleTypes.ELECTRIC_SPARK,rx,airY,rz,1,0,0,0,0.0);
			}
		}
	}
}
