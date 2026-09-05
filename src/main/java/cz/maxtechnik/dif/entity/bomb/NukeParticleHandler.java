package cz.maxtechnik.dif.entity.bomb;

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
	private static final double SEND_RADIUS = 512.0;
	private static final double MAX_HEAD_HEIGHT = 39.0;
	private static final double ASCENT_DURATION = 240.0;

	// HLAVNÍ SMYČKA A ŘÍZENÍ FÁZÍ (TICK)

	public static void tick(ServerLevel level, double bx, double by, double bz, int age, RandomSource random) {
		// Po odeznění aktivního generování oblaku
		if (age > 480) return;

		// Optimalizace serveru: pokud v dosahu 512 bloků není žádný hráč, částice se vůbec nepočítají
		double maxDistSq = SEND_RADIUS * SEND_RADIUS;
		List<ServerPlayer> nearbyPlayers = level.getPlayers(p -> p.distanceToSqr(bx, by, bz) < maxDistSq);
		if (nearbyPlayers.isEmpty()) return;

		// 1. Počáteční detonační záblesk a prudký rozlet částic
		if (age <= 12) {
			spawnDetonationBurst(nearbyPlayers, bx, by, bz, age, random);
		}

		// 2. Trajektorie stoupající žhavé koule
		double tNorm = Math.min(1.0, (double) age / ASCENT_DURATION);
		double easeOut = 1.0 - Math.pow(1.0 - tNorm, 2.4);

		double headY = by + 2.0 + easeOut * (MAX_HEAD_HEIGHT - 2.0);
		double headRadius = 4.5 + easeOut * 13.5;

		// 3. Stoupající koule a objemový oblak hřibu
		spawnMushroomHead(nearbyPlayers, bx, bz, headY, headRadius, easeOut, age, random);

		// 4. Souvislá kouřová noha a světlejší límec pod koulí
		if (age >= 4) {
			spawnStem(nearbyPlayers, bx, by, bz, headY, easeOut, age, random);
			spawnCollar(nearbyPlayers, bx, by, bz, headY, headRadius, age, random);
		}

		// 5. Wilsonův kondenzační prstenec
		if (age >= 25 && age <= 90) {
			spawnCondensationRing(nearbyPlayers, bx, by + 18.0, bz, age, random);
		}
	}

	// 1. DETONAČNÍ ZÁBLESK A VYTRYSKOVÉ ČÁSTICE

	private static void spawnDetonationBurst(List<ServerPlayer> players, double bx, double by, double bz, int age, RandomSource random) {
		if (age == 0) {
			// Centrální oslepující záblesk a výbuch
			sendVanilla(players, ParticleTypes.FLASH, bx, by + 2.0, bz, 1, 0, 0, 0, 0);
			sendVanilla(players, ParticleTypes.EXPLOSION_EMITTER, bx, by + 2.0, bz, 2, 1.0, 1.0, 1.0, 0);

			// Radiální výtrysky lávy, plamenů, Čerenkovova ionizačního záření a jisker
			spawnRadialBurst(players, ParticleTypes.LAVA, bx, by + 1.5, bz, 40, 0.0, Math.PI * 0.44, 1.2, 1.8, 0.25, random);
			spawnRadialBurst(players, ParticleTypes.FLAME, bx, by + 2.0, bz, 45, -0.2 * Math.PI * 0.45, Math.PI * 0.45, 1.0, 2.2, 0.20, random);
			spawnRadialBurst(players, ParticleTypes.SOUL_FIRE_FLAME, bx, by + 2.0, bz, 40, -0.1 * Math.PI * 0.45, Math.PI * 0.45, 1.3, 1.8, 0.30, random);
			spawnRadialBurst(players, ParticleTypes.ELECTRIC_SPARK, bx, by + 2.0, bz, 25, 0.0, Math.PI * 0.50, 0.8, 1.5, 0.20, random);

			// Počáteční obří žhnoucí oblak v epicentru
			float epicSmokeColor = packColor(1.0F, 0.96F, 0.22F);
			for (int i = 0; i < 10; i++) {
				double ox = (random.nextDouble() - 0.5) * 4.5;
				double oy = random.nextDouble() * 3.5;
				double oz = (random.nextDouble() - 0.5) * 4.5;
				spawnSmoke(players, bx + ox, by + 1.5 + oy, bz + oz, epicSmokeColor, 5.8F, 140);
			}
		} else {
			for (int i = 0; i < 5; i++) {
				double theta = random.nextDouble() * Math.PI * 2.0;
				double phi = random.nextDouble() * (Math.PI * 0.42);
				double speed = 0.8 + random.nextDouble() * 1.3;

				double vx = Math.cos(theta) * Math.cos(phi);
				double vy = Math.sin(phi) + 0.2;
				double vz = Math.sin(theta) * Math.cos(phi);

				sendVanilla(players, ParticleTypes.FLAME, bx, by + 2.0, bz, 0, vx, vy, vz, speed);
				sendVanilla(players, ParticleTypes.SOUL_FIRE_FLAME, bx, by + 2.0, bz, 0, vx, vy, vz, speed * 1.1);
			}
		}
	}

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

	// 2. STOUPAJÍCÍ ŽHAVÁ KOULE A OBLAK HŘIBU

	private static void spawnMushroomHead(List<ServerPlayer> players, double bx, double bz, double headY, double headRadius, double easeOut, int age, RandomSource random) {
		// Výpočet barvy dle fáze vzestupu
		float rCol, gCol, bCol;
		if (easeOut < 0.22) {
			// Fáze 1: Zářivá nukleární žlutá (při zemi a raný vzestup)
			float t = (float) (easeOut / 0.22);
			rCol = 1.0F;
			gCol = Mth.lerp(t, 0.96F, 0.82F);
			bCol = Mth.lerp(t, 0.18F, 0.05F);
		} else if (easeOut < 0.52) {
			// Fáze 2: Žhavá oranžová (střední vzestup)
			float t = (float) ((easeOut - 0.22) / 0.30);
			rCol = 1.0F;
			gCol = Mth.lerp(t, 0.82F, 0.38F);
			bCol = Mth.lerp(t, 0.05F, 0.02F);
		} else if (easeOut < 0.80) {
			// Fáze 3: Temně rudá až ohnivá (horní fáze vzestupu)
			float t = (float) ((easeOut - 0.52) / 0.28);
			rCol = Mth.lerp(t, 1.0F, 0.78F);
			gCol = Mth.lerp(t, 0.38F, 0.10F);
			bCol = 0.02F;
		} else {
			// Fáze 4: Popelavě tmavě šedý kouř (vrchol hřibu)
			float t = (float) Math.min(1.0, (easeOut - 0.80) / 0.20);
			rCol = Mth.lerp(t, 0.78F, 0.22F);
			gCol = Mth.lerp(t, 0.10F, 0.22F);
			bCol = Mth.lerp(t, 0.02F, 0.22F);
		}

		float packedColor = packColor(rCol, gCol, bCol);

		// Adaptivní počet částic: plná hustota během vzestupu, pozvolné ředění v pozdní fázi
		int count;
		if (age < 240) {
			count = 3;
		} else if (age < 380) {
			count = (age % 2 == 0) ? 2 : 1;
		} else {
			count = (age % 2 == 0) ? 1 : 0;
		}

		float pSize = (float) (4.6 + easeOut * 2.2);
		// Pozvolné zkracování životnosti v pozdní fázi pro přirozené rozptýlení kouře
		int lifetime = (age <= 260) ? 240 : Math.max(130, 240 - (age - 260));

		for (int i = 0; i < count; i++) {
			double px, py, pz;
			double theta = random.nextDouble() * Math.PI * 2.0;

			if (easeOut < 0.55) {
				// Vzestup: kompaktní stoupající plná žhavá koule
				double u = Math.cbrt(random.nextDouble());
				double phi = (random.nextDouble() - 0.5) * Math.PI;
				double r = headRadius * u;

				px = bx + Math.cos(theta) * Math.cos(phi) * r;
				py = headY + Math.sin(phi) * r * 0.85;
				pz = bz + Math.sin(theta) * Math.cos(phi) * r;
			} else {
				double r, yOff;
				if (random.nextBoolean()) {
					r = headRadius * 0.55 * Math.sqrt(random.nextDouble());
					yOff = (random.nextDouble() - 0.4) * (headRadius * 0.45);
				} else {
					r = headRadius * (0.45 + 0.55 * Math.sqrt(random.nextDouble()));
					yOff = (random.nextDouble() - 0.5) * (headRadius * 0.40);
				}
				px = bx + Math.cos(theta) * r;
				py = headY + yOff;
				pz = bz + Math.sin(theta) * r;
			}

			spawnSmoke(players, px, py, pz, packedColor, pSize, lifetime);
		}

		// Ionizační tyrkysové jiskry vířící kolem koule během žhavé fáze
		if (age >= 4 && age <= 160 && age % 2 == 0) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double r = headRadius * (0.80 + random.nextDouble() * 0.35);
			double sx = bx + Math.cos(angle) * r;
			double sy = headY + (random.nextDouble() - 0.5) * (headRadius * 0.65);
			double sz = bz + Math.sin(angle) * r;

			sendVanilla(players, ParticleTypes.SOUL_FIRE_FLAME, sx, sy, sz, 0, (random.nextDouble() - 0.5) * 0.08, 0.04, (random.nextDouble() - 0.5) * 0.08, 0.08);
		}
	}

	// 3. ZBYTEK HŘIBU

	private static void spawnStem(List<ServerPlayer> players, double bx, double by, double bz, double headY, double easeOut, int age, RandomSource random) {
		double stemBottomY = by - easeOut * 17.0;
		double stemTopY = Math.max(stemBottomY + 2.0, headY - 12.0);
		double stemHeight = stemTopY - stemBottomY;
		if (stemHeight < 1.0) return;

		float baseSmokeColor = packColor(0.17F, 0.17F, 0.17F);
		float glowSmokeColor = packColor(0.75F, 0.45F, 0.15F);

		// Během vzestupu 5 částic/tick pro rychlé vytvoření sloupu, po ustavení 3 částice/tick
		int stemParticles = (age < 140) ? 5 : ((age % 2 == 0) ? 3 : 2);
		int lifetime = (age <= 260) ? 240 : Math.max(130, 240 - (age - 260));

		for (int i = 0; i < stemParticles; i++) {
			double frac = random.nextDouble();
			double stemY = stemBottomY + frac * stemHeight;

			// Profil nohy
			double stemR;
			if (frac < 0.22) {
				double normH = frac / 0.22;
				stemR = 3.0 + 2.4 * (1.0 - normH) * (1.0 - normH);
			} else if (frac > 0.78) {
				double normH = (frac - 0.78) / 0.22;
				stemR = 3.0 + 0.8 * normH;
			} else {
				stemR = 3.0;
			}

			double angle = random.nextDouble() * Math.PI * 2.0;
			double dist = stemR * Math.sqrt(0.15 + 0.85 * random.nextDouble());

			float color = (age < 140 && frac > 0.85) ? glowSmokeColor : baseSmokeColor;
			float pSize = (float) (4.8 + random.nextDouble() * 1.2);

			spawnSmoke(players, bx + Math.cos(angle) * dist, stemY, bz + Math.sin(angle) * dist, color, pSize, lifetime);
		}

		// Přídavné sání prachu přímo na dně kráteru
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

	private static void spawnCollar(List<ServerPlayer> players, double bx, double by, double bz, double headY, double headRadius, int age, RandomSource random) {
		double yTop = headY - 2.5;
		double yBottom = Math.max(by + 2.5, headY - 14.5);
		double collarHeight = yTop - yBottom;
		if (collarHeight < 2.0) return;

		// Adaptivní počet: 4 během vzestupu, 2 po ustavení oblaku
		int collarCount = (age < 200) ? 4 : 2;
		int lifetime = (age <= 260) ? 240 : Math.max(130, 240 - (age - 260));
		double minCollarR = 3.2;
		double maxCollarR = Math.min(13.2, headRadius * 0.72);

		boolean earlyGlow = (age < 130);
		float darkAgeFrac = (float) Math.clamp((age - 190.0) / 150.0, 0.0, 1.0);
		float lightR = 0.62F * (1.0F - darkAgeFrac * 0.42F);
		float lightG = 0.65F * (1.0F - darkAgeFrac * 0.42F);
		float lightB = 0.70F * (1.0F - darkAgeFrac * 0.42F);

		for (int i = 0; i < collarCount; i++) {
			double h = random.nextDouble();
			double cy = yBottom + h * collarHeight + (random.nextDouble() - 0.5) * 1.4;

			double maxRAtH = minCollarR + (maxCollarR - minCollarR) * Math.pow(h, 1.35);
			double cr = maxRAtH * Math.sqrt(0.15 + 0.85 * random.nextDouble());
			double angle = random.nextDouble() * Math.PI * 2.0;

			float colR, colG, colB;
			if (earlyGlow) {
				colR = 0.84F; colG = 0.58F; colB = 0.20F;
			} else {
				float blend = Math.min((float) Math.clamp(h / 0.25, 0.0, 1.0), (float) Math.clamp((1.0 - h) / 0.20, 0.0, 1.0));
				colR = Mth.lerp(blend, 0.20F, lightR);
				colG = Mth.lerp(blend, 0.20F, lightG);
				colB = Mth.lerp(blend, 0.20F, lightB);
			}

			float pSize = (float) (4.8 + h * 1.8 + random.nextDouble() * 0.6);
			spawnSmoke(players, bx + Math.cos(angle) * cr, cy, bz + Math.sin(angle) * cr, colR, colG, colB, pSize, lifetime);
		}
	}

	// 4. WILSONŮV KONDENZAČNÍ PRSTENEC

	private static void spawnCondensationRing(List<ServerPlayer> players, double bx, double ringY, double bz, int age, RandomSource random) {
		double progress = (double) (age - 25) / 65.0;
		double ringRadius = 5.0 + progress * 30.0;

		int points = 8;
		double step = (Math.PI * 2.0) / points;
		double rotOffset = random.nextDouble() * step;
		float ringColor = packColor(0.80F, 0.92F, 1.0F);

		for (int i = 0; i < points; i++) {
			double angle = i * step + rotOffset;
			double rx = bx + Math.cos(angle) * ringRadius;
			double rz = bz + Math.sin(angle) * ringRadius;
			double ry = ringY + (random.nextDouble() - 0.5) * 1.8;

			spawnSmoke(players, rx, ry, rz, ringColor, 3.2F, 45);
		}
	}

	// 5. SÍŤOVÁ VRSTVA A ZASÍLÁNÍ PAKETŮ HRÁČŮM

	public static float packColor(float r, float g, float b) {
		int ir = Math.clamp((int) (r * 255.0F), 0, 255);
		int ig = Math.clamp((int) (g * 255.0F), 0, 255);
		int ib = Math.clamp((int) (b * 255.0F), 0, 255);
		return (float) ((ir << 16) | (ig << 8) | ib);
	}


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
