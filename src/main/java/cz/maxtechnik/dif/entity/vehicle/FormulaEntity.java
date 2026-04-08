package cz.maxtechnik.dif.entity.vehicle;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

// F1 Monopost 2012 (např. 750 bhp, 330 km/h max rychlost).
public class FormulaEntity extends BaseCarEntity {

    public FormulaEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override public float getCustomStepHeight()  { return 0.75f; }
    @Override public float getMaxSpeedKmh()       { return 330.0f; }
    @Override public float getBaseAcceleration()  { return 0.028f; }

    @Override
    public float[] getGearRatios() {
        return new float[]{ 3.35f, 2.47f, 1.97f, 1.63f, 1.39f, 1.23f, 1.10f };
    }

    @Override public int   getShiftCooldownTicks()      { return 2;       }
    @Override public float getIdleRPM()                 { return 5000.0f; }
    @Override public float getMaxRPM()                  { return 18000.0f; }
    @Override public float getRedlineRPM()              { return 17500.0f; }
    @Override public float getDownforceCoefficient()    { return 2.0f;    }
    @Override public float getAeroDrag()                { return 0.00022f; }
    @Override public float getBrakingDeceleration()     { return 0.10f;   }
    @Override public float getBaseHandling()            { return 5.5f;    }
    @Override public float getHighSpeedSteerReduction() { return 0.30f;   }
    @Override public float getCrashDamageThresholdKmh() { return 60.0f;   }
    @Override public float getCrashDamageMultiplier()   { return 0.12f;   }

    // ─── PALIVO ───────────────────────────────────────────────────────────────

    /** Formule jezdí na lávě. */
    @Override public Fluid getFuelFluid() { return Fluids.LAVA; }

    /** 24 bucketů = 24 000 mb. */
    @Override public float getMaxFuelMb() { return 24_000.0f; }

    /** Spawne prázdná – musí se natankovat před jízdou. */
    @Override public float getInitialFuelMb() { return 0.0f; }

    /** Nízká spotřeba (pod 150 km/h): 10 mb/s → 0.5 mb/tick. */
    @Override public float getFuelConsumptionLowMbPerTick()  { return 0.5f; }

    /** Vysoká spotřeba (150+ km/h): 20 mb/s → 1.0 mb/tick. */
    @Override public float getFuelConsumptionHighMbPerTick() { return 1.0f; }

    /** Práh přepnutí spotřeby. */
    @Override public float getFuelSpeedThresholdKmh() { return 150.0f; }
}