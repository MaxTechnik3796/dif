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
 *  Tato rychlost je zvládnutelná při render vzdálenosti 8+ chunků.
 *
 * Speciální chování F1 v tomto modu:
 *  - Po nasednutí: motor ihned nastartován (setEngineOn=true)
 *  - Po vystoupení: motor vypnut, minimální fyzika
 *  - Přítlak: při vyšší rychlosti výrazně lepší grip v zatáčkách
 *  - Pneumatické řazení: torque cut pouze 2 ticky (≈100 ms)
 *  - Velmi výkonné karbonové brzdy
 *  - Citlivé řízení – okamžitá reakce (getBaseHandling = 5.5°/tick)
 *  - Soul Sand: klouzání/bláto simulace (velký aerodynamický přítlak nepomůže)
 *  - Led: F1 pneumatiky jsou absolutně nevhodné – extrémní klouzání
 */
public class FormulaEntity extends BaseCarEntity {

    public FormulaEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    // =========================================================
    //  GEOMETRIE / KROK
    // =========================================================

    /**
     * 0.375 bl = 6 pixelů.
     * Formule může přejet nízké obrubníky na trati.
     */
    @Override
    public float getCustomStepHeight() { return 0.375f; }

    // =========================================================
    //  RYCHLOST A ZRYCHLENÍ
    // =========================================================

    /**
     * 330 km/h – průměrná maximální rychlost F1 sezóny 2012 na rovinkách.
     * V Minecraftu: 330 / 72 ≈ 4.58 bl/tick (zvládnutelné s render dist. 8+).
     */
    @Override
    public float getMaxSpeedKmh() { return 330.0f; }

    /**
     * Základní zrychlení v 1. stupni při plném plynu (bl/tick²).
     *
     * Výpočet pro reálné 0→100 km/h za ~2.5 s (50 ticků):
     *   cíl = (100/72) bl/tick ÷ 50 ticků ≈ 0.0277 bl/tick²
     *
     * Hodnota 0.028 je mírně optimistická (zrychlení není lineární,
     * v 1. stupni je momentová křivka na vrcholu).
     */
    @Override
    public float getBaseAcceleration() { return 0.028f; }

    // =========================================================
    //  PŘEVODOVKA  (7 stupňů, FIA 2012)
    // =========================================================

    /**
     * Přibližné převodové poměry F1 2012.
     * Každý tým měl vlastní ratia, toto jsou středové hodnoty.
     *
     * Stupeň:  1      2      3      4      5      6      7
     * Poměr: 3.35   2.47   1.97   1.63   1.39   1.23   1.10
     *
     * Fyzikální efekt: v nižším stupni je více tahu (accel × ratio/1stRatio),
     * v vyšším stupni je vyšší max rychlost v daném stupni.
     */
    @Override
    public float[] getGearRatios() {
        return new float[]{ 3.35f, 2.47f, 1.97f, 1.63f, 1.39f, 1.23f, 1.10f };
    }

    /**
     * Pneumatická převodovka F1 2012:
     * Řazení nahoru ≈ 50 ms = ~1 tick → torque cut 2 ticky (trochu konzervativnější)
     * Řazení dolů ≈ 200 ms → rev-matching, torque cut 4 ticky
     *
     * Výchozí hodnota z BaseCarEntity je 8 ticků (silniční auto).
     */
    @Override
    public int getShiftCooldownTicks() { return 2; }

    // =========================================================
    //  MOTOR – V8 2.4L  (Ferrari 056 / Renault RS27 / Mercedes FO 108Z)
    // =========================================================

    /**
     * Idle otáčky: ~5 000 ot/min.
     * Motor F1 nikdy nespadne níž – jinak by se zhasl.
     */
    @Override
    public float getIdleRPM() { return 5000.0f; }

    /**
     * Limitér FIA 2012: 18 000 ot/min.
     * Reálné motory v qualifying mohly jet přes 19 000, ale závodní limit byl 18 000.
     */
    @Override
    public float getMaxRPM() { return 18000.0f; }

    /**
     * Redline (vizuální upozornění v HUDu): 17 500 ot/min.
     * Při dosažení redline začnou blikat horní segmenty RPM indikátoru.
     */
    @Override
    public float getRedlineRPM() { return 17500.0f; }

    // =========================================================
    //  AERODYNAMIKA  (klíčový aspekt F1)
    // =========================================================

    /**
     * Koeficient přítlaku (downforce).
     *
     * Fyzika: downforceMult = 1 + coefficient × (v/vMax)²
     * Při 100% rychlosti (330 km/h): mult = 1 + 2.0 × 1 = 3.0
     * → 3× lepší grip v zatáčkách než v klidu!
     *
     * Reálný přítlak F1 2012:
     * ~2 500 N při 200 km/h → efektivně nesou více než vlastní váhu.
     */
    @Override
    public float getDownforceCoefficient() { return 2.0f; }

    /**
     * Aerodynamický odpor (CdA ≈ 1.0 m²).
     *
     * Výpočet pro max rychlost 4.58 bl/tick v 7. stupni:
     *   thrust_7th = baseAccel × (1.10/3.35) = 0.00917 bl/tick²
     *   při rovnováze: thrust = drag + rolling → drag ≈ 0.00022
     */
    @Override
    public float getAeroDrag() { return 0.00022f; }

    // =========================================================
    //  BRZDY  (uhlíkovo-keramické kotoučové brzdy)
    // =========================================================

    /**
     * Brzdná decelerace (bl/tick²).
     *
     * Reálné F1 2012: brzdí z 300 km/h na 0 za ~3.5 s (70 ticků).
     * 300 km/h = 4.17 bl/tick → 4.17/70 ≈ 0.060 bl/tick² jen od brzd.
     * Přičteme aerodynamický odpor (~0.003 při té rychlosti) → celkem ~0.063.
     *
     * Hodnota 0.10 je mírně nadhodnocená pro příjemnější hratelnost.
     */
    @Override
    public float getBrakingDeceleration() { return 0.10f; }

    // =========================================================
    //  ŘÍZENÍ  (F1 rack-and-pinion, okamžitá odpověď)
    // =========================================================

    /**
     * Základní handling = stupně natočení za tick při nízké rychlosti.
     * 5.5 = velmi citlivé řízení – formule reaguje okamžitě.
     *
     * Při vysoké rychlosti je handling redukován getHighSpeedSteerReduction,
     * ale downforce to kompenzuje → F1 se v zatáčkách drží lépe než silniční auto!
     */
    @Override
    public float getBaseHandling() { return 5.5f; }

    /**
     * Redukce řízení při max rychlosti.
     *
     * F1 mají přesné řízení i při vysoké rychlosti díky přítlaku.
     * Nižší hodnota = řízení je citlivější i při plné rychlosti.
     * (Výchozí BaseCarEntity je 0.55 – silniční auto je méně citlivé)
     */
    @Override
    public float getHighSpeedSteerReduction() { return 0.30f; }
}