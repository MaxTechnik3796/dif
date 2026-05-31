package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.block.entity.BlastSmelteryControllerBlockEntity;
import cz.maxtechnik.dif.block.entity.CokeOvenControllerBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.item.armor.ElectroRunners;
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
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;
public class DifModCapabilities{
	static BlockCapability<IItemHandler,@Nullable Direction> bITEM=Capabilities.ItemHandler.BLOCK;
	static BlockCapability<IFluidHandler,@Nullable Direction> bFLUID=Capabilities.FluidHandler.BLOCK;
	static BlockCapability<IEnergyStorage,@Nullable Direction> bENERGY=Capabilities.EnergyStorage.BLOCK;
	static ItemCapability<IFluidHandlerItem,@Nullable Void> iFLUID=Capabilities.FluidHandler.ITEM;
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
		event.registerBlockEntity(bITEM,COKE_OVEN_CONTROLLER.get(),(be,side)->{
			if(side!=null) return new SidedInvWrapper(be,side);
			return be.getInventory();
		});
		event.registerBlockEntity(bITEM,COKE_OVEN_CONTROLLER.get(),(be,side)->{
			if(side!=null) return new SidedInvWrapper(be,side);
			return be.getInventory();
		});
		event.registerBlockEntity(bITEM,BLAST_SMELTERY_CONTROLLER.get(),(be,side)->{
			if(side!=null) return new SidedInvWrapper(be,side);
			return be.getInventory();
		});
		event.registerBlockEntity(bITEM,BLAST_SMELTERY.get(),(be,side)->{
			var ctrl=be.getController();
			if(ctrl==null) return null;
			return side!=null?new SidedInvWrapper(ctrl,side):ctrl.getInventory();
		});
	}
	private static void registerFluidCapabilities(RegisterCapabilitiesEvent event){
		event.registerItem(iFLUID,(stack,side)->new Jetpack.Chestplate.FluidHandler(stack),DifModItems.JETPACK.get());
		event.registerBlockEntity(bFLUID,DISTILLATION_TANK.get(),(be,ctx)->be.fluidTank());
		event.registerBlockEntity(bFLUID,ENGINE.get(),(be,side)->be.fluidTank);
		event.registerBlockEntity(bFLUID,COKE_OVEN_CONTROLLER.get(),CokeOvenControllerBlockEntity::getFluidCapability);
		event.registerBlockEntity(bFLUID,COKE_OVEN.get(),(be,side)->{
			var ctrl=be.getController();
			return ctrl!=null?ctrl.getFluidCapability(side):null;
		});
		event.registerBlockEntity(bFLUID,BLAST_SMELTERY_CONTROLLER.get(),BlastSmelteryControllerBlockEntity::getFluidCapability);
		event.registerBlockEntity(bFLUID,BLAST_SMELTERY.get(),(be,side)->{
			var ctrl=be.getController();
			return ctrl!=null?ctrl.getFluidCapability(side):null;
		});
	}
	private static void registerEnergyCapabilities(RegisterCapabilitiesEvent event){
		ElectroRunners.Boots.registerCapability(event,DifModItems.ELECTRO_RUNNERS.get());
		event.registerBlockEntity(bENERGY,QUARRY.get(),(be,side)->be.getEnergyStorage());
		event.registerBlockEntity(bENERGY,BURNING_GENERATOR.get(),(be,side)->be.getEnergyStorage());
	}
}
