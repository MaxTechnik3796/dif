package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.client.renderer.ForgeGlassRenderer;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.model.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
@SuppressWarnings("removal")
@EventBusSubscriber(modid = cz.maxtechnik.dif.DifMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DifModModels{
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event){
		event.registerLayerDefinition(ModelSpaceHelmet.LAYER_LOCATION, ModelSpaceHelmet::createBodyLayer);
		event.registerLayerDefinition(ModelSpaceBoots.LAYER_LOCATION, ModelSpaceBoots::createBodyLayer);
		event.registerLayerDefinition(ModelSpaceLeggings.LAYER_LOCATION, ModelSpaceLeggings::createBodyLayer);
		event.registerLayerDefinition(ModelSpaceChestplate.LAYER_LOCATION, ModelSpaceChestplate::createBodyLayer);
		event.registerLayerDefinition(ModelJetpack.LAYER_LOCATION, ModelJetpack::createBodyLayer);
		event.registerLayerDefinition(ModelElectroRunners.LAYER_LOCATION,ModelElectroRunners::createBodyLayer);
		event.registerLayerDefinition(FormulaModel.LAYER_LOCATION, FormulaModel::createBodyLayer);
	}

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(DifModBlockEntities.FORGE_FURNACE_CONTROLLER.get(), ForgeGlassRenderer::new);
	}
}
