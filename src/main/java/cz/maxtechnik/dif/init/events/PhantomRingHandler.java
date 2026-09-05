package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicBoolean;
@EventBusSubscriber
public class PhantomRingHandler{
	@SubscribeEvent
	public static void onPhantomSpawn(EntityJoinLevelEvent event){
		if(event.getEntity() instanceof Phantom phantom){
			Player player=event.getLevel().getNearestPlayer(phantom,128);
			if(player!=null){
				AtomicBoolean hasRing=new AtomicBoolean(false);
				CuriosApi.getCuriosInventory(player).flatMap(handler->handler.findFirstCurio(stack->stack.getItem().equals(DifModItems.PHANTOM_RING.get()))).ifPresent(slotResult->hasRing.set(true));
				if(hasRing.get()){
					event.setCanceled(true);
				}
			}
		}
	}
}