package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.model.ModelJetpack;
import cz.maxtechnik.dif.model.PortalModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=cz.maxtechnik.dif.DifMod.MODID, bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class DifModModels{
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event){
		event.registerLayerDefinition(ModelJetpack.LAYER_LOCATION,ModelJetpack::createBodyLayer);
		event.registerLayerDefinition(PortalModel.LAYER_LOCATION,PortalModel::createBodyLayer);
	}
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event){
		event.registerEntityRenderer(DifModEntities.PORTAL.get(),cz.maxtechnik.dif.renderer.PortalRenderer::new);
	}
}