package cz.maxtechnik.dif.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * ============================================================
 *  MultiblockHelper – NeoForge 21.1.x
 * ============================================================
 *  Slouží jako JEDINÝ soubor pro validaci 3x3x3 multibloku
 *  stylem Coke Oven (Immersive Engineering):
 *   - Postavíš plnou 3x3x3 kostku
 *   - Uprostřed jedné stěny je Controller
 *   - Při položení se automaticky zformuje
 *
 *  VIZUALIZACE (pohled shora, střední vrstva Y=1):
 *        [F][F][F]   ← zadní stěna  (z=2)
 *        [F][ ][F]   ← střed        (z=1)
 *        [F][K][F]   ← přední stěna (z=0)  K = Controller
 *
 *  VZOR – indexy [y][x][z]:
 *   y: 0=spodek, 1=střed, 2=vrch
 *   x: 0=vlevo,  1=střed, 2=vpravo  (relativně k facing)
 *   z: 0=přední stěna (controller zde), 1=vnitřek, 2=zadní stěna
 *  Pozice controlleru [1][1][0] se VŽDY přeskakuje automaticky.
 * ============================================================
 */
@SuppressWarnings({"unchecked", "unused"})
public final class MultiblockHelper {

	private MultiblockHelper() {}

	// =========================================================
	//  HLAVNÍ API – validace struktury
	// =========================================================

	/**
	 * Pokusí se zformovat strukturu.
	 * Volej z Block#onPlace / Block#setPlacedBy (pouze server strana).
	 *
	 * @param level      svět
	 * @param controller pozice controlleru
	 * @param facing     směr do nitra struktury (facing.getOpposite() od FACING property)
	 * @param pattern    vzor [y 0-2][x 0-2][z 0-2], null = libovolný blok
	 * @return true pokud struktura odpovídá vzoru
	 */
	public static boolean tryForm(Level level, BlockPos controller, Direction facing, Predicate<BlockState>[][][] pattern) {
		if (level.isClientSide) return false;
		return check(level, controller, facing, pattern);
	}

	/**
	 * Zkontroluje zda je struktura stále platná.
	 * Volej z tick BlockEntity nebo Block#neighborChanged.
	 */
	public static boolean isValid(Level level, BlockPos controller, Direction facing, Predicate<BlockState>[][][] pattern) {
		if (level.isClientSide) return false;
		return check(level, controller, facing, pattern);
	}

	// =========================================================
	//  POMOCNÉ PREDIKÁTY
	// =========================================================

	/** Blok musí být přesně tento Block. */
	public static Predicate<BlockState> of(Block block) {
		return state -> state.is(block);
	}

	/** Blok musí být jeden z těchto. */
	public static Predicate<BlockState> any(Block... blocks) {
		return state -> {
			for (Block b : blocks) if (state.is(b)) return true;
			return false;
		};
	}

	/** Pozice musí být vzduch. */
	public static final Predicate<BlockState> AIR = BlockState::isAir;

	/** Pozice může být cokoliv (nevaliduje se). */
	public static final Predicate<BlockState> ANY = state -> true;

	// =========================================================
	//  ITERACE CIHLIČEK
	// =========================================================

	/**
	 * Callback pro iteraci přes všechny "cihličkové" pozice struktury
	 * (vše kromě pozice samotného controlleru).
	 *
	 * @return {@code true} pokud má iterace pokračovat, {@code false} pro předčasné ukončení
	 */
	@FunctionalInterface
	public interface BrickVisitor {
		boolean visit(BlockPos.MutableBlockPos pos);
	}

