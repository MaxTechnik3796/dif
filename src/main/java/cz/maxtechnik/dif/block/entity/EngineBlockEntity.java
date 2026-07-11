package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.Engine;
import cz.maxtechnik.dif.block.EngineExtender;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.FuelType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.stream.Stream;

import static cz.maxtechnik.dif.block.Engine.*;
import static cz.maxtechnik.dif.config.DifModCommonConfig.*;
import static cz.maxtechnik.dif.init.basic.DifModBlocks.*;
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
		if(level==null) return 0F;
		if(!(level.getBlockState(worldPosition).getBlock() instanceof Engine)) return 0F;
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
				speed=countExtenders()*ENGINE_DIESEL_SPEED.get();
				su=countExtenders()*ENGINE_DIESEL_SU.get();
				generating=fluidTank.getFluidAmount()>countExtenders()*ENGINE_DIESEL_CONSUMPTION.get()-1;
				fluidTank.drain(countExtenders()*ENGINE_DIESEL_CONSUMPTION.get(),IFluidHandler.FluidAction.EXECUTE);
			}else if(fuel.equals(FuelType.HEAVY_FUEL_OIL)){
				speed=countExtenders()*ENGINE_HEAVY_FUEL_OIL_SPEED.get();
				su=countExtenders()*ENGINE_HEAVY_FUEL_OIL_SU.get();
				generating=fluidTank.getFluidAmount()>countExtenders()*ENGINE_HEAVY_FUEL_OIL_CONSUMPTION.get()-1;
				fluidTank.drain(countExtenders()*ENGINE_HEAVY_FUEL_OIL_CONSUMPTION.get(),IFluidHandler.FluidAction.EXECUTE);
			}else if(fuel.equals(FuelType.GASOLINE)){
				speed=countExtenders()*ENGINE_GASOLINE_SPEED.get();
				su=countExtenders()*ENGINE_GASOLINE_SU.get();
				generating=fluidTank.getFluidAmount()>countExtenders()*ENGINE_GASOLINE_CONSUMPTION.get()-1;
				fluidTank.drain(countExtenders()*ENGINE_GASOLINE_CONSUMPTION.get(),IFluidHandler.FluidAction.EXECUTE);
			}else if(fuel.equals(FuelType.LPG)){
				speed=countExtenders()*ENGINE_LPG_SPEED.get();
				su=countExtenders()*ENGINE_LPG_SU.get();
				generating=fluidTank.getFluidAmount()>countExtenders()*ENGINE_LPG_CONSUMPTION.get()-1;
				fluidTank.drain(countExtenders()*ENGINE_LPG_CONSUMPTION.get(),IFluidHandler.FluidAction.EXECUTE);
			}
		}else{
			generating=false;
			speed=0F;
			su=0F;
		}
		if(uGenerating!=generating||uSpeed!=speed||uSu!=su){
			KineticBlockEntity.switchToBlockState(level,worldPosition,getBlockState().setValue(ACTIVE,generating));
			updateGeneratedRotation();
			uGenerating=generating;
			uSpeed=speed;
			uSu=su;
		}
		if(level.isClientSide&&getBlockState().getValue(ACTIVE)) clientTick();
	}
	public void clientTick(){
		if(level==null) return;
		Direction.Axis axis=getBlockState().getValue(FACING).getAxis();
		boolean ext0=isEngineExtender(worldPosition.above());
		boolean ext1;
		boolean ext2;
		double vel=0.007;
		Block ownBlock=getBlockState().getBlock();
		if(axis.equals(Direction.Axis.Z)){
			if(ownBlock.equals(ENGINE2.get())||ownBlock.equals(ENGINE4.get())){
				ext1=isEngineExtender(worldPosition.east());
				ext2=isEngineExtender(worldPosition.west());
				if(ext0){
					particle(new Vec3(worldPosition.getX()+0.5,worldPosition.getY()+2,worldPosition.getZ()+0.3),new Vec3(0,vel,0));
					particle(new Vec3(worldPosition.getX()+0.5,worldPosition.getY()+2,worldPosition.getZ()+0.5),new Vec3(0,vel,0));
					particle(new Vec3(worldPosition.getX()+0.5,worldPosition.getY()+2,worldPosition.getZ()+0.7),new Vec3(0,vel,0));
				}
				if(ext1){
					particle(new Vec3(worldPosition.getX()+2,worldPosition.getY()+0.5,worldPosition.getZ()+0.3),new Vec3(vel*2,vel,0));
					particle(new Vec3(worldPosition.getX()+2,worldPosition.getY()+0.5,worldPosition.getZ()+0.5),new Vec3(vel*2,vel,0));
					particle(new Vec3(worldPosition.getX()+2,worldPosition.getY()+0.5,worldPosition.getZ()+0.7),new Vec3(vel*2,vel,0));
				}
				if(ext2){
					particle(new Vec3(worldPosition.getX()-1,worldPosition.getY()+0.5,worldPosition.getZ()+0.3),new Vec3(-vel*2,vel,0));
					particle(new Vec3(worldPosition.getX()-1,worldPosition.getY()+0.5,worldPosition.getZ()+0.5),new Vec3(-vel*2,vel,0));
					particle(new Vec3(worldPosition.getX()-1,worldPosition.getY()+0.5,worldPosition.getZ()+0.7),new Vec3(-vel*2,vel,0));
				}
			}else{
				particle(new Vec3(worldPosition.getX(),worldPosition.getY(),worldPosition.getZ()),new Vec3(0,vel,0));//OFFSET
			}
		}else if(axis.equals(Direction.Axis.X)){
			if(ownBlock.equals(ENGINE2.get())||ownBlock.equals(ENGINE4.get())){
				ext1=isEngineExtender(worldPosition.north());
				ext2=isEngineExtender(worldPosition.south());
				if(ext0){
					particle(new Vec3(worldPosition.getX()+0.3,worldPosition.getY()+2,worldPosition.getZ()+0.5),new Vec3(0,vel,0));
					particle(new Vec3(worldPosition.getX()+0.5,worldPosition.getY()+2,worldPosition.getZ()+0.5),new Vec3(0,vel,0));
					particle(new Vec3(worldPosition.getX()+0.7,worldPosition.getY()+2,worldPosition.getZ()+0.5),new Vec3(0,vel,0));
				}
				if(ext1){
					particle(new Vec3(worldPosition.getX()+0.3,worldPosition.getY()+0.5,worldPosition.getZ()-1),new Vec3(0,vel,-vel*2));
					particle(new Vec3(worldPosition.getX()+0.5,worldPosition.getY()+0.5,worldPosition.getZ()-1),new Vec3(0,vel,-vel*2));
					particle(new Vec3(worldPosition.getX()+0.7,worldPosition.getY()+0.5,worldPosition.getZ()-1),new Vec3(0,vel,-vel*2));
				}
				if(ext2){
					particle(new Vec3(worldPosition.getX()+0.3,worldPosition.getY()+0.5,worldPosition.getZ()+2),new Vec3(0,vel,vel*2));
					particle(new Vec3(worldPosition.getX()+0.5,worldPosition.getY()+0.5,worldPosition.getZ()+2),new Vec3(0,vel,vel*2));
					particle(new Vec3(worldPosition.getX()+0.7,worldPosition.getY()+0.5,worldPosition.getZ()+2),new Vec3(0,vel,vel*2));
				}
			}else{
				particle(new Vec3(worldPosition.getX(),worldPosition.getY(),worldPosition.getZ()),new Vec3(0,vel,0));//OFFSET
			}
		}
	}
	public FuelType scanExtenders(){
		if(level==null) return FuelType.INVALID;
		BlockState ownState=getBlockState();
		if(!(ownState.getBlock() instanceof Engine)) return FuelType.INVALID;
		Block ownBlock=ownState.getBlock();
		if(ownBlock.equals(ENGINE2.get())||ownBlock.equals(ENGINE4.get())){
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
		}else{
			if(ownBlock.equals(ENGINE_PORTABLE_DIESEL.get())) return FuelType.DIESEL;
			else if(ownBlock.equals(ENGINE_PORTABLE_GASOLINE.get())) return FuelType.GASOLINE;
			else if(ownBlock.equals(ENGINE_PORTABLE_LPG.get())) return FuelType.LPG;
		}
		return FuelType.INVALID;
	}
	public FuelType getExtenderFuel(BlockPos pos){
		if(level==null) return FuelType.INVALID;
		Block block=level.getBlockState(pos).getBlock();
		if(!(block instanceof EngineExtender)) return FuelType.INVALID;
		if(block.equals(ENGINE_EXTENDER_DIESEL.get())) return FuelType.DIESEL;
		if(block.equals(ENGINE_EXTENDER_GASOLINE.get())) return FuelType.GASOLINE;
		if(block.equals(ENGINE_EXTENDER_LPG.get())) return FuelType.LPG;
		if(block.equals(ENGINE_EXTENDER_HEAVY_FUEL_OIL.get())) return FuelType.HEAVY_FUEL_OIL;
		return FuelType.INVALID;
	}
	public int countExtenders(){
		if(level==null) return 0;
		BlockState ownState=getBlockState();
		if(!(ownState.getBlock() instanceof Engine)) return 0;
		if(ownState.getBlock().equals(ENGINE2.get())||ownState.getBlock().equals(ENGINE4.get())){
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
		}else return 1;
	}
	public boolean isEngineExtender(BlockPos pos){
		if(level==null) return false;
		return level.getBlockState(pos).getBlock() instanceof EngineExtender;
	}
	private boolean isEngine4(){
		return getBlockState().getBlock().equals(ENGINE4.get());
	}
	public void particle(Vec3 pos,Vec3 velocity){
		if(level==null) return;
		if(DifMod.rouletteBoolean(4))
			level.addParticle(ParticleTypes.SMOKE,pos.x,pos.y,pos.z,velocity.x,velocity.y,velocity.z);
	}
}