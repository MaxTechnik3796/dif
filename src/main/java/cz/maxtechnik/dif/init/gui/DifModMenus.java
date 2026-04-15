package cz.maxtechnik.dif.init.gui;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.gui.menu.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
public class DifModMenus<T extends AbstractContainerMenu> implements FeatureElement, net.minecraftforge.common.extensions.IForgeMenuType<T>{
	public static final DeferredRegister<MenuType<?>>REGISTRY=DeferredRegister.create(ForgeRegistries.MENU_TYPES,DifMod.MODID);
	public static final RegistryObject<MenuType<SuperBoxMenu>> SUPER_BOX=REGISTRY.register("super_box",()->IForgeMenuType.create(SuperBoxMenu::new));
	public static final RegistryObject<MenuType<AndesiteBarrelMenu>> ANDESITE_BARREL=REGISTRY.register("andesite_barrel",()->IForgeMenuType.create(AndesiteBarrelMenu::new));
	public static final RegistryObject<MenuType<CopperBarrelMenu>> COPPER_BARREL=REGISTRY.register("copper_barrel",()->IForgeMenuType.create(CopperBarrelMenu::new));
	public static final RegistryObject<MenuType<BrassBarrelMenu>> BRASS_BARREL=REGISTRY.register("brass_barrel",()->IForgeMenuType.create(BrassBarrelMenu::new));
	public static final RegistryObject<MenuType<OldChestMenu>> OLD_CHEST=REGISTRY.register("old_chest",()->IForgeMenuType.create(OldChestMenu::new));
	public static final RegistryObject<MenuType<BurningGeneratorMenu>> GENERATOR=REGISTRY.register("generator",()->IForgeMenuType.create(BurningGeneratorMenu::new));
	public static final RegistryObject<MenuType<SpecialCraftingMenu>> SPECIAL_CRAFTING=REGISTRY.register("special_crafting",()->IForgeMenuType.create(SpecialCraftingMenu::new));
	public static final RegistryObject<MenuType<SpaceshipMenu>> SPACESHIP=REGISTRY.register("spaceship",()->IForgeMenuType.create(SpaceshipMenu::new));
	public static final RegistryObject<MenuType<MegaBackpackMenu>> MEGA_BACKPACK = REGISTRY.register("mega_backpack",() -> IForgeMenuType.create(MegaBackpackMenu::new));




	private FeatureFlagSet requiredFeatures;
	private final MenuType.MenuSupplier<T> constructor;
	public DifModMenus(MenuType.MenuSupplier<T> constructor){
		this.constructor=constructor;
	}
	@Override
	public @NotNull FeatureFlagSet requiredFeatures(){
		return this.requiredFeatures;
	}
	public T create(int p_39986_,Inventory p_39987_){
		return this.constructor.create(p_39986_,p_39987_);
	}
	@Override
	public T create(int windowId,Inventory playerInv,net.minecraft.network.FriendlyByteBuf extraData){
		if(this.constructor instanceof net.minecraftforge.network.IContainerFactory){
			return ((net.minecraftforge.network.IContainerFactory<T>)this.constructor).create(windowId,playerInv,extraData);
		}
		return create(windowId,playerInv);
	}
}

