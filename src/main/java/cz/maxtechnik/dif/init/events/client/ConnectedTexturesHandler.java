package cz.maxtechnik.dif.init.events.client;

import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import com.simibubi.create.foundation.model.ModelSwapper;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModSpriteShifts;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class ConnectedTexturesHandler{
	@SubscribeEvent
	public static void onModelBake(ModelEvent.ModifyBakingResult event){
		// Pomocí ModelSwapperu z Create vyměníme modely pro všechny stavy našeho CASING bloku
		ModelSwapper.swapModels(event.getModels(),ModelSwapper.getAllBlockStateModelLocations(DifModBlocks.ZINC_CASING.get()),model->new CTModel(model,new SimpleCTBehaviour(DifModSpriteShifts.ZINC_CASING_SHIFT)));
	}
}