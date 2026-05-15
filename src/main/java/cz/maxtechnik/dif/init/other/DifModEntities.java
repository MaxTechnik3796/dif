package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.entity.NuclearExplosionEntity;
import cz.maxtechnik.dif.entity.NuclearMushroomEntity;
import cz.maxtechnik.dif.entity.NuclearWaveEntity;
import cz.maxtechnik.dif.entity.vehicle.FormulaEntity;
import cz.maxtechnik.dif.entity.vehicle.RemoteControlMinecart;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DifModEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, DifMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<RemoteControlMinecart>> REMOTE_MINECART =
            REGISTRY.register("remote_minecart", () -> EntityType.Builder.<RemoteControlMinecart>of(RemoteControlMinecart::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .build("remote_minecart"));

    public static final DeferredHolder<EntityType<?>, EntityType<FormulaEntity>> FORMULA = REGISTRY.register("formula",
            () -> EntityType.Builder.of(FormulaEntity::new, MobCategory.MISC)
                    .sized(2.0F, 1.8F)
                    .clientTrackingRange(10)
                    .build("formula"));

    public static final DeferredHolder<EntityType<?>, EntityType<NuclearExplosionEntity>> NUCLEAR_EXPLOSION =
            REGISTRY.register("nuclear_explosion", () ->
                    EntityType.Builder.<NuclearExplosionEntity>of(NuclearExplosionEntity::new, MobCategory.MISC)
                            .sized(0.0f, 0.0f).clientTrackingRange(512).updateInterval(20)
                            .build("nuclear_explosion"));
    public static final DeferredHolder<EntityType<?>, EntityType<NuclearMushroomEntity>> NUCLEAR_MUSHROOM =
            REGISTRY.register("nuclear_mushroom", () ->
                    EntityType.Builder.<NuclearMushroomEntity>of(NuclearMushroomEntity::new, MobCategory.MISC)
                            .sized(0.0f, 0.0f)
                            .clientTrackingRange(512)
                            .updateInterval(20)
                            .build("nuclear_mushroom"));
    public static final DeferredHolder<EntityType<?>, EntityType<NuclearWaveEntity>> NUCLEAR_WAVE =
            REGISTRY.register("nuclear_wave", () ->
                    EntityType.Builder.<NuclearWaveEntity>of(NuclearWaveEntity::new, MobCategory.MISC)
                            .sized(0.0f, 0.0f)
                            .clientTrackingRange(512)
                            .updateInterval(1)
                            .build("nuclear_wave"));
}