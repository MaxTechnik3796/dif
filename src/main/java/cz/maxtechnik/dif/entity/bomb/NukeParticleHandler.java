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
 * Zajišťuje veškeré vizuální a částicové efekty atomového výbuchu.
 * Čistě modulární logika:
 * 1. Počáteční detonace a radiální rozlet jisker/plamenů/ionizace do všech směrů.
 * 2. Stoupající žlutá žhavá koule, která plynule zpomaluje výstup (ease-out).
 * 3. Tvorba stoupající tmavé nohy (sloupu) přímo pod stoupající koulí.
 * 4. Plynulý barevný přechod koule: Žlutá -> Oranžová -> Červená -> Šedý popel.
 * 5. Rozvinutí do tvaru klobouku hřibu nahoře, kde zůstává jako šedý kouř a pomalu mizí.
 * 6. Wilsonův kondenzační parní prstenec a pozemní tlaková rázová vlna s odhozením entit.
 */
public class NukeParticleHandler{
	private static final double SEND_RADIUS=512.0;
	private static final double MAX_HEAD_HEIGHT=38.0;
	private static final double ASCENT_DURATION=80.0;

	public static void tick(ServerLevel level,double bx,double by,double bz,int age,RandomSource random){
		// ---------------------------------------------------------------------
		// 1. Počáteční detonační záblesk a prudký rozlet částic (věk 0 - 4 ticky)
		// ---------------------------------------------------------------------
		if(age<=4){
			spawnDetonationBurst(level,bx,by,bz,age,random);
		}

		// ---------------------------------------------------------------------
		// 2. Trajektorie stoupající žhavé koule (ease-out zpomalování)
		// ---------------------------------------------------------------------
		double tNorm=Math.min(1.0,(double)age/ASCENT_DURATION);
		// Kvadraticko-exponenciální ease-out: rychlý start ode dna, plynulé zpomalení nahoře
		double easeOut=1.0-Math.pow(1.0-tNorm,2.4);

		double headY=by+2.0+easeOut*(MAX_HEAD_HEIGHT-2.0);
		double headRadius=4.0+easeOut*12.0;

		// ---------------------------------------------------------------------
		// 3. Stoupající koule a tvorba klobouku hřibu (věk 0 - 160 ticků)
		// ---------------------------------------------------------------------
		if(age<=160){
			spawnMushroomHead(level,bx,bz,headY,headRadius,easeOut,age,random);
		}

		// ---------------------------------------------------------------------
		// 4. Tvorba stoupající nohy pod koulí (věk 4 - 85 ticků)
		// ---------------------------------------------------------------------
		if(age>=4&&age<=85){
			spawnStem(level,bx,by,bz,headY,age,random);
		}

		// ---------------------------------------------------------------------
		// 5. Barevný Wilsonův kondenzační prstenec (věk 12 - 38 ticků)
		// ---------------------------------------------------------------------
		if(age>=12&&age<=38){
			spawnCondensationRing(level,bx,by+16.0,bz,age,random);
		}

		// ---------------------------------------------------------------------
		// 6. Pozemní rázová vlna s prachem a odhozením entit (věk 0 - 36 ticků)
		// ---------------------------------------------------------------------
		if(age<=36){
			spawnGroundShockwave(level,bx,by,bz,age);
		}
	}

