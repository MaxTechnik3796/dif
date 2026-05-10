package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.DistillationTank;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.HeatLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
public class DistillationControllerBlockEntity extends BlockEntity{
	public static final int CAPACITY=8000;
	public static int timer=0;
	public DistillationControllerBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.DISTILLATION_CONTROLLER.get(),pos,blockState);
	}
	public final FluidTank tank=new FluidTank(CAPACITY){
		@Override
		protected void onContentsChanged(){
			setChanged();
			if(level!=null&&!level.isClientSide)
				level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
		}
	};
	public IFluidHandler getFluidHandler(){
		return tank;
	}
	public static HeatLevel getHeatBelow(Level level,BlockPos pos){
		BlockState below=level.getBlockState(pos.below());
		try{
			for(var prop: below.getProperties()){
				if(!prop.getName().equals("blaze")) continue;
				String val=below.getValue(prop).toString().toLowerCase();
				if(val.equals("seething")) return HeatLevel.SUPERHEATED;
				if(val.equals("kindled")) return HeatLevel.HEATED;
				return HeatLevel.NONE;
			}
		}catch(Exception ignored){
		}
		return HeatLevel.NONE;
	}
	public static void serverTick(Level level,BlockPos pos,BlockState blockState,DistillationControllerBlockEntity blockEntity){
		HeatLevel heatLevel=getHeatBelow(level,pos);
		if(heatLevel.equals(HeatLevel.NONE)) return;
		if(++timer>=heatLevel.speed){
			timer=0;
			IFluidHandler tank=blockEntity.tank;
			FluidStack input=new FluidStack(DifModFluids.CRUDE_OIL,10);
			FluidStack[] output={
					new FluidStack(DifModFluids.JETPACK_FUEL,5),
					new FluidStack(Fluids.WATER,5)
			};
			if(!(level.getBlockState(pos.above()).getBlock()instanceof DistillationTank)) return;
			if(tank.getFluidInTank(0).getAmount()>=input.getAmount()&&tank.getFluidInTank(0).getFluid().equals(input.getFluid())){
				for(int i=0;i<output.length;i++){
					IFluidHandler outputTank=level.getCapability(Capabilities.FluidHandler.BLOCK,pos.above(i+1),null);
					if(outputTank==null||!(level.getBlockState(pos.above()).getBlock()instanceof DistillationTank))break;
					outputTank.fill(output[i],IFluidHandler.FluidAction.EXECUTE);
				}
				tank.drain(input.getAmount(),IFluidHandler.FluidAction.EXECUTE);
			}
		}
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.saveAdditional(tag,provider);
		tag.put("tank",tank.writeToNBT(provider,new CompoundTag()));
		tag.putInt("timer",timer);
	}
	@Override
	public void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(tag,provider);
		if(tag.contains("tank")) tank.readFromNBT(provider,tag.getCompound("tank"));
		if(tag.contains("timer")) timer=tag.getInt("timer");
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider){
		return saveWithFullMetadata(provider);
	}
}