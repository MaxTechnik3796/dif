package cz.maxtechnik.dif.init.event.nuke;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
public class NukeShockwaveHandler {
	// Maximální poloměr, kam až po zemi dosáhne prachová rázová vlna (v blocích)
	private static final double MAX_GROUND_RADIUS = 85.0;
	// Maximální poloměr vzdušného prstence vysoko nad zemí (v blocích)
	private static final double MAX_AIR_RADIUS = 155.0;

	/**
	 * VÝPOČET POLOMĚRU POZEMNÍ VLNY V ČASE:
	 * Tato funkce je klíčem k celé synchronizaci! Používá ji jak tato třída pro vizuální kruh,
	 * tak NukeCraterHandler pro postupné vykopávání kráteru.
	 *
	 * MATEMATICKÝ PRINCIP (Rovnoměrný přímočarý pohyb):
	 * dráha = rychlost * čas  =>  r = age * 1.15
	 * - 'age' je čas v ticích (1 sekunda = 20 ticků).
	 * - Koeficient 1.15 znamená, že každou 1/20 sekundy se vlna posune o 1.15 bloku dál.
	 * - Za 1 celou sekundu (20 ticků) tedy vlna urazí: 20 * 1.15 = 23 bloků (23 m/s).
	 */
	public static double getGroundWaveRadius(int age) {
		return age * 1.15;
	}

	/**
	 * Hlavní řídící metoda rázové vlny, volaná každý tick z NuclearExplosionEntity.
	 */
	public static void tick(ServerLevel level, double bx, double by, double bz, int age) {
		// 1. Dolní pozemní vlna: běží od ticku 0 do ticku 75 (cca 3.75 sekundy od výbuchu)
		if (age <= 75) {
			tickGroundWave(level, bx, by, bz, age);
		}

		// 2. Horní vzdušná vlna: začíná v ticku 6 (s mírným zpožděním po záblesku)
		// a šíří se ve výšce 34 bloků nad epicentrem (by + 34.0)
		if (age >= 6 && age <= 80) {
			tickAirWave(level, bx, by + 34.0, bz, age);
		}
	}

