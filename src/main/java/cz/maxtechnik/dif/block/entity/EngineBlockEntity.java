package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import cz.maxtechnik.dif.block.Engine;
import cz.maxtechnik.dif.block.EngineExtender;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.FuelType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.stream.Stream;

import static cz.maxtechnik.dif.block.Engine.FACING;
import static cz.maxtechnik.dif.block.Engine.INVERT;
public class EngineBlockEntity extends GeneratingKineticBlockEntity{
	boolean generating=false;
	boolean uGenerating=false;
	float speed=0F;
	float uSpeed=0F;
	float su=0F;
	float uSu=0F;
	public EngineBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.ENGINE.get(),pos,blockState);
	}
	public final FluidTank fluidTank=new FluidTank(1000){
		@Override
		protected void onContentsChanged(){
			super.onContentsChanged();
			setChanged();
			if(level!=null) level.sendBlockUpdated(worldPosition,level.getBlockState(worldPosition),level.getBlockState(worldPosition),2);
		}
	};
	@Override
	public float getGeneratedSpeed(){
		if(level==null) return 0F;
		return generating?speed*(level.getBlockState(worldPosition).getValue(INVERT)?-1F:1F):0F;
	}
	@Override
	public float calculateAddedStressCapacity(){
		lastCapacityProvided=generating?su:0F;
		return lastCapacityProvided;
	}
	@Override
	public void read(CompoundTag tag,HolderLookup.Provider registries,boolean clientPacket){
		super.read(tag,registries,clientPacket);
		if(tag.get("fluidTank") instanceof CompoundTag fluidTag) fluidTank.readFromNBT(registries,fluidTag);
	}
	@Override
	public void write(CompoundTag tag,HolderLookup.Provider registries,boolean clientPacket){
		super.write(tag,registries,clientPacket);
		tag.put("fluidTank",fluidTank.writeToNBT(registries,new CompoundTag()));
	}
	@Override
	public void initialize(){
		super.initialize();
		if(level!=null&&!level.isClientSide) updateGeneratedRotation();
	}
	@Override
	public void tick(){
		super.tick();
		if(level==null||!(getBlockState().getBlock() instanceof Engine)) return;
		if(reActivateSource){
			updateGeneratedRotation();
			reActivateSource=false;
		}
		if(!level.hasNeighborSignal(worldPosition)){
			FuelType fuel=scanExtenders();
			if(fuel.equals(FuelType.INVALID)){
				generating=false;
				speed=0F;
				su=0F;
			}else if(fuel.equals(FuelType.DIESEL)){
				speed=countExtenders()*12F;
				su=countExtenders()*2F;
				generating=fluidTank.getFluidAmount()>0;
				fluidTank.drain(1,IFluidHandler.FluidAction.EXECUTE);
			}else if(fuel.equals(FuelType.HEAVY_FUEL_OIL)){
				speed=countExtenders()*10F;
				su=countExtenders()*2.3F;
				generating=fluidTank.getFluidAmount()>0;
				fluidTank.drain(1,IFluidHandler.FluidAction.EXECUTE);
			}else if(fuel.equals(FuelType.GASOLINE)){
				speed=countExtenders()*8F;
				su=countExtenders()*3F;
				generating=fluidTank.getFluidAmount()>0;
				fluidTank.drain(1,IFluidHandler.FluidAction.EXECUTE);
			}else if(fuel.equals(FuelType.LPG)){
				speed=countExtenders()*9F;
				su=countExtenders()*2.2F;
				generating=fluidTank.getFluidAmount()>0;
				fluidTank.drain(1,IFluidHandler.FluidAction.EXECUTE);
			}
		}
		// FIX: původně 3 samostatné if-bloky → updateGeneratedRotation() mohlo běžet 3× per tick.
		if(uGenerating!=generating||uSpeed!=speed||uSu!=su){
			updateGeneratedRotation();
			uGenerating=generating;
			uSpeed=speed;
			uSu=su;
		}
	}
	private FuelType scanExtenders(){
		if(level==null) return FuelType.INVALID;
		BlockState ownState=getBlockState();
		if(!(ownState.getBlock() instanceof Engine)) return FuelType.INVALID;
		Direction.Axis axis=ownState.getValue(FACING).getAxis();
		FuelType ext0=getExtenderFuel(worldPosition.above());
		FuelType ext1=FuelType.INVALID;
		FuelType ext2=FuelType.INVALID;
		if(axis==Direction.Axis.Z){
			ext1=getExtenderFuel(worldPosition.east());
			ext2=getExtenderFuel(worldPosition.west());
		}else if(axis==Direction.Axis.X){
			ext1=getExtenderFuel(worldPosition.north());
			ext2=getExtenderFuel(worldPosition.south());
		}
		long validCount=Stream.of(ext0,ext1,ext2).filter(f->f!=FuelType.INVALID).count();
		long uniqueValidCount=Stream.of(ext0,ext1,ext2).filter(f->f!=FuelType.INVALID).distinct().count();
		if(validCount==0) return FuelType.INVALID;
		if((validCount==3&&uniqueValidCount==1)||(validCount==2&&uniqueValidCount==1)||(validCount==1))
			return Stream.of(ext0,ext1,ext2).filter(f->f!=FuelType.INVALID).findFirst().orElse(FuelType.INVALID);
		return FuelType.INVALID;
	}
	private FuelType getExtenderFuel(BlockPos pos){
		if(level==null) return FuelType.INVALID;
		Block block=level.getBlockState(pos).getBlock();
		if(!(block instanceof EngineExtender)) return FuelType.INVALID;
		if(block.equals(DifModBlocks.ENGINE_EXTENDER_DIESEL.get())) return FuelType.DIESEL;
		if(block.equals(DifModBlocks.ENGINE_EXTENDER_GASOLINE.get())) return FuelType.GASOLINE;
		if(block.equals(DifModBlocks.ENGINE_EXTENDER_LPG.get())) return FuelType.LPG;
		if(block.equals(DifModBlocks.ENGINE_EXTENDER_HEAVY_FUEL_OIL.get())) return FuelType.HEAVY_FUEL_OIL;
		return FuelType.INVALID;
	}
	private int countExtenders(){
		if(level==null) return 0;
		BlockState ownState=getBlockState();
		if(!(ownState.getBlock() instanceof Engine)) return 0;
		Direction.Axis axis=ownState.getValue(FACING).getAxis();
		int count=0;
		if(isEngineExtender(worldPosition.above())) count++;
		if(axis==Direction.Axis.Z){
			if(isEngineExtender(worldPosition.east())) count++;
			if(isEngineExtender(worldPosition.west())) count++;
		}else if(axis==Direction.Axis.X){
			if(isEngineExtender(worldPosition.north())) count++;
			if(isEngineExtender(worldPosition.south())) count++;
		}
		return count;
	}
	private boolean isEngineExtender(BlockPos pos){
		if(level==null) return false;
		return level.getBlockState(pos).getBlock() instanceof EngineExtender;
	}
}