package cz.maxtechnik.dif.init.event.nuke;

import cz.maxtechnik.dif.init.other.DifModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
public class NukeMushroomHandler{
	public enum Phase{FIREBALL,ASCENT,CAP_EXPANSION,DISSIPATION}
	public static final int FIREBALL_END=20;
	public static final int ASCENT_END=70;
	public static final int CAP_END=125;
	public static final int TOTAL_MUSHROOM_TICKS=160;
	public static Phase getPhase(int age){
		if(age<=FIREBALL_END) return Phase.FIREBALL;
		if(age<=ASCENT_END) return Phase.ASCENT;
		if(age<=CAP_END) return Phase.CAP_EXPANSION;
		return Phase.DISSIPATION;
	}
	public static void spawnInitialFlash(ServerLevel level,double x,double y,double z,RandomSource random){
		NukeShockwaveHandler.spawnParticle(level,ParticleTypes.FLASH,x,y+3.0,z,2,0,0,0,0.0);
		for(int i=0;i<10;i++){
			double angle=random.nextDouble()*Math.PI*2.0;
			double speed=0.2+random.nextDouble()*0.4;
			double vx=Math.cos(angle)*speed;
			double vz=Math.sin(angle)*speed;
			NukeShockwaveHandler.spawnParticle(level,DifModParticles.NUKE_BLAST.get(),x,y+2.0,z,0,vx,0.15,vz,1.0);
			NukeShockwaveHandler.spawnParticle(level,ParticleTypes.LAVA,x,y+2.0,z,0,vx,0.30,vz,1.0);
		}
	}
	public static void tickMushroomCloud(ServerLevel level,double x,double y,double z,int age,RandomSource random){
		int radius=NukeCraterHandler.getRadius();
		int downRadius=NukeCraterHandler.getCraterDownRadius(radius);
		double bottomY=y-downRadius;
		double maxHeight=y+radius*0.65;
		double ascentProgress=Math.min(1.0,(double)age/CAP_END);
		double headY=y+ascentProgress*(maxHeight-y);
		switch(getPhase(age)){
			case FIREBALL -> tickFireballPhase(level,x,y,z,bottomY,headY,radius,random);
			case ASCENT -> tickAscentPhase(level,x,z,bottomY,headY,radius,ascentProgress,random);
			case CAP_EXPANSION -> tickCapExpansionPhase(level,x,z,bottomY,headY,radius,ascentProgress,random);
			case DISSIPATION -> tickDissipationPhase(level,x,z,bottomY,maxHeight,radius,random);
		}
	}
	private static void tickFireballPhase(ServerLevel level,double x,double y,double z,double bottomY,double headY,int radius,RandomSource random){
		double ballRadius=radius*0.14;
		for(int i=0;i<3;i++){
			double angle=random.nextDouble()*Math.PI*2.0;
			double dist=random.nextDouble()*ballRadius;
			double px=x+Math.cos(angle)*dist;
			double py=headY+(random.nextDouble()-0.5)*10.0;
			double pz=z+Math.sin(angle)*dist;
			ParticleOptions particle=(i==0)?DifModParticles.NUKE_BLAST.get():DifModParticles.NUKE_SMOKE.get();
			NukeShockwaveHandler.spawnParticle(level,particle,px,py,pz,0,0.0,0.05,0.0,1.0);
		}
		spawnStemColumn(level,x,z,bottomY,headY,radius*0.10,2,random);
	}
	private static void tickAscentPhase(ServerLevel level,double x,double z,double bottomY,double headY,int radius,double progress,RandomSource random){
		double headRadius=radius*0.14+progress*(radius*0.16);
		for(int i=0;i<3;i++){
			double angle=random.nextDouble()*Math.PI*2.0;
			double dist=random.nextDouble()*headRadius;
			double px=x+Math.cos(angle)*dist;
			double py=headY+(random.nextDouble()-0.5)*12.0;
			double pz=z+Math.sin(angle)*dist;
			NukeShockwaveHandler.spawnParticle(level,DifModParticles.NUKE_SMOKE.get(),px,py,pz,0,0.0,0.04,0.0,1.0);
		}
		spawnStemColumn(level,x,z,bottomY,headY,radius*0.09,3,random);
	}
	private static void tickCapExpansionPhase(ServerLevel level,double x,double z,double bottomY,double headY,int radius,double progress,RandomSource random){
		double capRadius=radius*0.20+progress*(radius*0.18);
		for(int i=0;i<3;i++){
			double angle=random.nextDouble()*Math.PI*2.0;
			double dirX=Math.cos(angle);
			double dirZ=Math.sin(angle);
			double distFrac=0.25+0.75*random.nextDouble();
			double dist=capRadius*distFrac;
			double rimDrop=(distFrac>0.65)?(distFrac-0.65)*8.0:0.0;
			double px=x+dirX*dist;
			double py=headY+(random.nextDouble()-0.5)*10.0-rimDrop;
			double pz=z+dirZ*dist;
			NukeShockwaveHandler.spawnParticle(level,DifModParticles.NUKE_SMOKE.get(),px,py,pz,0,dirX*0.05,0.01,dirZ*0.05,1.0);
			if(i==0) NukeShockwaveHandler.spawnParticle(level,ParticleTypes.CAMPFIRE_COSY_SMOKE,px,py,pz,0,dirX*0.03,0.02,dirZ*0.03,1.0);
		}
		spawnCollar(level,x,z,headY,capRadius,random);
		spawnStemColumn(level,x,z,bottomY,headY,radius*0.09,3,random);
	}
	private static void tickDissipationPhase(ServerLevel level,double x,double z,double bottomY,double maxHeight,int radius,RandomSource random){
		double capRadius=radius*0.38;
		for(int i=0;i<2;i++){
			double angle=random.nextDouble()*Math.PI*2.0;
			double dist=random.nextDouble()*capRadius;
			double px=x+Math.cos(angle)*dist;
			double py=maxHeight+(random.nextDouble()-0.5)*10.0;
			double pz=z+Math.sin(angle)*dist;
			NukeShockwaveHandler.spawnParticle(level,DifModParticles.NUKE_SMOKE.get(),px,py,pz,0,0.0,0.02,0.0,1.0);
		}
		spawnStemColumn(level,x,z,bottomY,maxHeight,radius*0.08,2,random);
	}
	private static void spawnStemColumn(ServerLevel level,double x,double z,double bottomY,double topY,double baseStemRadius,int count,RandomSource random){
		double height=Math.max(4.0,topY-bottomY);
		for(int i=0;i<count;i++){
			double frac=random.nextDouble();
			double py=bottomY+frac*height;
			double currentRadius=baseStemRadius;
			if(frac<0.30) currentRadius=baseStemRadius*2.0*(1.0-frac*0.5);
			else if(frac>0.80) currentRadius=baseStemRadius*1.3;
			double angle=random.nextDouble()*Math.PI*2.0;
			double dist=random.nextDouble()*currentRadius;
			double px=x+Math.cos(angle)*dist;
			double pz=z+Math.sin(angle)*dist;
			NukeShockwaveHandler.spawnParticle(level,DifModParticles.NUKE_SMOKE.get(),px,py,pz,0,0.0,0.03,0.0,1.0);
		}
	}
	private static void spawnCollar(ServerLevel level,double x,double z,double headY,double capRadius,RandomSource random){
		double collarRadius=capRadius*0.50;
		double angle=random.nextDouble()*Math.PI*2.0;
		double dist=collarRadius*(0.4+0.6*random.nextDouble());
		double px=x+Math.cos(angle)*dist;
		double py=headY-8.0+(random.nextDouble()-0.5)*5.0;
		double pz=z+Math.sin(angle)*dist;
		NukeShockwaveHandler.spawnParticle(level,DifModParticles.NUKE_SMOKE.get(),px,py,pz,0,0.0,0.02,0.0,1.0);
	}
}
