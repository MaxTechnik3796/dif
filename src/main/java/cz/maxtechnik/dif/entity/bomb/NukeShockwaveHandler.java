package cz.maxtechnik.dif.entity.bomb;

import cz.maxtechnik.dif.init.other.DifModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Spravuje rázové vlny atomového výbuchu.
 * Obě vlny jsou čisté KRUŽNICE (pouze tenká obvodová linka bez vnitřní výplně, žádné disky),
 * které se pohybují velkou rychlostí pryč od výbuchu a po dosažení konce cesty ihned zmizí:
 * 1. Dolní pozemní kružnice: Rychle letící obvodová linka po zemi,
 *    přičemž bloky kráteru mizí přesně synchronizovaně pod jejím čelem.
 * 2. Horní vzdušná kružnice: Tenká zářivá obvodová linka vysoko na obloze,
 *    letící rychlostí přes celou atmosféru.
 */
public class NukeShockwaveHandler{
	private static final double SEND_RADIUS=512.0;
	private static final double MAX_GROUND_RADIUS=85.0;
	private static final double MAX_AIR_RADIUS=155.0;

	public static double getGroundWaveRadius(int age){
		return age*1.15;
	}

	public static void tick(ServerLevel level,double bx,double by,double bz,int age,RandomSource random){
		tick(level,bx,by,bz,age);
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

	/**
	 * Dolní pozemní kružnice:
	 * Čistá tenká obvodová linka (životnost jen 2 ticky = nikdy netvoří disk ani výplň).
	 * Jakmile dorazí na konec cesty, okamžitě zmizí.
	 */
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
				// Ohnivá obvodová linka – životnost 2 ticky zaručuje, že nevzniká žádný disk
				spawnSmoke(level,wx,by+0.35,wz,1.0F,0.70F,0.15F,1.2F,2);
				if(i%4==0){
					sendVanilla(level,ParticleTypes.FLAME,wx,by+0.25,wz,1,0,0,0,0.0);
				}
			}else{
				// Prachová obvodová linka mimo kráter – životnost 2 ticky
				spawnSmoke(level,wx,by+0.35,wz,0.65F,0.60F,0.50F,1.2F,2);
				if(i%4==0){
					sendVanilla(level,ParticleTypes.POOF,wx,by+0.25,wz,1,0,0,0,0.0);
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

	/**
	 * Horní vzdušná kružnice:
	 * Čistá tenká obvodová linka vysoko na obloze (žádný disk, žádná výplň),
	 * která letí do dálky 155 bloků a jakmile tam dorazí, ihned zmizí.
	 */
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
			spawnSmoke(level,rx,airY,rz,0.85F,0.95F,1.0F,1.1F,2);

			if(i%5==0){
				sendVanilla(level,ParticleTypes.ELECTRIC_SPARK,rx,airY,rz,1,0,0,0,0.0);
			}
		}
	}

	private static void spawnSmoke(ServerLevel level,double x,double y,double z,float r,float g,float b,float size,int lifetime){
		int ir=Math.clamp((int)(r*255.0F),0,255);
		int ig=Math.clamp((int)(g*255.0F),0,255);
		int ib=Math.clamp((int)(b*255.0F),0,255);
		float packedColor=(float)((ir<<16)|(ig<<8)|ib);

		ClientboundLevelParticlesPacket packet=new ClientboundLevelParticlesPacket(
				DifModParticles.NUKE_SMOKE.get(),true,x,y,z,packedColor,size,(float)lifetime,1.0F,0
		);
		double maxDistSq=SEND_RADIUS*SEND_RADIUS;
		for(ServerPlayer player: level.getPlayers(p->p.distanceToSqr(x,y,z)<maxDistSq)){
			player.connection.send(packet);
		}
	}

	private static void sendVanilla(ServerLevel level,ParticleOptions particle,double x,double y,double z,int count,double dx,double dy,double dz,double speed){
		ClientboundLevelParticlesPacket packet=new ClientboundLevelParticlesPacket(particle,true,x,y,z,(float)dx,(float)dy,(float)dz,(float)speed,count);
		double maxDistSq=SEND_RADIUS*SEND_RADIUS;
		for(ServerPlayer player: level.getPlayers(p->p.distanceToSqr(x,y,z)<maxDistSq)){
			player.connection.send(packet);
		}
	}
}
