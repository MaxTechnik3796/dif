// ── NuclearMushroomEntity.java ────────────────────────────────────────────
package cz.maxtechnik.dif.entity;

import cz.maxtechnik.dif.init.other.DifModParticles;
import cz.maxtechnik.dif.init.other.DifModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;

public class NuclearMushroomEntity extends Entity {

    // ── Fireball mrak ─────────────────────────────────────────────────────
    private static final int    FIREBALL_LIFETIME   = 300;
    private static final double FIREBALL_RADIUS     = 12.0;
    private static final int    FIREBALL_COUNT      = 80;
    private static final double FIREBALL_RISE_SPEED = 0.4;

    // ── Huge smoke mrak ───────────────────────────────────────────────────
    private static final int    SMOKE_LIFETIME      = 500;
    private static final double SMOKE_RADIUS        = 8.0;
    private static final int    SMOKE_COUNT         = 60;
    private static final double SMOKE_RISE_SPEED    = 0.4;

    // ── Sloup ─────────────────────────────────────────────────────────────
    private static final int    STEM_INTERVAL       = 10;
    private static final int    STEM_STOP_BEFORE    = 40;

    // ── Paprsky ───────────────────────────────────────────────────────────
    private static final int    RAY_COUNT           = 12;
    private static final double RAY_SPEED           = 4.0;
    private static final int    RAY_STOP_BEFORE     = 40;

    // ─────────────────────────────────────────────────────────────────────

    private static final EntityDataAccessor<Integer> DATA_TICK =
            SynchedEntityData.defineId(NuclearMushroomEntity.class, EntityDataSerializers.INT);

    public NuclearMushroomEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TICK, 0);
    }

    private void setT(int t) { entityData.set(DATA_TICK, t); }
    private int  getT()      { return entityData.get(DATA_TICK); }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel sl)) return;

        int t = getT();
        double ox = getX(), oy = getY(), oz = getZ();

        // ── Tick 0: spawn mraku + vlny ────────────────────────────────────
        if (t == 0) {
            spawnFireballCloud(sl, ox, oy, oz);
            spawnSmokeCloud(sl, ox, oy, oz);

            // Spawn vlnové entity
            NuclearWaveEntity wave = new NuclearWaveEntity(
                    DifModEntities.NUCLEAR_WAVE.get(), level());
            wave.setPos(ox, oy + 2, oz);
            level().addFreshEntity(wave);
        }

        // ── Paprsky ───────────────────────────────────────────────────────
        if (t < FIREBALL_LIFETIME - RAY_STOP_BEFORE) {
            spawnRays(sl, ox, oy + 2, oz);
        }

        // ── Sloup ─────────────────────────────────────────────────────────
        if (t % STEM_INTERVAL == 0 && t < SMOKE_LIFETIME - STEM_STOP_BEFORE) {
            sl.sendParticles(DifModParticles.HUGE_SMOKE.get(),
                    ox + (random.nextDouble() - 0.5) * 2,
                    oy,
                    oz + (random.nextDouble() - 0.5) * 2,
                    1, 0, 0.05, 0, 1.0);
        }

        setT(t + 1);
        if (t >= SMOKE_LIFETIME) this.discard();
    }

    // ── Spawn metody ──────────────────────────────────────────────────────

    private void spawnFireballCloud(ServerLevel sl, double ox, double oy, double oz) {
        for (int i = 0; i < FIREBALL_COUNT; i++) {
            double[] pos = randomInSphere(FIREBALL_RADIUS);
            sl.sendParticles(DifModParticles.FIREBALL.get(),
                    ox + pos[0], oy + pos[1], oz + pos[2],
                    1, 0, FIREBALL_RISE_SPEED, 0, 1.0);
        }
    }

    private void spawnSmokeCloud(ServerLevel sl, double ox, double oy, double oz) {
        for (int i = 0; i < SMOKE_COUNT; i++) {
            double[] pos = randomInSphere(SMOKE_RADIUS);
            sl.sendParticles(DifModParticles.HUGE_SMOKE.get(),
                    ox + pos[0], oy + pos[1], oz + pos[2],
                    1, 0, SMOKE_RISE_SPEED, 0, 1.0);
        }
    }

    private void spawnRays(ServerLevel sl, double ox, double oy, double oz) {
        for (int i = 0; i < RAY_COUNT; i++) {
            double angle = (Math.PI * 2 / RAY_COUNT) * i;
            sl.sendParticles(ParticleTypes.FLASH,
                    ox, oy, oz,
                    1,
                    Math.cos(angle) * RAY_SPEED,
                    0,
                    Math.sin(angle) * RAY_SPEED,
                    1.0);
        }
    }

    private double[] randomInSphere(double radius) {
        double u     = random.nextDouble();
        double v     = random.nextDouble();
        double theta = 2 * Math.PI * u;
        double phi   = Math.acos(2 * v - 1);
        double r     = radius * Math.cbrt(random.nextDouble());
        return new double[]{
                r * Math.sin(phi) * Math.cos(theta),
                r * Math.sin(phi) * Math.sin(theta),
                r * Math.cos(phi)
        };
    }

    // ── NBT ───────────────────────────────────────────────────────────────

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setT(tag.getInt("T"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("T", getT());
    }

    @Override public boolean isAttackable() { return false; }
    @Override public boolean isPickable()   { return false; }
}