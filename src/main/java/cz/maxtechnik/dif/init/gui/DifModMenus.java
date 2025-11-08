package cz.maxtechnik.dif.init.gui;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.gui.menu.BurningGeneratorMenu;
import cz.maxtechnik.dif.gui.menu.SuperBoxMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DifModMenus{
	public static final DeferredRegister<MenuType<?>>REGISTRY=DeferredRegister.create(ForgeRegistries.MENU_TYPES,DifMod.MODID);
	public static final RegistryObject<MenuType<SuperBoxMenu>>SUPER_BOX_MENU=REGISTRY.register("super_box_menu",()->IForgeMenuType.create(SuperBoxMenu::new));
	public static final RegistryObject<MenuType<BurningGeneratorMenu>>GENERATOR_MENU=REGISTRY.register("generator_menu",()->IForgeMenuType.create(BurningGeneratorMenu::new));
}

