package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems; // nebo jak se jmenuje tvá item registrace
import cz.maxtechnik.dif.item.armor.ElectroRunners;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = DifMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CapabilityHandler {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        ElectroRunners.Boots.registerCapability(event, DifModItems.ELECTRO_RUNNERS.get());
    }
}