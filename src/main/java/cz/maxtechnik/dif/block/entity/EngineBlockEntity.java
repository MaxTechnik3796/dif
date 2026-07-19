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
	private static final int FUEL_TICK_INTERVAL=10;
	private int fuelTickCounter=0;
	private double fuelAccumulator=0.0D;
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
			}else{
				int extenders=countExtenders();
				double baseRpm=0.0D;
				double baseSu=0.0D;
				double baseConsumption=0.0D;
				if(fuel.equals(FuelType.DIESEL)){
					baseRpm=ENGINE_DIESEL_RPM.get();
					baseSu=ENGINE_DIESEL_SU.get();
					baseConsumption=ENGINE_DIESEL_CONSUMPTION.get();
				}else if(fuel.equals(FuelType.HEAVY_FUEL_OIL)){
					baseRpm=ENGINE_HEAVY_FUEL_OIL_RPM.get();
					baseSu=ENGINE_HEAVY_FUEL_OIL_SU.get();
					baseConsumption=ENGINE_HEAVY_FUEL_OIL_CONSUMPTION.get();
				}else if(fuel.equals(FuelType.GASOLINE)){
					baseRpm=ENGINE_GASOLINE_RPM.get();
					baseSu=ENGINE_GASOLINE_SU.get();
					baseConsumption=ENGINE_GASOLINE_CONSUMPTION.get();
				}else if(fuel.equals(FuelType.LPG)){
					baseRpm=ENGINE_LPG_RPM.get();
					baseSu=ENGINE_LPG_SU.get();
					baseConsumption=ENGINE_LPG_CONSUMPTION.get();
				}
				// Speed stays constant regardless of extender count; SU and consumption scale linearly
				speed=(float)baseRpm;
				su=(float)(baseSu*extenders);
				double consumptionMultiplier = 1.0 + (extenders - 1) * 0.5;
				double consumptionPerTick=(baseConsumption*consumptionMultiplier)/FUEL_TICK_INTERVAL;
				fuelAccumulator+=consumptionPerTick;
				if(fuelTickCounter++>=FUEL_TICK_INTERVAL){
					fuelTickCounter=0;
					int fuelToDrain=(int)Math.floor(fuelAccumulator);
					int availableFuel=fluidTank.getFluidAmount();
					if(fuelToDrain>0){
						int drainAmount=Math.min(fuelToDrain,availableFuel);
						if(drainAmount>0){
							fluidTank.drain(drainAmount,IFluidHandler.FluidAction.EXECUTE);
							fuelAccumulator-=drainAmount;
							if(fuelAccumulator<0D) fuelAccumulator=0D;
						}
					}
				}
				generating=fluidTank.getFluidAmount()>0;
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
			if(isEngineBlock(ownBlock)){
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
			}else if(isEngineBlockPortable(ownBlock)){
				particle(new Vec3(worldPosition.getX()+0.15,worldPosition.getY()+0.99,worldPosition.getZ()+0.22),new Vec3(-vel*2,vel,0));
				particle(new Vec3(worldPosition.getX()+0.15,worldPosition.getY()+0.99,worldPosition.getZ()+0.78),new Vec3(-vel*2,vel,0));
				particle(new Vec3(worldPosition.getX()+0.85,worldPosition.getY()+0.99,worldPosition.getZ()+0.22),new Vec3(vel*2,vel,0));
				particle(new Vec3(worldPosition.getX()+0.85,worldPosition.getY()+0.99,worldPosition.getZ()+0.78),new Vec3(vel*2,vel,0));
			}
		}else if(axis.equals(Direction.Axis.X)){
			if(isEngineBlock(ownBlock)){
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
			}else if(isEngineBlockPortable(ownBlock)){
				particle(new Vec3(worldPosition.getX()+0.22,worldPosition.getY()+0.99,worldPosition.getZ()+0.15),new Vec3(0,vel,-vel*2));
				particle(new Vec3(worldPosition.getX()+0.75,worldPosition.getY()+0.99,worldPosition.getZ()+0.15),new Vec3(0,vel,-vel*2));
				particle(new Vec3(worldPosition.getX()+0.22,worldPosition.getY()+0.99,worldPosition.getZ()+0.85),new Vec3(0,vel,vel*2));
				particle(new Vec3(worldPosition.getX()+0.78,worldPosition.getY()+0.99,worldPosition.getZ()+0.85),new Vec3(0,vel,vel*2));
			}
		}
	}
	public FuelType scanExtenders(){
		if(level==null) return FuelType.INVALID;
		BlockState ownState=getBlockState();
		if(!(ownState.getBlock() instanceof Engine)) return FuelType.INVALID;
		Block ownBlock=ownState.getBlock();
		if(isEngineBlock(ownBlock)){
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
		if(isEngineBlock(ownState.getBlock())){
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
	private boolean isEngineBlock(Block block){
		return block.equals(ENGINE_BASE.get());
	}
	private boolean isEngineBlockPortable(Block block){
		return block.equals(ENGINE_PORTABLE_DIESEL.get())||block.equals(ENGINE_PORTABLE_GASOLINE.get())||block.equals(ENGINE_PORTABLE_LPG.get());
	}

	public void particle(Vec3 pos,Vec3 velocity){
		if(level==null) return;
		if(DifMod.rouletteBoolean(4))
			level.addParticle(ParticleTypes.SMOKE,pos.x,pos.y,pos.z,velocity.x,velocity.y,velocity.z);
	}
}