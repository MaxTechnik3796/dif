package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.client.model.ModelSpaceBoots;
import cz.maxtechnik.dif.client.model.ModelSpaceChestplate;
import cz.maxtechnik.dif.client.model.ModelSpaceHelmet;
import cz.maxtechnik.dif.client.model.ModelSpaceLeggings;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;


@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD,value={Dist.CLIENT})
public class DifModModels{
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event){
		event.registerLayerDefinition(ModelSpaceHelmet.LAYER_LOCATION, ModelSpaceHelmet::createBodyLayer);
		event.registerLayerDefinition(ModelSpaceBoots.LAYER_LOCATION, ModelSpaceBoots::createBodyLayer);
		event.registerLayerDefinition(ModelSpaceLeggings.LAYER_LOCATION, ModelSpaceLeggings::createBodyLayer);
		event.registerLayerDefinition(ModelSpaceChestplate.LAYER_LOCATION, ModelSpaceChestplate::createBodyLayer);
	}
}
