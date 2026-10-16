package cz.maxtechnik.dif.init.event.nuke;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.config.DifModServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
public class NukeCraterHandler {
	// Značky (tagy) bloků pro rozpoznání hornin a vodních rostlin
	private static final TagKey<Block> STONES = BlockTags.create(ResourceLocation.parse("c:stones"));
	private static final TagKey<Block> NUKE_WATER_PLANTS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "nuke_water_plants"));

	// Maximální počet bloků, které smíme zkontrolovat za 1 herní tick (např. 36 000).
	// Tím zabráníme záseku serveru (lag spike).
	private static final int BLOCKS_PER_TICK = DifModServerConfig.NUKE_BLOCKS_PER_TICK.get();
	// Maximální odolnost bloku proti výbuchu, kterou ještě dokážeme zničit (bedrock má 3 600 000, ten nepůjde)
	private static final double MAX_DESTROYABLE_RESISTANCE = DifModServerConfig.NUKE_MAX_DESTROYABLE_RESISTANCE.get();

	// --- POLOMĚRY KRÁTERU (Poloosy asymetrického elipsoidu) ---
	// Horizontální poloměry (do stran v rovině XZ):
	// FULL = zóna 100% zkázy (vše vypařeno)
	// TOTAL = maximální vnější okraj (přechodová zóna s nepravidelným okrajem)
	private static final double HOR_R_FULL = DifModServerConfig.NUKE_HOR_R_FULL.get(), HOR_R_TOTAL = DifModServerConfig.NUKE_HOR_R_TOTAL.get();
	// Vertikální poloměr směrem NAHORU do vzduchu:
	private static final double UP_R_FULL = DifModServerConfig.NUKE_UP_R_FULL.get(), UP_R_TOTAL = DifModServerConfig.NUKE_HOR_R_TOTAL.get();
	// Vertikální poloměr směrem DOLŮ do podloží (zem klade odpor, kráter je mělčí než vyšší):
	private static final double DOWN_R_FULL = DifModServerConfig.NUKE_DOWN_R_FULL.get(), DOWN_R_TOTAL = DifModServerConfig.NUKE_DOWN_R_TOTAL.get();

	// --- VÝUKA OPTIMALIZACE: PŘEDPOČÍTANÉ DRUHÉ MOCNINY (SQUARES) ---
	// Proč si předem počítáme R * R?
	// V počítačové grafice a hrách je odmocnina Math.sqrt() poměrně pomalá matematická operace.
	// Když máme rovnici x² + y² <= R², je mnohem rychlejší nechat obě strany na druhou mocninu,
	// než počítat √(x² + y²) <= R. Tím ušetříme miliony zbytečných volání odmocniny za sekundu!
	private static final double HOR_FULL_SQ = HOR_R_FULL * HOR_R_FULL, HOR_TOTAL_SQ = HOR_R_TOTAL * HOR_R_TOTAL;
	private static final double UP_FULL_SQ = UP_R_FULL * UP_R_FULL, UP_TOTAL_SQ = UP_R_TOTAL * UP_R_TOTAL;
	private static final double DN_FULL_SQ = DOWN_R_FULL * DOWN_R_FULL, DN_TOTAL_SQ = DOWN_R_TOTAL * DOWN_R_TOTAL;

	// Poloměr tepelné vlny (ožehnutí povrchu za hranicí samotné díry kráteru)
	private static final double SCORCH_RADIUS = 116D, SCORCH_RADIUS_SQ = SCORCH_RADIUS * SCORCH_RADIUS;

	// Předpřipravené stavy bloků vzduchu a vody, abychom nevytvářeli nové instance v paměti dokola
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final BlockState WATER = Blocks.WATER.defaultBlockState();

	// Příznaky aktualizace bloků (Update Flags v Minecraftu):
	// 2 = poslat změnu klientům (SEND_TO_CLIENTS)
	// 16 = nezpůsobovat pád sousedních bloků (PREVENT_NEIGHBOR_DROPS)
	// 64 = potlačit okamžité sousední reakce (NO_NEIGHBOR_REACTIONS)
	// Dohromady 2 | 16 | 64 zaručuje extrémně rychlou změnu bloků bez lagování fyziky štěrku a vody!
	private static final int UPDATE_FLAGS = 2 | 16 | 64;

	// --- STAVOVÉ PROMĚNNÉ PROCHÁZENÍ SLUPEK KRYCHLE ---
	// currentShell: poloměr aktuální slupky (0 = střed, 1, 2, 3...)
	// maxShell: největší poloměr, který vůbec budeme zkoumat (strop pro ukončení)
	// shellFace: aktuální stěna krychle (0 až 5)
	// shellU, shellV: 2D souřadnice pixelu na dané stěně krychle
	private int currentShell = 0, maxShell = (int) Math.ceil(SCORCH_RADIUS), shellFace = 0, shellU = 0, shellV = 0;
	private boolean debrisSpawned = false;

	// MutableBlockPos je jeden znovupoužitelný objekt pozice.
	// Kdybychom v cyklu dělali pokaždé 'new BlockPos(...)', vytvořili bychom miliony objektů
	// a zahltili bychom Garbage Collector (způsobilo by to záseky hry).
	private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

	/**
	 * Hlavní metoda prováděná každý tick.
	 * Vrací true, pokud je kráter kompletně dokončen, nebo false, pokud má pokračovat v dalším ticku.
	 */
	public boolean tick(Level level, BlockPos center, double groundWaveRadius, RandomSource random) {
		if (level.isClientSide) return true;

		// V úplně prvním ticku vystřelíme do vzduchu suť z kráteru
		if (!debrisSpawned) {
			debrisSpawned = true;
			spawnDebris(level, center, random);
		}

		// SYNCHRONIZACE: Kráter se nesmí prokopat dál, než kam doopravdy dorazila rázová vlna po zemi!
		int targetShell = (int) Math.floor(groundWaveRadius);
		int cx = center.getX(), cy = center.getY(), cz = center.getZ(), processed = 0;

		// Cyklus s rozpočtem: zpracujeme maximálně BLOCKS_PER_TICK bloků za tento tick
		while (processed < BLOCKS_PER_TICK) {
			// Pokud jsme dosáhli maximálního poloměru, kráter je hotov!
			if (currentShell > maxShell) {
				return true;
			}
			// Pokud jsme předběhli rázovou vlnu, počkáme do dalšího ticku
			if (currentShell > targetShell) {
				return false;
			}

			int r = currentShell;

			// Pro r = 0 existuje pouze jediný středový bod [0, 0, 0]
			if (r == 0) {
				destroyAt(level, cx, cy, cz);
				currentShell = 1;
				shellFace = shellU = shellV = 0;
				processed++;
				continue;
			}

			// --- VÝUKA ALGORITMU: PROCHÁZENÍ 6 STĚN KRYCHLE (CUBE SHELL) ---
			// Představ si dutou krychli o velikosti (2r + 1). Krychle má 6 stěn:
			// shellFace 0: stěna +X  (dx = +r, dy a dz se mění v rozsahu -r až +r)
			// shellFace 1: stěna -X  (dx = -r, dy a dz se mění v rozsahu -r až +r)
			// shellFace 2: stěna +Y  (dy = +r, dx a dz se mění)
			// shellFace 3: stěna -Y  (dy = -r, dx a dz se mění)
			// shellFace 4: stěna +Z  (dz = +r, dx a dy se mění)
			// shellFace 5: stěna -Z  (dz = -r, dx a dy se mění)
			// Všimni si, že u dalších stěn používáme (r - 1) místo r.
			// Proč? Abychom rohy a hrany krychle nekontrolovali dvakrát!
			int dx, dy, dz, uSize, vSize;
			switch (shellFace) {
				case 0 -> { // Stěna +X
					dy = -r + shellU;
					dz = -r + shellV;
					dx = r;
					uSize = 2 * r + 1;
					vSize = 2 * r + 1;
				}
				case 1 -> { // Stěna -X
					dy = -r + shellU;
					dz = -r + shellV;
					dx = -r;
					uSize = 2 * r + 1;
					vSize = 2 * r + 1;
				}
				case 2 -> { // Stěna +Y (nahoře)
					dx = -(r - 1) + shellU;
					dz = -r + shellV;
					dy = r;
					uSize = 2 * (r - 1) + 1;
					vSize = 2 * r + 1;
				}
				case 3 -> { // Stěna -Y (dole)
					dx = -(r - 1) + shellU;
					dz = -r + shellV;
					dy = -r;
					uSize = 2 * (r - 1) + 1;
					vSize = 2 * r + 1;
				}
				case 4 -> { // Stěna +Z
					dx = -(r - 1) + shellU;
					dy = -(r - 1) + shellV;
					dz = r;
					uSize = 2 * (r - 1) + 1;
					vSize = 2 * (r - 1) + 1;
				}
				case 5 -> { // Stěna -Z
					dx = -(r - 1) + shellU;
					dy = -(r - 1) + shellV;
					dz = -r;
					uSize = 2 * (r - 1) + 1;
					vSize = 2 * (r - 1) + 1;
				}
				default -> {
					dx = dy = dz = 0;
					uSize = vSize = 0;
				}
			}

			// Posun 2D souřadnic (U, V) na ploše stěny:
			shellV++;
			if (shellV >= vSize) {
				shellV = 0;
				shellU++;
				if (shellU >= uSize) {
					shellU = 0;
					shellFace++; // Přejdeme na další stěnu krychle
					if (shellFace > 5) {
						shellFace = 0;
						currentShell++; // Prošli jsme všech 6 stěn, zvětšíme slupku o 1!
					}
				}
			}

			// --- MATEMATIKA KRÁTERU: TEST ELIPSOIDU ---
			// 1. Horizontální vzdálenost na druhou: dx² + dz²
			double dxSq = (double) dx * dx, dzSq = (double) dz * dz;
			double horizDistSq = dxSq + dzSq;

			// Pokud je bod dál než tepelný poloměr, ignorujeme ho
			if (horizDistSq > SCORCH_RADIUS_SQ) continue;

			// 2. Asymetrie: Nahoru vs. Dolů
			double verFullSq, verTotalSq, dyEff;
			if (dy >= 0) {
				// Nad středem výbuchu: používáme horní poloosy elipsoidu
				verFullSq = UP_FULL_SQ;
				verTotalSq = UP_TOTAL_SQ;
				dyEff = dy;
			} else {
				// Pod středem výbuchu:
				// horFrac je poměr od středu k okraji (0.0 až 1.0).
				// Přičtení horFrac * 2 zvedá efektivní dno směrem k okrajům.
				// Tím vznikne přirozená mělká miska (saucer profile) místo ostrého hrotu!
				double horFrac = Math.sqrt(horizDistSq) / HOR_R_TOTAL;
				dyEff = dy + horFrac * 2;
				verFullSq = DN_FULL_SQ;
				verTotalSq = DN_TOTAL_SQ;
			}
			double dyEffSq = dyEff * dyEff;

			// --- ROVNICE ELIPSOIDU: dx²/a² + dy²/b² + dz²/c² <= 1 ---
			// nTotal testuje vnější okraj kráteru:
			double nTotal = dxSq / HOR_TOTAL_SQ + dyEffSq / verTotalSq + dzSq / HOR_TOTAL_SQ;

			if (nTotal <= 1) {
				// Bod je uvnitř celkového dosahu výbuchu!
				// nFull testuje vnitřní zónu absolutní zkázy:
				double nFull = dxSq / HOR_FULL_SQ + dyEffSq / verFullSq + dzSq / HOR_FULL_SQ;
				boolean destroy;

				if (nFull <= 1) {
					// 100% zóna: Zde se blok bez milosti vypaří na vzduch
					destroy = true;
				} else {
					// --- PŘECHODOVÁ ZÓNA NA OKRAJI KRÁTERU ---
					// t jde od 0.0 (vnitřní okraj) do 1.0 (vnější okraj).
					// Šance na zničení klesá od cca 100% k minimu 15%.
					// Tím získáme nádherný, realisticky rozeklaný okraj kráteru!
					double t = (nFull - 1) / ((HOR_TOTAL_SQ / HOR_FULL_SQ) - 1.0);
					double chance = Math.max(0.15, 1 - t * 0.8);
					destroy = random.nextDouble() < chance;
				}

				if (destroy) {
					// Zničíme blok
					destroyAt(level, cx + dx, cy + dy, cz + dz);
					// Pokud jsme hluboko v kráteru (více než 8 bloků pod epicentrem),
					// roztavíme podloží pod tímto blokem tepelnou energií
					if (dy < -8) {
						mutateFloorAt(level, cx + dx, cy + dy - 1, cz + dz, random);
					}
				} else {
					// Blok přežil zničení, ale dostal žár -> ožehneme ho
					thermalScorchAt(level, cx + dx, cy + dy, cz + dz, random);
				}
			} else if (dy >= -16 && dy <= 28) {
				// Mimo kráter v rozumné výšce: tepelná vlna ožehne povrch
				thermalScorchAt(level, cx + dx, cy + dy, cz + dz, random);
			}

			processed++;
		}
		return false;
	}

	/**
	 * Vystřelí do vzduchu padající bloky sutin (FallingBlockEntity).
	 * Vytváří efekt meteorického deště vymrštěných kamenů.
	 */
	private void spawnDebris(Level level, BlockPos center, RandomSource random) {
		if (!(level instanceof ServerLevel sl)) return;

		for (int i = 0; i < 45; i++) {
			// Náhodný výběr materiálu sutin podle pravděpodobnosti
			BlockState debrisState = (random.nextFloat() < 0.40F) ? Blocks.COBBLED_DEEPSLATE.defaultBlockState()
					: (random.nextFloat() < 0.40F) ? Blocks.BLACKSTONE.defaultBlockState()
					: (random.nextFloat() < 0.50F) ? Blocks.COBBLESTONE.defaultBlockState()
					: Blocks.DIRT.defaultBlockState();

			// Vytvoříme entitu padajícího bloku 3 bloky nad středem
			FallingBlockEntity falling = FallingBlockEntity.fall(sl, center.above(3), debrisState);
			falling.time = 1;
			falling.dropItem = false; // Po dopadu se nerozpadne na item, ale umístí se jako blok

			// --- VÝUKA FYZIKY VRHU: NÁHODNÝ VEKTOR RYCHLOSTI ---
			// Úhel v rovině XZ: náhodné číslo od 0 do 2π (všemi směry dokola)
			double angle = random.nextDouble() * Math.PI * 2.0;
			// Horizontální rychlost do stran
			double speed = 0.5 + random.nextDouble() * 0.9;
			// Vertikální rychlost nahoru do vzduchu
			double vy = 0.65 + random.nextDouble() * 0.80;

			// Rozklad na složky X, Y, Z:
			// vx = cos(angle) * speed
			// vz = sin(angle) * speed
			falling.setDeltaMovement(Math.cos(angle) * speed, vy, Math.sin(angle) * speed);
		}
	}

	/**
	 * Zjistí, zda je blok natolik tvrdý, že odolá atomovému výbuchu (např. Bedrock).
	 */
	@SuppressWarnings("deprecation")
	private static boolean isBlastResistant(BlockState state) {
		return state.getBlock().getExplosionResistance() > (float) MAX_DESTROYABLE_RESISTANCE;
	}

	/**
	 * Smaže blok na dané souřadnici a nahradí ho vzduchem (AIR).
	 */
	private void destroyAt(Level level, int x, int y, int z) {
		// Bezpečnostní kontrola výškových limitů světa (od -64 do 320)
		if (y < level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) return;
		mutablePos.set(x, y, z);
		BlockState state = level.getBlockState(mutablePos);
		// Vzduch a nezničitelné bloky ignorujeme
		if (state.isAir() || isBlastResistant(state)) return;
		// Nastavíme blok na AIR s optimalizovanými flagy
		level.setBlock(mutablePos, AIR, UPDATE_FLAGS);
	}

	/**
	 * Roztaví dno kráteru: přemění původní kámen na sopečné a hlubinné horniny.
	 */
	private void mutateFloorAt(Level level, int x, int y, int z, RandomSource random) {
		if (y < level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) return;
		mutablePos.set(x, y, z);
		BlockState state = level.getBlockState(mutablePos);
		if (state.isAir() || isBlastResistant(state)) return;

		// Přeměňujeme pouze pevné, neprůhledné bloky
		if (state.isSolidRender(level, mutablePos)) {
			float roll = random.nextFloat();
			// Náhodný výběr roztaveného materiálu
			BlockState melted = roll < 0.38F ? Blocks.COBBLED_DEEPSLATE.defaultBlockState()
					: roll < 0.68F ? Blocks.DEEPSLATE.defaultBlockState()
					: roll < 0.84F ? Blocks.BLACKSTONE.defaultBlockState()
					: roll < 0.94F ? Blocks.POLISHED_BLACKSTONE.defaultBlockState()
					: Blocks.MAGMA_BLOCK.defaultBlockState();
			level.setBlock(mutablePos, melted, UPDATE_FLAGS);
		}
	}

	/**
	 * Simuluje tepelnou vlnu (Thermal Scorch):
	 * Spaluje stromy na čedič (uhel), odpařuje vodu a sníh, zapaluje oheň.
	 */
	private void thermalScorchAt(Level level, int x, int y, int z, RandomSource random) {
		if (y < level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) return;
		mutablePos.set(x, y, z);
		BlockState state = level.getBlockState(mutablePos);
		if (state.isAir()) return;
		if (state.is(Blocks.WATER)) return;

		// Vodní rostliny nahradíme čistou vodou
		if (state.is(NUKE_WATER_PLANTS)) {
			level.setBlock(mutablePos, WATER, UPDATE_FLAGS);
			return;
		}

		// Křoví, tráva, led a sníh se okamžitým žárem vypaří
		if (state.is(BlockTags.SWORD_EFFICIENT) || state.is(BlockTags.REPLACEABLE_BY_TREES)
				|| state.is(BlockTags.ICE) || state.is(BlockTags.SNOW)) {
			level.setBlock(mutablePos, AIR, UPDATE_FLAGS);
			return;
		}

		// Kmeny stromů se zuhelnatí na Basalt / Polished Basalt
		if (state.is(BlockTags.LOGS)) {
			BlockState charred = (random.nextFloat() < 0.70F) ? Blocks.POLISHED_BASALT.defaultBlockState() : Blocks.BASALT.defaultBlockState();
			level.setBlock(mutablePos, charred, UPDATE_FLAGS);
			return;
		}

		// Hlína a jíl se spálí na Coarse Dirt a občas na povrchu vzplane oheň
		if (state.is(BlockTags.DIRT) || state.is(Blocks.CLAY)) {
			float r = random.nextFloat();
			BlockState dirt = (r < 0.60F) ? Blocks.COARSE_DIRT.defaultBlockState()
					: (r < 0.90F) ? Blocks.DIRT.defaultBlockState()
					: Blocks.ROOTED_DIRT.defaultBlockState();
			level.setBlock(mutablePos, dirt, UPDATE_FLAGS);

			// Kontrola, zda je nad blokem vzduch pro zapálení ohně
			mutablePos.set(x, y + 1, z);
			if (level.getBlockState(mutablePos).isAir() && random.nextFloat() < 0.16F) {
				level.setBlock(mutablePos, Blocks.FIRE.defaultBlockState(), UPDATE_FLAGS);
			}
			return;
		}

		// Hladké kameny popraskají žárem na Cobblestone
		if (state.is(STONES)) {
			if (random.nextFloat() < 0.10F) {
				level.setBlock(mutablePos, Blocks.COBBLESTONE.defaultBlockState(), UPDATE_FLAGS);
			}
		}

		// Náhodné zapálení ohně na pevných površích
		if (random.nextFloat() < 0.08F && state.isSolidRender(level, mutablePos)) {
			mutablePos.set(x, y + 1, z);
			if (level.getBlockState(mutablePos).isAir()) {
				level.setBlock(mutablePos, Blocks.FIRE.defaultBlockState(), UPDATE_FLAGS);
			}
		}
	}

	// --- PERSISTENCE (UKLÁDÁNÍ DO NBT) ---
	// Pokud hráč během výbuchu odejde ze světa, tyto metody uloží
	// rozečtený stav slupky na disk a při načtení ho obnoví, takže výbuch plynule pokračuje!
	public void writeNbt(CompoundTag tag) {
		tag.putInt("CurrentShell", currentShell);
		tag.putInt("MaxShell", maxShell);
		tag.putInt("ShellFace", shellFace);
		tag.putInt("ShellU", shellU);
		tag.putInt("ShellV", shellV);
		tag.putBoolean("DebrisSpawned", debrisSpawned);
	}

	public void readNbt(CompoundTag tag) {
		currentShell = tag.getInt("CurrentShell");
		maxShell = tag.getInt("MaxShell");
		shellFace = tag.getInt("ShellFace");
		shellU = tag.getInt("ShellU");
		shellV = tag.getInt("ShellV");
		debrisSpawned = tag.getBoolean("DebrisSpawned");
	}
}
