package cz.maxtechnik.dif.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;

public class WitherTitanEntity extends WitherBoss {
    public WitherTitanEntity(EntityType<? extends WitherBoss> type, Level level) {
        super(type, level);
    }

    // Povolí interakci s entitou
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public float getPickRadius() {
        return 8.0F;
    }
}