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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.stream.Stream;

import static cz.maxtechnik.dif.block.Engine.FACING;
public class EngineBlockEntity extends GeneratingKineticBlockEntity{
	boolean generating=false;
	boolean update=false;
	boolean validExtenders=true;
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
		return generating?4F:0F;
	}
	@Override
	public float calculateAddedStressCapacity(){
		lastCapacityProvided=generating?4F:0F;
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
		if(reActivateSource){
			updateGeneratedRotation();
			reActivateSource=false;
		}
		if(validExtenders){
			generating=fluidTank.getFluidAmount()>0;
			fluidTank.drain(1,IFluidHandler.FluidAction.EXECUTE);
		}
		if(update!=generating){
			updateGeneratedRotation();
			update=generating;
		}
	}
	private FuelType scanExtenders(){
		assert level!=null;
		Direction.Axis axis=level.getBlockState(worldPosition).getValue(FACING).getAxis();
		FuelType ext0=FuelType.INVALID;
		FuelType ext1=FuelType.INVALID;
		FuelType ext2=FuelType.INVALID;
		if(axis.equals(Direction.Axis.Z)){
			if(level.getBlockState(worldPosition.above()).getBlock() instanceof EngineExtender){
				if(level.getBlockState(worldPosition.above()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_DIESEL.get()))
					ext0=FuelType.DIESEL;
				else if(level.getBlockState(worldPosition.above()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_GASOLINE.get()))
					ext0=FuelType.GASOLINE;
				else if(level.getBlockState(worldPosition.above()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_LPG.get()))
					ext0=FuelType.LPG;
				else if(level.getBlockState(worldPosition.above()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_HEAVY_FUEL_OIL.get()))
					ext0=FuelType.HEAVY_FUEL_OIL;
			}
			if(level.getBlockState(worldPosition.east()).getBlock() instanceof EngineExtender){
				if(level.getBlockState(worldPosition.east()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_DIESEL.get()))
					ext1=FuelType.DIESEL;
				else if(level.getBlockState(worldPosition.east()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_GASOLINE.get()))
					ext1=FuelType.GASOLINE;
				else if(level.getBlockState(worldPosition.east()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_LPG.get()))
					ext1=FuelType.LPG;
				else if(level.getBlockState(worldPosition.east()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_HEAVY_FUEL_OIL.get()))
					ext1=FuelType.HEAVY_FUEL_OIL;
			}
			if(level.getBlockState(worldPosition.west()).getBlock() instanceof EngineExtender){
				if(level.getBlockState(worldPosition.west()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_DIESEL.get()))
					ext2=FuelType.DIESEL;
				else if(level.getBlockState(worldPosition.west()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_GASOLINE.get()))
					ext2=FuelType.GASOLINE;
				else if(level.getBlockState(worldPosition.west()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_LPG.get()))
					ext2=FuelType.LPG;
				else if(level.getBlockState(worldPosition.west()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_HEAVY_FUEL_OIL.get()))
					ext2=FuelType.HEAVY_FUEL_OIL;
			}
		}else if(axis.equals(Direction.Axis.X)){
			if(level.getBlockState(worldPosition.above()).getBlock() instanceof EngineExtender){
				if(level.getBlockState(worldPosition.above()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_DIESEL.get()))
					ext0=FuelType.DIESEL;
				else if(level.getBlockState(worldPosition.above()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_GASOLINE.get()))
					ext0=FuelType.GASOLINE;
				else if(level.getBlockState(worldPosition.above()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_LPG.get()))
					ext0=FuelType.LPG;
				else if(level.getBlockState(worldPosition.above()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_HEAVY_FUEL_OIL.get()))
					ext0=FuelType.HEAVY_FUEL_OIL;
			}
			if(level.getBlockState(worldPosition.north()).getBlock() instanceof EngineExtender){
				if(level.getBlockState(worldPosition.north()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_DIESEL.get()))
					ext1=FuelType.DIESEL;
				else if(level.getBlockState(worldPosition.north()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_GASOLINE.get()))
					ext1=FuelType.GASOLINE;
				else if(level.getBlockState(worldPosition.north()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_LPG.get()))
					ext1=FuelType.LPG;
				else if(level.getBlockState(worldPosition.north()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_HEAVY_FUEL_OIL.get()))
					ext1=FuelType.HEAVY_FUEL_OIL;
			}
			if(level.getBlockState(worldPosition.south()).getBlock() instanceof EngineExtender){
				if(level.getBlockState(worldPosition.south()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_DIESEL.get()))
					ext2=FuelType.DIESEL;
				else if(level.getBlockState(worldPosition.south()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_GASOLINE.get()))
					ext2=FuelType.GASOLINE;
				else if(level.getBlockState(worldPosition.south()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_LPG.get()))
					ext2=FuelType.LPG;
				else if(level.getBlockState(worldPosition.south()).getBlock().equals(DifModBlocks.ENGINE_EXTENDER_HEAVY_FUEL_OIL.get()))
					ext2=FuelType.HEAVY_FUEL_OIL;
			}
		}
		long validCount=Stream.of(ext0,ext1,ext2).filter(f->f!=FuelType.INVALID).count();
		long uniqueValidCount=Stream.of(ext0,ext1,ext2).filter(f->f!=FuelType.INVALID).distinct().count();
		if(validCount==0) return FuelType.INVALID;
		if(validCount==3&&uniqueValidCount==1) return ext0;
		else if(validCount==2&&uniqueValidCount==1)
			return Stream.of(ext0,ext1,ext2).filter(f->f!=FuelType.INVALID).findFirst().orElse(FuelType.INVALID);
		else if(validCount==1)
			return Stream.of(ext0,ext1,ext2).filter(f->f!=FuelType.INVALID).findFirst().orElse(FuelType.INVALID);
		return FuelType.INVALID;
	}
}
