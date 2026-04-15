package cz.maxtechnik.dif.init.events.client;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;

/**
 * Client-side setup.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    /**
     * Registruje KineticBlockEntityRenderer pro ReinforcedShaft block entitu.
     * Tento renderer se stará o rotaci modelu podle rychlosti v BlockEntity.
     */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                DifModBlockEntities.REINFORCED_SHAFT.get(),
                KineticBlockEntityRenderer::new
        );
    }
}