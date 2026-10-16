package cz.maxtechnik.dif.init.event.nuke;

import cz.maxtechnik.dif.init.other.DifModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.List;
public class NukeParticleHandler {
	// Maximální dosah v blocích (512 bloků), do kterého posíláme částice hráčům.
	// Hráči dále než 512 bloků by částice stejně neviděli a ušetříme síťový přenos.
	private static final double SEND_RADIUS = 512.0;
	// Maximální výška klobouku hřibu nad zemí (39 bloků)
	private static final double MAX_HEAD_HEIGHT = 39.0;
	// Doba stoupání klobouku do maximální výšky v ticích (240 ticků = 12 sekund)
	private static final double ASCENT_DURATION = 240.0;

	/**
	 * Hlavní řídící metoda částicového systému volaná každý tick.
	 *
	 * @param level  serverový svět
	 * @param bx,by,bz přesné souřadnice epicentra výbuchu
	 * @param age    stáří výbuchu v ticích (od 0 do 480)
	 * @param random zdroj náhodných čísel
	 */
	public static void tick(ServerLevel level, double bx, double by, double bz, int age, RandomSource random) {
		// Po 480 ticích (24 sekundách) se kouř rozptýlil a animace částic končí
		if (age > 480) return;

		// --- OPTIMALIZACE VÝKONU: KONTROLA HRÁČŮ V OKOLÍ ---
		// Použijeme druhou mocninu poloměru (512² = 262 144) a porovnáme ji s distanceToSqr.
		// Pokud v okruhu 512 bloků nestojí ANI JEDEN hráč, vůbec nic nepočítáme a neposíláme!
		double maxDistSq = SEND_RADIUS * SEND_RADIUS;
		List<ServerPlayer> nearbyPlayers = level.getPlayers(p -> p.distanceToSqr(bx, by, bz) < maxDistSq);
		if (nearbyPlayers.isEmpty()) return;

		// 1. POČÁTEČNÍ DETONAČNÍ ZÁBLESK A PLAMENNÁ KOULE (prvních 12 ticků = 0.6 sekundy)
		if (age <= 12) {
			spawnDetonationBurst(nearbyPlayers, bx, by, bz, age, random);
		}

		// --- VÝUKA MATEMATIKY: EASING KŘIVKA (NELINEÁRNÍ ZPOMALENÍ) ---
		// Chceme, aby klobouk hřibu na začátku vystřelil prudce vzhůru a nahoře plynule zpomalil.
		// Krok 1: Normalizovaný čas 'tNorm' od 0.0 do 1.0 (0% až 100% stoupání)
		double tNorm = Math.min(1.0, (double) age / ASCENT_DURATION);

		// Krok 2: Polynomiální zpomalení (Ease-Out): y = 1 - (1 - t)^2.4
		// Při t = 0.1 je easeOut už cca 0.22 (velmi rychlý start)
		// Při t = 0.5 je easeOut už cca 0.81 (v polovině času je skoro nahoře)
		// Při t = 1.0 je easeOut = 1.0 (hladké zastavení v cílové výšce)
		double easeOut = 1.0 - Math.pow(1.0 - tNorm, 2.4);

		// Krok 3: Aktuální výška klobouku headY (od země by+2.0 do maxima 39 bloků)
		double headY = by + 2.0 + easeOut * (MAX_HEAD_HEIGHT - 2.0);

		// Krok 4: Aktuální poloměr klobouku (jak stoupá, rozpíná se z 4.5 na 18 bloků)
		double headRadius = 4.5 + easeOut * 13.5;

		// 2. STOUPAJÍCÍ A CHLADNOUCÍ KLOBOUK HŘIBU
		spawnMushroomHead(nearbyPlayers, bx, bz, headY, headRadius, easeOut, age, random);

		// 3. NOHA HŘIBU A SPOJOVACÍ LÍMEC (od 4. ticku, kdy se klobouk zvedne od země)
		if (age >= 4) {
			spawnStem(nearbyPlayers, bx, by, bz, headY, easeOut, age, random);
			spawnCollar(nearbyPlayers, bx, by, bz, headY, headRadius, age, random);
		}

		// 4. KONDENZAČNÍ PRSTENEC WILSONOVA MRAKU (mezi tickem 25 a 90)
		if (age >= 25 && age <= 90) {
			spawnCondensationRing(nearbyPlayers, bx, by + 18.0, bz, age, random);
		}
	}

