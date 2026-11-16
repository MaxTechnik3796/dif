package cz.maxtechnik.dif.init.event.nuke;

import cz.maxtechnik.dif.init.other.DifModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
public class NukeShockwaveHandler{
	private static final double VIEW_RANGE_SQ=512.0*512.0;
	private static final double WAVE_START_RADIUS=6.0;
	public static void tickShockwaves(ServerLevel level,double x,double y,double z,int age,int waveDuration,RandomSource random){
		int radius=NukeCraterHandler.getRadius();
		int downRadius=NukeCraterHandler.getCraterDownRadius(radius);
		if(age<=waveDuration)
			tickGroundWave(level,x,y,z,radius,age,waveDuration,random);
		if(age>=12&&age<=40)
			tickVaporRing(level,x,y+radius*0.20,z,radius,age,random);
		if(age>=10&&age<=110)
			tickBaseSurge(level,x,y,z,radius,downRadius,age,random);
	}
	private static void tickGroundWave(ServerLevel level,double x,double y,double z,int maxRadius,int age,int duration,RandomSource random){
		double progress=(double)age/duration;
		double currentRadius=WAVE_START_RADIUS+progress*(maxRadius-WAVE_START_RADIUS);
		int points=Math.clamp((int)(currentRadius/3.0),16,64);
		for(int i=0;i<points;i++){
			double angle=(2.0*Math.PI*i)/points+(random.nextDouble()-0.5)*0.1;
			double dirX=Math.cos(angle);
			double dirZ=Math.sin(angle);
			double px=x+dirX*currentRadius;
			double pz=z+dirZ*currentRadius;
			spawnParticle(level,DifModParticles.NUKE_BLAST.get(),px,y+0.6,pz,0,dirX*0.22,0.0,dirZ*0.22,1.0);
			if(i%2==0)
				spawnParticle(level,ParticleTypes.CAMPFIRE_COSY_SMOKE,px,y+0.4,pz,0,dirX*0.06,0.02,dirZ*0.06,1.0);
			if(i%4==0)
				spawnParticle(level,ParticleTypes.LARGE_SMOKE,px,y+0.3,pz,0,dirX*0.10,0.02,dirZ*0.10,1.0);
		}
	}
	private static void tickVaporRing(ServerLevel level,double x,double ringY,double z,int maxRadius,int age,RandomSource random){
		double progress=(age-12.0)/28.0;
		double ringRadius=maxRadius*0.10+progress*(maxRadius*0.35);
		int points=10;
		double step=(Math.PI*2.0)/points;
		double offset=random.nextDouble()*step;
		for(int i=0;i<points;i++){
			double angle=i*step+offset;
			double dirX=Math.cos(angle);
			double dirZ=Math.sin(angle);
			double px=x+dirX*ringRadius;
			double pz=z+dirZ*ringRadius;
			spawnParticle(level,ParticleTypes.CLOUD,px,ringY+(random.nextDouble()-0.5)*1.2,pz,0,dirX*0.08,0.0,dirZ*0.08,1.0);
		}
	}
	private static void tickBaseSurge(ServerLevel level,double x,double y,double z,int radius,int downRadius,int age,RandomSource random){
		double progress=Math.min(1.0,(age-10.0)/100.0);
		double craterFloorY=y-downRadius*0.85;
		double surgeRadius=radius*0.14+progress*(radius*0.26);
		double angle=random.nextDouble()*Math.PI*2.0;
		double dirX=Math.cos(angle);
		double dirZ=Math.sin(angle);
		double dist=random.nextDouble()*surgeRadius;
		double px=x+dirX*dist;
		double pz=z+dirZ*dist;
		double py=craterFloorY+(dist/surgeRadius)*(downRadius*0.5);
		spawnParticle(level,DifModParticles.NUKE_SMOKE.get(),px,py,pz,0,dirX*0.03,0.03,dirZ*0.03,1.0);
		spawnParticle(level,ParticleTypes.CAMPFIRE_COSY_SMOKE,px,py+1.0,pz,0,dirX*0.04,0.03,dirZ*0.04,1.0);
	}
	public static void spawnParticle(ServerLevel level,ParticleOptions particle,double x,double y,double z,int count,double dx,double dy,double dz,double speed){
		for(ServerPlayer player: level.players()){
			if(player.distanceToSqr(x,y,z)<VIEW_RANGE_SQ)
				level.sendParticles(player,particle,true,x,y,z,count,dx,dy,dz,speed);
		}
	}
}
