package cz.maxtechnik.dif.block;

import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import cz.maxtechnik.dif.config.DifModServerConfig;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
public class MithrilFluidTank extends FluidTankBlock{
	public MithrilFluidTank(){
		super(BlockBehaviour.Properties.of()
				.strength(4.0F,5.0F)
				.sound(SoundType.METAL)
				.noOcclusion()
				.isRedstoneConductor((p1,p2,p3)->true)
				.requiresCorrectToolForDrops(),false);
	}
	@Override
	public BlockEntityType<? extends FluidTankBlockEntity> getBlockEntityType(){
		return DifModBlockEntities.MITHRIL_FLUID_TANK.get();
	}
	// BLOCK ENTITY
	@SuppressWarnings("unchecked")
	public static class Entity extends FluidTankBlockEntity{
		private int customCapacityMultiplier=0;
		public Entity(BlockEntityType<?> type,BlockPos pos,BlockState state){
			super(type,pos,state);
		}
		public static int getCapacityMultiplier(){
			try{
				return DifModServerConfig.MITHRIL_FLUID_TANK_CAPACITY.get()*1000;
			}catch(Exception e){
				return 32000;
			}
		}
		public int getEffectiveCapacityMultiplier(){
			if(hasLevel()) {
                assert level != null;
                if (!level.isClientSide) {
                    return getCapacityMultiplier();
                }
            }
			if(customCapacityMultiplier>0){
				return customCapacityMultiplier;
			}
			return getCapacityMultiplier();
		}
		@Override
		protected SmartFluidTank createInventory(){
			return new SmartFluidTank(getEffectiveCapacityMultiplier(),this::onFluidStackChanged);
		}
		@Override
		public void applyFluidTankSize(int blocks){
			tankInventory.setCapacity(blocks*getEffectiveCapacityMultiplier());
			int overflow=tankInventory.getFluidAmount()-tankInventory.getCapacity();
			if(overflow>0)
				tankInventory.drain(overflow,FluidAction.EXECUTE);
			forceFluidLevelUpdate=true;
		}
		@Override
		public int getTankSize(int tank){
			return getEffectiveCapacityMultiplier();
		}
		@Override
		public float getFillState(){
			FluidTankBlockEntity controller=getControllerBE();
			if(controller!=null&&controller!=this){
				return controller.getFillState();
			}
			int cap=tankInventory.getCapacity();
			return cap>0?(float)tankInventory.getFluidAmount()/cap:0f;
		}
		@Override
		public void write(CompoundTag compound,HolderLookup.Provider registries,boolean clientPacket){
			super.write(compound,registries,clientPacket);
			if(isController()){
				compound.putInt("MithrilCapacityMultiplier",getCapacityMultiplier());
			}
		}
		@Override
		public void writeSafe(CompoundTag compound,HolderLookup.Provider registries){
			super.writeSafe(compound,registries);
			if(isController()){
				compound.putInt("MithrilCapacityMultiplier",getCapacityMultiplier());
			}
		}
		@Override
		protected void read(CompoundTag compound,HolderLookup.Provider registries,boolean clientPacket){
			if(compound.contains("MithrilCapacityMultiplier")){
				customCapacityMultiplier=compound.getInt("MithrilCapacityMultiplier");
			}
			CompoundTag tankContent=compound.contains("TankContent")?compound.getCompound("TankContent").copy():null;
			CompoundTag modified=compound.copy();
			if(tankContent!=null){
				modified.remove("TankContent");
			}
			super.read(modified,registries,clientPacket);
			if(isController()){
				int capacity=getTotalTankSize()*getEffectiveCapacityMultiplier();
				tankInventory.setCapacity(capacity);
				if(tankContent!=null){
					tankInventory.readFromNBT(registries,tankContent);
					if(tankInventory.getSpace()<0)
						tankInventory.drain(-tankInventory.getSpace(),FluidAction.EXECUTE);
				}
				float fillState=getFillState();
				if(compound.contains("ForceFluidLevel")||getFluidLevel()==null){
					setFluidLevel(LerpedFloat.linear().startWithValue(fillState));
				}
				LerpedFloat fluidLevel=getFluidLevel();
				if(fluidLevel!=null){
					fluidLevel.chase(fillState,0.5f,Chaser.EXP);
					if(compound.contains("LazySync"))
						fluidLevel.chase(fluidLevel.getChaseTarget(),0.125f,Chaser.EXP);
				}
			}
		}
		@Override
		public void lazyTick(){
			super.lazyTick();
			if(isController()){
				int expectedCap=getTotalTankSize()*getEffectiveCapacityMultiplier();
				if(tankInventory.getCapacity()!=expectedCap){
					tankInventory.setCapacity(expectedCap);
					int overflow=tankInventory.getFluidAmount()-tankInventory.getCapacity();
					if(overflow>0)
						tankInventory.drain(overflow,FluidAction.EXECUTE);
					forceFluidLevelUpdate=true;
                    assert level != null;
                    if(!level.isClientSide){
						setChanged();
						sendData();
					}
				}
			}
		}
		public IFluidHandler getFluidCapability(){
			return fluidCapability;
		}
	}
}