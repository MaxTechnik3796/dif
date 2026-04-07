package cz.maxtechnik.dif.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;

// Základ pro všechna auta v modu s fyzikou pohybu, převodovkou a kolizemi.
public abstract class BaseCarEntity extends Entity {

    protected static final EntityDataAccessor<Float>   DATA_RPM       = SynchedEntityData.defineId(BaseCarEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> DATA_GEAR      = SynchedEntityData.defineId(BaseCarEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float>   DATA_SPEED     = SynchedEntityData.defineId(BaseCarEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Boolean> DATA_ENGINE_ON = SynchedEntityData.defineId(BaseCarEntity.class, EntityDataSerializers.BOOLEAN);

    protected float velocity      = 0.0f; // Fyzikální stav (kladné = vpřed).
    protected int   shiftCooldown = 0;    // Ticků do dalšího přeřazení.

    private float prevVelocity = 0.0f; // Rychlost z minulého ticku pro detekci nárazu.
    private int crashDamageCooldown = 0; // Ochrana proti spamu damage.

    public enum SurfaceType { NORMAL, SOUL_SAND, ICE, CARPET }

    // Reflexe pro zjištění stisku mezerníku (ruční brzda).
    private static Field jumpingField;
    static {
        try {
            try   { jumpingField = LivingEntity.class.getDeclaredField("f_20899_"); }
            catch (NoSuchFieldException e) { jumpingField = LivingEntity.class.getDeclaredField("jumping"); }
            jumpingField.setAccessible(true);
        } catch (Exception ignored) {}
    }

    public BaseCarEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
        this.setMaxUpStep(getCustomStepHeight());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_RPM,       getIdleRPM());
        this.entityData.define(DATA_GEAR,      1);
        this.entityData.define(DATA_SPEED,     0.0f);
        this.entityData.define(DATA_ENGINE_ON, false);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) return InteractionResult.PASS;
        if (!this.level().isClientSide) {
            boolean ok = player.startRiding(this);
            if (ok) setEngineOn(true);
            return ok ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        // Vyčištění pohybu na straně serveru i KLIENTA zabraňuje "odskakování" a desynchronizaci po vystoupení.
        if (!this.isVehicle()) {
            if (!this.level().isClientSide) setEngineOn(false);
            velocity = 0.0f;
            prevVelocity = 0.0f;
            this.setDeltaMovement(Vec3.ZERO);
            this.hasImpulse = true;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && !this.isRemoved()) {
            this.discard();
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (shiftCooldown > 0) shiftCooldown--;
        if (crashDamageCooldown > 0) crashDamageCooldown--;

        LivingEntity driver = this.getControllingPassenger();
        prevVelocity = velocity;

        if (this.isVehicle() && driver != null) {
            simulateActivePhysics(driver);
        } else {
            simulateIdlePhysics();
        }

        this.hasImpulse = true; // Rychlejší synchronizace se serverem.
        this.entityData.set(DATA_SPEED, velocity);
    }

    // Fyzika prázdného auta (dojíždění).
    private void simulateIdlePhysics() {
        velocity *= 0.88f;
        if (Math.abs(velocity) < 0.0005f) velocity = 0.0f;
        setRPM(Math.max(0.0f, getRPM() - getMaxRPM() * 0.04f));

        double yawRad = Math.toRadians(this.getYRot());
        double newYMot = this.onGround() ? -0.05 : this.getDeltaMovement().y - 0.04;
        newYMot = Math.max(newYMot, -1.5);

        this.setDeltaMovement(new Vec3(-Math.sin(yawRad) * velocity, newYMot, Math.cos(yawRad) * velocity));
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    // Fyzika s řidičem (zrychlení, odpor, zatáčení).
    protected void simulateActivePhysics(LivingEntity driver) {
        float throttle   = driver.zza;
        float steerInput = -driver.xxa;
        boolean handbrake = getJumping(driver);
        SurfaceType surface = detectSurface();

        float surfaceSpeedMult = getSurfaceSpeedMult(surface);
        float lateralGrip      = getSurfaceLateralGrip(surface);
        float rollingRes       = getSurfaceRollingResistance(surface);
        float maxSpeedBT       = (getMaxSpeedKmh() * surfaceSpeedMult) / 72.0f;

        float rpmConv   = computeRPMConversionFactor();
        float gearRatio = getGearRatios()[getCurrentGear() - 1];
        float targetRPM = Math.abs(velocity) * gearRatio * rpmConv;

        if (Math.abs(velocity) < 0.05f && throttle > 0) {
            targetRPM = Math.max(targetRPM, getIdleRPM() + (getMaxRPM() - getIdleRPM()) * throttle * 0.25f);
        }
        setRPM(Math.max(getIdleRPM(), Math.min(getMaxRPM(), targetRPM)));

        float torqueFactor = (shiftCooldown > 0) ? (float) shiftCooldown / getShiftCooldownTicks() : 1.0f;
        if (getRPM() >= getMaxRPM() * 0.999f) torqueFactor = 0.0f;

        float thrust = 0.0f;
        if (throttle > 0.0f) {
            float rpmPowerFactor = computeTorqueCurve(getRPM());
            thrust = throttle * getBaseAcceleration() * (gearRatio / getGearRatios()[0]) * rpmPowerFactor * torqueFactor;
        } else if (throttle < 0.0f) {
            thrust = throttle * getBrakingDeceleration();
        }

        if (handbrake) {
            velocity    *= 0.87f;
            lateralGrip *= 0.22f;
        }

        float drag = velocity * Math.abs(velocity) * getAeroDrag();
        float rolling = velocity * rollingRes;
        velocity += thrust - drag - rolling;

        if (surface == SurfaceType.SOUL_SAND) {
            float ssCap = 22.0f / 72.0f;
            if (Math.abs(velocity) > ssCap) {
                velocity = velocity * 0.82f + Math.signum(velocity) * ssCap * 0.18f;
            }
        }

        velocity = Math.max(-0.25f, Math.min(maxSpeedBT, velocity));

        float downforceMult = 1.0f;
        if (maxSpeedBT > 0.001f) {
            float speedRatio = velocity / maxSpeedBT;
            downforceMult = 1.0f + getDownforceCoefficient() * speedRatio * speedRatio;
        }

        float effectiveHandling = getBaseHandling() * downforceMult * lateralGrip;

        if (Math.abs(velocity) > 0.015f) {
            float speedNorm   = Math.abs(velocity) / maxSpeedBT;
            float speedFactor = 1.0f - speedNorm * getHighSpeedSteerReduction();
            float turnRate    = effectiveHandling * steerInput * Math.max(0.05f, speedFactor);
            this.setYRot(this.getYRot() + turnRate);
        }

        double yawRad  = Math.toRadians(this.getYRot());
        double newYMot = this.onGround() ? -0.05 : this.getDeltaMovement().y - 0.04;
        newYMot = Math.max(newYMot, -1.5);

        Vec3 intendedMotion = new Vec3(-Math.sin(yawRad) * velocity, newYMot, Math.cos(yawRad) * velocity);
        this.setDeltaMovement(intendedMotion);

        double preX = this.getX();
        double preZ = this.getZ();

        this.move(MoverType.SELF, this.getDeltaMovement());

        // NÁRAZ (Automobility styl): Pokud narazíme do překážky, srovnáme vnitřní rychlost s reálným posunem.
        if (this.horizontalCollision) {
            double actualDX = this.getX() - preX;
            double actualDZ = this.getZ() - preZ;
            double actualSpeed = Math.sqrt(actualDX * actualDX + actualDZ * actualDZ);

            // Zastaví "tlačení" auta do zdi a okamžitě srovná rychlost na tachometru na 0.
            this.velocity = (float) actualSpeed * Math.signum(this.velocity);

            if (!this.level().isClientSide && crashDamageCooldown == 0) {
                double expectedSpeed = Math.sqrt(intendedMotion.x * intendedMotion.x + intendedMotion.z * intendedMotion.z);
                double blockedSpeed = expectedSpeed - actualSpeed;
                float blockedKmh = (float) (blockedSpeed * 72.0);

                if (blockedKmh > getCrashDamageThresholdKmh()) {
                    float damage = (blockedKmh - getCrashDamageThresholdKmh()) * getCrashDamageMultiplier();
                    driver.hurt(this.level().damageSources().generic(), damage);
                    crashDamageCooldown = 10;
                }
            }
        }
    }

    protected SurfaceType detectSurface() {
        BlockPos feet  = this.blockPosition();
        BlockPos below = feet.below();
        Block atFeet  = this.level().getBlockState(feet).getBlock();
        Block atBelow = this.level().getBlockState(below).getBlock();

        if (atFeet instanceof CarpetBlock || atBelow instanceof CarpetBlock) return SurfaceType.CARPET;
        if (isIceBlock(atBelow) || isIceBlock(atFeet)) return SurfaceType.ICE;
        if (atBelow == Blocks.SOUL_SAND || atBelow == Blocks.SOUL_SOIL || atFeet == Blocks.SOUL_SAND || atFeet == Blocks.SOUL_SOIL) return SurfaceType.SOUL_SAND;

        return SurfaceType.NORMAL;
    }

    private static boolean isIceBlock(Block b) {
        return b == Blocks.ICE || b == Blocks.PACKED_ICE || b == Blocks.BLUE_ICE || b == Blocks.FROSTED_ICE;
    }

    protected float getSurfaceSpeedMult(SurfaceType s) {
        return switch (s) { case SOUL_SAND -> 0.35f; case CARPET -> 0.88f; default -> 1.0f; };
    }

    protected float getSurfaceLateralGrip(SurfaceType s) {
        return switch (s) { case ICE -> 0.10f; case SOUL_SAND -> 0.50f; case CARPET -> 0.75f; default -> 1.0f; };
    }

    protected float getSurfaceRollingResistance(SurfaceType s) {
        return switch (s) { case SOUL_SAND -> 0.025f; case ICE -> 0.0004f; case CARPET -> 0.006f; default -> 0.002f; };
    }

    private float computeTorqueCurve(float rpm) {
        float norm = rpm / getMaxRPM();
        return Math.max(0.25f, Math.min(1.0f, (float)(-4.0 * Math.pow(norm - 0.75f, 2) + 1.0)));
    }

    private float computeRPMConversionFactor() {
        return getMaxRPM() / ((getMaxSpeedKmh() / 72.0f) * getGearRatios()[getGearRatios().length - 1]);
    }

    private boolean getJumping(LivingEntity e) {
        try { return jumpingField != null && jumpingField.getBoolean(e); }
        catch (Exception ex) { return false; }
    }

    // Povinné parametry auta.
    public abstract float getCustomStepHeight();
    public abstract float getMaxSpeedKmh();
    public abstract float getBaseAcceleration();
    public abstract float[] getGearRatios();
    public abstract float getBaseHandling();
    public abstract float getIdleRPM();
    public abstract float getMaxRPM();
    public abstract float getRedlineRPM();

    // Volitelné přepisy.
    public float getDownforceCoefficient()    { return 0.0f;    }
    public float getAeroDrag()               { return 0.00020f; }
    public float getBrakingDeceleration()    { return 0.05f;   }
    public float getHighSpeedSteerReduction(){ return 0.55f;   }
    public int   getShiftCooldownTicks()     { return 8;       }
    public void  applyShiftCooldown()        { this.shiftCooldown = getShiftCooldownTicks(); }

    // Rychlost a násobič škody pro náraz.
    public float getCrashDamageThresholdKmh() { return 40.0f; }
    public float getCrashDamageMultiplier() { return 0.15f; }

    public float   getRPM()              { return this.entityData.get(DATA_RPM); }
    public void    setRPM(float v)       { this.entityData.set(DATA_RPM, v); }
    public int     getCurrentGear()      { return this.entityData.get(DATA_GEAR); }
    public void    setCurrentGear(int v) { this.entityData.set(DATA_GEAR, v); }
    public float   getSpeedKmh()         { return Math.abs(this.entityData.get(DATA_SPEED)) * 72.0f; }
    public boolean isEngineOn()          { return this.entityData.get(DATA_ENGINE_ON); }
    public void    setEngineOn(boolean v){ this.entityData.set(DATA_ENGINE_ON, v); }

    @Override public LivingEntity getControllingPassenger() { return (this.getFirstPassenger() instanceof LivingEntity le) ? le : null; }
    @Override public boolean isPickable() { return !this.isRemoved(); }
    @Override public boolean isPushable() { return true; }

    @Override protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag n) {}
    @Override protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag n) {}
}