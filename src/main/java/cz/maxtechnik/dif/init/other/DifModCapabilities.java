package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.item.armor.Jetpack;

import static cz.maxtechnik.dif.init.other.DifModBlockEntities.*;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
public class DifModCapabilities{
	static final BlockCapability<IItemHandler,@Nullable Direction> bITEM=Capabilities.ItemHandler.BLOCK;
	static final BlockCapability<IFluidHandler,@Nullable Direction> bFLUID=Capabilities.FluidHandler.BLOCK;
	static final BlockCapability<IEnergyStorage,@Nullable Direction> bENERGY=Capabilities.EnergyStorage.BLOCK;
	static final ItemCapability<IFluidHandlerItem,@Nullable Void> iFLUID=Capabilities.FluidHandler.ITEM;
	public static void registerCapabilities(RegisterCapabilitiesEvent event){
		registerItemCapabilities(event);
		registerFluidCapabilities(event);
		registerEnergyCapabilities(event);
	}
	private static void registerItemCapabilities(RegisterCapabilitiesEvent event){
		event.registerBlockEntity(bITEM,QUARRY.get(),(be,side)->be.getInventory());
		event.registerBlockEntity(bITEM,ANDESITE_BARREL.get(),(be,side)->be.getInventory());
		event.registerBlockEntity(bITEM,COPPER_BARREL.get(),(be,side)->be.getInventory());
		event.registerBlockEntity(bITEM,BRASS_BARREL.get(),(be,side)->be.getInventory());
		event.registerBlockEntity(bITEM,SUPER_BOX.get(),(be,side)->be.getInventory());
		event.registerBlockEntity(bITEM,OLD_CHEST.get(),(be,side)->be.getInventory());
		event.registerBlockEntity(bITEM,BURNING_GENERATOR.get(),(be,side)->be.getInventory());
		event.registerBlockEntity(bITEM,SPACE_CRATE.get(),(be,side)->be.getInventory());
		event.registerBlockEntity(bITEM,SPACESHIP.get(),(be,side)->be.getInventory());
	}
	private static void registerFluidCapabilities(RegisterCapabilitiesEvent event){
		event.registerItem(iFLUID,(stack,side)->new Jetpack.Chestplate.FluidHandler(stack),DifModItems.JETPACK.get());
		event.registerBlockEntity(bFLUID,DISTILLATION_TANK.get(),(be,ctx)->be.fluidTank());
		event.registerBlockEntity(bFLUID,ENGINE.get(),(be,side)->be.fluidTank);
	}
	private static void registerEnergyCapabilities(RegisterCapabilitiesEvent event){
		event.registerBlockEntity(bENERGY,QUARRY.get(),(be,side)->be.getEnergyStorage());
		event.registerBlockEntity(bENERGY,BURNING_GENERATOR.get(),(be,side)->be.getEnergyStorage());
	}
}
