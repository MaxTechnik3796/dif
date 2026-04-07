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

/**
 * Základ pro všechna auta v modu DIF.
 *
 * Fyzikální model:
 *  - velocity  (bl/tick) = pohyb podél osy auta
 *  - Tah: throttle × baseAcceleration × (gearRatio / 1stGearRatio) × torqueCurve(RPM)
 *  - Brzdění: aktivní (S) + aerodynamický odpor (v²) + valivý odpor
 *  - Zatáčení: handling × ovlivněno přítlakem (downforce) a typem povrchu
 *
 * Povrchy: NORMAL, SOUL_SAND (pomalé), ICE (kluže), CARPET (mírně pomalejší)
 *
 * Když hráč vyleze: motor se vypne, minimální fyzika (pouze setrvačnost + gravitace).
 *
 * OPRAVY v této verzi:
 *  - hasImpulse = true každý tick → server posílá pozici každý tick → žádné 30s zpoždění
 *  - removePassenger(): velocity a deltaMovement se vynulují → formule neodlétá
 *  - Nárazová fyzika: pokud entita narazí do zdi při vysoké rychlosti, řidič dostane damage
 *  - setMaxUpStep() voláno v konstruktoru → auto přejede 3/4 bloku
 */
public abstract class BaseCarEntity extends Entity {

    // =========================================================
    //  SYNCED DATA
    // =========================================================
    protected static final EntityDataAccessor<Float>   DATA_RPM       =
            SynchedEntityData.defineId(BaseCarEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> DATA_GEAR      =
            SynchedEntityData.defineId(BaseCarEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float>   DATA_SPEED     =
            SynchedEntityData.defineId(BaseCarEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Boolean> DATA_ENGINE_ON =
            SynchedEntityData.defineId(BaseCarEntity.class, EntityDataSerializers.BOOLEAN);

    //  PHYSICS STATE  (jen na serveru/single-player)
    protected float velocity      = 0.0f;  // bl/tick, kladné = vpřed
    protected int   shiftCooldown = 0;     // ticků do dalšího přeřazení

    /**
     * Rychlost v minulém ticku — používá se pro detekci nárazu.
     * Pokud se velocity prudce sníží (náraz do zdi), hráč dostane damage.
     */
    private float prevVelocity = 0.0f;

    /**
     * Cooldown po nárazu — zabraňuje tomu, aby hráč dostával damage každý tick
     * při klouzání po zdi (jen jednou za X ticků).
     */
    private int crashDamageCooldown = 0;

    //  SURFACE TYPES
    public enum SurfaceType { NORMAL, SOUL_SAND, ICE, CARPET }

    //  REFLECTION – přístup k "jumping" (mezerník/handbrake)
    private static Field jumpingField;
    static {
        try {
            try   { jumpingField = LivingEntity.class.getDeclaredField("f_20899_"); }
            catch (NoSuchFieldException e) {
                jumpingField = LivingEntity.class.getDeclaredField("jumping"); }
            jumpingField.setAccessible(true);
        } catch (Exception ignored) {}
    }

    //  CONSTRUCTOR
    public BaseCarEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
        // Výchozí výška přeskoku — podtřídy mohou přepsat přes getCustomStepHeight()
        // Nastavujeme zde, aby bylo funkční ihned po vytvoření entity.
        this.setMaxUpStep(getCustomStepHeight());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_RPM,       getIdleRPM());
        this.entityData.define(DATA_GEAR,      1);
        this.entityData.define(DATA_SPEED,     0.0f);
        this.entityData.define(DATA_ENGINE_ON, false);
    }

    //  NASEDNUTÍ / VYSTOUPENÍ
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
        if (!this.level().isClientSide && !this.isVehicle()) {
            setEngineOn(false);

            // -------------------------------------------------------
            // OPRAVA ZMIZENÍ:
            // Při vystoupení z jedoucího auta server okamžitě zastaví
            // veškerý pohyb. Bez toho by entita odletěla mimo "oblast
            // zájmu" klienta a znovu se objevila až po desítkách sekund.
            // -------------------------------------------------------
            velocity = 0.0f;
            prevVelocity = 0.0f;
            this.setDeltaMovement(Vec3.ZERO);

            // Vynutíme okamžité odeslání pozice klientům.
            this.hasImpulse = true;
        }
    }

    //  POŠKOZENÍ (levé tlačítko = zničení)
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

        // Získáme pasažéra přímo
        LivingEntity driver = this.getControllingPassenger();

