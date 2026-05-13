package cz.maxtechnik.dif.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class NuclearExplosionEntity extends Entity {

    // KONSTANTY ────────────────────────────────────────────────────────────
    private static final int BLOCKS_PER_TICK = 16_000;
    private static final float MAX_DESTROYABLE_RESISTANCE = 1500f;

    private static final double HOR_R_FULL = 40.0, HOR_R_TOTAL = 60.0;
    private static final double UP_R_FULL = 45.0, UP_R_TOTAL = 56.0;
    private static final double DOWN_R_FULL = 10.0, DOWN_R_TOTAL = 16.0;

    private static final double SHOCKWAVE_EXTRA = 48;
    private static final double SHOCKWAVE_R = HOR_R_TOTAL + SHOCKWAVE_EXTRA;
    private static final int SHOCKWAVE_HEIGHT_UP = 8, SHOCKWAVE_HEIGHT_DN = 2;
    private static final int SHOCKWAVE_BLOCKS_PER_TICK = 20_000;

    // Pre-computed
    private static final double HOR_FULL_SQ = HOR_R_FULL * HOR_R_FULL;
    private static final double HOR_TOTAL_SQ = HOR_R_TOTAL * HOR_R_TOTAL;
    private static final double UP_FULL_SQ = UP_R_FULL * UP_R_FULL, UP_TOTAL_SQ = UP_R_TOTAL * UP_R_TOTAL;
    private static final double DN_FULL_SQ = DOWN_R_FULL * DOWN_R_FULL, DN_TOTAL_SQ = DOWN_R_TOTAL * DOWN_R_TOTAL;
    private static final double SHOCKWAVE_R_SQ = SHOCKWAVE_R * SHOCKWAVE_R;
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private static final int PHASE_INIT = 0, PHASE_CRATER = 1, PHASE_SHOCKWAVE = 2, PHASE_DONE = 3;
    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(NuclearExplosionEntity.class, EntityDataSerializers.INT);

    // Shell iterátor (kráter)
    private int currentShell, maxShell, shellFace, shellU, shellV;
    // Shockwave iterátor (2D shell v XZ)
    private int swCurrentShell, swInnerShell, swOuterShell, swFace, swFacePos;

    private boolean entitiesHit = false;
    private int radius = (int) HOR_R_TOTAL;
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public NuclearExplosionEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) { b.define(DATA_PHASE, PHASE_INIT); }
    public void setRadius(int r) { this.radius = r; }
    private void setPhase(int p) { entityData.set(DATA_PHASE, p); }
    private int getPhase() { return entityData.get(DATA_PHASE); }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        switch (getPhase()) {
            case PHASE_INIT      -> { maxShell = (int) Math.ceil(HOR_R_TOTAL); currentShell = shellFace = shellU = shellV = 0; hitEntities(); setPhase(PHASE_CRATER); }
            case PHASE_CRATER    -> tickCrater();
            case PHASE_SHOCKWAVE -> tickShockwave();
            case PHASE_DONE      -> discard();
        }
    }

    // ── Kráter – shell-based od středu ven ───────────────────────────────

    private void tickCrater() {
        BlockPos center = blockPosition();
        int cx = center.getX(), cy = center.getY(), cz = center.getZ();
        int processed = 0;

        while (processed < BLOCKS_PER_TICK) {
            if (currentShell > maxShell) {
                swInnerShell = (int) Math.floor(HOR_R_TOTAL / 1.41421356);
                swOuterShell = (int) Math.ceil(SHOCKWAVE_R);
                swCurrentShell = swInnerShell;
                swFace = swFacePos = 0;
                setPhase(PHASE_SHOCKWAVE);
                return;
            }

            int r = currentShell;
            if (r == 0) {
                destroyAt(cx, cy, cz);
                currentShell = 1; shellFace = shellU = shellV = 0;
                processed++;
                continue;
            }

            int dx, dy, dz, uSize, vSize;
            switch (shellFace) {
                case 0 -> { dy = -r+shellU; dz = -r+shellV; dx = r;    uSize = 2*r+1;     vSize = 2*r+1; }
                case 1 -> { dy = -r+shellU; dz = -r+shellV; dx = -r;   uSize = 2*r+1;     vSize = 2*r+1; }
                case 2 -> { dx = -(r-1)+shellU; dz = -r+shellV; dy = r;  uSize = 2*(r-1)+1; vSize = 2*r+1; }
                case 3 -> { dx = -(r-1)+shellU; dz = -r+shellV; dy = -r; uSize = 2*(r-1)+1; vSize = 2*r+1; }
                case 4 -> { dx = -(r-1)+shellU; dy = -(r-1)+shellV; dz = r;  uSize = 2*(r-1)+1; vSize = 2*(r-1)+1; }
                case 5 -> { dx = -(r-1)+shellU; dy = -(r-1)+shellV; dz = -r; uSize = 2*(r-1)+1; vSize = 2*(r-1)+1; }
                default -> { dx = dy = dz = 0; uSize = vSize = 0; }
            }

            shellV++;
            if (shellV >= vSize) { shellV = 0; shellU++; if (shellU >= uSize) { shellU = 0; shellFace++; if (shellFace > 5) { shellFace = 0; currentShell++; } } }

            double verFullSq = dy >= 0 ? UP_FULL_SQ : DN_FULL_SQ;
            double verTotalSq = dy >= 0 ? UP_TOTAL_SQ : DN_TOTAL_SQ;
            double dxSq = (double) dx*dx, dySq = (double) dy*dy, dzSq = (double) dz*dz;

            double nTotal = dxSq/HOR_TOTAL_SQ + dySq/verTotalSq + dzSq/HOR_TOTAL_SQ;
            if (nTotal > 1.0) continue;

            double nFull = dxSq/HOR_FULL_SQ + dySq/verFullSq + dzSq/HOR_FULL_SQ;
            boolean destroy;
            if (nFull <= 1.0) {
                destroy = true;
            } else {
                double scaleSq = 1.0 / nTotal;
                double maxNFull = (dxSq*scaleSq)/HOR_FULL_SQ + (dySq*scaleSq)/verFullSq + (dzSq*scaleSq)/HOR_FULL_SQ;
                double t = Math.min(1.0, Math.max(0.0, (nFull - 1.0) / (maxNFull - 1.0)));
                destroy = (1.0 - t * 0.99) >= 1.0 || random.nextDouble() < (1.0 - t * 0.99);
            }

            if (destroy) destroyAt(cx + dx, cy + dy, cz + dz);
            processed++;
        }
    }

    // ── Rázová vlna – 2D shell od středu ven, skrz vše ──────────────────

    private void tickShockwave() {
        BlockPos center = blockPosition();
        int cx = center.getX(), cy = center.getY(), cz = center.getZ();
        int yMin = cy - SHOCKWAVE_HEIGHT_DN, yMax = cy + SHOCKWAVE_HEIGHT_UP;
        int colHeight = SHOCKWAVE_HEIGHT_UP + SHOCKWAVE_HEIGHT_DN + 1;
        int processed = 0;

        while (processed < SHOCKWAVE_BLOCKS_PER_TICK) {
            if (swCurrentShell > swOuterShell) { setPhase(PHASE_DONE); return; }

            int r = swCurrentShell, dx, dz, faceSize;
            switch (swFace) {
                case 0 -> { dx = r;  dz = -r + swFacePos; faceSize = 2*r+1; }
                case 1 -> { dx = -r; dz = -r + swFacePos; faceSize = 2*r+1; }
                case 2 -> { dz = r;  dx = -(r-1) + swFacePos; faceSize = Math.max(2*r-1, 0); }
                case 3 -> { dz = -r; dx = -(r-1) + swFacePos; faceSize = Math.max(2*r-1, 0); }
                default -> { dx = dz = 0; faceSize = 0; }
            }

            swFacePos++;
            if (swFacePos >= faceSize || faceSize == 0) { swFacePos = 0; swFace++; if (swFace > 3) { swFace = 0; swCurrentShell++; } }

            double distSq = (double) dx*dx + (double) dz*dz;
            if (distSq < HOR_TOTAL_SQ || distSq > SHOCKWAVE_R_SQ) continue;

            double chance = 1.0 - ((Math.sqrt(distSq) - HOR_R_TOTAL) / SHOCKWAVE_EXTRA) * 0.92;
            int bx = cx + dx, bz = cz + dz;

            for (int y = yMin; y <= yMax; y++) {
                mutablePos.set(bx, y, bz);
                if (!level().isLoaded(mutablePos)) continue;
                BlockState st = level().getBlockState(mutablePos);
                if (st.isAir()) continue;
                if (chance >= 1.0 || random.nextDouble() < chance)
                    level().setBlock(mutablePos, AIR, 2 | 16 | 64);
            }
            processed += colHeight;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void destroyAt(int x, int y, int z) {
        mutablePos.set(x, y, z);
        if (!level().isLoaded(mutablePos)) return;
        BlockState state = level().getBlockState(mutablePos);
        if (state.isAir()) return;
        float res = state.getBlock().getExplosionResistance();
        if (res >= 0 && res <= MAX_DESTROYABLE_RESISTANCE)
            level().setBlock(mutablePos, AIR, 2 | 16 | 64);
    }

    private void hitEntities() {
        if (entitiesHit) return;
        entitiesHit = true;
        AABB area = new AABB(getX()-SHOCKWAVE_R, getY()-SHOCKWAVE_R, getZ()-SHOCKWAVE_R,
                getX()+SHOCKWAVE_R, getY()+SHOCKWAVE_R, getZ()+SHOCKWAVE_R);
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, area)) {
            double dist = e.distanceTo(this);
            if (dist < HOR_R_FULL) {
                e.hurt(level().damageSources().explosion(this, this), Float.MAX_VALUE);
            } else if (dist < HOR_R_TOTAL * 1.5) {
                e.hurt(level().damageSources().explosion(this, this), (float)(100.0 * (1.0 - (dist - HOR_R_FULL) / HOR_R_TOTAL)));
                e.setDeltaMovement(e.position().subtract(position()).normalize().scale(3.0));
                e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 1));
            } else if (dist < SHOCKWAVE_R) {
                e.hurt(level().damageSources().explosion(this, this), (float)(30.0 * (1.0 - (dist - HOR_R_TOTAL) / SHOCKWAVE_EXTRA)));
                e.setDeltaMovement(e.position().subtract(position()).normalize().scale(2.0));
                e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 150, 1));
            } else {
                e.setDeltaMovement(e.position().subtract(position()).normalize().scale(1.5));
                e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            }
        }
    }

    // ── NBT ───────────────────────────────────────────────────────────────

    @Override
    protected void readAdditionalSaveData(CompoundTag t) {
        radius = t.getInt("Radius"); entitiesHit = t.getBoolean("EntitiesHit");
        currentShell = t.getInt("CurrentShell"); maxShell = t.getInt("MaxShell");
        shellFace = t.getInt("ShellFace"); shellU = t.getInt("ShellU"); shellV = t.getInt("ShellV");
        swCurrentShell = t.getInt("SwCurrentShell"); swInnerShell = t.getInt("SwInnerShell");
        swOuterShell = t.getInt("SwOuterShell"); swFace = t.getInt("SwFace"); swFacePos = t.getInt("SwFacePos");
        setPhase(t.getInt("Phase"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag t) {
        t.putInt("Radius", radius); t.putInt("Phase", getPhase()); t.putBoolean("EntitiesHit", entitiesHit);
        t.putInt("CurrentShell", currentShell); t.putInt("MaxShell", maxShell);
        t.putInt("ShellFace", shellFace); t.putInt("ShellU", shellU); t.putInt("ShellV", shellV);
        t.putInt("SwCurrentShell", swCurrentShell); t.putInt("SwInnerShell", swInnerShell);
        t.putInt("SwOuterShell", swOuterShell); t.putInt("SwFace", swFace); t.putInt("SwFacePos", swFacePos);
    }

    @Override public boolean isAttackable() { return false; }
    @Override public boolean isPickable()   { return false; }
}