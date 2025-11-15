package cz.maxtechnik.dif.init.gui;

import cz.maxtechnik.dif.gui.screen.BurningGeneratorScreen;
import cz.maxtechnik.dif.gui.screen.SpecialCraftingScreen;
import cz.maxtechnik.dif.gui.screen.SuperBoxScreen;
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
			MenuScreens.register(DifModMenus.SUPER_BOX_MENU.get(),SuperBoxScreen::new);
			MenuScreens.register(DifModMenus.GENERATOR_MENU.get(),BurningGeneratorScreen::new);
			MenuScreens.register(DifModMenus.SPECIAL_CRAFTING_MENU.get(),SpecialCraftingScreen::new);
		});
	}
}
