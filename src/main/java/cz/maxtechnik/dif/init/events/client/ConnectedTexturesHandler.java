package cz.maxtechnik.dif.init.events.client;

import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import com.simibubi.create.foundation.model.ModelSwapper;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.DistillationTankModel;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModSpriteShifts;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class ConnectedTexturesHandler{
	@SubscribeEvent
	public static void onModelBake(ModelEvent.ModifyBakingResult event){
		register(event,DifModBlocks.ZINC_CASING,DifModSpriteShifts.ZINC_CASING);
		register(event,DifModBlocks.STEEL_CASING,DifModSpriteShifts.STEEL_CASING);
		register(event,DifModBlocks.AURORA_CASING,DifModSpriteShifts.AURORA_CASING);
		register(event,DifModBlocks.FORGE_GLASS,DifModSpriteShifts.FORGE_GLASS);

		ModelSwapper.swapModels(event.getModels(),ModelSwapper.getAllBlockStateModelLocations(DifModBlocks.DISTILLATION_TANK.get()),DistillationTankModel::standard);
	}
	private static void register(ModelEvent.ModifyBakingResult event,DeferredBlock<Block> block,CTSpriteShiftEntry shift){
		ModelSwapper.swapModels(event.getModels(),ModelSwapper.getAllBlockStateModelLocations(block.get()),model->new CTModel(model,new SimpleCTBehaviour(shift)));
	}
}