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
 * Čistá modulární logika – prodlouženo na ~3x delší velkolepý průběh:
 * 1. Počáteční detonace a prudký rozlet jisker/lávy/ionizace do všech směrů.
 * 2. Stoupající žlutá žhavá koule, která plynule stoupá a zpomaluje (240 ticků ~ 12 s výstupu).
 * 3. Souvislá kouřová noha pod koulí udržující propojení se zemí po celou dobu aktivního oblaku.
 * 4. Plynulý barevný přechod koule: Žlutá -> Oranžová -> Červená -> Šedý popelavý kouř.
 * 5. Žádná dutá "polokoule" ve vzduchu – plně objemový, valící se oblak hřibu, který přirozeně mizí.
 * 6. Wilsonův kondenzační prstenec a pozemní tlaková rázová vlna.
 */
public class NukeParticleHandler{
	private static final double SEND_RADIUS=512.0;
	private static final double MAX_HEAD_HEIGHT=44.0;
	private static final double ASCENT_DURATION=240.0; // 3x delší výstup (~12 sekund)

	public static void tick(ServerLevel level,double bx,double by,double bz,int age,RandomSource random){
		// ---------------------------------------------------------------------
		// 1. Počáteční detonační záblesk a prudký rozlet částic (věk 0 - 12 ticků)
		// ---------------------------------------------------------------------
		if(age<=12){
			spawnDetonationBurst(level,bx,by,bz,age,random);
		}

		// ---------------------------------------------------------------------
		// 2. Trajektorie stoupající žhavé koule (ease-out zpomalování po dobu 240 ticků)
		// ---------------------------------------------------------------------
		double tNorm=Math.min(1.0,(double)age/ASCENT_DURATION);
		// Kvadraticko-exponenciální ease-out: energický start od země, plynulé zpomalování
		double easeOut=1.0-Math.pow(1.0-tNorm,2.4);

		double headY=by+2.0+easeOut*(MAX_HEAD_HEIGHT-2.0);
		double headRadius=4.5+easeOut*13.5;

		// ---------------------------------------------------------------------
		// 3. Stoupající koule a objemový oblak hřibu (věk 0 - 420 ticků)
		// ---------------------------------------------------------------------
		if(age<=420){
			spawnMushroomHead(level,bx,bz,headY,headRadius,easeOut,age,random);
		}

		// ---------------------------------------------------------------------
		// 4. Souvislá kouřová noha pod koulí (věk 6 - 380 ticků)
		// ---------------------------------------------------------------------
		if(age>=6&&age<=380){
			spawnStem(level,bx,by,bz,headY,age,random);
		}

		// ---------------------------------------------------------------------
		// 5. Barevný Wilsonův kondenzační prstenec (věk 25 - 90 ticků)
		// ---------------------------------------------------------------------
		if(age>=25&&age<=90){
			spawnCondensationRing(level,bx,by+18.0,bz,age,random);
		}

		// ---------------------------------------------------------------------
		// 6. Pozemní rázová vlna s prachem a odhozením entit (věk 0 - 80 ticků)
		// ---------------------------------------------------------------------
		if(age<=80){
			spawnGroundShockwave(level,bx,by,bz,age);
		}
	}

