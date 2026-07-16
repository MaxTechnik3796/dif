package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.entity.bomb.NuclearExplosionEntity;
import cz.maxtechnik.dif.entity.bomb.NuclearMushroomEntity;
import cz.maxtechnik.dif.entity.bomb.NuclearRadiationEntity;
import cz.maxtechnik.dif.entity.bomb.NuclearWaveEntity;
import cz.maxtechnik.dif.entity.vehicle.FormulaEntity;
import cz.maxtechnik.dif.entity.creature.SilkwormMothEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
public class DifModEntities{
	public static final DeferredRegister<EntityType<?>> REGISTRY=DeferredRegister.create(Registries.ENTITY_TYPE,DifMod.MODID);
	public static final DeferredHolder<EntityType<?>,EntityType<FormulaEntity>> FORMULA=REGISTRY.register("formula",
			()->EntityType.Builder.of(FormulaEntity::new,MobCategory.MISC)
					.sized(2F,1.8F)
					.clientTrackingRange(10)
					.build("formula"));
	public static final DeferredHolder<EntityType<?>,EntityType<NuclearExplosionEntity>> NUCLEAR_EXPLOSION=
			REGISTRY.register("nuclear_explosion",()->
					EntityType.Builder.of(NuclearExplosionEntity::new,MobCategory.MISC)
							.sized(0F,0F).clientTrackingRange(512).updateInterval(20)
							.build("nuclear_explosion"));
	public static final DeferredHolder<EntityType<?>,EntityType<NuclearMushroomEntity>> NUCLEAR_MUSHROOM=
			REGISTRY.register("nuclear_mushroom",()->
					EntityType.Builder.of(NuclearMushroomEntity::new,MobCategory.MISC)
							.sized(0F,0F)
							.clientTrackingRange(512)
							.updateInterval(20)
							.build("nuclear_mushroom"));
	public static final DeferredHolder<EntityType<?>,EntityType<NuclearWaveEntity>> NUCLEAR_WAVE=
			REGISTRY.register("nuclear_wave",()->
					EntityType.Builder.of(NuclearWaveEntity::new,MobCategory.MISC)
							.sized(0F,0F)
							.clientTrackingRange(512)
							.updateInterval(1)
							.build("nuclear_wave"));
	public static final DeferredHolder<EntityType<?>,EntityType<NuclearRadiationEntity>> NUCLEAR_RADIATION=
			REGISTRY.register("nuclear_radiation",()->
					EntityType.Builder.of(NuclearRadiationEntity::new,MobCategory.MISC)
							.sized(0F,0F)
							.clientTrackingRange(512)
							.updateInterval(20)
							.build("nuclear_radiation"));
	public static final DeferredHolder<EntityType<?>,EntityType<cz.maxtechnik.dif.entity.portal.PortalEntity>> PORTAL=
			REGISTRY.register("portal",()->
					EntityType.Builder.<cz.maxtechnik.dif.entity.portal.PortalEntity>of(cz.maxtechnik.dif.entity.portal.PortalEntity::new,MobCategory.MISC)
							.sized(1.0F,2.0F)
							.clientTrackingRange(10)
							.updateInterval(1)
							.build("portal"));
	public static final DeferredHolder<EntityType<?>,EntityType<SilkwormMothEntity>> BOROUS_MORUSOVY=
			REGISTRY.register("borous_morusovy",()->
					EntityType.Builder.of(SilkwormMothEntity::new,MobCategory.CREATURE)
							.sized(0.6F,0.5F)
							.clientTrackingRange(8)
							.build("borous_morusovy"));
}