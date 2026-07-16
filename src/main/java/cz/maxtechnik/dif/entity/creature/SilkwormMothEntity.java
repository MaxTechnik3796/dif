package cz.maxtechnik.dif.entity.creature;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Hedvábný můřeň – neutrální brouk, chodí i létá.
 * Po zásahu (hitnutí) panicky uteče od hráče (může uletět).
 */
public class SilkwormMothEntity extends PathfinderMob {

    // true = momentálně létá, false = chodí po zemi
    private boolean isFlying = false;

    public SilkwormMothEntity(EntityType<? extends SilkwormMothEntity> type, Level level) {
        super(type, level);
        // Vlastní pohybový controller – umožní plynulý let, když isFlying == true
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    // ---------- Atributy ----------
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    // ---------- AI Goals ----------
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Panika po zásahu – uteče od hráče, který ho udeřil (nebo od nejbližšího hráče)
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.6D, 1.6D) {
            @Override
            public boolean canUse() {
                // Spustí se jen pokud byl brouk nedávno zraněn
                return SilkwormMothEntity.this.getLastHurtByMob() != null && super.canUse();
            }
        });

        // Náhodné poletování / procházení, když není v panice
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    // ---------- Přepínání chůze / létání ----------
    @Override
    public PathNavigation getNavigation() {
        return super.getNavigation();
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        // FlyingPathNavigation umožní broučkovi navigovat i vzduchem
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    public boolean isFlapping() {
        return this.isFlying;
    }

    public boolean isFlyingNow() {
        return this.isFlying;
    }

    public void setFlying(boolean flying) {
        this.isFlying = flying;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        // Jednoduchá logika: pokud je "v panice" (utíká) nebo dost vysoko nad zemí, létá
        boolean onGroundNow = this.onGround();
        boolean fleeing = this.getLastHurtByMob() != null
                && this.tickCount - this.getLastHurtByMobTimestamp() < 100;

        if (fleeing && onGroundNow) {
            // Vzlétne, aby utekl efektivněji
            this.setFlying(true);
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.35D, 0.0D));
        } else if (!fleeing && onGroundNow) {
            this.setFlying(false);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result) {
            // Vynutí okamžitou reakci útěku i letu
            this.setFlying(true);
        }
        return result;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
        // Létající hmyz nebere fall damage
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isFlying || !this.onGround()) {
            if (this.isInWater()) {
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
            } else {
                this.moveRelative(this.getSpeed(), travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.91D));
            }
        } else {
            super.travel(travelVector);
        }
    }

}