	/**
	 * Detonace: Záblesk a prudký radiální rozlet jisker, lávy a ionizačních plamenů do 3D prostoru.
	 */
	private static void spawnDetonationBurst(ServerLevel level,double bx,double by,double bz,int age,RandomSource random){
		if(age==0){
			// Centrální oslepující záblesk a výbuch
			sendVanilla(level,ParticleTypes.FLASH,bx,by+2.0,bz,1,0,0,0,0);
			sendVanilla(level,ParticleTypes.EXPLOSION_EMITTER,bx,by+2.0,bz,2,1.0,1.0,1.0,0);

			// A. Žhavé lávové kapky vystřelující vysokou rychlostí
			for(int i=0;i<40;i++){
				double theta=random.nextDouble()*Math.PI*2.0;
				double phi=random.nextDouble()*(Math.PI*0.44);
				double speed=1.2+random.nextDouble()*1.8;

				double vx=Math.cos(theta)*Math.cos(phi);
				double vy=Math.sin(phi)+0.25;
				double vz=Math.sin(theta)*Math.cos(phi);

				sendVanilla(level,ParticleTypes.LAVA,bx,by+1.5,bz,0,vx,vy,vz,speed);
			}

			// B. Ohnivé plameny vystřelující radiálně ven
			for(int i=0;i<45;i++){
				double theta=random.nextDouble()*Math.PI*2.0;
				double phi=(random.nextDouble()-0.2)*(Math.PI*0.45);
				double speed=1.0+random.nextDouble()*2.2;

				double vx=Math.cos(theta)*Math.cos(phi);
				double vy=Math.sin(phi)+0.2;
				double vz=Math.sin(theta)*Math.cos(phi);

				sendVanilla(level,ParticleTypes.FLAME,bx,by+2.0,bz,0,vx,vy,vz,speed);
			}

			// C. Tyrkysový ionizační efekt (Čerenkovovo záření)
			for(int i=0;i<40;i++){
				double theta=random.nextDouble()*Math.PI*2.0;
				double phi=(random.nextDouble()-0.1)*(Math.PI*0.45);
				double speed=1.3+random.nextDouble()*1.8;

				double vx=Math.cos(theta)*Math.cos(phi);
				double vy=Math.sin(phi)+0.3;
				double vz=Math.sin(theta)*Math.cos(phi);

				sendVanilla(level,ParticleTypes.SOUL_FIRE_FLAME,bx,by+2.0,bz,0,vx,vy,vz,speed);
			}

			// D. Elektrické jiskry
			for(int i=0;i<25;i++){
				double theta=random.nextDouble()*Math.PI*2.0;
				double phi=random.nextDouble()*Math.PI*0.5;
				double speed=0.8+random.nextDouble()*1.5;

				double vx=Math.cos(theta)*Math.cos(phi);
				double vy=Math.sin(phi)+0.2;
				double vz=Math.sin(theta)*Math.cos(phi);

				sendVanilla(level,ParticleTypes.ELECTRIC_SPARK,bx,by+2.0,bz,0,vx,vy,vz,speed);
			}

			// E. Počáteční obří žhnoucí oblak v epicentru
			for(int i=0;i<10;i++){
				double ox=(random.nextDouble()-0.5)*4.5;
				double oy=random.nextDouble()*3.5;
				double oz=(random.nextDouble()-0.5)*4.5;
				spawnSmoke(level,bx+ox,by+1.5+oy,bz+oz,1.0F,0.96F,0.22F,5.8F,140);
			}
		}else{
			// Dozvuk detonace v ticích 1-12: kontinuální vylétávání jisker a ionizačních plamenů
			for(int i=0;i<5;i++){
				double theta=random.nextDouble()*Math.PI*2.0;
				double phi=random.nextDouble()*(Math.PI*0.42);
				double speed=0.8+random.nextDouble()*1.3;

				double vx=Math.cos(theta)*Math.cos(phi);
				double vy=Math.sin(phi)+0.2;
				double vz=Math.sin(theta)*Math.cos(phi);

				sendVanilla(level,ParticleTypes.FLAME,bx,by+2.0,bz,0,vx,vy,vz,speed);
				sendVanilla(level,ParticleTypes.SOUL_FIRE_FLAME,bx,by+2.0,bz,0,vx,vy,vz,speed*1.1);
			}
		}
	}

