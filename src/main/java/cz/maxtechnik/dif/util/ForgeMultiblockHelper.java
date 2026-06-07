package cz.maxtechnik.dif.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

/**
 * Helper pro Forge Furnace multiblok s proměnnou výškou sklo vrstev.
 *
 * Struktura (zdola nahoru):
 *   Y+0  → Blaze Burner vrstva   (3×3, pevná)
 *   Y+1  → Controller vrstva     (Controller + 8 Forge Bricks, pevná)
 *   Y+2+ → Sklo vrstvy           (3×3 ForgeGlass, 1 až MAX_GLASS_LAYERS)
 *
 * Na rozdíl od MultiblockHelper tato třída nepoužívá pattern pole —
 * každá vrstva se ověřuje separátně, protože sklo vrstev může být 1–16.
 */
public final class ForgeMultiblockHelper {

    /** Maximální počet sklo vrstev — lze přepsat hodnotou z configu. */
    public static int MAX_GLASS_LAYERS = 16;

    /** mB kapacity na jednu sklo vrstvu (9 bloků × tato hodnota). */
    public static int MB_PER_GLASS_LAYER = 18000;

    /** Počet item slotů na jednu sklo vrstvu. */
    public static int SLOTS_PER_GLASS_LAYER = 9;

    private ForgeMultiblockHelper() {}

    // =========================================================
    //  VALIDACE SPODNÍCH DVOU VRSTEV
    // =========================================================

