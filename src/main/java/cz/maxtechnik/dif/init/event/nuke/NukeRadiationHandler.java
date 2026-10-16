package cz.maxtechnik.dif.init.event.nuke;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
public class NukeRadiationHandler {
	// Maximální dosah radiace a tlakové vlny v blocích (180 bloků od středu)
	private static final double RADIATION_RADIUS = 180.0;

	/**
	 * Tuto metodu volá entita výbuchu pouze jednou: v ticku 0 (při detonaci).
	 * Má za úkol vytvořit počáteční otřes, udělit negativní efekty ozáření
	 * a odmrštit všechny živé tvory v okolí.
	 *
	 * @param level  svět, ve kterém výbuch nastal
	 * @param center souřadnice bloku, kde bomba bouchla
	 */
	public static void apply(Level level, BlockPos center) {
		// Všechny herní výpočty (poškození, efekty) musíme provádět VÝHRADNĚ na serveru.
		// Klient slouží jen k vykreslování obrazu. Kdybychom to spustili na klientovi,
		// došlo by k desynchronizaci s ostatními hráči.
		if (level.isClientSide) return;

		// Převod celočíselných souřadnic bloku (BlockPos) na přesný střed výbuchu (double).
		// +0.5 posune bod do přesného středu bloku v rovině X a Z.
		// +1.5 posune střed mírně nad zem, odkud se šíří tlaková vlna.
		double ex = center.getX() + 0.5;
		double ey = center.getY() + 1.5;
		double ez = center.getZ() + 0.5;

		// 1. KROK: Vanilla exploze pro zvuk, částice a poškození.
		// Síla 48.0F je zhruba 12x silnější než běžné TNT (které má 4.0F).
		// Používáme ExplosionInteraction.NONE, což znamená:
		// "Udělej zvuk a poškoď okolí, ale NENIČ žádné bloky!".
		// Ničení bloků má totiž na starosti náš vlastní NukeCraterHandler.
		level.explode(null, ex, ey, ez, 48.0F, Level.ExplosionInteraction.NONE);

		// 2. KROK: Vyhledání živých entit v okolí.
		// AABB (Axis-Aligned Bounding Box) je neviditelná krychle pro rychlé filtrování.
		// Minecraft si vnitřně drží entity v prostorových strukturách a pomocí této krabice
		// bleskově omezí hledání pouze na tvory v dosahu +-180 bloků v osách X, Y a Z.
		AABB area = new AABB(
				ex - RADIATION_RADIUS, ey - RADIATION_RADIUS, ez - RADIATION_RADIUS,
				ex + RADIATION_RADIUS, ey + RADIATION_RADIUS, ez + RADIATION_RADIUS
		);

		// level.getEntitiesOfClass nám vrátí seznam pouze těch entit, které dědí z LivingEntity
		// (což jsou hráči, zvířata, mobové – vyloučí to padající písek, ležící itemy, šípy apod.).
		for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
			// Spektátor (divák) nesmí být ovlivněn ničím ve světě.
			if (entity.isSpectator()) continue;
			// Hráč v Creativu je nesmrtelný a nemá dostávat efekty ani odhození.
			if (entity instanceof Player player && player.isCreative()) continue;

			// 3. KROK: Výpočet skutečné 3D vzdálenosti pomocí Pythagorovy věty.
			// distanceToSqr spočítá: (Δx)² + (Δy)² + (Δz)².
			// Odmocninou Math.sqrt získáme reálnou eukleidovskou vzdálenost v blocích (metrech).
			double dist = Math.sqrt(entity.distanceToSqr(ex, ey, ez));

			// AABB byla krychle, ale výbuch se šíří jako koule!
			// Proto musíme otestovat, zda je vzdálenost menší nebo rovna poloměru koule (180 bloků).
			if (dist <= RADIATION_RADIUS) {

				// 4. KROK: Dávkování radiace podle vzdálenosti (Inverse Square Law princip v praxi).
				if (dist <= 65.0) {
					// --- ZÓNA SMRTI (do 65 bloků od epicentra) ---
					// Wither II: masivní úbytek životů. Doba: 1200 ticků = 60 sekund (20 ticků = 1 s).
					// Amplifier 1 znamená úroveň efektu II (0 je úroveň I).
					entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 1200, 1));
					// Zpomalení III: radiace a šok paralyzují svaly na 20 sekund (400 ticků).
					entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 2));
					// Temnota (Darkness): simulace oslepnutí ze záblesku na 10 sekund (200 ticků).
					entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0));
				} else {
					// --- OKRAJOVÁ ZÓNA (od 65 do 180 bloků) ---
					// Mírnější ozáření: Wither I na 40 sekund (800 ticků).
					entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 800, 0));
					// Zmatení / nevolnost (Nausea) ze šoku na 10 sekund (200 ticků).
					entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
				}

				// 5. KROK: Fyzika kinetického rázu (odhození tlakovou vlnou).
				// Chceme entitu odmrštit SMĚREM OD EPICENTRA pryč po zemi (rovina XZ).
				// Vektor od středu výbuchu k entitě je: [dx, dz]
				double dx = entity.getX() - ex;
				double dz = entity.getZ() - ez;
				// Horizontální vzdálenost v rovině XZ: √(dx² + dz²)
				double horizDist = Math.sqrt(dx * dx + dz * dz);

				// OCHRANA PŘED DĚLENÍM NULOU:
				// Kdyby entita stála přesně ve středu (horizDist = 0), dělení dx / horizDist
				// by dalo NaN (Not-a-Number), což by rozbilo souřadnice a smazalo entitu ze světa.
				if (horizDist > 0.1) {
					// VÝPOČET SÍLY ODFOUKNUTÍ:
					// dist / RADIATION_RADIUS je poměr od 0.0 (ve středu) do 1.0 (na okraji).
					// 1.0 - (dist / ...) znamená, že čím blíže středu cíl je, tím silnější ránu dostane.
					// Math.max(0.4, ...) zajistí, že i na samém okraji zbude minimální impuls 0.4.
					// Výsledek násobíme celkovou silou 2.2.
					double impulse = Math.max(0.4, 1.0 - (dist / RADIATION_RADIUS)) * 2.2;

					// NORMALIZACE VEKTORU SMĚRU:
					// dx / horizDist a dz / horizDist převedou vektor na jednotkovou délku 1 (udává pouze směr).
					// Vynásobením číslem 'impulse' dostaneme přesnou rychlost v daném směru.
					// Osa Y má hodnotu 0.45, což entitu zároveň nadhodí do vzduchu.
					Vec3 push = new Vec3((dx / horizDist) * impulse, 0.45, (dz / horizDist) * impulse);

					// Přičteme impuls k aktuálnímu pohybu entity
					entity.setDeltaMovement(entity.getDeltaMovement().add(push));

					// EXTRÉMNĚ DŮLEŽITÉ PRO MINECRAFT SÍŤOVOU KOMUNIKACI:
					// Server sice změnil rychlost entity, ale u hráčů má klient vlastní fyziku.
					// Nastavením hurtMarked = true Minecraft okamžitě odešle paket ClientboundSetEntityMotionPacket,
					// takže hráč na svém monitoru skutečně pocítí prudký let vzad.
					entity.hurtMarked = true;
				}
			}
		}
	}
}
