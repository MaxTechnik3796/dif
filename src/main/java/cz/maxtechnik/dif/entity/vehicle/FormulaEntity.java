package cz.maxtechnik.dif.entity.vehicle;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

// F1 Monopost 2012 (např. 750 bhp, 330 km/h max rychlost).
public class FormulaEntity extends BaseCarEntity {

    public FormulaEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    // Výška přeskoku 0.75 bl umožní plynulé přejetí obrubníků na trati.
    @Override
    public float getCustomStepHeight() { return 0.75f; }

    // Max průměrná rychlost na rovinkách.
    @Override
    public float getMaxSpeedKmh() { return 330.0f; }

    // Zrychlení 0.028 bl/tick² dá 0-100 za ~2.5s.
    @Override
    public float getBaseAcceleration() { return 0.028f; }

    // 7 stupňů dle FIA 2012.
    @Override
    public float[] getGearRatios() {
        return new float[]{ 3.35f, 2.47f, 1.97f, 1.63f, 1.39f, 1.23f, 1.10f };
    }

    // Pneumatická převodovka, cooldown přeřazení ~100 ms.
    @Override
    public int getShiftCooldownTicks() { return 2; }

    // Nejnižší stabilní otáčky.
    @Override
    public float getIdleRPM() { return 5000.0f; }

    // Omezovač dle FIA.
    @Override
    public float getMaxRPM() { return 18000.0f; }

    // Červená zóna na HUDu.
    @Override
    public float getRedlineRPM() { return 17500.0f; }

    // Až 3x silnější grip při maximální rychlosti.
    @Override
    public float getDownforceCoefficient() { return 2.0f; }

    // Koeficient aerodynamického odporu.
    @Override
    public float getAeroDrag() { return 0.00022f; }

    // Mnohem silnější brzdy než u klasického auta.
    @Override
    public float getBrakingDeceleration() { return 0.10f; }

    // Velmi citlivé řízení (5.5° za tick).
    @Override
    public float getBaseHandling() { return 5.5f; }

    // Redukce zatáčení ve vysoké rychlosti.
    @Override
    public float getHighSpeedSteerReduction() { return 0.30f; }

    // Změkčený náraz díky safety zóně, damage až nad 60 km/h.
    @Override
    public float getCrashDamageThresholdKmh() { return 60.0f; }

    // Mírně menší ztráta zdraví při nárazu než u běžného auta.
    @Override
    public float getCrashDamageMultiplier() { return 0.12f; }
}