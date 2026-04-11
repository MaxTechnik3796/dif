package cz.maxtechnik.dif.entity.vehicle;

import cz.maxtechnik.dif.init.basic.DifModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FormulaEntity extends BaseCarEntity {
    public FormulaEntity(EntityType<?> t, Level l) { super(t, l); }
    @Override public SoundEvent getEngineSound() { return DifModSounds.FORMULA_ENGINE.get(); }
    @Override public net.minecraft.world.item.Item getDropItem() { return cz.maxtechnik.dif.init.basic.DifModItems.FORMULA_ITEM.get(); }
    @Override public float getCustomStepHeight() { return 0.75f; }
    @Override public float getMaxSpeedKmh() { return 330.0f; }
    @Override public float getBaseAcceleration() { return 0.042f; }
    @Override public float[] getGearRatios() { return new float[]{3.35f, 2.47f, 1.97f, 1.63f, 1.39f, 1.23f, 1.18f}; }
    @Override public int getShiftCooldownTicks() { return 2; }
    @Override public float getIdleRPM() { return 5000.0f; }
    @Override public float getMaxRPM() { return 18000.0f; }
    @Override public float getRedlineRPM() { return 17500.0f; }
    @Override public float getDownforceCoefficient() { return 2.0f; }
    @Override public float getAeroDrag() { return 0.00018f; }
    @Override public float getBrakingDeceleration() { return 0.10f; }
    @Override public float getBaseHandling() { return 5.5f; }
    @Override public float getHighSpeedSteerReduction() { return 0.30f; }
    @Override public float getCrashDamageThresholdKmh() { return 60.0f; }
    @Override public float getCrashDamageMultiplier() { return 0.12f; }
    @Override public Fluid getFuelFluid() { return Fluids.LAVA; }
    @Override public float getMaxFuelMb() { return 24000.0f; }
    @Override public float getInitialFuelMb() { return 0.0f; }
    @Override public float getFuelConsumptionLowMbPerTick() { return 0.5f; }
    @Override public float getFuelConsumptionHighMbPerTick() { return 1.0f; }
    @Override public float getFuelSpeedThresholdKmh() { return 150.0f; }
}