	/**
	 * Stoupající koule: Plynule stoupá po dobu 240 ticků (~12 s), postupně mění barvu
	 * Žlutá -> Oranžová -> Červená -> Šedá a nahoře tvoří plně objemový (nikoli dutou skořápku)
	 * valící se kouřový oblak, který přirozeně mizí.
	 */
	private static void spawnMushroomHead(ServerLevel level,double bx,double bz,double headY,double headRadius,double easeOut,int age,RandomSource random){
		// 1. Výpočet barvy dle výšky/fáze výstupu
		float rCol, gCol, bCol;
		if(easeOut<0.22){
			// Fáze 1: Zářivá nukleární žlutá (při zemi a raný výstup)
			float t=(float)(easeOut/0.22);
			rCol=1.0F;
			gCol=0.96F*(1.0F-t)+0.82F*t;
			bCol=0.18F*(1.0F-t)+0.05F*t;
		}else if(easeOut<0.52){
			// Fáze 2: Žhavá oranžová (střední výstup)
			float t=(float)((easeOut-0.22)/0.30);
			rCol=1.0F;
			gCol=0.82F*(1.0F-t)+0.38F*t;
			bCol=0.05F*(1.0F-t)+0.02F*t;
		}else if(easeOut<0.80){
			// Fáze 3: Temně rudá / ohnivá (horní fáze výstupu)
			float t=(float)((easeOut-0.52)/0.28);
			rCol=1.0F*(1.0F-t)+0.78F*t;
			gCol=0.38F*(1.0F-t)+0.10F*t;
			bCol=0.02F;
		}else{
			// Fáze 4: Popelavě tmavě šedý kouř (vrchol hřibu)
			float t=(float)Math.min(1.0,(easeOut-0.80)/0.20);
			rCol=0.78F*(1.0F-t)+0.22F*t;
			gCol=0.10F*(1.0F-t)+0.22F*t;
			bCol=0.02F*(1.0F-t)+0.22F*t;
		}

		// Počet částic: během stoupání 3 částice/tick, při udržování oblaku 2 částice/tick
		int count=(age<240)?3:((age%2==0)?2:1);

		for(int i=0;i<count;i++){
			double px, py, pz;
			double theta=random.nextDouble()*Math.PI*2.0;

			if(easeOut<0.55){
				// Při výstupu: kompaktní stoupající plná žhavá koule
				double u=Math.cbrt(random.nextDouble());
				double phi=(random.nextDouble()-0.5)*Math.PI;
				double r=headRadius*u;

				px=bx+Math.cos(theta)*Math.cos(phi)*r;
				py=headY+Math.sin(phi)*r*0.85;
				pz=bz+Math.sin(theta)*Math.cos(phi)*r;
			}else{
				// Nahoře: Plně objemový valící se oblak (žádná prázdná skořápka / polokoule)
				// Částice vyplňují jak centrální stoupající jádro, tak valící se prstenec
				if(random.nextBoolean()){
					// Vnitřní plné jádro propojující nohu s kloboukem
					double r=headRadius*0.55*Math.sqrt(random.nextDouble());
					px=bx+Math.cos(theta)*r;
					pz=bz+Math.sin(theta)*r;
					py=headY+(random.nextDouble()-0.4)*(headRadius*0.45);
				}else{
					// Vnější valící se kouřový torus
					double ringRadius=headRadius*(0.45+0.55*Math.sqrt(random.nextDouble()));
					px=bx+Math.cos(theta)*ringRadius;
					pz=bz+Math.sin(theta)*ringRadius;
					py=headY+(random.nextDouble()-0.5)*(headRadius*0.40);
				}
			}

			float pSize=(float)(4.4+easeOut*2.2); // velikost 4.4 až 6.6 bloku
			int lifetime=220; // dlouhá životnost pro plynulé visení a postupné rozplynutí

			spawnSmoke(level,px,py,pz,rCol,gCol,bCol,pSize,lifetime);
		}

		// Ionizační tyrkysové jiskry vířící kolem koule během žhavé fáze
		if(age>=4&&age<=160&&age%2==0){
			double angle=random.nextDouble()*Math.PI*2.0;
			double r=headRadius*(0.80+random.nextDouble()*0.35);
			double sx=bx+Math.cos(angle)*r;
			double sy=headY+(random.nextDouble()-0.5)*(headRadius*0.65);
			double sz=bz+Math.sin(angle)*r;

			sendVanilla(level,ParticleTypes.SOUL_FIRE_FLAME,sx,sy,sz,0,(random.nextDouble()-0.5)*0.08,0.04,(random.nextDouble()-0.5)*0.08,0.08);
		}
	}

