package cz.maxtechnik.dif.init.gui;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.gui.menu.BurningGeneratorMenu;
import cz.maxtechnik.dif.gui.menu.BrassBarrelMenu;
import cz.maxtechnik.dif.gui.menu.SpecialCraftingMenu;
import cz.maxtechnik.dif.gui.menu.SuperBoxMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class DifModMenus<T extends AbstractContainerMenu>implements FeatureElement,net.minecraftforge.common.extensions.IForgeMenuType<T> {
	public static final DeferredRegister<MenuType<?>>REGISTRY=DeferredRegister.create(ForgeRegistries.MENU_TYPES,DifMod.MODID);
	public static final RegistryObject<MenuType<SuperBoxMenu>>SUPER_BOX_MENU=REGISTRY.register("super_box_menu",()->IForgeMenuType.create(SuperBoxMenu::new));
	public static final RegistryObject<MenuType<BrassBarrelMenu>>BRASS_BARREL_MENU=REGISTRY.register("brass_barrel_menu",()->IForgeMenuType.create(BrassBarrelMenu::new));
	public static final RegistryObject<MenuType<BurningGeneratorMenu>>GENERATOR_MENU=REGISTRY.register("generator_menu",()->IForgeMenuType.create(BurningGeneratorMenu::new));
	public static final RegistryObject<MenuType<SpecialCraftingMenu>>SPECIAL_CRAFTING_MENU=REGISTRY.register("special_crafting_menu",()->IForgeMenuType.create(SpecialCraftingMenu::new));
	private FeatureFlagSet requiredFeatures;
	private MenuType.MenuSupplier<T> constructor;

	public DifModMenus(MenuType.MenuSupplier<T> constructor){
		this.constructor=constructor;
	}

	@Override
	public @NotNull FeatureFlagSet requiredFeatures(){
		return this.requiredFeatures;
	}
	private static <T extends AbstractContainerMenu> MenuType<T> register(String p_39989_, MenuType.MenuSupplier<T> p_39990_) {
		return Registry.register(BuiltInRegistries.MENU, p_39989_, new MenuType<>(p_39990_, FeatureFlags.VANILLA_SET));
	}

	private static <T extends AbstractContainerMenu> MenuType<T> register(String p_267295_, MenuType.MenuSupplier<T> p_266945_, FeatureFlag... p_267055_) {
		return Registry.register(BuiltInRegistries.MENU, p_267295_, new MenuType<>(p_266945_, FeatureFlags.REGISTRY.subset(p_267055_)));
	}
	public void MenuType(MenuType.MenuSupplier<T> p_267054_,FeatureFlagSet p_266909_) {
		this.constructor = p_267054_;
		this.requiredFeatures = p_266909_;
	}
	public T create(int p_39986_, Inventory p_39987_) {
		return this.constructor.create(p_39986_, p_39987_);
	}
	@Override
	public T create(int windowId, Inventory playerInv, net.minecraft.network.FriendlyByteBuf extraData) {
		if (this.constructor instanceof net.minecraftforge.network.IContainerFactory) {
			return ((net.minecraftforge.network.IContainerFactory<T>) this.constructor).create(windowId, playerInv, extraData);
		}
		return create(windowId, playerInv);
	}
	public interface MenuSupplier<T extends AbstractContainerMenu> {
		T create(int p_39995_, Inventory p_39996_);
	}
}

