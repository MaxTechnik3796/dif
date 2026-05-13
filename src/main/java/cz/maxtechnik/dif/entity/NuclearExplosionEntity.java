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

    // LADITELNÉ KONSTANTY ──────────────────────────────────────────────

    //Zničené blocky za tick
    private static final int BLOCKS_PER_TICK = 16_000;

    //Resistance co to neničí
    private static final float MAX_DESTROYABLE_RESISTANCE = 1500f;

    // Horizontální poloměr (do stran, x/z jsou stejné)
    private static final double HOR_R_FULL  = 40.0;
    private static final double HOR_R_TOTAL = 60.0;

    // Vertikální poloměr – nahoru
    private static final double UP_R_FULL  = 45.0;
    private static final double UP_R_TOTAL = 56.0;

    // Vertikální poloměr – dolů
    private static final double DOWN_R_FULL  = 10.0;
    private static final double DOWN_R_TOTAL = 16.0;

    // Rázová vlna ───────────────────────────────────────────────────────
    //Extra dosah rázové vlny
    private static final double SHOCKWAVE_EXTRA = 48;
    //Celkový max horizontální dosah
    private static final double SHOCKWAVE_R = HOR_R_TOTAL + SHOCKWAVE_EXTRA;
    //Výška sloupce
    private static final int SHOCKWAVE_HEIGHT_UP = 12;
    //Hloubka
    private static final int SHOCKWAVE_HEIGHT_DN = 2;
    //Bloky za tick
    private static final int SHOCKWAVE_BLOCKS_PER_TICK = 20_000;
    //Podíl solidních bloků ve sloupci, nad kterým se rázová vlna zastaví
    private static final double SHOCKWAVE_WALL_THRESHOLD = 0.55;


    private static final int PHASE_INIT      = 0;
    private static final int PHASE_CRATER    = 1;
    private static final int PHASE_SHOCKWAVE = 2;
    private static final int PHASE_DONE      = 3;

    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(NuclearExplosionEntity.class, EntityDataSerializers.INT);

    // ── Shell iterátor (šíření od středu ven) ────────────────────────────
    private int currentShell;
    private int maxShell;
    private int shellFace;
    private int shellU, shellV;

    // ── Shockwave iterátor (2D shell – expandující prstence v XZ) ───────
    private int swCurrentShell;
    private int swInnerShell;
    private int swOuterShell;
    private int swFace;
    private int swFacePos;

    private boolean entitiesHit = false;
    private int radius = (int) HOR_R_TOTAL;

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public NuclearExplosionEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_PHASE, PHASE_INIT);
    }

    public void setRadius(int r) { this.radius = r; }
    private void setPhase(int p) { this.entityData.set(DATA_PHASE, p); }
    private int  getPhase()      { return this.entityData.get(DATA_PHASE); }

    // ── Tick ──────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        switch (getPhase()) {
            case PHASE_INIT      -> initExplosion();
            case PHASE_CRATER    -> tickCrater();
            case PHASE_SHOCKWAVE -> tickShockwave();
            case PHASE_DONE      -> this.discard();
        }
    }

    // ── Fáze 0: Init ─────────────────────────────────────────────────────

    private void initExplosion() {
        maxShell = (int) Math.ceil(HOR_R_TOTAL);
        currentShell = 0;
        shellFace = 0;
        shellU = 0;
        shellV = 0;

        hitEntities();
        setPhase(PHASE_CRATER);
    }

    // ── Fáze 1: Kráter – shell-based od středu ven ──────────────────────

    private void tickCrater() {
        BlockPos center = this.blockPosition();
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        final double horFullSq  = HOR_R_FULL * HOR_R_FULL;
        final double horTotalSq = HOR_R_TOTAL * HOR_R_TOTAL;
        final double upFullSq   = UP_R_FULL * UP_R_FULL;
        final double upTotalSq  = UP_R_TOTAL * UP_R_TOTAL;
        final double dnFullSq   = DOWN_R_FULL * DOWN_R_FULL;
        final double dnTotalSq  = DOWN_R_TOTAL * DOWN_R_TOTAL;

        int processed = 0;

        while (processed < BLOCKS_PER_TICK) {
            if (currentShell > maxShell) {
                // Kráter hotov → shockwave
                // Začínáme od HOR_R_TOTAL/√2 aby kruhový filtr zachytil i diagonály
                // (Chebyshev shell r má rohy na eukl. vzdálenosti r*√2)
                swInnerShell = (int) Math.floor(HOR_R_TOTAL / Math.sqrt(2.0));
                swOuterShell = (int) Math.ceil(SHOCKWAVE_R);
                swCurrentShell = swInnerShell;
                swFace = 0;
                swFacePos = 0;
                setPhase(PHASE_SHOCKWAVE);
                return;
            }

            int r = currentShell;

            // Shell 0 = střed
            if (r == 0) {
                mutablePos.set(cx, cy, cz);
                if (level().isLoaded(mutablePos)) destroyBlock(mutablePos);
                currentShell = 1;
                shellFace = 0;
                shellU = 0;
                shellV = 0;
                processed++;
                continue;
            }

            // Získáme dx, dy, dz z pozice na face shellu
            int dx, dy, dz;
            int uSize, vSize;

            switch (shellFace) {
                case 0: // +X: dx=r
                    dy = -r + shellU; dz = -r + shellV; dx = r;
                    uSize = 2*r+1; vSize = 2*r+1;
                    break;
                case 1: // -X: dx=-r
                    dy = -r + shellU; dz = -r + shellV; dx = -r;
                    uSize = 2*r+1; vSize = 2*r+1;
                    break;
                case 2: // +Y: dy=r (bez rohů X)
                    dx = -(r-1) + shellU; dz = -r + shellV; dy = r;
                    uSize = 2*(r-1)+1; vSize = 2*r+1;
                    break;
                case 3: // -Y: dy=-r
                    dx = -(r-1) + shellU; dz = -r + shellV; dy = -r;
                    uSize = 2*(r-1)+1; vSize = 2*r+1;
                    break;
                case 4: // +Z: dz=r (bez rohů X,Y)
                    dx = -(r-1) + shellU; dy = -(r-1) + shellV; dz = r;
                    uSize = 2*(r-1)+1; vSize = 2*(r-1)+1;
                    break;
                case 5: // -Z: dz=-r
                    dx = -(r-1) + shellU; dy = -(r-1) + shellV; dz = -r;
                    uSize = 2*(r-1)+1; vSize = 2*(r-1)+1;
                    break;
                default:
                    dx = dy = dz = 0; uSize = vSize = 0;
            }

            // Posun iterátoru
            shellV++;
            if (shellV >= vSize) {
                shellV = 0;
                shellU++;
                if (shellU >= uSize) {
                    shellU = 0;
                    shellFace++;
                    if (shellFace > 5) {
                        shellFace = 0;
                        currentShell++;
                    }
                }
            }

            // ── Elipsoidní test ──
            double verFullSq  = dy >= 0 ? upFullSq  : dnFullSq;
            double verTotalSq = dy >= 0 ? upTotalSq : dnTotalSq;

            double dxSq = (double) dx * dx;
            double dySq = (double) dy * dy;
            double dzSq = (double) dz * dz;

            double nTotal = dxSq / horTotalSq + dySq / verTotalSq + dzSq / horTotalSq;
            if (nTotal > 1.0) continue;

            double nFull = dxSq / horFullSq + dySq / verFullSq + dzSq / horFullSq;

            double chance;
            if (nFull <= 1.0) {
                chance = 1.0;
            } else {
                double scale = 1.0 / Math.sqrt(nTotal);
                double bxSq = dxSq * scale * scale;
                double bySq = dySq * scale * scale;
                double bzSq = dzSq * scale * scale;
                double maxNFull = bxSq / horFullSq + bySq / verFullSq + bzSq / horFullSq;
                double t = (nFull - 1.0) / (maxNFull - 1.0);
                if (t < 0.0) t = 0.0;
                else if (t > 1.0) t = 1.0;
                chance = 1.0 - t * 0.99;
            }

            if (chance >= 1.0 || random.nextDouble() < chance) {
                mutablePos.set(cx + dx, cy + dy, cz + dz);
                if (level().isLoaded(mutablePos)) destroyBlock(mutablePos);
            }

            processed++;
        }
    }

    // ── Fáze 2: Rázová vlna – 2D shell iterace od středu ven ─────────────

    private void tickShockwave() {
        BlockPos center = this.blockPosition();
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        final double innerRSq = HOR_R_TOTAL * HOR_R_TOTAL;
        final double outerRSq = SHOCKWAVE_R * SHOCKWAVE_R;
        final int colHeight = SHOCKWAVE_HEIGHT_UP + SHOCKWAVE_HEIGHT_DN + 1;

        int processed = 0;

        while (processed < SHOCKWAVE_BLOCKS_PER_TICK) {
            if (swCurrentShell > swOuterShell) {
                setPhase(PHASE_DONE);
                return;
            }

            int r = swCurrentShell;

            // Získáme dx, dz z pozice na 2D face
            int dx, dz;
            int faceSize;

            switch (swFace) {
                case 0: // +X: dx=r, dz ∈ [-r, r]
                    dx = r; dz = -r + swFacePos;
                    faceSize = 2 * r + 1;
                    break;
                case 1: // -X: dx=-r, dz ∈ [-r, r]
                    dx = -r; dz = -r + swFacePos;
                    faceSize = 2 * r + 1;
                    break;
                case 2: // +Z: dz=r, dx ∈ [-(r-1), r-1]
                    dz = r; dx = -(r - 1) + swFacePos;
                    faceSize = Math.max(2 * r - 1, 0);
                    break;
                case 3: // -Z: dz=-r, dx ∈ [-(r-1), r-1]
                    dz = -r; dx = -(r - 1) + swFacePos;
                    faceSize = Math.max(2 * r - 1, 0);
                    break;
                default:
                    dx = dz = 0; faceSize = 0;
            }

            // Posun iterátoru
            swFacePos++;
            if (swFacePos >= faceSize || faceSize == 0) {
                swFacePos = 0;
                swFace++;
                if (swFace > 3) {
                    swFace = 0;
                    swCurrentShell++;
                }
            }

            // Kruhový test – musí být v kruhovém prstenci
            double distSq = (double) dx * dx + (double) dz * dz;
            if (distSq < innerRSq || distSq > outerRSq) continue;

            int bx = cx + dx;
            int bz = cz + dz;
            int yMin = cy - SHOCKWAVE_HEIGHT_DN;
            int yMax = cy + SHOCKWAVE_HEIGHT_UP;

            // Ničíme s klesající intenzitou od okraje kráteru ven
            double dist = Math.sqrt(distSq);
            double progress = (dist - HOR_R_TOTAL) / SHOCKWAVE_EXTRA; // 0.0 → 1.0
            double chance = 1.0 - progress * 0.92; // 1.0 → 0.08

            for (int y = yMin; y <= yMax; y++) {
                mutablePos.set(bx, y, bz);
                if (!level().isLoaded(mutablePos)) continue;

                BlockState st = level().getBlockState(mutablePos);
                if (st.isAir()) continue;

                float resistance = st.getBlock().getExplosionResistance();
                if (resistance < 0 || resistance > MAX_DESTROYABLE_RESISTANCE) continue;

                if (chance >= 1.0 || random.nextDouble() < chance) {
                    level().setBlock(mutablePos, Blocks.AIR.defaultBlockState(), 2 | 16 | 64);
                }
            }

            processed += colHeight;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void destroyBlock(BlockPos pos) {
        BlockState state = level().getBlockState(pos);
        if (state.isAir()) return;

        float resistance = state.getBlock().getExplosionResistance();
        if (resistance < 0 || resistance > MAX_DESTROYABLE_RESISTANCE) return;

        level().setBlock(pos, Blocks.AIR.defaultBlockState(), 2 | 16 | 64);
    }

    private void hitEntities() {
        if (entitiesHit) return;
        entitiesHit = true;
        double maxDist = SHOCKWAVE_R;
        AABB area = new AABB(getX()-maxDist, getY()-maxDist, getZ()-maxDist,
                getX()+maxDist, getY()+maxDist, getZ()+maxDist);
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, area)) {
            double dist = entity.distanceTo(this);
            if (dist < HOR_R_FULL) {
                entity.hurt(level().damageSources().explosion(this, this), Float.MAX_VALUE);
            } else if (dist < HOR_R_TOTAL * 1.5) {
                entity.hurt(level().damageSources().explosion(this, this),
                        (float)(100.0 * (1.0 - (dist - HOR_R_FULL) / HOR_R_TOTAL)));
                entity.setDeltaMovement(entity.position().subtract(position()).normalize().scale(3.0));
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 1));
            } else if (dist < SHOCKWAVE_R) {
                entity.hurt(level().damageSources().explosion(this, this),
                        (float)(30.0 * (1.0 - (dist - HOR_R_TOTAL) / SHOCKWAVE_EXTRA)));
                entity.setDeltaMovement(entity.position().subtract(position()).normalize().scale(2.0));
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 150, 1));
            } else {
                entity.setDeltaMovement(entity.position().subtract(position()).normalize().scale(1.5));
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            }
        }
    }

    // ── NBT ───────────────────────────────────────────────────────────────

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        radius         = tag.getInt("Radius");
        entitiesHit    = tag.getBoolean("EntitiesHit");
        currentShell   = tag.getInt("CurrentShell");
        maxShell       = tag.getInt("MaxShell");
        shellFace      = tag.getInt("ShellFace");
        shellU         = tag.getInt("ShellU");
        shellV         = tag.getInt("ShellV");
        swCurrentShell = tag.getInt("SwCurrentShell");
        swInnerShell   = tag.getInt("SwInnerShell");
        swOuterShell   = tag.getInt("SwOuterShell");
        swFace         = tag.getInt("SwFace");
        swFacePos      = tag.getInt("SwFacePos");
        setPhase(tag.getInt("Phase"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Radius",          radius);
        tag.putInt("Phase",           getPhase());
        tag.putBoolean("EntitiesHit", entitiesHit);
        tag.putInt("CurrentShell",    currentShell);
        tag.putInt("MaxShell",        maxShell);
        tag.putInt("ShellFace",       shellFace);
        tag.putInt("ShellU",          shellU);
        tag.putInt("ShellV",          shellV);
        tag.putInt("SwCurrentShell",  swCurrentShell);
        tag.putInt("SwInnerShell",    swInnerShell);
        tag.putInt("SwOuterShell",    swOuterShell);
        tag.putInt("SwFace",          swFace);
        tag.putInt("SwFacePos",       swFacePos);
    }

    @Override public boolean isAttackable() { return false; }
    @Override public boolean isPickable()   { return false; }
}