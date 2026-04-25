package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicBoolean;
@Mod.EventBusSubscriber
public class PhantomRingHandler{
	@SubscribeEvent
	public static void onPhantomSpawn(EntityJoinLevelEvent event){
		// Kontrolujeme, zda se jedná o Fantoma
		if(event.getEntity() instanceof Phantom phantom){
			// Najdeme nejbližšího hráče (Fantomové se spawnují nad konkrétním hráčem)
			Player player=event.getLevel().getNearestPlayer(phantom,128);
			if(player!=null){
				// KONTROLA CURIOS SLOTU:
				// Zkontrolujeme, zda má hráč v Curios slotu náš prsten
				AtomicBoolean hasRing=new AtomicBoolean(false);
				CuriosApi.getCuriosInventory(player).ifPresent(handler->handler.findFirstCurio(stack->stack.getItem().equals(DifModItems.PHANTOM_RING.get())).ifPresent(slotResult->{
					hasRing.set(true);
				}));
				if(hasRing.get()){
					// Zrušíme přidání entity do světa
					event.setCanceled(true);
				}
			}
		}
	}
}