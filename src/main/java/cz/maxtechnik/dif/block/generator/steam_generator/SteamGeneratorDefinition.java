package cz.maxtechnik.dif.block.generator.steam_generator;

import cz.maxtechnik.dif.block.generator.GeneratorDefinition;
import net.minecraft.core.Direction;

/**
 * Definice Steam Generátoru.
 *
 * Všechna čísla upravuj ZDE – bez nutnosti sahat do logiky.
 *
 * ┌───────────────────────────────────────────────────────┐
 * │  Hřídel      │ Y osa (vertikální – nahoru a dolů)     │
 * │  Spotřeba    │ 2 mB/tick (= 40 mB/s)                  │
 * │  Nádrž       │ 4 000 mB                               │
 * │  Rychlost    │ 96 RPM                                 │
 * │  SU kapacita │ 512 SU                                 │
 * └───────────────────────────────────────────────────────┘
 */
public final class SteamGeneratorDefinition implements GeneratorDefinition {

    public static final SteamGeneratorDefinition INSTANCE = new SteamGeneratorDefinition();

    private SteamGeneratorDefinition() {}

    // ── Kapalina ──────────────────────────────────────────────────────────────

    @Override public int fluidConsumptionPerTick() { return 2; }
    @Override public int tankCapacity()             { return 4000; }

    // ── Kinetika ─────────────────────────────────────────────────────────────

    @Override public float generatedSpeed()    { return 96f; }
    @Override public float generatedCapacity() { return 512f; }

    // ── Hřídel ────────────────────────────────────────────────────────────────

    /**
     * Hřídel jde vertikálně.
     * Pro horizontální hřídel změň na {@code Direction.Axis.X} nebo {@code Direction.Axis.Z}.
     */
    @Override public Direction.Axis shaftAxis()       { return Direction.Axis.Y; }
    @Override public Direction shaftSideOfBlock()     { return Direction.UP; }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override public net.minecraft.resources.ResourceLocation overlayModel() { return null; }
    @Override public boolean renderShaft() { return true; }

    // ── Název ─────────────────────────────────────────────────────────────────

    @Override public String registryName() { return "steam_generator"; }
}