	/**
	 * Iteruje přes všech 26 cihličkových pozic 3×3×3 struktury
	 * (pozice controlleru [1][1][0] je přeskočena).
	 * <p>
	 * Používají to controller block entity pro claiming/releasing —
	 * centralizováno sem, aby se logika neduplikovala.
	 *
	 * @param controllerPos  světová pozice controlleru
	 * @param intoStructure  směr do nitra struktury ({@code FACING.getOpposite()})
	 * @param visitor        callback, vrací false pro předčasné ukončení
	 */
	public static void forEachBrick(BlockPos controllerPos, Direction intoStructure, BrickVisitor visitor) {
		final Direction right = intoStructure.getClockWise();
		final BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();
		outer:
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				for (int z = 0; z < 3; z++) {
					if (y == 1 && x == 1 && z == 0) continue; // přeskoč controller
					mp.set(controllerPos)
							.move(intoStructure, z)
							.move(right, x - 1)
							.move(Direction.UP, y - 1);
					if (!visitor.visit(mp)) break outer;
				}
			}
		}
	}

	// =========================================================
	//  VZORY
	// =========================================================

	/**
	 * Připraví vzor pro plnou 3×3×3 kostku.
	 * Všechny pozice (stěny i vnitřek) musí splňovat predikáty.
	 *
	 * @param framePred  predikát pro stěny (shell)
	 * @param innerPred  predikát pro vnitřní blok (pozice [1][1][1])
	 */
	public static Predicate<BlockState>[][][] buildSolidShellPattern(
			Predicate<BlockState> framePred,
			Predicate<BlockState> innerPred
	) {
		Predicate<BlockState>[][][] pattern = new Predicate[3][3][3];
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				for (int z = 0; z < 3; z++) {
					boolean isShell = (y == 0 || y == 2) || (x == 0 || x == 2) || (z == 0 || z == 2);
					pattern[y][x][z] = isShell ? framePred : innerPred;
				}
			}
		}
		return pattern;
	}

	// =========================================================
	//  FLUID UTILITY
	// =========================================================

	/**
	 * Kombinovaný {@link IFluidHandler} se dvěma oddělenými tanky:
	 * <ul>
	 *   <li>{@code fill()}  → jde do {@code input}  (jen čerpat dovnitř)</li>
	 *   <li>{@code drain()} → jde z   {@code output} (jen čerpat ven)</li>
	 * </ul>
	 * Použití v capability registraci:
	 * <pre>
	 * MultiblockHelper.combinedInOut(be.fluidInputTank, be.fluidOutputTank)
	 * </pre>
	 */
	public static IFluidHandler combinedInOut(IFluidHandler input, IFluidHandler output) {
		return new IFluidHandler() {
			@Override public int getTanks() { return input.getTanks() + output.getTanks(); }

			@Override public @NotNull FluidStack getFluidInTank(int tank) {
				return tank < input.getTanks()
						? input.getFluidInTank(tank)
						: output.getFluidInTank(tank - input.getTanks());
			}

			@Override public int getTankCapacity(int tank) {
				return tank < input.getTanks()
						? input.getTankCapacity(tank)
						: output.getTankCapacity(tank - input.getTanks());
			}

			@Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
				return tank < input.getTanks()
						? input.isFluidValid(tank, stack)
						: output.isFluidValid(tank - input.getTanks(), stack);
			}

			/** Čerpání dovnitř → jde výhradně do vstupního tanku. */
			@Override public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
				return input.fill(resource, action);
			}

			/** Čerpání ven → jde výhradně z výstupního tanku. */
			@Override public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
				return output.drain(resource, action);
			}

			@Override public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
				return output.drain(maxDrain, action);
			}
		};
	}

	// =========================================================
	//  INTERNÍ LOGIKA
	// =========================================================

	private static boolean check(Level level, BlockPos controller, Direction facing, Predicate<BlockState>[][][] pattern) {
		Direction right = facing.getClockWise();
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				for (int z = 0; z < 3; z++) {
					if (y == 1 && x == 1 && z == 0) continue; // přeskoč controller
					Predicate<BlockState> pred = pattern[y][x][z];
					if (pred == null) continue;
					BlockPos pos = toWorld(controller, facing, right, x - 1, y - 1, z);
					if (!pred.test(level.getBlockState(pos))) return false;
				}
			}
		}
		return true;
	}

	/**
	 * Převod lokálních souřadnic na světové s ohledem na rotaci.
	 *
	 * @param dx  -1=vlevo, 0=střed, +1=vpravo
	 * @param dy  -1=dole,  0=střed, +1=nahoře
	 * @param dz   0=přední stěna, 1=vnitřek, 2=zadní stěna
	 */
	private static BlockPos toWorld(BlockPos origin, Direction facing, Direction right, int dx, int dy, int dz) {
		return origin.relative(facing, dz).relative(right, dx).above(dy);
	}
}