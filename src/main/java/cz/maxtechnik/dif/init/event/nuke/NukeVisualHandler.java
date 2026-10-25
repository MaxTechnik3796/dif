package cz.maxtechnik.dif.init.event.nuke;

import cz.maxtechnik.dif.network.NukeDetonationMessage;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;
public class NukeVisualHandler{
	public static final double INITIAL_SPHERE_RADIUS=9.0;
	public static final double MAX_EXPAND_RADIUS=48.0;
	private static final DustParticleOptions PURPLE_PARTICLE=new DustParticleOptions(new Vector3f(0.70F,0.15F,1.00F),4.0F);
	private static final DustParticleOptions GRAY_PARTICLE=new DustParticleOptions(new Vector3f(0.38F,0.38F,0.42F),4.0F);
	private static final DustColorTransitionOptions PURPLE_TO_GRAY=new DustColorTransitionOptions(
			new Vector3f(0.75F,0.20F,1.00F),
			new Vector3f(0.28F,0.28F,0.32F),
			4.0F
	);
	public static void triggerInitialBlast(ServerLevel level,double x,double y,double z){
		level.playSound(null,x,y,z,SoundEvents.GENERIC_EXPLODE.value(),SoundSource.BLOCKS,8.0F,0.8F);
		level.playSound(null,x,y,z,SoundEvents.BEACON_ACTIVATE,SoundSource.BLOCKS,10.0F,0.7F);
		spawnParticle(level,ParticleTypes.EXPLOSION_EMITTER,x,y,z,2,1.5,1.5,1.5,0.0);
		spawnParticle(level,ParticleTypes.FLASH,x,y,z,1,0,0,0,0.0);
	}
	public static void tickImplosion(ServerLevel level,double x,double y,double z,int age,int implosionTicks,RandomSource random){
		double progress=(double)age/implosionTicks;
		double currentRadius=Math.max(0.4,INITIAL_SPHERE_RADIUS*(1.0-progress));
		spawnParticleSphere(level,x,y,z,currentRadius,45,random);
		if(age%12==0){
			float pitch=0.6F+(float)progress*1.2F;
			level.playSound(null,x,y,z,SoundEvents.RESPAWN_ANCHOR_CHARGE,SoundSource.BLOCKS,10.0F,pitch);
		}
	}
	public static void triggerMainDetonation(ServerLevel level,double x,double y,double z){
		for(ServerPlayer player: level.getPlayers(p->p.distanceToSqr(x,y,z)<512*512)){
			PacketDistributor.sendToPlayer(player,new NukeDetonationMessage(x,y,z,1.0F));
		}
		level.playSound(null,x,y,z,SoundEvents.WARDEN_SONIC_BOOM,SoundSource.BLOCKS,25.0F,0.5F);
		level.playSound(null,x,y,z,SoundEvents.GENERIC_EXPLODE.value(),SoundSource.BLOCKS,25.0F,0.5F);
		level.playSound(null,x,y,z,SoundEvents.WITHER_DEATH,SoundSource.BLOCKS,20.0F,0.6F);
		level.playSound(null,x,y,z,SoundEvents.LIGHTNING_BOLT_THUNDER,SoundSource.BLOCKS,25.0F,0.7F);
		spawnParticle(level,ParticleTypes.FLASH,x,y,z,6,3.0,3.0,3.0,0.0);
		spawnParticle(level,ParticleTypes.EXPLOSION_EMITTER,x,y,z,8,4.0,4.0,4.0,0.0);
	}
	public static void tickExplosionExpand(ServerLevel level,double x,double y,double z,int blastAge,int expandTicks,RandomSource random){
		double progress=(double)blastAge/expandTicks;
		double currentRadius=1.0+progress*MAX_EXPAND_RADIUS;
		int count=(int)(80*(1.0-progress*0.5));
		spawnParticleSphere(level,x,y,z,currentRadius,count,random);
	}
	private static void spawnParticleSphere(ServerLevel level,double x,double y,double z,double radius,int count,RandomSource random){
		for(int i=0;i<count;i++){
			double dx=random.nextDouble()*2.0-1.0;
			double dy=random.nextDouble()*2.0-1.0;
			double dz=random.nextDouble()*2.0-1.0;
			double len=Math.sqrt(dx*dx+dy*dy+dz*dz);
			if(len<0.01||len>1.0) continue;
			double px=x+(dx/len)*radius;
			double py=y+(dy/len)*radius;
			double pz=z+(dz/len)*radius;
			ParticleOptions particle=(i%3==0)?PURPLE_PARTICLE:(i%3==1)?GRAY_PARTICLE:PURPLE_TO_GRAY;
			spawnParticle(level,particle,px,py,pz,1,0.2,0.2,0.2,0.0);
		}
	}
	public static void spawnParticle(ServerLevel level,ParticleOptions particle,double x,double y,double z,int count,double dx,double dy,double dz,double speed){
		for(ServerPlayer player: level.players()){
			if(player.distanceToSqr(x,y,z)<512*512){
				level.sendParticles(player,particle,true,x,y,z,count,dx,dy,dz,speed);
			}
		}
	}
}
