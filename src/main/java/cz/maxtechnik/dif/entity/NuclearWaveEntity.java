package cz.maxtechnik.dif.entity;

import cz.maxtechnik.dif.init.other.DifModParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class NuclearWaveEntity extends Entity {

    // ── Konstanty ─────────────────────────────────────────────────────────
    private static final double WAVE_SPEED        = 3.0;
    private static final int    WAVE_DENSITY      = 64;
    private static final int    WAVE_MAX_RADIUS   = 256;

    private static final EntityDataAccessor<Integer> DATA_TICK =
            SynchedEntityData.defineId(NuclearWaveEntity.class, EntityDataSerializers.INT);

    private double waveRadius = 0;

    public NuclearWaveEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TICK, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel sl)) return;

        waveRadius += WAVE_SPEED;

        if (waveRadius > WAVE_MAX_RADIUS) {
            this.discard();
            return;
        }

        double ox = getX(), oy = getY(), oz = getZ();

        for (int i = 0; i < WAVE_DENSITY; i++) {
            double angle = (Math.PI * 2 / WAVE_DENSITY) * i;
            double px = ox + Math.cos(angle) * waveRadius;
            double pz = oz + Math.sin(angle) * waveRadius;
            sl.sendParticles(DifModParticles.HUGE_SMOKE.get(),
                    px, oy, pz,
                    1, 0, 0, 0, 0);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        waveRadius = tag.getDouble("WaveRadius");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("WaveRadius", waveRadius);
    }

    @Override public boolean isAttackable() { return false; }
    @Override public boolean isPickable()   { return false; }
}