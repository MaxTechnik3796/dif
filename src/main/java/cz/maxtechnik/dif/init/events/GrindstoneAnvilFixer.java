package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.modular.ModularBase;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid = DifMod.MODID)
public class GrindstoneAnvilFixer{
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
		if(event.getLeft().getItem()instanceof ModularBase||event.getRight().getItem()instanceof ModularBase)event.setCanceled(true);
    }
    @SubscribeEvent
    public static void onGrindstoneUpdate(GrindstoneEvent.OnPlaceItem event) {
		if(event.getTopItem().getItem()instanceof ModularBase||event.getBottomItem().getItem()instanceof ModularBase)event.setCanceled(true);
    }
}