package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.entity.WitherTitanEntity; // PŘIDAT IMPORT
import cz.maxtechnik.dif.entity.vehicle.FormulaEntity;
import cz.maxtechnik.dif.entity.vehicle.RemoteControlMinecart;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DifModEntities {
    public static final DeferredRegister<EntityType<?>>REGISTRY=DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, DifMod.MODID);

    public static final RegistryObject<EntityType<RemoteControlMinecart>> REMOTE_MINECART =
            REGISTRY.register("remote_minecart", () -> EntityType.Builder.<RemoteControlMinecart>of(RemoteControlMinecart::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .build("remote_minecart"));

    public static final RegistryObject<EntityType<WitherTitanEntity>> WITHER_TITAN =
            REGISTRY.register("wither_titan", () -> EntityType.Builder.of(WitherTitanEntity::new, MobCategory.MONSTER)
                    .sized(2.0F, 8.0F) // BOX 1: Collision box u země
                    .clientTrackingRange(128)
                    .fireImmune()
                    .build("wither_titan"));
    public static final RegistryObject<EntityType<FormulaEntity>> FORMULA =REGISTRY.register("formula",
					() -> EntityType.Builder.of(FormulaEntity::new, MobCategory.MISC)
                    .sized(2.0F, 1.8F) // Zvýšeno o 1 block (původně 0.8F)
                    .clientTrackingRange(10)
                    .build("formula"));
}