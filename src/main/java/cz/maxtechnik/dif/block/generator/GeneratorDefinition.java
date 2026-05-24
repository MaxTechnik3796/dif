package cz.maxtechnik.dif.block.generator;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/**
 * Definuje všechny nastavitelné vlastnosti generátoru na kapalinu.
 * Každý konkrétní generátor implementuje toto rozhraní a nastavuje si
 * vlastní hodnoty – abstraktní základní třídy zůstávají čistě datové.
 */
public interface GeneratorDefinition {

    // ── Kapalina ──────────────────────────────────────────────────────────────

    /** mB kapaliny spotřebované za tick při plném výkonu */
    int fluidConsumptionPerTick();

    /** Maximální kapacita nádrže v mB */
    int tankCapacity();

    // ── Kinetika ─────────────────────────────────────────────────────────────

    /** Základní otáčky (RPM) při plném výkonu */
    float generatedSpeed();

    /** Stres kapacita (SU) při základní rychlosti */
    float generatedCapacity();

    // ── Hřídel / Orientace ───────────────────────────────────────────────────

    /**
     * Osa, podél které hřídel vystupuje.
     * Příklad: {@code Direction.Axis.Y} = vertikální hřídel (nahoru/dolů).
     */
    Direction.Axis shaftAxis();

    /**
     * Která ze dvou stran osy je "primární" hřídelová strana.
     * Opačná strana je automaticky druhá hřídelová strana.
     * Příklad: {@code Direction.UP} = hřídel vystupuje nahoru.
     */
    Direction shaftSideOfBlock();

    // ── Rendering ─────────────────────────────────────────────────────────────

    /**
     * ResourceLocation částečného modelu renderovaného přes základní blok.
     * Vrať {@code null} pokud nechceš overlay model.
     */
    default ResourceLocation overlayModel() { return null; }

    /** Má se renderovat rotující hřídel? */
    default boolean renderShaft() { return true; }

    // ── Misc ─────────────────────────────────────────────────────────────────

    /**
     * Unikátní identifikátor pro registry name bloku a block entity.
     * Příklad: {@code "steam_generator"}
     */
    String registryName();
}
