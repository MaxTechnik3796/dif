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
    // Konstantní hitbox – velikost jako při 45° rotaci (nejhorší případ), nikdy se nemění
    // sqrt(0.6² + 1.8²) ≈ 1.9 jako poloměr
    private static final double HALF_BOX = 1.7D;
    private static final float HH = 1.4F;

    public FormulaEntity(EntityType<?> t, Level l) { super(t, l); }

    // Konstantní AABB – nemění se s rotací, vždy stejně velký
    private AABB buildAABB() {
        return new AABB(getX()-HALF_BOX, getY(), getZ()-HALF_BOX, getX()+HALF_BOX, getY()+HH, getZ()+HALF_BOX);
    }
    @Override public void tick() { super.tick(); setBoundingBox(buildAABB()); }
    @Override protected AABB makeBoundingBox() { return buildAABB(); }

    @Override
    protected void positionRider(net.minecraft.world.entity.Entity rider, net.minecraft.world.entity.Entity.MoveFunction moveFunction) {
        if (this.hasPassenger(rider)) {
            // offsetZ: Kladné číslo posouvá hráče DOPŘEDU.
            // Pokud je teď moc vzadu, zvyšte toto číslo (např. z -0.2F na 0.3F nebo 0.5F).
            float offsetZ = 0.55F;
            float offsetY = -0.525F; // Výška zůstává, aby seděl v díře

            double x = this.getX() + (double)(net.minecraft.util.Mth.sin(-this.getYRot() * ((float)Math.PI / 180F)) * offsetZ);
            double z = this.getZ() + (double)(net.minecraft.util.Mth.cos(this.getYRot() * ((float)Math.PI / 180F)) * offsetZ);

            moveFunction.accept(rider, x, this.getY() + this.getPassengersRidingOffset() + rider.getMyRidingOffset() + offsetY, z);
        }
    }

    @Override
    public double getPassengersRidingOffset() {
        // Základní výška sedu nad středem (originem) entity
        return 0.4D;
    }

    // === Barva ===
    @Override protected void defineSynchedData() { super.defineSynchedData(); entityData.define(DATA_COLOR, 0xFFFFFF); }
    public int getColor() { return entityData.get(DATA_COLOR); }
    public void setColor(int c) { entityData.set(DATA_COLOR, c); }
    @Override protected void readAdditionalSaveData(CompoundTag n) { super.readAdditionalSaveData(n); if(n.contains("Color")) setColor(n.getInt("Color")); }
    @Override protected void addAdditionalSaveData(CompoundTag n) { super.addAdditionalSaveData(n); n.putInt("Color", getColor()); }

    @Override
    public InteractionResult interact(Player p, InteractionHand h) {
        ItemStack s = p.getItemInHand(h);
        if (!p.isSecondaryUseActive() && s.getItem() instanceof DyeItem dye) {
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
    @Override public float getWheelbase() { return 3.2f; }
    @Override public float getMaxSteerAngleDegrees() { return 25f; }
    @Override public float getMaxSpeedKmh() { return 330f; }
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
    @Override public float getCrashDamageThresholdKmh() { return 60f; }
    @Override public float getCrashDamageMultiplier() { return 0.12f; }
    @Override public Fluid getFuelFluid() { return Fluids.LAVA; }
    @Override public float getMaxFuelMb() { return 24000f; }
    @Override public float getInitialFuelMb() { return 0f; }
    @Override public float getFuelConsumptionLowMbPerTick() { return 0.5f; }
    @Override public float getFuelConsumptionHighMbPerTick() { return 1.0f; }
    @Override public float getFuelSpeedThresholdKmh() { return 150f; }
}