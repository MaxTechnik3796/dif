package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent;
import top.theillusivec4.curios.api.CuriosApi;

@EventBusSubscriber(modid=DifMod.MODID)
public class PhantomRingHandler{
	public static boolean hasPhantomRing(Player player){
		if(player==null) return false;
		if(player.getMainHandItem().is(DifModItems.PHANTOM_RING.get())||player.getOffhandItem().is(DifModItems.PHANTOM_RING.get())){
			return true;
		}
		for(ItemStack stack: player.getInventory().items){
			if(stack.is(DifModItems.PHANTOM_RING.get())) return true;
		}
		if(ModList.get().isLoaded("curios")){
			return CuriosApi.getCuriosInventory(player)
					.map(handler->handler.findFirstCurio(s->s.is(DifModItems.PHANTOM_RING.get())).isPresent())
					.orElse(false);
		}
		return false;
	}
	@SubscribeEvent
	public static void onPlayerSpawnPhantoms(PlayerSpawnPhantomsEvent event){
		Player player=event.getEntity();
		if(hasPhantomRing(player)){
			event.setResult(PlayerSpawnPhantomsEvent.Result.DENY);
		}
	}

	@SubscribeEvent
	public static void onPhantomTarget(LivingChangeTargetEvent event){
		if(event.getEntity() instanceof Phantom&&event.getNewAboutToBeSetTarget() instanceof Player player){
			if(hasPhantomRing(player)){
				event.setCanceled(true);
			}
		}
	}
	@SubscribeEvent
	public static void onPhantomDamage(LivingIncomingDamageEvent event){
		if(event.getEntity() instanceof Player player&&event.getSource().getEntity() instanceof Phantom){
			if(hasPhantomRing(player)){
				event.setCanceled(true);
			}
		}
	}
}