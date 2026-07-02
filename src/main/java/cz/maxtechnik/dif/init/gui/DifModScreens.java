package cz.maxtechnik.dif.init.gui;

import cz.maxtechnik.dif.gui.screen.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import cz.maxtechnik.dif.DifMod;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = DifMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DifModScreens {

	@SubscribeEvent
	public static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(DifModMenus.SUPER_BOX.get(), SuperBoxScreen::new);
		event.register(DifModMenus.COPPER_BARREL.get(), CopperBarrelScreen::new);
		event.register(DifModMenus.SPACE_CRATE.get(), SpaceCrateScreen::new);
		event.register(DifModMenus.ANDESITE_BARREL.get(), AndesiteBarrelScreen::new);
		event.register(DifModMenus.BRASS_BARREL.get(), BrassBarrelScreen::new);
		event.register(DifModMenus.GENERATOR.get(), BurningGeneratorScreen::new);
		event.register(DifModMenus.OLD_CHEST.get(), OldChestScreen::new);
		event.register(DifModMenus.SPACESHIP.get(), SpaceshipScreen::new);
		event.register(DifModMenus.QUARRY.get(), QuarryScreen::new);
	}
}