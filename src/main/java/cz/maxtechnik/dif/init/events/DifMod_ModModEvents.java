package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=DifMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class DifMod_ModModEvents{
	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item event){
		// Zde registrujeme barvy pro náš modulární krumpáč
		event.register((stack,tintIndex)->{
			if(stack.getTag()!=null){
				// tintIndex odpovídá vrstvě v JSON modelu (layer0 = 0, layer1 = 1...)
				return switch(tintIndex){
					case 0 -> stack.getTag().contains("HandleColor")?stack.getTag().getInt("HandleColor"):-1;
					case 1 -> stack.getTag().contains("BindingColor")?stack.getTag().getInt("BindingColor"):-1;
					case 2 -> stack.getTag().contains("HeadColor")?stack.getTag().getInt("HeadColor"):-1;
					default -> -1; // Výchozí barva (bílá/beze změny)
				};
			}
			return -1;
		},DifModItems.MODULAR_PICKAXE.get());
	}
}