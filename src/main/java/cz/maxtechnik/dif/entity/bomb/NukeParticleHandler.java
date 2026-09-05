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
	private static final double MAX_HEAD_HEIGHT=39.0;
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
		// 3. Stoupající koule a objemový oblak hřibu (věk 0 - 480 ticků)
		// ---------------------------------------------------------------------
		if(age<=480){
			spawnMushroomHead(level,bx,bz,headY,headRadius,easeOut,age,random);
		}

		// ---------------------------------------------------------------------
		// 4. Souvislá kouřová noha a světlejší límec pod koulí (věk 4 - 480 ticků)
		// ---------------------------------------------------------------------
		if(age>=4&&age<=480){
			spawnStem(level,bx,by,bz,headY,easeOut,age,random);
			spawnCollar(level,bx,by,bz,headY,headRadius,easeOut,age,random);
		}

		// ---------------------------------------------------------------------
		// 5. Barevný Wilsonův kondenzační prstenec (věk 25 - 90 ticků)
		// ---------------------------------------------------------------------
		if(age>=25&&age<=90){
			spawnCondensationRing(level,bx,by+18.0,bz,age,random);
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

			float pSize=(float)(4.6+easeOut*2.2); // velikost 4.6 až 6.8 bloku
			int lifetime=240; // dlouhá životnost pro plynulé visení a postupné rozplynutí

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
	 * Souvislá noha hřibu: Zasahuje hluboko do kráteru a propojuje dno se spodkem límce.
	 * Tmavý popelavý sloup se širokým sáním na dně kráteru, který stoupá nahoru
	 * a plynule přechází do světlého kuželového límce bez jakéhokoliv levitování.
	 */
	private static void spawnStem(ServerLevel level,double bx,double by,double bz,double headY,double easeOut,int age,RandomSource random){
		double stemBottomY=by-easeOut*17.0; // noha sahá hluboko na dno vykopaného kráteru
		double stemTopY=Math.max(stemBottomY+2.0,headY-12.0);
		double stemHeight=stemTopY-stemBottomY;
		if(stemHeight<1.0) return;

		int stemParticles=6;
		for(int i=0;i<stemParticles;i++){
			double frac=random.nextDouble();
			double stemY=stemBottomY+frac*stemHeight;

			// Profil nohy: na dně kráteru široké sání z prachu, uprostřed štíhlá, nahoře plynulý náběh do límce
			double stemR;
			if(frac<0.22){
				double normH=frac/0.22;
				stemR=3.0+2.4*(1.0-normH)*(1.0-normH); // 3.0 až 5.4 na dně kráteru
			}else if(frac>0.78){
				double normH=(frac-0.78)/0.22;
				stemR=3.0+0.8*normH; // mírné rozšíření do náběhu límce (3.0 až 3.8)
			}else{
				stemR=3.0;
			}

			double angle=random.nextDouble()*Math.PI*2.0;
			double dist=stemR*Math.sqrt(0.15+0.85*random.nextDouble());

			double px=bx+Math.cos(angle)*dist;
			double pz=bz+Math.sin(angle)*dist;

			// Barva nohy: sytě tmavý popelavý kouř
			float sr=0.17F, sg=0.17F, sb=0.17F;
			if(age<140&&frac>0.85){
				// V rané fázi žhavý odlesk u vrchu
				sr=0.75F;
				sg=0.45F;
				sb=0.15F;
			}

			float pSize=(float)(4.8+random.nextDouble()*1.2); // dostatečně velké pro eliminaci děr
			int lifetime=240;

			spawnSmoke(level,px,stemY,pz,sr,sg,sb,pSize,lifetime);
		}

		// Přídavné částice sání prachu přímo na dně kráteru, aby dno kráteru nebylo holé
		if(easeOut>0.10){
			for(int j=0;j<2;j++){
				double crAngle=random.nextDouble()*Math.PI*2.0;
				double crDist=1.0+random.nextDouble()*6.0;
				double cpx=bx+Math.cos(crAngle)*crDist;
				double cpz=bz+Math.sin(crAngle)*crDist;
				double cpy=stemBottomY+random.nextDouble()*3.0;
				spawnSmoke(level,cpx,cpy,cpz,0.15F,0.15F,0.15F,5.2F,240);
			}
		}
	}

	/**
	 * Světlejší límec / spojení mezi nohou a kloboukem:
	 * Plynulý kuželový trychtýř (límec) světlejších popelavě stříbřitých částic,
	 * který pozvolna roste od šířky nohy (~3.2) až k podhledu klobouku (~12.5 bloků)
	 * bez jakéhokoliv skoku v šířce, přesně podle referenčního obrázku.
	 */
	private static void spawnCollar(ServerLevel level,double bx,double by,double bz,double headY,double headRadius,double easeOut,int age,RandomSource random){
		double yTop=headY-2.5;
		double yBottom=Math.max(by+2.5,headY-14.5);
		double collarHeight=yTop-yBottom;
		if(collarHeight<2.0) return;

		int collarCount=(age<240)?5:3;
		double minCollarR=3.2;
		double maxCollarR=Math.min(13.2,headRadius*0.72);

		for(int i=0;i<collarCount;i++){
			double h=random.nextDouble(); // výškový zlomek v límci (0 = spodek u nohy, 1 = vršek u klobouku)
			double cy=yBottom+h*collarHeight+(random.nextDouble()-0.5)*1.4;

			// Plynulý náběh poloměru: žádný skok! (3.2 u nohy -> ~12.5 u klobouku)
			double maxRAtH=minCollarR+(maxCollarR-minCollarR)*Math.pow(h,1.35);
			double cr=maxRAtH*Math.sqrt(0.15+0.85*random.nextDouble());
			double angle=random.nextDouble()*Math.PI*2.0;

			double px=bx+Math.cos(angle)*cr;
			double pz=bz+Math.sin(angle)*cr;

			// Barva: charakteristický světlejší stříbřitě popelavý kouř z obrázku
			float cr_col, cg_col, cb_col;
			if(age<130){
				// Během stoupání ohnivý/oranžový odlesk
				cr_col=0.84F;
				cg_col=0.58F;
				cb_col=0.20F;
			}else{
				// Trvalý světlejší kouř přecházející dole ze tmy a nahoře do klobouku
				float tBottom=(float)Math.clamp(h/0.25,0.0,1.0);
				float tTop=(float)Math.clamp((1.0-h)/0.20,0.0,1.0);
				float blend=Math.min(tBottom,tTop);

				// Postupné ztmavování límce v pozdějším stádiu ("možná aby byl pak tmavší")
				float darkAgeFrac=(float)Math.clamp((age-190.0)/150.0,0.0,1.0);
				float collarLightR=0.62F*(1.0F-darkAgeFrac*0.42F); // z 0.62 na ~0.36
				float collarLightG=0.65F*(1.0F-darkAgeFrac*0.42F); // z 0.65 na ~0.38
				float collarLightB=0.70F*(1.0F-darkAgeFrac*0.42F); // z 0.70 na ~0.41

				float baseDark=0.20F;

				cr_col=baseDark*(1.0F-blend)+collarLightR*blend;
				cg_col=baseDark*(1.0F-blend)+collarLightG*blend;
				cb_col=baseDark*(1.0F-blend)+collarLightB*blend;
			}

			float pSize=(float)(4.8+h*1.8+random.nextDouble()*0.6); // 4.8 bloku dole, až 7.2 bloku nahoře
			int lifetime=240;

			spawnSmoke(level,px,cy,pz,cr_col,cg_col,cb_col,pSize,lifetime);
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