	// =========================================================================
	// 1. ZÁBLESK A PRVOTNÍ DETONACE
	// =========================================================================
	private static void spawnDetonationBurst(List<ServerPlayer> players, double bx, double by, double bz, int age, RandomSource random) {
		if (age == 0) {
			// V ticku 0 vytvoříme oslepující bílý záblesk (FLASH) a obří explozi (EXPLOSION_EMITTER)
			sendVanilla(players, ParticleTypes.FLASH, bx, by + 2.0, bz, 1, 0, 0, 0, 0);
			sendVanilla(players, ParticleTypes.EXPLOSION_EMITTER, bx, by + 2.0, bz, 2, 1.0, 1.0, 1.0, 0);

			// Vystřelíme do všech stran paprsky lávy, plamenů, dušových modrých plamenů a elektrických jisker
			spawnRadialBurst(players, ParticleTypes.LAVA, bx, by + 1.5, bz, 40, 0.0, Math.PI * 0.44, 1.2, 1.8, 0.25, random);
			spawnRadialBurst(players, ParticleTypes.FLAME, bx, by + 2.0, bz, 45, -0.2 * Math.PI * 0.45, Math.PI * 0.45, 1.0, 2.2, 0.20, random);
			spawnRadialBurst(players, ParticleTypes.SOUL_FIRE_FLAME, bx, by + 2.0, bz, 40, -0.1 * Math.PI * 0.45, Math.PI * 0.45, 1.3, 1.8, 0.30, random);
			spawnRadialBurst(players, ParticleTypes.ELECTRIC_SPARK, bx, by + 2.0, bz, 25, 0.0, Math.PI * 0.50, 0.8, 1.5, 0.20, random);

			// Zářivě žlutá počáteční koule kouře (R=1.0, G=0.96, B=0.22)
			float epicSmokeColor = packColor(1.0F, 0.96F, 0.22F);
			for (int i = 0; i < 10; i++) {
				double ox = (random.nextDouble() - 0.5) * 4.5;
				double oy = random.nextDouble() * 3.5;
				double oz = (random.nextDouble() - 0.5) * 4.5;
				spawnSmoke(players, bx + ox, by + 1.5 + oy, bz + oz, epicSmokeColor, 5.8F, 140);
			}
		} else {
			// V dalších ticích (1 až 12) létají ze středu do vzduchu ohnivé jiskry a žhavíky
			for (int i = 0; i < 5; i++) {
				// Náhodný směr v 3D polárních souřadnicích
				double theta = random.nextDouble() * Math.PI * 2.0; // Azimut (vodorovný směr 0 až 360°)
				double phi = random.nextDouble() * (Math.PI * 0.42); // Elevace (sklon nahoru do vzduchu)
				double speed = 0.8 + random.nextDouble() * 1.3;

				// Převod sférických souřadnic na kartézský vektor rychlosti [vx, vy, vz]:
				double vx = Math.cos(theta) * Math.cos(phi);
				double vy = Math.sin(phi) + 0.2;
				double vz = Math.sin(theta) * Math.cos(phi);

				sendVanilla(players, ParticleTypes.FLAME, bx, by + 2.0, bz, 0, vx, vy, vz, speed);
				sendVanilla(players, ParticleTypes.SOUL_FIRE_FLAME, bx, by + 2.0, bz, 0, vx, vy, vz, speed * 1.1);
			}
		}
	}

