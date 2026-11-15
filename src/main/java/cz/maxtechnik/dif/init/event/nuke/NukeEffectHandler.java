package cz.maxtechnik.dif.init.event.nuke;

import cz.maxtechnik.dif.config.DifModServerConfig;
import cz.maxtechnik.dif.network.NukeDetonationMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
public class NukeEffectHandler{
	private static final double PACKET_RANGE_SQ=600.0*600.0;
	public static int getCraterRadius(){
		return DifModServerConfig.NUKE_RADIUS.get();
	}
	public static double getDamageRadius(){
		return DifModServerConfig.NUKE_DAMAGE_RADIUS.get();
	}
	public static void applyDetonationEffects(ServerLevel level,double x,double y,double z){
		sendClientEffects(level,x,y,z);
		playDetonationSounds(level,x,y,z);
		applyRadiationAndBlast(level,x,y,z);
	}
	public static void tickFalloutRadiation(ServerLevel level,double x,double y,double z){
		int radius=getCraterRadius();
		AABB area=new AABB(x-radius,y-radius,z-radius,x+radius,y+radius,z+radius);
		for(LivingEntity entity: level.getEntitiesOfClass(LivingEntity.class,area)){
			if(entity.isSpectator()||(entity instanceof Player player&&player.isCreative())) continue;
			if(entity.distanceToSqr(x,y,z)<=radius*radius){
				entity.addEffect(new MobEffectInstance(MobEffects.WITHER,100,1));
				entity.addEffect(new MobEffectInstance(MobEffects.POISON,100,0));
				entity.addEffect(new MobEffectInstance(MobEffects.HUNGER,200,1));
			}
		}
	}
	private static void sendClientEffects(ServerLevel level,double x,double y,double z){
		for(ServerPlayer player: level.getPlayers(p->p.distanceToSqr(x,y,z)<PACKET_RANGE_SQ))
			PacketDistributor.sendToPlayer(player,new NukeDetonationMessage(x,y,z,1.0F));
	}
	private static void playDetonationSounds(ServerLevel level,double x,double y,double z){
		level.playSound(null,x,y,z,SoundEvents.WARDEN_SONIC_BOOM,SoundSource.BLOCKS,35.0F,0.5F);
		level.playSound(null,x,y,z,SoundEvents.GENERIC_EXPLODE.value(),SoundSource.BLOCKS,35.0F,0.5F);
		level.playSound(null,x,y,z,SoundEvents.WITHER_SPAWN,SoundSource.BLOCKS,30.0F,0.7F);
		level.playSound(null,x,y,z,SoundEvents.WITHER_DEATH,SoundSource.BLOCKS,25.0F,0.55F);
		level.playSound(null,x,y,z,SoundEvents.LIGHTNING_BOLT_THUNDER,SoundSource.BLOCKS,35.0F,0.6F);
	}
	public static void applyRadiationAndBlast(ServerLevel level,double x,double y,double z){
		int craterRadius=getCraterRadius();
		double damageRadius=getDamageRadius();
		double maxRadius=Math.max(craterRadius,damageRadius);
		AABB area=new AABB(x-maxRadius,y-maxRadius,z-maxRadius,x+maxRadius,y+maxRadius,z+maxRadius);
		for(LivingEntity entity: level.getEntitiesOfClass(LivingEntity.class,area)){
			if(entity.isSpectator()||(entity instanceof Player player&&player.isCreative())) continue;
			double dist=Math.sqrt(entity.distanceToSqr(x,y,z));
			if(dist>maxRadius) continue;
			if(dist<=craterRadius){
				entity.hurt(level.damageSources().wither(),250.0F);
				entity.addEffect(new MobEffectInstance(MobEffects.WITHER,1200,2));
				entity.addEffect(new MobEffectInstance(MobEffects.POISON,600,1));
				entity.addEffect(new MobEffectInstance(MobEffects.HUNGER,1200,1));
				entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS,400));
			}else{
				float falloff=(float)(1.0-(dist-craterRadius)/(maxRadius-craterRadius));
				entity.hurt(level.damageSources().wither(),50.0F*falloff);
				entity.addEffect(new MobEffectInstance(MobEffects.WITHER,(int)(600*falloff),1));
				entity.addEffect(new MobEffectInstance(MobEffects.POISON,(int)(400*falloff),0));
				entity.addEffect(new MobEffectInstance(MobEffects.HUNGER,(int)(600*falloff),0));
			}
			Vec3 dir=entity.position().subtract(x,y,z);
			if(dir.lengthSqr()>0.01){
				double strength=Math.max(0.6,1.0-(dist/maxRadius))*3.5;
				Vec3 push=dir.normalize().scale(strength).add(0,0.8,0);
				entity.setDeltaMovement(entity.getDeltaMovement().add(push));
				entity.hurtMarked=true;
			}
		}
	}
}
