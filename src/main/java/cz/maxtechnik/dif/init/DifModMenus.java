package cz.maxtechnik.dif.init;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.world.inventory.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DifModMenus{
	public static final DeferredRegister<MenuType<?>>REGISTRY=DeferredRegister.create(ForgeRegistries.MENU_TYPES,DifMod.MODID);
	public static final RegistryObject<MenuType<SuperBoxGuiMenu>>SUPER_BOX_GUI=REGISTRY.register("super_box_gui",()->IForgeMenuType.create(SuperBoxGuiMenu::new));
}