	/**
	 * --- VÝUKA MATEMATIKY: SFÉRICKÉ SOUŘADNICE (RADIAL BURST) ---
	 * Pomocná metoda pro radiální rozstřik částic do polokoule:
	 * @param theta úhel azimutu okolo osy Y (0 až 2π čili 360 stupňů)
	 * @param phi   úhel výšky nad horizontem (od phiMin do phiMin + phiRange)
	 * Vzorce rozkladu jednotkového vektoru rychlosti:
	 * vx = cos(theta) * cos(phi)
	 * vy = sin(phi) + vyOffset
	 * vz = sin(theta) * cos(phi)
	 */
	private static void spawnRadialBurst(List<ServerPlayer> players, ParticleOptions particle, double x, double y, double z,
										 int count, double phiMin, double phiRange, double speedMin, double speedRange, double vyOffset, RandomSource random) {
		for (int i = 0; i < count; i++) {
			double theta = random.nextDouble() * Math.PI * 2.0;
			double phi = phiMin + random.nextDouble() * phiRange;
			double speed = speedMin + random.nextDouble() * speedRange;
			double vx = Math.cos(theta) * Math.cos(phi);
			double vy = Math.sin(phi) + vyOffset;
			double vz = Math.sin(theta) * Math.cos(phi);
			sendVanilla(players, particle, x, y, z, 0, vx, vy, vz, speed);
		}
	}

