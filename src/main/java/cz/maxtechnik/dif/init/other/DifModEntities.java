package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.entity.WitherTitanEntity; // PŘIDAT IMPORT
import cz.maxtechnik.dif.entity.vehicle.RemoteControlMinecart;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DifModEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, DifMod.MODID);

    public static final RegistryObject<EntityType<RemoteControlMinecart>> REMOTE_MINECART =
            REGISTRY.register("remote_minecart", () -> EntityType.Builder.<RemoteControlMinecart>of(RemoteControlMinecart::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .build("remote_minecart"));

    // --- PŘIDAT TOTO ---
    public static final RegistryObject<EntityType<WitherTitanEntity>> WITHER_TITAN =
            REGISTRY.register("wither_titan", () -> EntityType.Builder.of(WitherTitanEntity::new, MobCategory.MONSTER)
                    .sized(2.0F, 8.0F) // Malý box pro plynulý pohyb a TPS
                    .clientTrackingRange(64)
                    .fireImmune()
                    .build("wither_titan"));
}