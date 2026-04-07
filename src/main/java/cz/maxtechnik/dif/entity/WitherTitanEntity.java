package cz.maxtechnik.dif.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.entity.PartEntity;
import java.util.List;

public class WitherTitanEntity extends WitherBoss {
    private final WitherTitanPart upperHitbox;
    private final WitherTitanPart[] parts;
    private int skeletonRespawnTimer = 0;

    public WitherTitanEntity(EntityType<? extends WitherBoss> type, Level level) {
        super(type, level);
        // Vytvoření Hitboxu 10x30
        this.upperHitbox = new WitherTitanPart(this, "upper_hitbox", 10.0F, 30.0F);
        this.parts = new WitherTitanPart[]{this.upperHitbox};
        this.setId(ENTITY_COUNTER.getAndIncrement()); // Důležité pro synchronizaci ID
    }

    public static AttributeSupplier.Builder createAttributes() {
        return WitherBoss.createAttributes()
                .add(Attributes.MAX_HEALTH, 1000000.0D)
                .add(Attributes.ARMOR, 100.0D)
                .add(Attributes.FOLLOW_RANGE, 120.0D);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // FIX PROBLIKÁVÁNÍ: Nastavujeme pozici boxu v každém kroku
        double hitboxY = this.getY() + 8.0D;
        this.upperHitbox.setPos(this.getX(), hitboxY, this.getZ());

        // Aktualizace detekční zóny (AABB)
        this.upperHitbox.setBoundingBox(new AABB(
                this.getX() - 5.0D, hitboxY, this.getZ() - 5.0D,
                this.getX() + 5.0D, hitboxY + 30.0D, this.getZ() + 5.0D
        ));

        // Logika skeletonů (u nohou modelu -30 bloků od hitboxu)
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            List<WitherSkeleton> skeletons = serverLevel.getEntitiesOfClass(WitherSkeleton.class,
                    this.getBoundingBox().inflate(50.0D).move(0, -30, 0));

            if (skeletons.isEmpty()) {
                skeletonRespawnTimer++;
                if (skeletonRespawnTimer >= 1200) {
                    for (int i = 0; i < 12; i++) {
                        WitherSkeleton skeleton = EntityType.WITHER_SKELETON.create(serverLevel);
                        if (skeleton != null) {
                            double rx = this.getX() + (this.random.nextDouble() - 0.5D) * 15.0D;
                            double rz = this.getZ() + (this.random.nextDouble() - 0.5D) * 15.0D;
                            skeleton.moveTo(rx, this.getY() - 30.0D, rz, 0, 0);
                            serverLevel.addFreshEntity(skeleton);
                        }
                    }
                    skeletonRespawnTimer = 0;
                }
            }
        }
    }

    // PŘESMĚROVÁNÍ POŠKOZENÍ: Když hráč bouchne do Partu, dostane to hlavní tělo
    public boolean hurtPart(WitherTitanPart part, DamageSource source, float amount) {
        return this.hurt(source, amount);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        double spawnY = this.getY() - 10.0D;
        double tx = target.getX() - this.getX();
        double ty = target.getY(0.5D) - spawnY;
        double tz = target.getZ() - this.getZ();

        WitherSkull skull = new WitherSkull(this.level(), this, tx, ty, tz);
        skull.setPos(this.getX(), spawnY, this.getZ());

        CompoundTag nbt = new CompoundTag();
        skull.addAdditionalSaveData(nbt);
        nbt.putInt("ExplosionPower", 4);
        skull.readAdditionalSaveData(nbt);

        this.level().addFreshEntity(skull);
    }

    @Override
    public boolean isMultipartEntity() { return true; }

    @Override
    public PartEntity<?>[] getParts() { return this.parts; }

    // --- TŘÍDA PRO HITBOX ČÁST ---
    public static class WitherTitanPart extends PartEntity<WitherTitanEntity> {
        public final WitherTitanEntity parent;
        public final String name;
        private final EntityDimensions size;

        public WitherTitanPart(WitherTitanEntity parent, String name, float width, float height) {
            super(parent);
            this.parent = parent;
            this.name = name;
            this.size = EntityDimensions.scalable(width, height);
            this.refreshDimensions();
        }

        @Override
        public boolean hurt(DamageSource source, float amount) {
            // Tohle propojí hitbox s hlavním životem bosse
            return !this.isInvulnerableTo(source) && this.parent.hurtPart(this, source, amount);
        }

        @Override
        public boolean isPickable() { return true; } // Aby šlo na box kliknout

        @Override
        public boolean is(net.minecraft.world.entity.Entity entity) {
            return this == entity || this.parent == entity;
        }

        @Override protected void defineSynchedData() {}
        @Override protected void readAdditionalSaveData(CompoundTag nbt) {}
        @Override protected void addAdditionalSaveData(CompoundTag nbt) {}
        @Override public EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose) { return this.size; }
    }
}