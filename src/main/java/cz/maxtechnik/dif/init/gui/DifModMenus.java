package cz.maxtechnik.dif.init.gui;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.gui.menu.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DifModMenus {
	// Opraveno - v 1.21.1 je to Registries.MENU
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, DifMod.MODID);

	public static final DeferredHolder<MenuType<?>, MenuType<SuperBoxMenu>> SUPER_BOX = REGISTRY.register("super_box", () -> IMenuTypeExtension.create(SuperBoxMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<AndesiteBarrelMenu>> ANDESITE_BARREL = REGISTRY.register("andesite_barrel", () -> IMenuTypeExtension.create(AndesiteBarrelMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<CopperBarrelMenu>> COPPER_BARREL = REGISTRY.register("copper_barrel", () -> IMenuTypeExtension.create(CopperBarrelMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<BrassBarrelMenu>> BRASS_BARREL = REGISTRY.register("brass_barrel", () -> IMenuTypeExtension.create(BrassBarrelMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<OldChestMenu>> OLD_CHEST = REGISTRY.register("old_chest", () -> IMenuTypeExtension.create(OldChestMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<BurningGeneratorMenu>> GENERATOR = REGISTRY.register("generator", () -> IMenuTypeExtension.create(BurningGeneratorMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<SpecialCraftingMenu>> SPECIAL_CRAFTING = REGISTRY.register("special_crafting", () -> IMenuTypeExtension.create(SpecialCraftingMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<SpaceshipMenu>> SPACESHIP = REGISTRY.register("spaceship", () -> IMenuTypeExtension.create(SpaceshipMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<MegaBackpackMenu>> MEGA_BACKPACK = REGISTRY.register("mega_backpack", () -> IMenuTypeExtension.create(MegaBackpackMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<QuarryMenu>> QUARRY = REGISTRY.register("quarry", () -> IMenuTypeExtension.create(QuarryMenu::new));
}