	/**
	 * Detonace: Záblesk a prudký radiální rozstřik jisker, lávy a ionizačních plamenů.
	 */
	private static void spawnDetonationBurst(ServerLevel level,double bx,double by,double bz,int age,RandomSource random){
		if(age==0){
			// Centrální oslepující záblesk a výbuch
			sendVanilla(level,ParticleTypes.FLASH,bx,by+2.0,bz,1,0,0,0,0);
			sendVanilla(level,ParticleTypes.EXPLOSION_EMITTER,bx,by+2.0,bz,2,1.0,1.0,1.0,0);

			// A. Žhavé lávové kapky rozlétávající se velkou rychlostí do prostoru
			for(int i=0;i<35;i++){
				double theta=random.nextDouble()*Math.PI*2.0;
				double phi=random.nextDouble()*(Math.PI*0.42);
				double speed=1.2+random.nextDouble()*1.6;

				double vx=Math.cos(theta)*Math.cos(phi);
				double vy=Math.sin(phi)+0.25;
				double vz=Math.sin(theta)*Math.cos(phi);

				sendVanilla(level,ParticleTypes.LAVA,bx,by+1.5,bz,0,vx,vy,vz,speed);
			}

			// B. Ohnivé plameny vystřelující radiálně ven
			for(int i=0;i<40;i++){
				double theta=random.nextDouble()*Math.PI*2.0;
				double phi=(random.nextDouble()-0.2)*(Math.PI*0.45);
				double speed=1.0+random.nextDouble()*2.0;

				double vx=Math.cos(theta)*Math.cos(phi);
				double vy=Math.sin(phi)+0.2;
				double vz=Math.sin(theta)*Math.cos(phi);

				sendVanilla(level,ParticleTypes.FLAME,bx,by+2.0,bz,0,vx,vy,vz,speed);
			}

			// C. Tyrkysový ionizační efekt (Čerenkovovo záření vzduchu)
			for(int i=0;i<35;i++){
				double theta=random.nextDouble()*Math.PI*2.0;
				double phi=(random.nextDouble()-0.1)*(Math.PI*0.45);
				double speed=1.4+random.nextDouble()*1.8;

				double vx=Math.cos(theta)*Math.cos(phi);
				double vy=Math.sin(phi)+0.3;
				double vz=Math.sin(theta)*Math.cos(phi);

				sendVanilla(level,ParticleTypes.SOUL_FIRE_FLAME,bx,by+2.0,bz,0,vx,vy,vz,speed);
			}

			// D. Elektrické jiskry
			for(int i=0;i<20;i++){
				double theta=random.nextDouble()*Math.PI*2.0;
				double phi=random.nextDouble()*Math.PI*0.5;
				double speed=0.8+random.nextDouble()*1.4;

				double vx=Math.cos(theta)*Math.cos(phi);
				double vy=Math.sin(phi)+0.2;
				double vz=Math.sin(theta)*Math.cos(phi);

				sendVanilla(level,ParticleTypes.ELECTRIC_SPARK,bx,by+2.0,bz,0,vx,vy,vz,speed);
			}

			// E. Počáteční obří žhnoucí oblak v epicentru
			for(int i=0;i<8;i++){
				double ox=(random.nextDouble()-0.5)*4.0;
				double oy=random.nextDouble()*3.0;
				double oz=(random.nextDouble()-0.5)*4.0;
				spawnSmoke(level,bx+ox,by+1.5+oy,bz+oz,1.0F,0.96F,0.22F,5.5F,100);
			}
		}else{
			// Dozvuk detonace v ticích 1-4 (stále vylétávají rychlé jiskry)
			for(int i=0;i<6;i++){
				double theta=random.nextDouble()*Math.PI*2.0;
				double phi=random.nextDouble()*(Math.PI*0.4);
				double speed=0.8+random.nextDouble()*1.2;

				double vx=Math.cos(theta)*Math.cos(phi);
				double vy=Math.sin(phi)+0.2;
				double vz=Math.sin(theta)*Math.cos(phi);

				sendVanilla(level,ParticleTypes.FLAME,bx,by+2.0,bz,0,vx,vy,vz,speed);
				sendVanilla(level,ParticleTypes.SOUL_FIRE_FLAME,bx,by+2.0,bz,0,vx,vy,vz,speed*1.1);
			}
		}
	}

