package cz.maxtechnik.dif.entity.vehicle;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * =========================================================
 *  F1 MONOPOST – SEZÓNA 2012  (FIA Technical Regulations)
 * =========================================================
 *
 * Základní technické parametry (reálná data 2012):
 *  Motor:      2.4 L V8 přirozeně sací, ~18 000 ot/min limitér
 *  Výkon:      ~750 bhp  (~560 kW)
 *  Hmotnost:   640 kg (bez paliva) + ~75 kg řidič ≈ 720 kg celkem
 *  Skříň:      7-stupňová sekvenční poloautomatická (pneumatická)
 *  Řazení:     ~50 ms při řazení nahoru, ~200 ms dolů (rev-matching)
 *  Přítlak:    ~2 500 N při 200 km/h, až ~5 000 N při 300 km/h
 *  CdA:        ~1.0 m² (vyšší než silniční auto – přítlakové křídlo)
 *  0–100 km/h: ~2.5 s (závisí na podlaze, pneumatikách)
 *  0–200 km/h: ~5.5 s
 *  Brzdění z 300 km/h: ~3.5 s / ~130 m
 *  Max rychlost (rovina): ~330 km/h (záleží na okruhu)
 *
 * Minecraft přepočet:
 *  1 blok = 1 metr, 20 ticků = 1 sekunda
 *  330 km/h = 91.7 m/s = 4.58 bl/tick
 *
 * OPRAVY v této verzi:
 *  - getCustomStepHeight() = 0.75f → formule přejede 3/4 bloku (obrubníky na trati)
 *  - Vlastní prahy škody při nárazu přizpůsobené F1 rychlostem
 */
public class FormulaEntity extends BaseCarEntity {

    public FormulaEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    // =========================================================
    //  GEOMETRIE / KROK
    // =========================================================

    /**
     * 0.75 bl = 12 pixelů = 3/4 bloku.
     *
     * Formule F1 má nízký, tuhý podvozek — ale na závodních okruzích
     * přejíždí nízké obrubníky (kerbs). 0.75 bl umožňuje přejet
     * půlblok bez problémů a zvládnout i 3/4 bloku (schodek z bloku na
     * blok se sklonem). Plný blok (1.0) by byl nerealistický pro F1.
     *
     * Tato hodnota se předává do setMaxUpStep() v BaseCarEntity konstruktoru.
     */
    @Override
    public float getCustomStepHeight() { return 0.75f; }

    // =========================================================
    //  RYCHLOST A ZRYCHLENÍ
    // =========================================================

    /**
     * 330 km/h – průměrná maximální rychlost F1 sezóny 2012 na rovinkách.
     */
    @Override
    public float getMaxSpeedKmh() { return 330.0f; }

    /**
     * Základní zrychlení v 1. stupni při plném plynu (bl/tick²).
     * Výpočet pro 0→100 km/h za ~2.5 s (50 ticků): ≈ 0.028 bl/tick²
     */
    @Override
    public float getBaseAcceleration() { return 0.028f; }

    // =========================================================
    //  PŘEVODOVKA  (7 stupňů, FIA 2012)
    // =========================================================

    /**
     * Přibližné převodové poměry F1 2012.
     * Stupeň:  1      2      3      4      5      6      7
     * Poměr: 3.35   2.47   1.97   1.63   1.39   1.23   1.10
     */
    @Override
    public float[] getGearRatios() {
        return new float[]{ 3.35f, 2.47f, 1.97f, 1.63f, 1.39f, 1.23f, 1.10f };
    }

    /**
     * Pneumatická převodovka F1 2012: torque cut pouze 2 ticky (~100 ms).
     */
    @Override
    public int getShiftCooldownTicks() { return 2; }

    // =========================================================
    //  MOTOR – V8 2.4L
    // =========================================================

    /** Idle otáčky: ~5 000 ot/min. */
    @Override
    public float getIdleRPM() { return 5000.0f; }

    /** Limitér FIA 2012: 18 000 ot/min. */
    @Override
    public float getMaxRPM() { return 18000.0f; }

    /** Redline (vizuální upozornění v HUDu): 17 500 ot/min. */
    @Override
    public float getRedlineRPM() { return 17500.0f; }

    // =========================================================
    //  AERODYNAMIKA
    // =========================================================

    /**
     * Koeficient přítlaku.
     * Při 100% rychlosti (330 km/h): mult = 1 + 2.0 × 1 = 3.0 → 3× lepší grip.
     */
    @Override
    public float getDownforceCoefficient() { return 2.0f; }

    /**
     * Aerodynamický odpor (CdA ≈ 1.0 m²).
     */
    @Override
    public float getAeroDrag() { return 0.00022f; }

    // =========================================================
    //  BRZDY  (uhlíkovo-keramické)
    // =========================================================

    /**
     * Brzdná decelerace: 0.10 bl/tick² — silnější než silniční auto.
     */
    @Override
    public float getBrakingDeceleration() { return 0.10f; }

    // =========================================================
    //  ŘÍZENÍ
    // =========================================================

    /** Základní handling = 5.5°/tick — velmi citlivé. */
    @Override
    public float getBaseHandling() { return 5.5f; }

    /** Redukce řízení při max rychlosti — F1 má přesné řízení díky přítlaku. */
    @Override
    public float getHighSpeedSteerReduction() { return 0.30f; }

    // =========================================================
    //  KOLIZE / NÁRAZ
    // =========================================================

    /**
     * Práh škody při nárazu: 60 km/h.
     *
     * Vyšší než výchozích 40 km/h — F1 má bezpečnostní zábrany (HANS,
     * crashbox), takže při nízkých rychlostech hráč neutrpí škodu.
     * Při 60+ km/h ovšem i F1 řidič dostane solidní ránu.
     *
     * Příklady škody (getCrashDamageMultiplier = 0.12):
     *   60 km/h náraz  →  0 HP  (pod prahem)
     *   100 km/h náraz → (100-60) × 0.12 =  4.8 HP
     *   200 km/h náraz → (200-60) × 0.12 = 16.8 HP
     *   300 km/h náraz → (300-60) × 0.12 = 28.8 HP  (skoro jistá smrt)
     */
    @Override
    public float getCrashDamageThresholdKmh() { return 60.0f; }

    /**
     * Koeficient škody při nárazu.
     * 0.12 = mírně nižší než výchozí 0.15 (F1 crashbox trochu pomáhá).
     */
    @Override
    public float getCrashDamageMultiplier() { return 0.12f; }
}