package cz.maxtechnik.dif.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

    // ═══════════════════════════════════════════════════════════════════════
    // LADITELNÉ KONSTANTY
    // ═══════════════════════════════════════════════════════════════════════

    /** Bloků zpracovaných za tick. Více = rychlejší výbuch, ale větší zátěž. */
    private static final int BLOCKS_PER_TICK = 8000;

    /** Bloky s blast resistance nad touto hodnotu nelze zničit (obsidián = 1200, takže ten projde). */
    private static final float MAX_DESTROYABLE_RESISTANCE = 1500f;

    // Horizontální poloměr (do stran, x/z jsou stejné)
    private static final double HOR_R_FULL  = 40.0; // do tohoto = 100% ničení
    private static final double HOR_R_TOTAL = 60.0; // do tohoto klesá na 1%, za tím = nic

    // Vertikální poloměr – nahoru
    private static final double UP_R_FULL  = 45.0;
    private static final double UP_R_TOTAL = 56.0;

    // Vertikální poloměr – dolů (mělká prohlubeň)
    private static final double DOWN_R_FULL  = 10.0;
    private static final double DOWN_R_TOTAL = 16.0;

    // ═══════════════════════════════════════════════════════════════════════

    private static final int PHASE_INIT   = 0;
    private static final int PHASE_CRATER = 1;
    private static final int PHASE_DONE   = 2;

    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(NuclearExplosionEntity.class, EntityDataSerializers.INT);

    // Iterátor stav – místo fronty iterujeme přímo přes elipsoid
    private int iterX, iterY, iterZ;
    private int xzRange, upRange, dnRange;

    private boolean entitiesHit = false;
    private int radius = (int) HOR_R_TOTAL;

    // Reusable mutable BlockPos – šetří alokace
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

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) { tickClient(); return; }

        switch (getPhase()) {
            case PHASE_INIT   -> initExplosion();
            case PHASE_CRATER -> tickCrater();
            case PHASE_DONE   -> this.discard();
        }
    }

    // ── Fáze 0: Init (jen efekty + setup iterátoru) ──────────────────────────

    private void initExplosion() {
        xzRange = (int) Math.ceil(HOR_R_TOTAL);
        upRange = (int) Math.ceil(UP_R_TOTAL);
        dnRange = (int) Math.ceil(DOWN_R_TOTAL);

        // Iterátor začíná od dolního rohu boxu
        iterX = -xzRange;
        iterY = -dnRange;
        iterZ = -xzRange;

        playExplosionEffects();
        hitEntities();
        setPhase(PHASE_CRATER);
    }

    // ── Fáze 1: Ničení ────────────────────────────────────────────────────────

    /**
     * Klíčová optimalizace: místo budování fronty 600k bloků iterujeme
     * přímo přes bounding box a kontrolujeme každý blok inline.
     * Žádný sort, žádné remove(0), žádné alokace BlockPos.
     */
    private void tickCrater() {
        BlockPos center = this.blockPosition();
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        // Cache hodnot pro rychlost (lokální proměnné jsou rychlejší než pole/static)
        final double horFull   = HOR_R_FULL;
        final double horTotal  = HOR_R_TOTAL;
        final double upFull    = UP_R_FULL;
        final double upTotal   = UP_R_TOTAL;
        final double dnFull    = DOWN_R_FULL;
        final double dnTotal   = DOWN_R_TOTAL;
        final double horFullSq  = horFull * horFull;
        final double horTotalSq = horTotal * horTotal;
        final double upFullSq   = upFull * upFull;
        final double upTotalSq  = upTotal * upTotal;
        final double dnFullSq   = dnFull * dnFull;
        final double dnTotalSq  = dnTotal * dnTotal;

        int processed = 0;
        int xz = xzRange;
        int up = upRange;
        int dn = dnRange;

        while (processed < BLOCKS_PER_TICK) {
            int dx = iterX;
            int dy = iterY;
            int dz = iterZ;

            // Posun iterátoru
            iterZ++;
            if (iterZ > xz) {
                iterZ = -xz;
                iterY++;
                if (iterY > up) {
                    iterY = -dn;
                    iterX++;
                    if (iterX > xz) {
                        // Hotovo
                        setPhase(PHASE_DONE);
                        return;
                    }
                }
            }

            // Vyber poloměry pro daný směr y
            double verFullSq  = dy >= 0 ? upFullSq  : dnFullSq;
            double verTotalSq = dy >= 0 ? upTotalSq : dnTotalSq;

            double dxSq = dx * dx;
            double dySq = dy * dy;
            double dzSq = dz * dz;

            // Test 1: je vůbec v total elipsoidu?
            double nTotal = dxSq/horTotalSq + dySq/verTotalSq + dzSq/horTotalSq;
            if (nTotal > 1.0) continue; // mimo dosah

            // Test 2: je v full elipsoidu?
            double nFull = dxSq/horFullSq + dySq/verFullSq + dzSq/horFullSq;

            double chance;
            if (nFull <= 1.0) {
                chance = 1.0; // 100% ničení v jádru
            } else {
                // Lineární přechod mezi full a total
                // Najdeme bod na hranici total ve stejném směru a spočítáme jeho nFull
                double scale = 1.0 / Math.sqrt(nTotal);
                double bxSq = dxSq * scale * scale;
                double bySq = dySq * scale * scale;
                double bzSq = dzSq * scale * scale;
                double maxNFull = bxSq/horFullSq + bySq/verFullSq + bzSq/horFullSq;

                double t = (nFull - 1.0) / (maxNFull - 1.0);
                if (t < 0.0) t = 0.0;
                else if (t > 1.0) t = 1.0;

                chance = 1.0 - t * 0.99; // 1.0 → 0.01
            }

            if (chance >= 1.0 || random.nextDouble() < chance) {
                mutablePos.set(cx + dx, cy + dy, cz + dz);

                if (level().isLoaded(mutablePos)) {
                    destroyBlock(mutablePos);
                }
            }

            processed++;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void destroyBlock(BlockPos pos) {
        BlockState state = level().getBlockState(pos);
        if (state.isAir()) return;

        // Blast resistance check – respektuje odolnost bloků
        float resistance = state.getBlock().getExplosionResistance();
        if (resistance < 0 || resistance > MAX_DESTROYABLE_RESISTANCE) return;

        // Flag 2 = pošli update klientovi, 16 = neaktualizuj sousedy, 64 = no neighbor reaction
        // → výrazně rychlejší než výchozí flag 3
        level().setBlock(pos, Blocks.AIR.defaultBlockState(), 2 | 16 | 64);
    }

    private void hitEntities() {
        if (entitiesHit) return;
        entitiesHit = true;
        double maxDist = HOR_R_TOTAL * 2.0;
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
            } else {
                entity.setDeltaMovement(entity.position().subtract(position()).normalize().scale(1.5));
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            }
        }
    }

    private void playExplosionEffects() {
        if (!(level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(),
                20, HOR_R_TOTAL*0.3, HOR_R_TOTAL*0.3, HOR_R_TOTAL*0.3, 0.5);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(),
                100, HOR_R_TOTAL*0.5, HOR_R_TOTAL*0.5, HOR_R_TOTAL*0.5, 0.3);
        sl.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(),
                200, HOR_R_TOTAL*0.4, HOR_R_TOTAL*0.4, HOR_R_TOTAL*0.4, 1.0);
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 20.0f, 0.4f);
    }

    // ── Klient efekty ─────────────────────────────────────────────────────────

    private void tickClient() {
        int phase = getPhase();
        if (phase == PHASE_DONE || phase == PHASE_INIT) return;
        if (tickCount % 2 == 0) {
            double h = tickCount * 0.8;
            level().addParticle(ParticleTypes.LARGE_SMOKE,
                    getX()+(random.nextDouble()-0.5)*5, getY()+h,
                    getZ()+(random.nextDouble()-0.5)*5, 0, 0.5, 0);
            level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    getX()+(random.nextDouble()-0.5)*10, getY()+h*0.5,
                    getZ()+(random.nextDouble()-0.5)*10, 0, 0.3, 0);
        }
        if (tickCount < 60 && tickCount % 2 == 0)
            level().addParticle(ParticleTypes.FLAME,
                    getX()+(random.nextDouble()-0.5)*HOR_R_TOTAL*0.5,
                    getY()+random.nextDouble()*5,
                    getZ()+(random.nextDouble()-0.5)*HOR_R_TOTAL*0.5,
                    0, 0.1, 0);
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        radius      = tag.getInt("Radius");
        entitiesHit = tag.getBoolean("EntitiesHit");
        iterX       = tag.getInt("IterX");
        iterY       = tag.getInt("IterY");
        iterZ       = tag.getInt("IterZ");
        xzRange     = tag.getInt("XzRange");
        upRange     = tag.getInt("UpRange");
        dnRange     = tag.getInt("DnRange");
        setPhase(tag.getInt("Phase"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Radius",          radius);
        tag.putInt("Phase",           getPhase());
        tag.putBoolean("EntitiesHit", entitiesHit);
        tag.putInt("IterX",           iterX);
        tag.putInt("IterY",           iterY);
        tag.putInt("IterZ",           iterZ);
        tag.putInt("XzRange",         xzRange);
        tag.putInt("UpRange",         upRange);
        tag.putInt("DnRange",         dnRange);
    }

    @Override public boolean isAttackable() { return false; }
    @Override public boolean isPickable()   { return false; }
}