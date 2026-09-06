package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.SleepingBag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

@EventBusSubscriber(modid=DifMod.MODID)
public class PlayerSpawnHandler{
	@SubscribeEvent
	public static void onPlayerSetSpawn(PlayerSetSpawnEvent event){
		Level level=event.getEntity().level();
		BlockPos pos=event.getNewSpawn();
		if(pos!=null){
			BlockState state=level.getBlockState(pos);
			if(state.getBlock() instanceof SleepingBag){
				event.setCanceled(true);
			}
		}
	}
	@SubscribeEvent
	public static void onPlayerWakeUp(PlayerWakeUpEvent event){
		Player player=event.getEntity();
		player.getSleepingPos().ifPresent(pos->{
			Level level=player.level();
			if(!level.isClientSide){
				BlockState state=level.getBlockState(pos);
				if(state.getBlock() instanceof SleepingBag&&state.getValue(SleepingBag.OCCUPIED)){
					level.setBlock(pos,state.setValue(SleepingBag.OCCUPIED,false),3);
				}
			}
		});
	}
}