package cz.maxtechnik.dif.entity.bomb;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
	private static final double SEND_RADIUS=384.0;
	private static final double STEM_HEIGHT=34.0;
	private static final double CAP_RADIUS=16.0;
	private static final int LIFETIME_TICKS=300;

	private int age=0;

	public NuclearMushroomEntity(EntityType<?> type,Level level){
		super(type,level);
		this.noPhysics=true;
	}

	@Override
	public void tick(){
		super.tick();
		if(this.level() instanceof ServerLevel serverLevel){
			double bx=getX(), by=getY(), bz=getZ();

			// 1. Počáteční masivní záblesk a výbuch v epicentru
			if(age==0){
				sendParticle(serverLevel,ParticleTypes.FLASH,bx,by+1.5,bz,0,0,0,0);
				sendParticle(serverLevel,ParticleTypes.EXPLOSION_EMITTER,bx,by+1.0,bz,0,0,0,0);
				for(int i=0;i<8;i++){
					double ox=(random.nextDouble()-0.5)*6.0;
					double oz=(random.nextDouble()-0.5)*6.0;
					sendParticle(serverLevel,ParticleTypes.EXPLOSION_EMITTER,bx+ox,by+1.0,bz+oz,0,0,0,0);
				}
			}

			// 2. Aktivní růst sloupu a klobouku (ticks 0 - 50)
			if(age<50){
				// Patní prstenec (base surge) rozlévající se po zemi
				double surgeAngle=random.nextDouble()*Math.PI*2.0;
				double surgeR=2.0+(age*0.4)*(0.8+random.nextDouble()*0.4);
				double sx=bx+Math.cos(surgeAngle)*surgeR;
				double sz=bz+Math.sin(surgeAngle)*surgeR;
				sendParticle(serverLevel,ParticleTypes.CAMPFIRE_COSY_SMOKE,sx,by+0.3,sz,
						(float)(Math.cos(surgeAngle)*0.08),0.02F,(float)(Math.sin(surgeAngle)*0.08),1.0F);

				// Noha hřibu (stoupající sloup)
				for(int i=0;i<4;i++){
					double h=random.nextDouble()*STEM_HEIGHT*Math.min(1.0,(age+5.0)/25.0);
					double rad=(1.5+(h/STEM_HEIGHT)*1.5)*Math.sqrt(random.nextDouble());
					double a=random.nextDouble()*Math.PI*2.0;
					double px=bx+Math.cos(a)*rad;
					double pz=bz+Math.sin(a)*rad;
					double py=by+h;

					sendParticle(serverLevel,ParticleTypes.CAMPFIRE_COSY_SMOKE,px,py,pz,0F,0.35F,0F,1.0F);
					if(age<25&&random.nextFloat()<0.4F){
						sendParticle(serverLevel,ParticleTypes.FLAME,px,py,pz,0F,0.25F,0F,1.0F);
					}
					if(age<15&&random.nextFloat()<0.2F){
						sendParticle(serverLevel,ParticleTypes.LAVA,px,py,pz,(float)((random.nextDouble()-0.5)*0.2),0.3F,(float)((random.nextDouble()-0.5)*0.2),1.0F);
					}
				}

				// Klobouk (toroidní cirkulace nahoře)
				if(age>8){
					double progress=Math.min(1.0,(age-8.0)/30.0);
					double currentCapR=CAP_RADIUS*progress;
					for(int i=0;i<6;i++){
						double capAngle=random.nextDouble()*Math.PI*2.0;
						double capDist=Math.sqrt(random.nextDouble())*currentCapR;
						// Profil klobouku
						double capY=by+STEM_HEIGHT+Math.cos((capDist/CAP_RADIUS)*(Math.PI*0.5))*4.5;
						double cpx=bx+Math.cos(capAngle)*capDist;
						double cpz=bz+Math.sin(capAngle)*capDist;

						// Radiální expanze ven a mírně dolů na okrajích
						float vx=(float)(Math.cos(capAngle)*0.08);
						float vz=(float)(Math.sin(capAngle)*0.08);
						float vy=capDist>(currentCapR*0.6)?-0.03F:0.04F;

						sendParticle(serverLevel,ParticleTypes.CAMPFIRE_COSY_SMOKE,cpx,capY,cpz,vx,vy,vz,1.0F);
						if(random.nextFloat()<0.3F){
							sendParticle(serverLevel,ParticleTypes.LARGE_SMOKE,cpx,capY,cpz,vx*0.5F,vy,vz*0.5F,1.0F);
						}
					}
				}
			}

			// 3. Wilsonův kondenzační prstenec (rychlá horizontální rázová disková vlna v půlce výšky)
			if(age>=8&&age<=24){
				double ringRadius=4.0+(age-8)*1.5;
				double ringY=by+(STEM_HEIGHT*0.55);
				for(int i=0;i<12;i++){
					double ringAngle=random.nextDouble()*Math.PI*2.0;
					double rx=bx+Math.cos(ringAngle)*ringRadius;
					double rz=bz+Math.sin(ringAngle)*ringRadius;
					sendParticle(serverLevel,ParticleTypes.CLOUD,rx,ringY,rz,
							(float)(Math.cos(ringAngle)*0.1),0.01F,(float)(Math.sin(ringAngle)*0.1),1.0F);
				}
			}

			// 4. Doznívající stoupající dým ze spáleniště (ticks 50 - 240)
			if(age>=50&&age<240&&age%2==0){
				for(int i=0;i<2;i++){
					double rx=bx+(random.nextDouble()-0.5)*8.0;
					double rz=bz+(random.nextDouble()-0.5)*8.0;
					sendParticle(serverLevel,ParticleTypes.CAMPFIRE_COSY_SMOKE,rx,by+0.5,rz,0F,0.12F,0F,1.0F);
				}
			}
		}

		age++;
		if(age>=LIFETIME_TICKS) this.discard();
	}

	private void sendParticle(ServerLevel serverLevel,ParticleOptions particleType,double x,double y,double z,float vx,float vy,float vz,float speed){
		ClientboundLevelParticlesPacket packet=new ClientboundLevelParticlesPacket(particleType,true,x,y,z,vx,vy,vz,speed,0);
		for(ServerPlayer player: serverLevel.getPlayers(p->p.distanceToSqr(x,y,z)<SEND_RADIUS*SEND_RADIUS)){
			player.connection.send(packet);
		}
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