package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = DifMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CobblestoneGenerator{
	@SubscribeEvent
	public static void cobbleGeneratorEvent(BlockEvent.FluidPlaceBlockEvent event){
		if(event.getPos().getY()<0){
			if(event.getState().getBlock().equals(Blocks.COBBLESTONE)){
				event.setNewState(Blocks.COBBLED_DEEPSLATE.defaultBlockState());
			}else if(event.getState().getBlock().equals(Blocks.STONE)){
				event.setNewState(Blocks.DEEPSLATE.defaultBlockState());
			}
		}
	}
}