	/**
	 * Souvislá noha hřibu: Propojuje dno výbuchu se stoupající koulí po celou dobu,
	 * takže oblak nikdy nezůstane viset osamoceně ve vzduchu.
	 */
	private static void spawnStem(ServerLevel level,double bx,double by,double bz,double headY,int age,RandomSource random){
		int stemParticles=2;
		for(int i=0;i<stemParticles;i++){
			double stemY=by+1.0+random.nextDouble()*Math.max(1.0,headY-by);
			double heightFrac=(stemY-by)/Math.max(1.0,headY-by);

			// Profil nohy: dole u země rozšířená (sání prachu), uprostřed štíhlá, nahoře se rozšiřuje do oblaku
			double stemR=1.4+1.3*(1.0-heightFrac)*(1.0-heightFrac)+1.5*heightFrac*heightFrac;
			double angle=random.nextDouble()*Math.PI*2.0;
			double dist=stemR*Math.sqrt(random.nextDouble());

			double px=bx+Math.cos(angle)*dist;
			double pz=bz+Math.sin(angle)*dist;

			// Barva nohy: hustý tmavý kouř, těsně pod stoupající koulí teplý oranžový odlesk
			float sr=0.17F, sg=0.17F, sb=0.17F;
			if(heightFrac>0.78&&age<180){
				sr=0.75F;
				sg=0.38F;
				sb=0.08F;
			}

			spawnSmoke(level,px,stemY,pz,sr,sg,sb,4.0F,220);
		}
	}

	/**
	 * Wilsonův kondenzační prstenec: Horizontální parní rázová vlna ve střední výšce.
	 */
	private static void spawnCondensationRing(ServerLevel level,double bx,double ringY,double bz,int age,RandomSource random){
		double progress=(double)(age-25)/65.0;
		double ringRadius=5.0+progress*30.0; // od 5 do 35 bloků

		int points=8;
		double step=(Math.PI*2.0)/points;
		double rotOffset=random.nextDouble()*step;

		for(int i=0;i<points;i++){
			double angle=i*step+rotOffset;
			double rx=bx+Math.cos(angle)*ringRadius;
			double rz=bz+Math.sin(angle)*ringRadius;

			// Světle azurovo-bílá kondenzační pára
			spawnSmoke(level,rx,ringY+(random.nextDouble()-0.5)*1.8,rz,0.80F,0.92F,1.0F,3.2F,45);
		}
	}

	/**
	 * Pozemní rázová vlna: Prachový prstenec a fyzické odhození entit.
	 */
	private static void spawnGroundShockwave(ServerLevel level,double bx,double by,double bz,int age){
		if(age%2!=0) return;

		double waveRadius=age*1.1; // plynulé šíření do dálky
		if(waveRadius<1.5||waveRadius>72.0) return;

		int waveParticles=20;
		double step=(Math.PI*2.0)/waveParticles;
		for(int i=0;i<waveParticles;i++){
			double angle=i*step;
			double wx=bx+Math.cos(angle)*waveRadius;
			double wz=bz+Math.sin(angle)*waveRadius;

			sendVanilla(level,ParticleTypes.POOF,wx,by+0.4,wz,1,0,0,0,0.04);
			if(age<30&&i%3==0){
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
					double pushFactor=Math.max(0.35,1.0-(dist/72.0))*1.7;
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