    /**
     * Ověří Blaze Burner vrstvu (Y+0 od controlleru = Y-1 ve světě).
     * Všech 9 pozic musí splňovat {@code burnerPred}.
     *
     * @param level      svět
     * @param controller pozice controller bloku (Y+1 vrstva)
     * @param burnerPred predikát pro Blaze Burner blok
     */
    public static boolean validateBurnerLayer(Level level, BlockPos controller,
                                              Predicate<BlockState> burnerPred) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos check = controller.offset(dx, -1, dz);
                if (!burnerPred.test(level.getBlockState(check))) return false;
            }
        }
        return true;
    }

    /**
     * Ověří Controller vrstvu (Y+1).
     * 8 okolních pozic (bez středu = controller) musí splňovat {@code brickPred}.
     *
     * @param level    svět
     * @param ctrlPos  pozice controlleru
     * @param brickPred predikát pro Forge Brick blok
     */
    public static boolean validateBrickLayer(Level level, BlockPos ctrlPos,
                                             Predicate<BlockState> brickPred) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue; // controller samotný se přeskakuje
                BlockPos check = ctrlPos.offset(dx, 0, dz);
                if (!brickPred.test(level.getBlockState(check))) return false;
            }
        }
        return true;
    }

    // =========================================================
    //  POČÍTÁNÍ SKLO VRSTEV
    // =========================================================

    /**
     * Spočítá počet plných, souvislých sklo vrstev nad controllerem.
     * Prochází od Y+2 nahoru dokud nenarazí na neúplnou vrstvu, vzduch
     * nebo dosáhne {@link #MAX_GLASS_LAYERS}.
     *
     * Vrátí 0 pokud první vrstva (Y+2) není kompletní.
     *
     * @param level     svět
     * @param ctrlPos   pozice controlleru
     * @param glassPred predikát pro ForgeGlass blok
     * @return počet kompletních vrstev (0 až MAX_GLASS_LAYERS)
     */
    public static int countGlassLayers(Level level, BlockPos ctrlPos,
                                       Predicate<BlockState> glassPred) {
        int count = 0;
        for (int layer = 1; layer <= MAX_GLASS_LAYERS; layer++) {
            if (isGlassLayerComplete(level, ctrlPos, layer, glassPred)) {
                count++;
            } else {
                break; // Přeruš při první neúplné vrstvě
            }
        }
        return count;
    }

    /**
     * Zkontroluje jednu konkrétní sklo vrstvu.
     *
     * @param layer  1 = první sklo vrstva (Y+2 od controlleru), 2 = Y+3 atd.
     */
    public static boolean isGlassLayerComplete(Level level, BlockPos ctrlPos,
                                               int layer, Predicate<BlockState> glassPred) {
        int worldY = ctrlPos.getY() + layer; // Sklo začíná na Y+1 nad controllerem
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos check = new BlockPos(ctrlPos.getX() + dx, worldY, ctrlPos.getZ() + dz);
                if (!glassPred.test(level.getBlockState(check))) return false;
            }
        }
        return true;
    }

    // =========================================================
    //  KAPACITA A SLOTY
    // =========================================================

    /** Celková fluid kapacita v mB pro daný počet sklo vrstev. */
    public static int totalFluidCapacity(int glassLayers) {
        return glassLayers * MB_PER_GLASS_LAYER;
    }

    /** Celkový počet item slotů pro daný počet sklo vrstev. */
    public static int totalItemSlots(int glassLayers) {
        return glassLayers * SLOTS_PER_GLASS_LAYER;
    }

    // =========================================================
    //  HEAT VÝPOČET
    // =========================================================

    /**
     * Spočítá celkové heat body ze všech 9 Blaze Burnerů pod controllerem.
     *
     * Kindled  = 1 bod
     * Seething = 2 body
     * Maximum  = 18 bodů (9× Seething)
     *
     * Využívá Create BlazeBurnerBlock pro čtení heat levelu stejným
     * způsobem jako DistillationTankBlockEntity.refreshCache().
     *
     * @param level    svět
     * @param ctrlPos  pozice controlleru
     * @return celkové heat body 0–18
     */
    public static int calculateHeatPoints(Level level, BlockPos ctrlPos) {
        int points = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos burnerPos = ctrlPos.offset(dx, -1, dz);
                BlockState burnerState = level.getBlockState(burnerPos);
                com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel heat =
                        com.simibubi.create.content.processing.burner.BlazeBurnerBlock.getHeatLevelOf(burnerState);
                if (heat == com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.KINDLED)   points += 1;
                if (heat == com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.SEETHING)  points += 2;
            }
        }
        return points;
    }

    /**
     * Převede heat body na rychlostní multiplikátor tavení.
     *
     * 1–3  → 0.5×  (měď, zlato, cín)
     * 4–9  → 1.0×  (železo, bronz, stříbro)
     * 10–15 → 1.5× (obsidian, tvoje tier 2 kovy)
     * 16–18 → 2.0× (endgame kovy)
     */
    public static float heatPointsToSpeed(int points) {
        if (points <= 0)  return 0f;
        if (points <= 3)  return 0.5f;
        if (points <= 9)  return 1.0f;
        if (points <= 15) return 1.5f;
        return 2.0f;
    }

    /**
     * Vrátí minimální heat body potřebné pro daný materiál tier.
     * Využiješ v receptech abys omezil co lze tavit.
     *
     * Tier 0 = základní kovy (měď, zlato)
     * Tier 1 = střední kovy  (železo, bronz)
     * Tier 2 = pokročilé kovy
     * Tier 3 = endgame kovy
     */
    public static int minHeatForTier(int tier) {
        return switch (tier) {
            case 0 -> 1;
            case 1 -> 4;
            case 2 -> 10;
            case 3 -> 16;
            default -> 1;
        };
    }

    // =========================================================
    //  ITERACE BLOKŮ
    // =========================================================

    /**
     * Iteruje přes 8 brick pozic v controller vrstvě (bez controlleru samotného).
     */
    public static void forEachBrick(BlockPos ctrlPos, BrickVisitor visitor) {
        BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                mp.set(ctrlPos.getX() + dx, ctrlPos.getY(), ctrlPos.getZ() + dz);
                if (!visitor.visit(mp)) return;
            }
        }
    }

    /**
     * Iteruje přes všechny bloky skla v daném počtu vrstev.
     */
    public static void forEachGlassBlock(BlockPos ctrlPos, int glassLayers, GlassVisitor visitor) {
        for (int layer = 1; layer <= glassLayers; layer++) {
            int worldY = ctrlPos.getY() + layer; // Sklo začíná na Y+1 nad controllerem
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = new BlockPos(ctrlPos.getX() + dx, worldY, ctrlPos.getZ() + dz);
                    if (!visitor.visit(pos, layer)) return;
                }
            }
        }
    }

    // =========================================================
    //  VISITOR INTERFACES
    // =========================================================

    @FunctionalInterface
    public interface BrickVisitor {
        /** @return true = pokračuj, false = přeruš */
        boolean visit(BlockPos.MutableBlockPos pos);
    }

    @FunctionalInterface
    public interface GlassVisitor {
        /**
         * @param pos   světová pozice skla
         * @param layer číslo vrstvy (1 = nejspodnější)
         * @return true = pokračuj, false = přeruš
         */
        boolean visit(BlockPos pos, int layer);
    }

    // =========================================================
    //  PREDIKÁTY (utility)
    // =========================================================

    public static Predicate<BlockState> of(Block block) {
        return state -> state.is(block);
    }

    public static Predicate<BlockState> any(Block... blocks) {
        return state -> {
            for (Block b : blocks) if (state.is(b)) return true;
            return false;
        };
    }
}