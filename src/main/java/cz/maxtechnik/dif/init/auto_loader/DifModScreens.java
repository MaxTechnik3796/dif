package cz.maxtechnik.dif.init.auto_loader;

import cz.maxtechnik.dif.init.DifModMenus;
import cz.maxtechnik.dif.client.gui.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public class DifModScreens{
	@SubscribeEvent
	public static void clientLoad(FMLClientSetupEvent event){
		event.enqueueWork(()->{
			MenuScreens.register(DifModMenus.SUPER_BOX_GUI.get(),SuperBoxGuiScreen::new);
		});
	}
}
