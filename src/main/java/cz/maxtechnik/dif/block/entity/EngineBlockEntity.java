package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import cz.maxtechnik.dif.block.barrel.EngineExtender;
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
			if(level!=null)
				level.sendBlockUpdated(worldPosition,level.getBlockState(worldPosition),level.getBlockState(worldPosition),2);
		}
	};
	@Override
	public float getGeneratedSpeed(){
		return generating?speed:0F;
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
		assert level!=null;
		int engine4=level.getBlockState(worldPosition).getBlock().equals(DifModBlocks.ENGINE4.get())?2:1;
		if(reActivateSource){
			updateGeneratedRotation();
			reActivateSource=false;
		}
		if(scanExtenders().equals(FuelType.INVALID)){
			generating=false;
			speed=0F;
			su=0F;
		}else if(scanExtenders().equals(FuelType.DIESEL)){
			speed=countExtenders()*10F;
			su=countExtenders()*2F;
			generating=fluidTank.getFluidAmount()>0;
			fluidTank.drain(1,IFluidHandler.FluidAction.EXECUTE);
		}else if(scanExtenders().equals(FuelType.HEAVY_FUEL_OIL)){
			speed=countExtenders()*500F;
			su=countExtenders()*2.3F;
			generating=fluidTank.getFluidAmount()>0;
			fluidTank.drain(1,IFluidHandler.FluidAction.EXECUTE);
		}
		if(uGenerating!=generating){
			updateGeneratedRotation();
			uGenerating=generating;
		}
		if(uSpeed!=speed){
			updateGeneratedRotation();
			uSpeed=speed;
		}
		if(uSu!=su){
			updateGeneratedRotation();
			uSu=su;
		}
	}
	private FuelType scanExtenders(){
		assert level!=null;
		Direction.Axis axis=level.getBlockState(worldPosition).getValue(FACING).getAxis();
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
		assert level!=null;
		Block block=level.getBlockState(pos).getBlock();
		if(!(block instanceof EngineExtender)) return FuelType.INVALID;
		if(block.equals(DifModBlocks.ENGINE_EXTENDER_DIESEL.get())) return FuelType.DIESEL;
		if(block.equals(DifModBlocks.ENGINE_EXTENDER_GASOLINE.get())) return FuelType.GASOLINE;
		if(block.equals(DifModBlocks.ENGINE_EXTENDER_LPG.get())) return FuelType.LPG;
		if(block.equals(DifModBlocks.ENGINE_EXTENDER_HEAVY_FUEL_OIL.get())) return FuelType.HEAVY_FUEL_OIL;
		return FuelType.INVALID;
	}
	private int countExtenders(){
		assert level!=null;
		Direction.Axis axis=level.getBlockState(worldPosition).getValue(FACING).getAxis();
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
		assert level!=null;
		return level.getBlockState(pos).getBlock() instanceof EngineExtender;
	}
}