	/**
	 * Stoupající koule: Fyzicky stoupá od země vzhůru, mění barvu ze žluté přes oranžovou a červenou
	 * až po šedou, zpomaluje svůj vzestup a na vrcholu rozvine klobouk hřibu.
	 */
	private static void spawnMushroomHead(ServerLevel level,double bx,double bz,double headY,double headRadius,double easeOut,int age,RandomSource random){
		// Výpočet barvy dle výšky/fáze výstupu
		float rCol, gCol, bCol;
		if(easeOut<0.25){
			// Fáze 1: Zářivá nukleární žlutá (při zemi a raný výstup)
			float t=(float)(easeOut/0.25);
			rCol=1.0F;
			gCol=0.96F*(1.0F-t)+0.82F*t;
			bCol=0.18F*(1.0F-t)+0.05F*t;
		}else if(easeOut<0.55){
			// Fáze 2: Žhavá oranžová (střední výstup)
			float t=(float)((easeOut-0.25)/0.30);
			rCol=1.0F;
			gCol=0.82F*(1.0F-t)+0.38F*t;
			bCol=0.05F*(1.0F-t)+0.02F*t;
		}else if(easeOut<0.85){
			// Fáze 3: Temně rudá / ohnivá (horní fáze výstupu)
			float t=(float)((easeOut-0.55)/0.30);
			rCol=1.0F*(1.0F-t)+0.78F*t;
			gCol=0.38F*(1.0F-t)+0.10F*t;
			bCol=0.02F;
		}else{
			// Fáze 4: Popelavě tmavě šedá (vrchol hřibu)
			float t=(float)Math.min(1.0,(easeOut-0.85)/0.15);
			rCol=0.78F*(1.0F-t)+0.22F*t;
			gCol=0.10F*(1.0F-t)+0.22F*t;
			bCol=0.02F*(1.0F-t)+0.22F*t;
		}

		// Počet částic: během stoupání 4 pro hustotu, nahoře 2 pro udržení tvaru
		int count=(age<80)?4:((age%2==0)?2:1);

		for(int i=0;i<count;i++){
			double px, py, pz;
			if(easeOut<0.45){
				// Při výstupu je to ještě kompaktní stoupající koule
				double u=Math.cbrt(random.nextDouble());
				double theta=random.nextDouble()*Math.PI*2.0;
				double phi=(random.nextDouble()-0.5)*Math.PI;

				double r=headRadius*u;
				px=bx+Math.cos(theta)*Math.cos(phi)*r;
				py=headY+Math.sin(phi)*r*0.85;
				pz=bz+Math.sin(theta)*Math.cos(phi)*r;
			}else{
				// Nahoře se rozvine do tvaru klobouku hřibu (klenutý deštník)
				double rFrac=Math.sqrt(random.nextDouble());
				double r=headRadius*rFrac;
				double theta=random.nextDouble()*Math.PI*2.0;

				// Klenutí: střed je výše (+3.2 bloku), okraje klesají mírně dolů (-1.6 bloku)
				double dome=(1.0-rFrac*rFrac)*3.2-(rFrac*1.6)+(random.nextDouble()-0.5)*1.8;
				px=bx+Math.cos(theta)*r;
				py=headY+dome;
				pz=bz+Math.sin(theta)*r;
			}

			float pSize=(float)(4.2+easeOut*2.0); // velikost 4.2 až 6.2 bloku
			int lifetime=(age<80)?120:140;

			spawnSmoke(level,px,py,pz,rCol,gCol,bCol,pSize,lifetime);
		}

		// Ionizační tyrkysové jiskry vířící kolem stoupající koule v žhavé fázi
		if(age>=2&&age<=55&&age%2==0){
			double angle=random.nextDouble()*Math.PI*2.0;
			double r=headRadius*(0.85+random.nextDouble()*0.35);
			double sx=bx+Math.cos(angle)*r;
			double sy=headY+(random.nextDouble()-0.5)*(headRadius*0.7);
			double sz=bz+Math.sin(angle)*r;

			sendVanilla(level,ParticleTypes.SOUL_FIRE_FLAME,sx,sy,sz,0,(random.nextDouble()-0.5)*0.08,0.04,(random.nextDouble()-0.5)*0.08,0.08);
		}
	}

	/**
	 * Noha hřibu: Jak koule stoupá, zanechává přímo pod sebou kouřový sloupec od země až ke kouli.
	 */
	private static void spawnStem(ServerLevel level,double bx,double by,double bz,double headY,int age,RandomSource random){
		int stemParticles=(age%2==0)?3:2;
		for(int i=0;i<stemParticles;i++){
			double stemY=by+1.0+random.nextDouble()*Math.max(1.0,headY-by-2.5);
			double heightFrac=(stemY-by)/Math.max(1.0,headY-by);

			// Profil nohy: dole u země mírně rozšířená, uprostřed štíhlá, nahoře se rozšiřuje do koule
			double stemR=1.3+1.2*(1.0-heightFrac)*(1.0-heightFrac)+1.4*heightFrac*heightFrac;
			double angle=random.nextDouble()*Math.PI*2.0;
			double dist=stemR*Math.sqrt(random.nextDouble());

			double px=bx+Math.cos(angle)*dist;
			double pz=bz+Math.sin(angle)*dist;

			// Barva nohy: převážně tmavý hustý kouř, pod stoupající koulí teplý odlesk
			float sr=0.17F, sg=0.17F, sb=0.17F;
			if(heightFrac>0.80&&age<60){
				sr=0.75F;
				sg=0.38F;
				sb=0.08F;
			}

			spawnSmoke(level,px,stemY,pz,sr,sg,sb,3.8F,130);
		}
	}

