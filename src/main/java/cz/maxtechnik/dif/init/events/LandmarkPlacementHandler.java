package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.block.entity.QuarryLandmarkBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
/**
 * Serverový handler – spouští pokus o aktivaci landmarků po každém umístění landmark bloku.
 */
@Mod.EventBusSubscriber
public class LandmarkPlacementHandler{
	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.EntityPlaceEvent event){
		if(!event.getPlacedBlock().is(DifModBlocks.QUARRY_LANDMARK.get())) return;
		Level level=(Level)event.getLevel();
		if(level.isClientSide) return;
		BlockPos pos=event.getPos();
		if(level.getBlockEntity(pos) instanceof QuarryLandmarkBlockEntity lbe)
			lbe.tryActivateNearbyQuarry();
	}
}