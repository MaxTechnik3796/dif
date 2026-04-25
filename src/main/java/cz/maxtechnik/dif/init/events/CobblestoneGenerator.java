package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=DifMod.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
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