	/**
	 * Wilsonův kondenzační prstenec: Horizontální parní rázová vlna ve střední výšce.
	 */
	private static void spawnCondensationRing(ServerLevel level,double bx,double ringY,double bz,int age,RandomSource random){
		double progress=(double)(age-12)/26.0;
		double ringRadius=4.0+progress*24.0;

		int points=8;
		double step=(Math.PI*2.0)/points;
		double rotOffset=random.nextDouble()*step;

		for(int i=0;i<points;i++){
			double angle=i*step+rotOffset;
			double rx=bx+Math.cos(angle)*ringRadius;
			double rz=bz+Math.sin(angle)*ringRadius;

			// Světle azurovo-bílá kondenzační pára
			spawnSmoke(level,rx,ringY+(random.nextDouble()-0.5)*1.5,rz,0.80F,0.90F,1.0F,2.8F,28);
		}
	}

	/**
	 * Pozemní rázová vlna: Prachový prstenec a fyzické odhození entit.
	 */
	private static void spawnGroundShockwave(ServerLevel level,double bx,double by,double bz,int age){
		if(age%2!=0) return;

		double waveRadius=age*1.9;
		if(waveRadius<1.5||waveRadius>64.0) return;

		int waveParticles=20;
		double step=(Math.PI*2.0)/waveParticles;
		for(int i=0;i<waveParticles;i++){
			double angle=i*step;
			double wx=bx+Math.cos(angle)*waveRadius;
			double wz=bz+Math.sin(angle)*waveRadius;

			sendVanilla(level,ParticleTypes.POOF,wx,by+0.4,wz,1,0,0,0,0.04);
			if(age<16&&i%3==0){
				sendVanilla(level,ParticleTypes.FLAME,wx,by+0.3,wz,1,0,0,0,0.02);
			}
		}

		// Fyzické odhození entit tlakovou vlnou
		AABB waveBox=new AABB(bx-waveRadius-3,by-2,bz-waveRadius-3,bx+waveRadius+3,by+6,bz+waveRadius+3);
		double rMinSq=(waveRadius-3.2)*(waveRadius-3.2);
		double rMaxSq=(waveRadius+3.2)*(waveRadius+3.2);

		for(LivingEntity entity: level.getEntitiesOfClass(LivingEntity.class,waveBox)){
			if(entity.isSpectator()) continue;
			double dx=entity.getX()-bx;
			double dz=entity.getZ()-bz;
			double dSq=dx*dx+dz*dz;
			if(dSq>=rMinSq&&dSq<=rMaxSq){
				double dist=Math.sqrt(dSq);
				if(dist>0.01){
					double pushFactor=Math.max(0.35,1.0-(dist/64.0))*1.7;
					Vec3 motion=new Vec3((dx/dist)*pushFactor,0.38,(dz/dist)*pushFactor);
					entity.setDeltaMovement(entity.getDeltaMovement().add(motion));
					entity.hurtMarked=true;
				}
			}
		}
	}

	/**
	 * Odeslání vlastního klientského kouře s 24-bitovou sbalenou barvou, velikostí a životností.
	 */
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

	/**
	 * Odeslání vanillových částic všem hráčům v okruhu 512 bloků.
	 */
	private static void sendVanilla(ServerLevel level,ParticleOptions particle,double x,double y,double z,int count,double dx,double dy,double dz,double speed){
		ClientboundLevelParticlesPacket packet=new ClientboundLevelParticlesPacket(particle,true,x,y,z,(float)dx,(float)dy,(float)dz,(float)speed,count);
		double maxDistSq=SEND_RADIUS*SEND_RADIUS;
		for(ServerPlayer player: level.getPlayers(p->p.distanceToSqr(x,y,z)<maxDistSq)){
			player.connection.send(packet);
		}
	}
}
