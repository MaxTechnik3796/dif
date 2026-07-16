package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.entity.creature.SilkwormMothEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = DifMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DifModAttributes {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(DifModEntities.SILKWORM_MOTH.get(), SilkwormMothEntity.createAttributes().build());
    }
}