package cz.maxtechnik.dif.init.gui;

import cz.maxtechnik.dif.gui.screen.*;
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
			MenuScreens.register(DifModMenus.SUPER_BOX.get(),SuperBoxScreen::new);
			MenuScreens.register(DifModMenus.COPPER_BARREL.get(),CopperBarrelScreen::new);
			MenuScreens.register(DifModMenus.ANDESITE_BARREL.get(),AndesiteBarrelScreen::new);
			MenuScreens.register(DifModMenus.BRASS_BARREL.get(),BrassBarrelScreen::new);
			MenuScreens.register(DifModMenus.GENERATOR.get(),BurningGeneratorScreen::new);
			MenuScreens.register(DifModMenus.SPECIAL_CRAFTING.get(),SpecialCraftingScreen::new);
			MenuScreens.register(DifModMenus.OLD_CHEST.get(),OldChestScreen::new);
			MenuScreens.register(DifModMenus.SPACESHIP.get(),SpaceshipScreen::new);
		});
	}
}
