package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.block.entity.QuarryBlockEntity;
import cz.maxtechnik.dif.block.entity.QuarryFrameBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
@EventBusSubscriber
public class QuarryFrameBreakHandler{
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event){
		if(!event.getState().is(DifModBlocks.QUARRY_FRAME.get())) return;
		Level level=(Level)event.getLevel();
		if(level.isClientSide) return;
		if(!(level.getBlockEntity(event.getPos()) instanceof QuarryFrameBlockEntity frameEntity)) return;
		BlockPos ownerPos=frameEntity.getOwnerPos();
		if(ownerPos!=null&&level.getBlockEntity(ownerPos) instanceof QuarryBlockEntity quarryEntity) quarryEntity.onFrameDestroyed(level);
	}
}