package cz.maxtechnik.dif.entity.creature;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
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
import org.jetbrains.annotations.NotNull;

/**
 * Hedvábný můřeň – neutrální brouk, chodí i létá.
 * Po zásahu (hitnutí) panicky uteče od hráče (může uletět).
 * Létání je plynulé (podobné papouškovi), ne trhavé.
 */
public class SilkwormMothEntity extends PathfinderMob {

    private boolean isFlying = false;

    public SilkwormMothEntity(EntityType<? extends SilkwormMothEntity> type, Level level) {
        super(type, level);
        // speedModifier 1.0 = přirozená rychlost letu, ne robotické cukání
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    // ---------- Atributy ----------
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.FLYING_SPEED, 0.4D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    // ---------- AI Goals ----------
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Panika po zásahu – uteče od hráče, který ho udeřil
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.2D, 1.4D) {
            @Override
            public boolean canUse() {
                return SilkwormMothEntity.this.getLastHurtByMob() != null && super.canUse();
            }
        });

        // Pomalejší, plynulé poletování - jako u papouška
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 0.6D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
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

    public void setFlying(boolean flying) {
        this.isFlying = flying;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        boolean fleeing = this.getLastHurtByMob() != null
                && this.tickCount - this.getLastHurtByMobTimestamp() < 100;

        // Přepínání stavu chůze/letu bez prudkého impulzu nahoru –
        // necháme to čistě na FlyingMoveControl a goalech, žádné umělé "vyskočení"
        if (fleeing) {
            this.setFlying(true);
        } else if (this.onGround()) {
            this.setFlying(false);
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result) {
            this.setFlying(true);
        }
        return result;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.@NotNull BlockState state, @NotNull BlockPos pos) {
        // Létající hmyz nebere fall damage
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false; // Nedespawne jen kvůli vzdálenosti od hráče
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, @NotNull DamageSource source) {
        return false;
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (this.isFlying || !this.onGround()) {
            if (this.isInWater()) {
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
            } else {
                // Nižší akcelerace a vyšší tlumení = plynulejší, méně "robotický" let
                this.moveRelative(0.05F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95D));
            }
        } else {
            super.travel(travelVector);
        }
    }

}