	// =========================================================================
	// 2. KLOBOUK HŘIBU (STOUPAJÍCÍ OHNIVÁ KOULE A ROZTAHUJÍCÍ SE OBLAK)
	// =========================================================================
	private static void spawnMushroomHead(List<ServerPlayer> players, double bx, double bz, double headY, double headRadius, double easeOut, int age, RandomSource random) {
		// --- VÝUKA MATEMATIKY: INTERPOLACE BAREV (CHLADNUTÍ PLAZMY) ---
		// Jak klobouk stoupá (easeOut roste od 0 do 1), simulujeme postupné chladnutí:
		// 1. Zářivě žluto-bílá (0.00 až 0.22)
		// 2. Sytě oranžovo-červená (0.22 až 0.52)
		// 3. Tmavě rudá (0.52 až 0.80)
		// 4. Tmavě šedý popelavý kouř (0.80 až 1.00)
		// Mth.lerp(t, start, end) = start + t * (end - start) provádí plynulý přechod mezi barvami.
		float rCol, gCol, bCol;
		if (easeOut < 0.22) {
			float t = (float)(easeOut / 0.22);
			rCol = 1.0F;
			gCol = Mth.lerp(t, 0.96F, 0.82F);
			bCol = Mth.lerp(t, 0.18F, 0.05F);
		} else if (easeOut < 0.52) {
			float t = (float)((easeOut - 0.22) / 0.30);
			rCol = 1.0F;
			gCol = Mth.lerp(t, 0.82F, 0.38F);
			bCol = Mth.lerp(t, 0.05F, 0.02F);
		} else if (easeOut < 0.80) {
			float t = (float)((easeOut - 0.52) / 0.28);
			rCol = Mth.lerp(t, 1.0F, 0.78F);
			gCol = Mth.lerp(t, 0.38F, 0.10F);
			bCol = 0.02F;
		} else {
			float t = (float) Math.min(1.0, (easeOut - 0.80) / 0.20);
			rCol = Mth.lerp(t, 0.78F, 0.22F);
			gCol = Mth.lerp(t, 0.10F, 0.22F);
			bCol = Mth.lerp(t, 0.02F, 0.22F);
		}
		float packedColor = packColor(rCol, gCol, bCol);

		// Jak mrak stárne, snižujeme počet nových částic za tick, aby se kouř přirozeně rozptyloval
		int count;
		if (age < 240) {
			count = 3;
		} else if (age < 380) {
			count = (age % 2 == 0) ? 2 : 1;
		} else {
			count = (age % 2 == 0) ? 1 : 0;
		}

		// Velikost částice roste s výškou (kouř expanduje do prostoru)
		float pSize = (float)(4.6 + easeOut * 2.2);
		// Doba života částice v ticích (čím starší výbuch, tím kratší životnost, aby kouř mizel)
		int lifetime = (age <= 260) ? 240 : Math.max(130, 240 - (age - 260));

		for (int i = 0; i < count; i++) {
			double px, py, pz;
			double theta = random.nextDouble() * Math.PI * 2.0;

			// --- VÝUKA MATEMATIKY: ROVNOMĚRNÝ VÝBĚR BODU V KRUHU ---
			// Proč Math.sqrt(random.nextDouble())?
			// Plocha kruhu roste se čtvercem poloměru: S = π * r².
			// Kdybychom násobili jen random, částice by se kumulovaly uprostřed a na okraji by byla prázdnota.
			// Odmocnina Math.sqrt zajistí, že hustota částic je stejná v centru i na okraji!
			if (easeOut < 0.55) {
				// Fáze koule (rané stoupání): částice se tvoří v 3D kouli
				double r = headRadius * Math.sqrt(random.nextDouble());
				double phi = (random.nextDouble() - 0.5) * Math.PI;
				px = bx + Math.cos(theta) * r;
				py = headY + Math.sin(phi) * r * 0.85;
				pz = bz + Math.sin(theta) * r;
			} else {
				// Fáze disku (pozdní rozlití klobouku): klobouk se zploští jako talíř
				double r = headRadius * (0.3 + 0.7 * Math.sqrt(random.nextDouble()));
				double yOff = (random.nextDouble() - 0.5) * (headRadius * 0.45);
				px = bx + Math.cos(theta) * r;
				py = headY + yOff;
				pz = bz + Math.sin(theta) * r;
			}
			spawnSmoke(players, px, py, pz, packedColor, pSize, lifetime);
		}

		// Modré plamínky na okraji klobouku (částečně hořící nespálené plyny na rozhraní se vzduchem)
		if (age >= 4 && age <= 160 && age % 2 == 0) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double r = headRadius * (0.80 + random.nextDouble() * 0.35);
			double sx = bx + Math.cos(angle) * r;
			double sy = headY + (random.nextDouble() - 0.5) * (headRadius * 0.65);
			double sz = bz + Math.sin(angle) * r;
			sendVanilla(players, ParticleTypes.SOUL_FIRE_FLAME, sx, sy, sz, 0, (random.nextDouble() - 0.5) * 0.08, 0.04, (random.nextDouble() - 0.5) * 0.08, 0.08);
		}
	}

	// =========================================================================
	// 3. NOHA HŘIBU (STEM) A ZVEDANÝ PRACH ZE ZEMĚ
	// =========================================================================
	private static void spawnStem(List<ServerPlayer> players, double bx, double by, double bz, double headY, double easeOut, int age, RandomSource random) {
		// Spodek nohy klesá s hloubkou kráteru (až 17 bloků pod původní zem)
		double stemBottomY = by - easeOut * 17.0;
		// Vršek nohy končí těsně pod kloboukem
		double stemTopY = Math.max(stemBottomY + 2.0, headY - 12.0);
		double stemHeight = stemTopY - stemBottomY;
		if (stemHeight < 1.0) return;

		// Barva kouře v noze: tmavě šedá, nahoře u klobouku žhavá oranžová
		float baseSmokeColor = packColor(0.17F, 0.17F, 0.17F);
		float glowSmokeColor = packColor(0.75F, 0.45F, 0.15F);

		int stemParticles = (age < 140) ? 5 : ((age % 2 == 0) ? 3 : 2);
		int lifetime = (age <= 260) ? 240 : Math.max(130, 240 - (age - 260));

		for (int i = 0; i < stemParticles; i++) {
			// frac udává výšku na noze: 0.0 = u země, 1.0 = pod kloboukem
			double frac = random.nextDouble();
			double stemY = stemBottomY + frac * stemHeight;

			// Dolní část nohy (frac < 0.25) se rozšiřuje do kuželu směrem k zemi:
			double stemR = (frac < 0.25) ? 3.0 + 2.0 * (1.0 - frac / 0.25) : 3.0;
			double angle = random.nextDouble() * Math.PI * 2.0;
			double dist = stemR * (0.3 + 0.7 * random.nextDouble());

			// Těsně pod kloboukem (frac > 0.85) kouř ještě žhne teplem
			float color = (age < 140 && frac > 0.85) ? glowSmokeColor : baseSmokeColor;
			float pSize = (float)(4.8 + random.nextDouble() * 1.2);
			spawnSmoke(players, bx + Math.cos(angle) * dist, stemY, bz + Math.sin(angle) * dist, color, pSize, lifetime);
		}

		// Prachové mraky valící se přímo ze dna kráteru
		if (easeOut > 0.10 && age % 2 == 0) {
			float craterDustColor = packColor(0.15F, 0.15F, 0.15F);
			for (int j = 0; j < 2; j++) {
				double crAngle = random.nextDouble() * Math.PI * 2.0;
				double crDist = 1.0 + random.nextDouble() * 6.0;
				double cpy = stemBottomY + random.nextDouble() * 3.0;
				spawnSmoke(players, bx + Math.cos(crAngle) * crDist, cpy, bz + Math.sin(crAngle) * crDist, craterDustColor, 5.2F, 130);
			}
		}
	}

	// =========================================================================
	// 4. LÍMEC POD KLOBOUKEM (COLLAR)
	// =========================================================================
	/**
	 * Límec představuje trychtýřovitý vír, kde se stoupající horký sloupec
	 * potkává se spodní stranou rozpínajícího se klobouku.
	 */
	private static void spawnCollar(List<ServerPlayer> players, double bx, double by, double bz, double headY, double headRadius, int age, RandomSource random) {
		double yTop = headY - 2.5;
		double yBottom = Math.max(by + 2.5, headY - 14.5);
		double collarHeight = yTop - yBottom;
		if (collarHeight < 2.0) return;

		int collarCount = (age < 200) ? 4 : 2;
		int lifetime = (age <= 260) ? 240 : Math.max(130, 240 - (age - 260));
		double minCollarR = 3.2;
		double maxCollarR = Math.min(13.2, headRadius * 0.72);
		boolean earlyGlow = (age < 130);

		for (int i = 0; i < collarCount; i++) {
			// h je relativní výška v límci (0.0 dole u nohy, 1.0 nahoře u klobouku)
			double h = random.nextDouble();
			double cy = yBottom + h * collarHeight + (random.nextDouble() - 0.5) * 1.4;

			// Poloměr roste lineárně s výškou: dole je úzký (minCollarR), nahoře široký (maxCollarR)
			double maxRAtH = minCollarR + (maxCollarR - minCollarR) * h;
			double cr = maxRAtH * (0.3 + 0.7 * random.nextDouble());
			double angle = random.nextDouble() * Math.PI * 2.0;

			// V rané fázi límec svítí oranžově, později zešedne
			float colR = earlyGlow ? 0.84F : 0.50F;
			float colG = earlyGlow ? 0.58F : 0.50F;
			float colB = earlyGlow ? 0.20F : 0.52F;
			float pSize = (float)(4.8 + h * 1.8 + random.nextDouble() * 0.6);

			spawnSmoke(players, bx + Math.cos(angle) * cr, cy, bz + Math.sin(angle) * cr, colR, colG, colB, pSize, lifetime);
		}
	}

	// =========================================================================
	// 5. KONDENZAČNÍ PRSTENEC WILSONOVA MRAKU
	// =========================================================================
	/**
	 * Tlaková vlna způsobí adiabatické ochlazení vzduchu, což srazí vodní páru
	 * do tenkého bílého prstence okolo středu exploze.
	 */
	private static void spawnCondensationRing(List<ServerPlayer> players, double bx, double ringY, double bz, int age, RandomSource random) {
		// progress jde od 0.0 (v ticku 25) do 1.0 (v ticku 90)
		double progress = (double)(age - 25) / 65.0;
		// Poloměr prstence expanduje z 5 na 35 bloků
		double ringRadius = 5.0 + progress * 30.0;

		int points = 8;
		double step = (Math.PI * 2.0) / points;
		// rotOffset mírně pootočí body v každém ticku, aby nevznikaly strnulé paprsky
		double rotOffset = random.nextDouble() * step;
		// Světle namodralá/bílá barva kondenzované vody (R=0.80, G=0.92, B=1.0)
		float ringColor = packColor(0.80F, 0.92F, 1.0F);

		for (int i = 0; i < points; i++) {
			double angle = i * step + rotOffset;
			double rx = bx + Math.cos(angle) * ringRadius;
			double rz = bz + Math.sin(angle) * ringRadius;
			double ry = ringY + (random.nextDouble() - 0.5) * 1.8;
			spawnSmoke(players, rx, ry, rz, ringColor, 3.2F, 45);
		}
	}

	// =========================================================================
	// 6. SÍŤOVÁ VRSTVA A BALENÍ DAT DO PAKETŮ
	// =========================================================================
	/**
	 * --- VÝUKA BITOVÝCH OPERACÍ: packColor ---
	 * Jak zabalit 3 barvy RGB (červená, zelená, modrá) do jednoho floatu?
	 * 1. Převedeme floaty (0.0 až 1.0) na celá čísla 0 až 255 (8 bitů na složku).
	 * 2. Červenou posuneme o 16 bitů doleva: (ir << 16)
	 * 3. Zelenou posuneme o 8 bitů doleva:   (ig << 8)
	 * 4. Modrou necháme na začátku:          ib
	 * 5. Spojíme je bitovým NEBO (|): [ RRRRRRRR GGGGGGGG BBBBBBBB ]
	 * Výsledek má 24 bitů a klient si ho v custom shaderu snadno rozbalí zpět!
	 */
	public static float packColor(float r, float g, float b) {
		int ir = Math.clamp((int)(r * 255.0F), 0, 255);
		int ig = Math.clamp((int)(g * 255.0F), 0, 255);
		int ib = Math.clamp((int)(b * 255.0F), 0, 255);
		return (float)((ir << 16) | (ig << 8) | ib);
	}

	/**
	 * Odešle síťový paket ClientboundLevelParticlesPacket hráčům v seznamu.
	 * Využívá custom částici DifModParticles.NUKE_SMOKE, které předává zabalenou barvu,
	 * velikost a životnost.
	 */
	public static void spawnSmoke(List<ServerPlayer> players, double x, double y, double z, float packedColor, float size, int lifetime) {
		ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(
				DifModParticles.NUKE_SMOKE.get(), true, x, y, z, packedColor, size, (float) lifetime, 1.0F, 0
		);
		for (ServerPlayer player : players) {
			player.connection.send(packet);
		}
	}

	public static void spawnSmoke(List<ServerPlayer> players, double x, double y, double z, float r, float g, float b, float size, int lifetime) {
		spawnSmoke(players, x, y, z, packColor(r, g, b), size, lifetime);
	}

	public static void spawnSmoke(ServerLevel level, double x, double y, double z, float r, float g, float b, float size, int lifetime) {
		spawnSmoke(level, x, y, z, packColor(r, g, b), size, lifetime);
	}

	public static void spawnSmoke(ServerLevel level, double x, double y, double z, float packedColor, float size, int lifetime) {
		ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(
				DifModParticles.NUKE_SMOKE.get(), true, x, y, z, packedColor, size, (float) lifetime, 1.0F, 0
		);
		double maxDistSq = SEND_RADIUS * SEND_RADIUS;
		for (ServerPlayer player : level.getPlayers(p -> p.distanceToSqr(x, y, z) < maxDistSq)) {
			player.connection.send(packet);
		}
	}

	/**
	 * Odešle standardní Minecraft vanilla částici (např. plamínek, jiskru, záblesk)
	 * přes síťový paket všem hráčům v seznamu.
	 */
	public static void sendVanilla(List<ServerPlayer> players, ParticleOptions particle, double x, double y, double z, int count, double dx, double dy, double dz, double speed) {
		ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(particle, true, x, y, z, (float) dx, (float) dy, (float) dz, (float) speed, count);
		for (ServerPlayer player : players) {
			player.connection.send(packet);
		}
	}

	public static void sendVanilla(ServerLevel level, ParticleOptions particle, double x, double y, double z, int count, double dx, double dy, double dz, double speed) {
		ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(particle, true, x, y, z, (float) dx, (float) dy, (float) dz, (float) speed, count);
		double maxDistSq = SEND_RADIUS * SEND_RADIUS;
		for (ServerPlayer player : level.getPlayers(p -> p.distanceToSqr(x, y, z) < maxDistSq)) {
			player.connection.send(packet);
		}
	}
}
