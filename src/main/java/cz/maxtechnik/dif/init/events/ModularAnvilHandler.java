package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.modular.v2.ModularTool;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
@EventBusSubscriber(modid=DifMod.MODID)
public class ModularAnvilHandler{
	@SubscribeEvent
	public static void onAnvilUpdate(AnvilUpdateEvent event){
		ItemStack left=event.getLeft();
		ItemStack right=event.getRight();
		// Modular tools nesmí být vkládány do kovadliny vůbec
		if(left.getItem() instanceof ModularTool||right.getItem() instanceof ModularTool){
			event.setOutput(ItemStack.EMPTY);
			event.setCost(0);
			event.setMaterialCost(0);
		}
	}
}