        // Uložíme velocity PŘED fyzickým krokem — pro detekci nárazu
        prevVelocity = velocity;

        if (this.isVehicle() && driver != null) {
            simulateActivePhysics(driver);
        } else {
            simulateIdlePhysics();
        }

        // -----------------------------------------------------------
        // OPRAVA SYNCHRONIZACE:
        // hasImpulse = true říká ServerEntity, že má ihned odeslat
        // aktuální pozici všem klientům v dosahu — místo čekání na
        // práh pohybu. Bez toho server posílá pozici jen každých N
        // ticků nebo při velkém pohybu, což způsobuje 30s zpoždění.
        // -----------------------------------------------------------
        this.hasImpulse = true;

        // Synchronizace dat pro HUD
        this.entityData.set(DATA_SPEED, velocity);
    }

    //  IDLE FYZIKA  – minimální výpočty, žádný řidič
    private void simulateIdlePhysics() {
        // Rychlé dojíždění
        velocity *= 0.88f;
        if (Math.abs(velocity) < 0.0005f) velocity = 0.0f;

        // RPM klesá na nulu (motor je vypnutý)
        setRPM(Math.max(0.0f, getRPM() - getMaxRPM() * 0.04f));

        // Gravitace + setrvačnost
        double yawRad = Math.toRadians(this.getYRot());
        double newYMot = this.onGround() ? -0.05 : this.getDeltaMovement().y - 0.04;
        newYMot = Math.max(newYMot, -1.5);

        this.setDeltaMovement(new Vec3(
                -Math.sin(yawRad) * velocity,
                newYMot,
                Math.cos(yawRad) * velocity
        ));
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    //  AKTIVNÍ FYZIKA  – řidič sedí v autě
    protected void simulateActivePhysics(LivingEntity driver) {
        float throttle   = driver.zza;
        float steerInput = -driver.xxa;
        boolean handbrake = getJumping(driver);

        SurfaceType surface = detectSurface();

        float surfaceSpeedMult = getSurfaceSpeedMult(surface);
        float lateralGrip      = getSurfaceLateralGrip(surface);
        float rollingRes       = getSurfaceRollingResistance(surface);
        float maxSpeedBT       = (getMaxSpeedKmh() * surfaceSpeedMult) / 72.0f;

        // ---- RPM ----
        float rpmConv   = computeRPMConversionFactor();
        float gearRatio = getGearRatios()[getCurrentGear() - 1];
        float targetRPM = Math.abs(velocity) * gearRatio * rpmConv;

        if (Math.abs(velocity) < 0.05f && throttle > 0) {
            targetRPM = Math.max(targetRPM,
                    getIdleRPM() + (getMaxRPM() - getIdleRPM()) * throttle * 0.25f);
        }
        setRPM(Math.max(getIdleRPM(), Math.min(getMaxRPM(), targetRPM)));

        // ---- TORQUE CUT při přeřazení ----
        float torqueFactor = (shiftCooldown > 0)
                ? (float) shiftCooldown / getShiftCooldownTicks()
                : 1.0f;

        if (getRPM() >= getMaxRPM() * 0.999f) torqueFactor = 0.0f;

        // ---- TAHOVÁ SÍLA ----
        float thrust = 0.0f;
        if (throttle > 0.0f) {
            float rpmPowerFactor = computeTorqueCurve(getRPM());
            thrust = throttle
                    * getBaseAcceleration()
                    * (gearRatio / getGearRatios()[0])
                    * rpmPowerFactor
                    * torqueFactor;
        } else if (throttle < 0.0f) {
            thrust = throttle * getBrakingDeceleration();
        }

        // ---- RUČNÍ BRZDA ----
        if (handbrake) {
            velocity    *= 0.87f;
            lateralGrip *= 0.22f;
        }

        // ---- AERODYNAMICKÝ ODPOR (v²) ----
        float drag = velocity * Math.abs(velocity) * getAeroDrag();

        // ---- VALIVÝ ODPOR ----
        float rolling = velocity * rollingRes;

        velocity += thrust - drag - rolling;

        // ---- SOUL SAND – tvrdý limit rychlosti ----
        if (surface == SurfaceType.SOUL_SAND) {
            float ssCap = 22.0f / 72.0f;
            if (Math.abs(velocity) > ssCap) {
                velocity = velocity * 0.82f + Math.signum(velocity) * ssCap * 0.18f;
            }
        }

        // ---- CLAMP rychlosti ----
        velocity = Math.max(-0.25f, Math.min(maxSpeedBT, velocity));

        // ---- PŘÍTLAK (downforce) ----
        float downforceMult = 1.0f;
        if (maxSpeedBT > 0.001f) {
            float speedRatio = velocity / maxSpeedBT;
            downforceMult = 1.0f + getDownforceCoefficient() * speedRatio * speedRatio;
        }

        float effectiveHandling = getBaseHandling() * downforceMult * lateralGrip;

        // ---- ZATÁČENÍ ----
        if (Math.abs(velocity) > 0.015f) {
            float speedNorm   = Math.abs(velocity) / maxSpeedBT;
            float speedFactor = 1.0f - speedNorm * getHighSpeedSteerReduction();
            float turnRate    = effectiveHandling * steerInput * Math.max(0.05f, speedFactor);
            this.setYRot(this.getYRot() + turnRate);
        }

        // ---- POHYB ----
        double yawRad  = Math.toRadians(this.getYRot());
        double newYMot = this.onGround() ? -0.05 : this.getDeltaMovement().y - 0.04;
        newYMot = Math.max(newYMot, -1.5);

        Vec3 intendedMotion = new Vec3(
                -Math.sin(yawRad) * velocity,
                newYMot,
                Math.cos(yawRad) * velocity
        );
        this.setDeltaMovement(intendedMotion);

        // Zapamatujeme si horizontální pohyb PŘED move() pro detekci nárazu
        double preX = this.getX();
        double preZ = this.getZ();

        this.move(MoverType.SELF, this.getDeltaMovement());

        // -----------------------------------------------------------
        // DETEKCE NÁRAZU
        //
        // Inspirováno Automobility: porovnáváme skutečný pohyb
        // s zamýšleným. Pokud se entita nehýbala, ačkoli měla,
        // narazila do zdi. Škoda závisí na rychlosti dopadu.
        //
        // Schéma škody (podobně jako Automobility):
        //   - pod getCrashDamageThreshold() km/h → žádná škoda
        //   - lineárně roste od prahu do maxRPM ekvivalentu
        //   - damage = impactSpeed * getCrashDamageMultiplier()
        // -----------------------------------------------------------
        if (!this.level().isClientSide && crashDamageCooldown == 0) {
            double actualDX = this.getX() - preX;
            double actualDZ = this.getZ() - preZ;
            double expectedDX = intendedMotion.x;
            double expectedDZ = intendedMotion.z;

            // Jak moc jsme se "zastavili" ve srovnání s tím, co jsme chtěli
            double blockedX = expectedDX - actualDX;
            double blockedZ = expectedDZ - actualDZ;
            double blockedSpeed = Math.sqrt(blockedX * blockedX + blockedZ * blockedZ); // bl/tick

            float blockedKmh = (float)(blockedSpeed * 72.0);

            if (blockedKmh > getCrashDamageThresholdKmh()) {
                float damage = (blockedKmh - getCrashDamageThresholdKmh())
                        * getCrashDamageMultiplier();

                driver.hurt(
                        this.level().damageSources().generic(),
                        damage
                );

                // Prudce sníž rychlost — náraz zastaví auto
                velocity *= (1.0f - Math.min(1.0f, blockedKmh / getMaxSpeedKmh()));

                // Cooldown 10 ticků (0.5s) — zabraňuje spamu škody při kluzu po zdi
                crashDamageCooldown = 10;
            }
        }
    }

    //  DETEKCE POVRCHU
    protected SurfaceType detectSurface() {
        BlockPos feet  = this.blockPosition();
        BlockPos below = feet.below();

        Block atFeet  = this.level().getBlockState(feet).getBlock();
        Block atBelow = this.level().getBlockState(below).getBlock();

        if (atFeet instanceof CarpetBlock || atBelow instanceof CarpetBlock)
            return SurfaceType.CARPET;

        if (isIceBlock(atBelow) || isIceBlock(atFeet))
            return SurfaceType.ICE;

        if (atBelow == Blocks.SOUL_SAND || atBelow == Blocks.SOUL_SOIL
                || atFeet  == Blocks.SOUL_SAND || atFeet  == Blocks.SOUL_SOIL)
            return SurfaceType.SOUL_SAND;

        return SurfaceType.NORMAL;
    }

    private static boolean isIceBlock(Block b) {
        return b == Blocks.ICE
                || b == Blocks.PACKED_ICE
                || b == Blocks.BLUE_ICE
                || b == Blocks.FROSTED_ICE;
    }

    //  MODIFIKÁTORY POVRCHU
    protected float getSurfaceSpeedMult(SurfaceType s) {
        return switch (s) {
            case SOUL_SAND -> 0.35f;
            case CARPET    -> 0.88f;
            default        -> 1.0f;
        };
    }

    protected float getSurfaceLateralGrip(SurfaceType s) {
        return switch (s) {
            case ICE       -> 0.10f;
            case SOUL_SAND -> 0.50f;
            case CARPET    -> 0.75f;
            default        -> 1.0f;
        };
    }

    protected float getSurfaceRollingResistance(SurfaceType s) {
        return switch (s) {
            case SOUL_SAND -> 0.025f;
            case ICE       -> 0.0004f;
            case CARPET    -> 0.006f;
            default        -> 0.002f;
        };
    }

    //  MOMENTOVÁ KŘIVKA
    private float computeTorqueCurve(float rpm) {
        float norm = rpm / getMaxRPM();
        float peak = 0.75f;
        float f    = (float)(-4.0 * Math.pow(norm - peak, 2) + 1.0);
        return Math.max(0.25f, Math.min(1.0f, f));
    }

    //  RPM PŘEPOČTOVÝ FAKTOR
    private float computeRPMConversionFactor() {
        float[] ratios  = getGearRatios();
        float topRatio  = ratios[ratios.length - 1];
        float maxVelBT  = getMaxSpeedKmh() / 72.0f;
        return getMaxRPM() / (maxVelBT * topRatio);
    }

    //  HELPER – mezerník přes reflexi
    private boolean getJumping(LivingEntity e) {
        try { return jumpingField != null && jumpingField.getBoolean(e); }
        catch (Exception ex) { return false; }
    }

    //  ABSTRAKTNÍ PARAMETRY AUTA

    /**
     * Výška přeskoku obrubníku (bl).
     * Nastavuje se přes setMaxUpStep() v konstruktoru.
     */
    public abstract float getCustomStepHeight();

    public abstract float getMaxSpeedKmh();
    public abstract float getBaseAcceleration();
    public abstract float[] getGearRatios();
    public abstract float getBaseHandling();
    public abstract float getIdleRPM();
    public abstract float getMaxRPM();
    public abstract float getRedlineRPM();

    //  VOLITELNÉ PŘEPSÁNÍ

    public float getDownforceCoefficient()    { return 0.0f;    }
    public float getAeroDrag()               { return 0.00020f; }
    public float getBrakingDeceleration()    { return 0.05f;   }
    public float getHighSpeedSteerReduction(){ return 0.55f;   }
    public int   getShiftCooldownTicks()     { return 8;       }
    public void  applyShiftCooldown()        { this.shiftCooldown = getShiftCooldownTicks(); }

    /**
     * Minimální rychlost nárazu (km/h) pro způsobení škody řidiči.
     * Pod touto hodnotou náraz nezpůsobí žádnou škodu.
     * Výchozí: 40 km/h — přepsatelné v podtřídách.
     */
    public float getCrashDamageThresholdKmh() { return 40.0f; }

    /**
     * Koeficient škody při nárazu.
     * damage = (impactSpeed_kmh - threshold) * multiplier
     *
     * Výchozí: 0.15 → při 100 km/h nárazu (60 nad prahem) = 9 HP
     * Přepsatelné v podtřídách.
     */
    public float getCrashDamageMultiplier() { return 0.15f; }

    //  GETTERY / SETTERY
    public float   getRPM()              { return this.entityData.get(DATA_RPM); }
    public void    setRPM(float v)       { this.entityData.set(DATA_RPM, v); }

    public int     getCurrentGear()      { return this.entityData.get(DATA_GEAR); }
    public void    setCurrentGear(int v) { this.entityData.set(DATA_GEAR, v); }

    public float   getSpeedKmh()         { return Math.abs(this.entityData.get(DATA_SPEED)) * 72.0f; }

    public boolean isEngineOn()          { return this.entityData.get(DATA_ENGINE_ON); }
    public void    setEngineOn(boolean v){ this.entityData.set(DATA_ENGINE_ON, v); }

    @Override
    public LivingEntity getControllingPassenger() {
        Entity first = this.getFirstPassenger();
        return (first instanceof LivingEntity le) ? le : null;
    }

    @Override public boolean isPickable() { return !this.isRemoved(); }
    @Override public boolean isPushable() { return true; }

    @Override protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag n) {}
    @Override protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag n) {}
}