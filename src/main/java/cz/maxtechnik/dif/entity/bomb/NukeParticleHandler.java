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

public class NukeParticleHandler{
	private static final double SEND_RADIUS=512.0;
	private static final double STEM_MAX_HEIGHT=38.0;
	private static final double CAP_MAX_RADIUS=18.0;

	public static void tick(ServerLevel level,double bx,double by,double bz,int age,RandomSource random){
		ParticleOptions nukeSmoke=DifModParticles.NUKE_SMOKE.get();

		// -------------------------------------------------------------
		// 1. Epicentrum: Počáteční gigantický záblesk a fireball (0 - 5 ticků)
		// -------------------------------------------------------------
		if(age<=4){
			sendBatch(level,ParticleTypes.FLASH,bx,by+2.0,bz,2,1.0,1.0,1.0,0.0);
			sendBatch(level,ParticleTypes.EXPLOSION_EMITTER,bx,by+1.5,bz,4,3.0,1.5,3.0,0.0);
			sendBatch(level,ParticleTypes.EXPLOSION,bx,by+2.0,bz,12,3.5,2.5,3.5,0.1);
			sendBatch(level,nukeSmoke,bx,by+2.0,bz,6,3.0,2.0,3.0,0.08);
			sendBatch(level,ParticleTypes.FLAME,bx,by+2.0,bz,20,3.0,2.0,3.0,0.2);
		}

		// -------------------------------------------------------------
		// 2. Noha hřibu (Stoupající masivní kouřový a ohnivý sloup, 0 - 240 ticků)
		// -------------------------------------------------------------
		if(age<=240){
			double currentHeight=Math.min(STEM_MAX_HEIGHT,6.0+age*0.45);
			int slices=4;
			for(int s=0;s<slices;s++){
				double sliceY=by+(currentHeight/slices)*s+random.nextDouble()*2.0;
				double radius=1.8+(sliceY-by)/STEM_MAX_HEIGHT*1.8;

				// Obří částice nukeSmoke (každá velká 6 - 12 bloků!)
				sendBatch(level,nukeSmoke,bx,sliceY,bz,2,radius*0.6,1.0,radius*0.6,0.04);
				// Tmavý kouř pro doplňkovou texturu a vrstvení
				sendBatch(level,ParticleTypes.LARGE_SMOKE,bx,sliceY,bz,3,radius*0.7,1.0,radius*0.7,0.03);

				// Žhavé plameny a jiskry v prvních 50 ticích
				if(age<50){
					if(random.nextFloat()<0.6F){
						sendBatch(level,ParticleTypes.FLAME,bx,sliceY,bz,3,radius*0.5,0.8,radius*0.5,0.05);
					}
					if(random.nextFloat()<0.3F){
						sendBatch(level,ParticleTypes.LAVA,bx,sliceY,bz,2,radius*0.4,0.5,radius*0.4,0.1);
					}
					if(random.nextFloat()<0.25F){
						sendBatch(level,ParticleTypes.EXPLOSION,bx,sliceY,bz,1,radius*0.5,0.5,radius*0.5,0.02);
					}
				}
			}
		}

		// -------------------------------------------------------------
		// 3. Klobouk hřibu (Toroidní mohutná koruna, 15 - 260 ticků)
		// -------------------------------------------------------------
		if(age>=15&&age<=260){
			double capProgress=Math.min(1.0,(age-15.0)/60.0);
			double currentCapRadius=CAP_MAX_RADIUS*capProgress;
			double capCenterY=by+STEM_MAX_HEIGHT;

			for(int i=0;i<5;i++){
				double angle=random.nextDouble()*Math.PI*2.0;
				double r=Math.sqrt(random.nextDouble())*currentCapRadius;
				double px=bx+Math.cos(angle)*r;
				double pz=bz+Math.sin(angle)*r;
				double domeY=capCenterY+Math.cos((r/CAP_MAX_RADIUS)*(Math.PI*0.5))*5.0+(random.nextDouble()-0.5)*2.0;

				float vx=(float)(Math.cos(angle)*0.06);
				float vz=(float)(Math.sin(angle)*0.06);
				float vy=r>(currentCapRadius*0.65)?-0.04F:0.02F;

				sendSingle(level,nukeSmoke,px,domeY,pz,vx,vy,vz,1.0F);
			}

			// Masivní shluk klobouku
			sendBatch(level,nukeSmoke,bx,capCenterY+2.5,bz,4,currentCapRadius*0.5,2.0,currentCapRadius*0.5,0.03);
			sendBatch(level,ParticleTypes.LARGE_SMOKE,bx,capCenterY+2.0,bz,6,currentCapRadius*0.7,2.2,currentCapRadius*0.7,0.02);

			if(age<70&&random.nextFloat()<0.5F){
				sendBatch(level,ParticleTypes.FLAME,bx,capCenterY+1.5,bz,4,currentCapRadius*0.4,1.5,currentCapRadius*0.4,0.04);
			}
		}

		// -------------------------------------------------------------
		// 4. Patní prstenec (Base surge - valící se prach po zemi, 0 - 65 ticků)
		// -------------------------------------------------------------
		if(age<=65){
			double surgeR=3.0+age*0.5;
			for(int i=0;i<6;i++){
				double angle=random.nextDouble()*Math.PI*2.0;
				double sx=bx+Math.cos(angle)*surgeR;
				double sz=bz+Math.sin(angle)*surgeR;
				sendSingle(level,nukeSmoke,sx,by+0.5,sz,
						(float)(Math.cos(angle)*0.08),0.02F,(float)(Math.sin(angle)*0.08),1.0F);
			}
		}

		// -------------------------------------------------------------
		// 5. Wilsonův kondenzační prstenec (10 - 30 ticků)
		// -------------------------------------------------------------
		if(age>=10&&age<=30){
			double ringRadius=4.0+(age-10)*1.4;
			double ringY=by+(STEM_MAX_HEIGHT*0.55);
			for(int i=0;i<16;i++){
				double ringAngle=i*(Math.PI*2.0/16.0)+(random.nextDouble()*0.2);
				double rx=bx+Math.cos(ringAngle)*ringRadius;
				double rz=bz+Math.sin(ringAngle)*ringRadius;
				sendSingle(level,ParticleTypes.CLOUD,rx,ringY,rz,
						(float)(Math.cos(ringAngle)*0.12),0.01F,(float)(Math.sin(ringAngle)*0.12),1.0F);
			}
		}

		// -------------------------------------------------------------
		// 6. Rázová vlna s kinetickým odhozem entit (0 - 45 ticků)
		// -------------------------------------------------------------
		if(age<=45){
			double waveRadius=age*1.8;
			if(waveRadius>1.0&&waveRadius<=64.0){
				int waveParticles=36;
				double step=(Math.PI*2.0)/waveParticles;
				for(int i=0;i<waveParticles;i++){
					double angle=i*step;
					double wx=bx+Math.cos(angle)*waveRadius;
					double wz=bz+Math.sin(angle)*waveRadius;
					ParticleOptions p=(i%2==0)?ParticleTypes.CAMPFIRE_COSY_SMOKE:ParticleTypes.POOF;
					sendSingle(level,p,wx,by+0.5,wz,(float)(Math.cos(angle)*0.2),0.04F,(float)(Math.sin(angle)*0.2),1.0F);
				}

				// Fyzické odhození entit
				AABB waveBox=new AABB(bx-waveRadius-3,by-4,bz-waveRadius-3,bx+waveRadius+3,by+8,bz+waveRadius+3);
				double rMinSq=(waveRadius-3.0)*(waveRadius-3.0);
				double rMaxSq=(waveRadius+3.0)*(waveRadius+3.0);
				for(LivingEntity entity: level.getEntitiesOfClass(LivingEntity.class,waveBox)){
					if(entity.isSpectator()) continue;
					double dx=entity.getX()-bx;
					double dz=entity.getZ()-bz;
					double dSq=dx*dx+dz*dz;
					if(dSq>=rMinSq&&dSq<=rMaxSq){
						double dist=Math.sqrt(dSq);
						if(dist>0.01){
							double pushFactor=Math.max(0.3,1.0-(dist/64.0))*1.8;
							Vec3 motion=new Vec3((dx/dist)*pushFactor,0.35,(dz/dist)*pushFactor);
							entity.setDeltaMovement(entity.getDeltaMovement().add(motion));
							entity.hurtMarked=true;
						}
					}
				}
			}
		}

		// -------------------------------------------------------------
		// 7. Doznívající kouř ze spáleniště (240 - 360 ticků)
		// -------------------------------------------------------------
		if(age>240&&age<=360&&age%2==0){
			sendBatch(level,nukeSmoke,bx,by+1.0,bz,2,4.0,0.8,4.0,0.05);
			sendBatch(level,ParticleTypes.LARGE_SMOKE,bx,by+0.8,bz,2,3.0,0.5,3.0,0.04);
		}
	}

	private static void sendBatch(ServerLevel level,ParticleOptions particle,double x,double y,double z,int count,double dx,double dy,double dz,double speed){
		ClientboundLevelParticlesPacket packet=new ClientboundLevelParticlesPacket(particle,true,x,y,z,(float)dx,(float)dy,(float)dz,(float)speed,count);
		double maxDistSq=SEND_RADIUS*SEND_RADIUS;
		for(ServerPlayer player: level.getPlayers(p->p.distanceToSqr(x,y,z)<maxDistSq)){
			player.connection.send(packet);
		}
	}

	private static void sendSingle(ServerLevel level,ParticleOptions particle,double x,double y,double z,float vx,float vy,float vz,float speed){
		ClientboundLevelParticlesPacket packet=new ClientboundLevelParticlesPacket(particle,true,x,y,z,vx,vy,vz,speed,0);
		double maxDistSq=SEND_RADIUS*SEND_RADIUS;
		for(ServerPlayer player: level.getPlayers(p->p.distanceToSqr(x,y,z)<maxDistSq)){
			player.connection.send(packet);
		}
	}
}
