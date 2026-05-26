package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Validátor 3×3×3 struktury Coke Ovenu.
 *
 * PRAVIDLA:
 *   - Vnějších 26 bloků = CokeBrick nebo CokeOvenController
 *   - Vnitřní 1 blok (střed) = prázdný (air nebo replaceable)
 *   - Controller musí být na čelní středové pozici (Y=střed, Z=čelní)
 *
 * SOUŘADNICE (controller = střed čelní stěny):
 *   Controller je na (cx, cy, cz).
 *   Struktura sahá od (cx-1, cy-1, cz) do (cx+1, cy+1, cz+2).
 *   Vnitřek = (cx, cy, cz+1) — jeden blok za controllerem.
 */
public class CokeOvenMultiblockValidator {

    /**
     * Ověří celou 3×3×3 strukturu.
     * @param level  svět
     * @param ctrlPos pozice controller bloku
     * @return true pokud je struktura validní
     */
    public static boolean validate(Level level, BlockPos ctrlPos) {
        // Projdi všech 27 pozic 3×3×3 kostky (střed = ctrlPos + (0,0,1))
        BlockPos center = ctrlPos.north(); // střed pece je blok za controllerem
        // Pozn: "north" = záporné Z = dovnitř pece (controller koukání je na jih)
        // Uprav orientaci podle toho jak bude controller otočen (FACING property)

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos check = center.offset(dx, dy, dz);
                    boolean isCenter = (dx == 0 && dy == 0 && dz == 0);
                    boolean isController = check.equals(ctrlPos);

                    if (isCenter) {
                        // Vnitřek musí být prázdný
                        if (!level.getBlockState(check).isAir()) return false;
                    } else if (isController) {
                        // Controller pozice OK — přeskočit
                        continue;
                    } else {
                        // Všechny ostatní pozice = CokeBrick nebo CokeOvenController
                        if (!isValidWallBlock(level.getBlockState(check))) return false;
                    }
                }
            }
        }
        return true;
    }

    /** Vrátí pozice všech 26 stěnových bloků (bez středu). */
    public static Iterable<BlockPos> getWallPositions(BlockPos ctrlPos) {
        BlockPos center = ctrlPos.north();
        java.util.List<BlockPos> positions = new java.util.ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue; // přeskoč střed
                    positions.add(center.offset(dx, dy, dz));
                }
            }
        }
        return positions;
    }

    private static boolean isValidWallBlock(BlockState state) {
        return state.getBlock() instanceof CokeBrickBlock
                || state.getBlock() instanceof CokeOvenControllerBlock;
    }
}