	/**
	 * Simuluje pozemní prstenec kouře a odfukování entit, které vlna zasáhne.
	 */
	private static void tickGroundWave(ServerLevel level, double bx, double by, double bz, int age) {
		// Zjistíme, jak velký má kruh v tomto ticku být
		double r = getGroundWaveRadius(age);
		// Pokud je vlna příliš malá (< 1 blok) nebo už přerostla limit, končíme
		if (r < 1.0 || r > MAX_GROUND_RADIUS) return;

		// --- VÝUKA MATEMATIKY: DYNAMICKÝ POČET BODŮ NA KRUŽNICI ---
		// Obvod kruhu se počítá jako O = 2 * π * r.
		// Když se kruh zvětšuje, jeho obvod roste. Kdybychom měli pevný počet bodů (např. 30),
		// na malém kruhu by byly částice nahňácané na sobě a na velkém by mezi nimi byly obří díry.
		// Proto počet bodů násobíme poloměrem: r * 2.5.
		// Funkce Math.clamp(hodnota, min, max) ořízne výsledek:
		// - Nikdy jich nebude méně než 24 (aby to byl hezký kruh i na začátku).
		// - Nikdy jich nebude více než 130 (abychom nezpůsobili lag na grafické kartě hráčů).
		int points = Math.clamp((int)(r * 2.5), 24, 130);

		// Celý kruh má v radiánech 2 * π (360 stupňů).
		// Úhlový krok 'step' je úhel mezi dvěma sousedními částicemi na kružnici.
		double step = (Math.PI * 2.0) / points;

		// Pokud je poloměr menší než 34 bloků, jsme ještě uvnitř horkého kráteru.
		// Mimo kráter už je chladnější zem a zvedá se jen prach.
		boolean insideCrater = (r <= 34.0);

		// Vykreslení kružnice bod po bodu:
		for (int i = 0; i < points; i++) {
			// Úhel pro aktuální i-tý bod: 0, step, 2*step, 3*step, ... až 2π
			double angle = i * step;

			// --- GONIOMETRICKÝ PŘEVOD Z POLÁRNÍCH SOUŘADNIC NA KARTÉZSKÉ (X, Z) ---
			// Střed výbuchu [bx, bz] + posun na kružnici o poloměru 'r':
			// cos(angle) určuje poměr posunu v ose X
			// sin(angle) určuje poměr posunu v ose Z
			double wx = bx + Math.cos(angle) * r;
			double wz = bz + Math.sin(angle) * r;

			if (insideCrater) {
				// Uvnitř kráteru: žhavý oranžový kouř (R=1.0, G=0.70, B=0.15) těsně nad zemí (+0.35)
				NukeParticleHandler.spawnSmoke(level, wx, by + 0.35, wz, 1.0F, 0.70F, 0.15F, 1.2F, 2);
				// Každá 4. částice je plamínek (i % 4 == 0 znamená zbytek po dělení 4 je 0)
				if (i % 4 == 0) {
					NukeParticleHandler.sendVanilla(level, ParticleTypes.FLAME, wx, by + 0.25, wz, 1, 0, 0, 0, 0.0);
				}
			} else {
				// Vně kráteru: šedohnědý prach zvednutý ze země (R=0.65, G=0.60, B=0.50)
				NukeParticleHandler.spawnSmoke(level, wx, by + 0.35, wz, 0.65F, 0.60F, 0.50F, 1.2F, 2);
				// Každá 4. částice je kouřový obláček (POOF)
				if (i % 4 == 0) {
					NukeParticleHandler.sendVanilla(level, ParticleTypes.POOF, wx, by + 0.25, wz, 1, 0, 0, 0, 0.0);
				}
			}
		}

		// --- KINETICKÉ OMETENÍ ENTIT POSTUPUJÍCÍ VLNOU ---
		// Vytvoříme vyhledávací AABB krabici okolo celého aktuálního prstence
		AABB waveBox = new AABB(bx - r - 2.5, by - 3.0, bz - r - 2.5, bx + r + 2.5, by + 6.0, bz + r + 2.5);

		for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, waveBox)) {
			if (entity.isSpectator()) continue;

			// Vektor a 2D vzdálenost entity od středu výbuchu
			double dx = entity.getX() - bx;
			double dz = entity.getZ() - bz;
			double dist = Math.sqrt(dx * dx + dz * dz);

			// --- GENIÁLNÍ PODMÍNKA VLNY: Math.abs(dist - r) <= 2.0 ---
			// 'dist' je, jak daleko stojí entita. 'r' je, kde se právě nachází čelo vlny.
			// Rozdíl |dist - r| říká, jak blízko je entita od okraje vlny.
			// Pokud je |dist - r| <= 2.0, znamená to, že entita stojí v pásmu širokém 2 bloky
			// PŘÍMO NA POSTUPUJÍCÍ HRANĚ VLNY!
			// Výsledek: Vlna neodhodí všechny najednou, ale postupně smete všechno, co potká v cestě!
			if (Math.abs(dist - r) <= 2.0 && dist > 0.01) {
				// Síla odhození slábne s rostoucí vzdáleností od středu (z 1.0 na 0.35)
				double pushFactor = Math.max(0.35, 1.0 - (dist / MAX_GROUND_RADIUS)) * 1.8;

				// dx / dist a dz / dist je normalizovaný směr (délka 1).
				// Vynásobením pushFactor získáme sílu odmrštění.
				// Osa Y má hodnotu 0.40 pro nadhození do vzduchu.
				entity.setDeltaMovement(entity.getDeltaMovement().add(
						(dx / dist) * pushFactor,
						0.40,
						(dz / dist) * pushFactor
				));
				// Upozorníme Minecraft, že se změnila rychlost entity, aby poslal paket hráči
				entity.hurtMarked = true;
			}
		}
	}

	/**
	 * Vzdušná rázová vlna (Air shockwave).
	 * Simuluje rázovou vlnu ve vzduchu, která se šíří rychleji než pozemní prach.
	 */
	private static void tickAirWave(ServerLevel level, double bx, double airY, double bz, int age) {
		// Rychlost vzdušné vlny: začíná v ticku 6 na poloměru 4 bloky
		// a každým tickem roste o 2.0 bloky (šíří se rychlostí 40 m/s!)
		double airRadius = 4.0 + (age - 6) * 2.0;
		if (airRadius > MAX_AIR_RADIUS) return;

		// Počet bodů na vzdušném kruhu
		int points = Math.clamp((int)(airRadius * 2.4), 28, 160);
		double step = (Math.PI * 2.0) / points;

		for (int i = 0; i < points; i++) {
			double angle = i * step;
			// Souřadnice bodu na kružnici
			double rx = bx + Math.cos(angle) * airRadius;
			double rz = bz + Math.sin(angle) * airRadius;

			// Světle namodralý kouř (ionizovaný vzduch za rázovou vlnou: R=0.85, G=0.95, B=1.0)
			NukeParticleHandler.spawnSmoke(level, rx, airY, rz, 0.85F, 0.95F, 1.0F, 1.1F, 2);

			// Každá 5. částice je elektrická jiskra (statický náboj ve vzduchu)
			if (i % 5 == 0) {
				NukeParticleHandler.sendVanilla(level, ParticleTypes.ELECTRIC_SPARK, rx, airY, rz, 1, 0, 0, 0, 0.0);
			}
		}
	}
}
