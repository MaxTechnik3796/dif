package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.SleepingBagBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;
@EventBusSubscriber(modid=DifMod.MODID)
public class PlayerSpawnHandler{
	@SubscribeEvent
	public static void onPlayerSetSpawn(PlayerSetSpawnEvent event){
		Level level=event.getEntity().level();
		BlockPos pos=event.getNewSpawn();
		if(pos!=null){
			BlockState state=level.getBlockState(pos);
			if(state.getBlock() instanceof SleepingBagBlock){
				event.setCanceled(true);
			}
		}
	}
}