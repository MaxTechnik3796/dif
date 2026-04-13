package cz.maxtechnik.dif.entity.vehicle;

import cz.maxtechnik.dif.init.basic.DifModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.AABB;

public class FormulaEntity extends BaseCarEntity {
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(FormulaEntity.class, EntityDataSerializers.INT);
    // Půlrozměry auta – zmenšeno aby při 45° hitbox nepřečníval přes model
    private static final float HW = 0.6F, HL = 1.8F, HH = 1.4F;

    public FormulaEntity(EntityType<?> t, Level l) { super(t, l); }

    // === Dynamický AABB podle rotace (Create-style) ===
    private AABB buildRotatedAABB() {
        float r = getYRot() * 0.017453292F; // PI/180
        float c = Math.abs((float)Math.cos(r)), s = Math.abs((float)Math.sin(r));
        double hx = HW*c + HL*s, hz = HW*s + HL*c;
        return new AABB(getX()-hx, getY(), getZ()-hz, getX()+hx, getY()+HH, getZ()+hz);
    }
    @Override public void tick() { super.tick(); setBoundingBox(buildRotatedAABB()); }
    @Override protected AABB makeBoundingBox() { return buildRotatedAABB(); }

    // === Barva ===
    @Override protected void defineSynchedData() { super.defineSynchedData(); entityData.define(DATA_COLOR, 0xFFFFFF); }
    public int getColor() { return entityData.get(DATA_COLOR); }
    public void setColor(int c) { entityData.set(DATA_COLOR, c); }

    @Override protected void readAdditionalSaveData(CompoundTag n) { super.readAdditionalSaveData(n); if(n.contains("Color")) setColor(n.getInt("Color")); }
    @Override protected void addAdditionalSaveData(CompoundTag n) { super.addAdditionalSaveData(n); n.putInt("Color", getColor()); }

    @Override
    public InteractionResult interact(Player p, InteractionHand h) {
        ItemStack s = p.getItemInHand(h);
        if (s.getItem() instanceof DyeItem dye) {
            if (!level().isClientSide) {
                float[] c = dye.getDyeColor().getTextureDiffuseColors();
                setColor(((int)(c[0]*255)<<16)|((int)(c[1]*255)<<8)|(int)(c[2]*255));
                if (!p.getAbilities().instabuild) s.shrink(1);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.interact(p, h);
    }

    // === Vlastnosti formule ===
    @Override public SoundEvent getEngineSound() { return DifModSounds.FORMULA_ENGINE.get(); }
    @Override public net.minecraft.world.item.Item getDropItem() { return cz.maxtechnik.dif.init.basic.DifModItems.FORMULA_ITEM.get(); }
    @Override public float getCustomStepHeight() { return 0.75f; }
    @Override public float getMaxSpeedKmh() { return 330.0f; }
    @Override public float getBaseAcceleration() { return 0.042f; }
    @Override public float[] getGearRatios() { return new float[]{3.35f,2.47f,1.97f,1.63f,1.39f,1.23f,1.18f}; }
    @Override public int getShiftCooldownTicks() { return 2; }
    @Override public float getIdleRPM() { return 5000f; }
    @Override public float getMaxRPM() { return 18000f; }
    @Override public float getRedlineRPM() { return 17500f; }
    @Override public float getDownforceCoefficient() { return 2.0f; }
    @Override public float getAeroDrag() { return 0.00018f; }
    @Override public float getBrakingDeceleration() { return 0.10f; }
    @Override public float getBaseHandling() { return 5.5f; }
    @Override public float getHighSpeedSteerReduction() { return 0.30f; }
    @Override public float getCrashDamageThresholdKmh() { return 60.0f; }
    @Override public float getCrashDamageMultiplier() { return 0.12f; }
    @Override public Fluid getFuelFluid() { return Fluids.LAVA; }
    @Override public float getMaxFuelMb() { return 24000f; }
    @Override public float getInitialFuelMb() { return 0f; }
    @Override public float getFuelConsumptionLowMbPerTick() { return 0.5f; }
    @Override public float getFuelConsumptionHighMbPerTick() { return 1.0f; }
    @Override public float getFuelSpeedThresholdKmh() { return 150f; }
}