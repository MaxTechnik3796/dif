package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.Engine;
import cz.maxtechnik.dif.block.EngineExtender;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.FuelType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;
import java.util.Locale;

import static cz.maxtechnik.dif.block.Engine.*;
import static cz.maxtechnik.dif.config.DifModServerConfig.*;
import static cz.maxtechnik.dif.init.basic.DifModBlocks.ENGINE_BASE;
import static cz.maxtechnik.dif.init.basic.DifModBlocks.ENGINE_PORTABLE;
public class EngineBlockEntity extends GeneratingKineticBlockEntity{
	public static final TagKey<Fluid> DIESEL_TAG=TagKey.create(Registries.FLUID,ResourceLocation.parse("c:diesel"));
	public static final TagKey<Fluid> GASOLINE_TAG=TagKey.create(Registries.FLUID,ResourceLocation.parse("c:gasoline"));
	public static final TagKey<Fluid> LPG_TAG=TagKey.create(Registries.FLUID,ResourceLocation.parse("c:lpg"));
	public static final TagKey<Fluid> HEAVY_FUEL_OIL_TAG=TagKey.create(Registries.FLUID,ResourceLocation.parse("c:heavy_fuel_oil"));
	boolean generating=false;
	float speed=0F;
	float su=0F;
	private boolean extUp=false, extNorth=false, extSouth=false, extEast=false, extWest=false;
	private int cachedExtenders=0;
	private int redstoneSignal=0;
	private double consumptionPerTick=0.0D;
	private boolean isDrainingInternally=false;
	private static final int FUEL_TICK_INTERVAL=10;
	private int fuelTickCounter=0;
	private double fuelAccumulator=0D;
	public EngineBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.ENGINE.get(),pos,blockState);
	}
	public final FluidTank fluidTank=new FluidTank(1000,stack->{
		if(stack.isEmpty()) return false;
		boolean isPortable=isEngineBlockPortable(getBlockState().getBlock());
		if(isPortable) return stack.is(GASOLINE_TAG)||stack.is(LPG_TAG);
		return stack.is(GASOLINE_TAG)||stack.is(DIESEL_TAG)||stack.is(HEAVY_FUEL_OIL_TAG);
	}){
		@Override
		protected void onContentsChanged(){
			super.onContentsChanged();
			setChanged();
			if(!isDrainingInternally&&level!=null&&!level.isClientSide)
				recalculateStats();
		}
	};
	@Override
	public float getGeneratedSpeed(){
		if(level==null) return 0F;
		BlockState state=getBlockState();
		if(!(state.getBlock() instanceof Engine)) return 0F;
		return generating?speed*(state.getValue(INVERT)?-1F:1F):0F;
	}
	@Override
	public float calculateAddedStressCapacity(){
		return generating?su:0F;
	}
	@Override
	public void read(CompoundTag tag,HolderLookup.Provider registries,boolean clientPacket){
		super.read(tag,registries,clientPacket);
		if(tag.get("fluidTank") instanceof CompoundTag fluidTag) fluidTank.readFromNBT(registries,fluidTag);
		generating=tag.getBoolean("generating");
		speed=tag.getFloat("speed");
		su=tag.getFloat("su");
		extUp=tag.getBoolean("extUp");
		extNorth=tag.getBoolean("extNorth");
		extSouth=tag.getBoolean("extSouth");
		extEast=tag.getBoolean("extEast");
		extWest=tag.getBoolean("extWest");
		cachedExtenders=tag.getInt("extenders");
		redstoneSignal=tag.getInt("redstoneSignal");
	}
	@Override
	public void write(CompoundTag tag,HolderLookup.Provider registries,boolean clientPacket){
		super.write(tag,registries,clientPacket);
		tag.put("fluidTank",fluidTank.writeToNBT(registries,new CompoundTag()));
		tag.putBoolean("generating",generating);
		tag.putFloat("speed",speed);
		tag.putFloat("su",su);
		tag.putBoolean("extUp",extUp);
		tag.putBoolean("extNorth",extNorth);
		tag.putBoolean("extSouth",extSouth);
		tag.putBoolean("extEast",extEast);
		tag.putBoolean("extWest",extWest);
		tag.putInt("extenders",cachedExtenders);
		tag.putInt("redstoneSignal",redstoneSignal);
	}
	@Override
	public void initialize(){
		super.initialize();
		updateExtenders();
		if(level!=null&&!level.isClientSide) recalculateStats();
	}
	public void updateExtenders(){
		if(level==null) return;
		BlockState ownState=getBlockState();
		if(!(ownState.getBlock() instanceof Engine)) return;
		if(isEngineBlock(ownState.getBlock())){
			Direction.Axis axis=ownState.getValue(FACING).getAxis();
			if(axis==Direction.Axis.Z){
				extUp=isEngineExtender(worldPosition.above());
				extEast=isEngineExtender(worldPosition.east());
				extWest=isEngineExtender(worldPosition.west());
				extNorth=false;
				extSouth=false;
				cachedExtenders=(extUp?1:0)+(extEast?1:0)+(extWest?1:0);
			}else if(axis==Direction.Axis.X){
				extUp=isEngineExtender(worldPosition.above());
				extNorth=isEngineExtender(worldPosition.north());
				extSouth=isEngineExtender(worldPosition.south());
				extEast=false;
				extWest=false;
				cachedExtenders=(extUp?1:0)+(extNorth?1:0)+(extSouth?1:0);
			}else{
				extUp=false;
				extNorth=isEngineExtender(worldPosition.north());
				extSouth=isEngineExtender(worldPosition.south());
				extEast=isEngineExtender(worldPosition.east());
				extWest=isEngineExtender(worldPosition.west());
				int total=(extNorth?1:0)+(extSouth?1:0)+(extEast?1:0)+(extWest?1:0);
				if(total>3) extWest=false;
				cachedExtenders=Math.min(3,total);
			}
		}else{
			extUp=false;
			extNorth=false;
			extSouth=false;
			extEast=false;
			extWest=false;
			cachedExtenders=1;
		}
		if(!level.isClientSide) recalculateStats();
	}
	public void recalculateStats(){
		if(level==null) return;
		boolean wasGenerating=generating;
		float prevSpeed=speed;
		float prevSu=su;
		int prevSignal=redstoneSignal;
		redstoneSignal=level.getBestNeighborSignal(worldPosition);
		FuelType fuel=getFuelType();
		if(redstoneSignal<=0||fuel==FuelType.INVALID||fluidTank.isEmpty()){
			generating=false;
			speed=0F;
			su=0F;
			consumptionPerTick=0.0D;
		}else{
			boolean isPortable=isEngineBlockPortable(getBlockState().getBlock());
			double baseRpm=getBaseRpm(fuel,isPortable);
			double maxSu=getBaseSu(fuel,isPortable)*(isPortable?1:cachedExtenders);
			double burnRatePerSec=calculateBurnRate(fuel,isPortable,cachedExtenders,redstoneSignal);
			speed=(float)baseRpm;
			su=(float)(maxSu*(redstoneSignal/15.0D));
			consumptionPerTick=burnRatePerSec/20.0D;
			generating=true;
		}
		if(wasGenerating!=generating||prevSpeed!=speed||prevSu!=su||prevSignal!=redstoneSignal){
			if(!level.isClientSide){
				BlockState state=getBlockState();
				if(state.getBlock() instanceof Engine&&state.getValue(ACTIVE)!=generating)
					KineticBlockEntity.switchToBlockState(level,worldPosition,state.setValue(ACTIVE,generating));
				updateGeneratedRotation();
				sendData();
			}
		}
	}
	private double getBaseRpm(FuelType fuel,boolean isPortable){
		if(isPortable){
			if(fuel==FuelType.GASOLINE) return PORTABLE_ENGINE_GASOLINE_RPM.get();
			if(fuel==FuelType.LPG) return PORTABLE_ENGINE_LPG_RPM.get();
		}else{
			if(fuel==FuelType.GASOLINE) return LARGE_ENGINE_GASOLINE_RPM.get();
			if(fuel==FuelType.DIESEL) return LARGE_ENGINE_DIESEL_RPM.get();
			if(fuel==FuelType.HEAVY_FUEL_OIL) return LARGE_ENGINE_HEAVY_FUEL_OIL_RPM.get();
		}
		return 0.0D;
	}
	private double getBaseSu(FuelType fuel,boolean isPortable){
		if(isPortable){
			if(fuel==FuelType.GASOLINE) return PORTABLE_ENGINE_GASOLINE_SU.get();
			if(fuel==FuelType.LPG) return PORTABLE_ENGINE_LPG_SU.get();
		}else{
			if(fuel==FuelType.GASOLINE) return LARGE_ENGINE_GASOLINE_SU.get();
			if(fuel==FuelType.DIESEL) return LARGE_ENGINE_DIESEL_SU.get();
			if(fuel==FuelType.HEAVY_FUEL_OIL) return LARGE_ENGINE_HEAVY_FUEL_OIL_SU.get();
		}
		return 0.0D;
	}
	private double getBaseConsumption(FuelType fuel,boolean isPortable){
		if(isPortable){
			if(fuel==FuelType.GASOLINE) return PORTABLE_ENGINE_GASOLINE_CONSUMPTION.get();
			if(fuel==FuelType.LPG) return PORTABLE_ENGINE_LPG_CONSUMPTION.get();
		}else{
			if(fuel==FuelType.GASOLINE) return LARGE_ENGINE_GASOLINE_CONSUMPTION.get();
			if(fuel==FuelType.DIESEL) return LARGE_ENGINE_DIESEL_CONSUMPTION.get();
			if(fuel==FuelType.HEAVY_FUEL_OIL) return LARGE_ENGINE_HEAVY_FUEL_OIL_CONSUMPTION.get();
		}
		return 0.0D;
	}
	private double calculateBurnRate(FuelType fuel,boolean isPortable,int extenders,int signal){
		if(signal<=0||fuel==FuelType.INVALID) return 0.0D;
		double maxBurnRate=getBaseConsumption(fuel,isPortable)*(isPortable?1:extenders);
		double factor=0.1D+0.9D*((signal-1)/14.0D);
		return Math.round(maxBurnRate*factor*10.0D)/10.0D;
	}
	@Override
	public void tick(){
		super.tick();
		if(level==null||!(getBlockState().getBlock() instanceof Engine)) return;
		if(reActivateSource){
			updateGeneratedRotation();
			reActivateSource=false;
		}
		if(!level.isClientSide){
			if(generating&&redstoneSignal>0){
				fuelAccumulator+=consumptionPerTick;
				if(++fuelTickCounter>=FUEL_TICK_INTERVAL){
					fuelTickCounter=0;
					int toDrain=(int)Math.floor(fuelAccumulator);
					if(toDrain>0){
						int available=fluidTank.getFluidAmount();
						int drainAmount=Math.min(toDrain,available);
						if(drainAmount>0){
							isDrainingInternally=true;
							try{
								fluidTank.drain(drainAmount,IFluidHandler.FluidAction.EXECUTE);
							}finally{
								isDrainingInternally=false;
							}
							fuelAccumulator-=drainAmount;
							if(fuelAccumulator<0D) fuelAccumulator=0D;
							if(fluidTank.isEmpty()) recalculateStats();
						}
					}
				}
			}
		}
		if(level.isClientSide&&getBlockState().getValue(ACTIVE)) clientTick();
	}
	private static final double[] EXTENDER_PARTICLE_OFFSETS={0.3,0.5,0.7};
	private static final double[] PORTABLE_SIDES={0.15,0.85};
	private static final double[] PORTABLE_ALONG={0.22,0.78};
	public void clientTick(){
		if(level==null) return;
		Direction facing=getBlockState().getValue(FACING);
		Direction.Axis axis=facing.getAxis();
		boolean isZ=axis==Direction.Axis.Z;
		double vel=0.007;
		double px=worldPosition.getX(), py=worldPosition.getY(), pz=worldPosition.getZ();
		Block ownBlock=getBlockState().getBlock();
		if(isEngineBlock(ownBlock)){
			if(extUp) spawnExtenderParticles(0.5,py+2,0,isZ);
			if(extEast) spawnExtenderParticles(2,py+0.5,vel*2,true);
			if(extWest) spawnExtenderParticles(-1,py+0.5,-vel*2,true);
			if(extSouth) spawnExtenderParticles(2,py+0.5,vel*2,false);
			if(extNorth) spawnExtenderParticles(-1,py+0.5,-vel*2,false);
		}else if(isEngineBlockPortable(ownBlock)){
			if(axis.isHorizontal()){
				for(double side: PORTABLE_SIDES){
					double sideVel=side<0.5?-vel*2:vel*2;
					for(double along: PORTABLE_ALONG){
						double x=isZ?px+side:px+along;
						double z=isZ?pz+along:pz+side;
						particle(new Vec3(x,py+0.99,z),new Vec3(isZ?sideVel:0,vel,isZ?0:sideVel));
					}
				}
			}else{
				double zOff=facing==Direction.UP?0.99:0.01;
				double vz=facing==Direction.UP?vel:-vel;
				for(double side: PORTABLE_SIDES){
					double sideVel=side<0.5?-vel*2:vel*2;
					for(double along: PORTABLE_ALONG){
						particle(new Vec3(px+side,py+along,pz+zOff),new Vec3(sideVel,0,vz));
					}
				}
			}
		}
	}
	private void spawnExtenderParticles(double sideOffset,double y,double sideVel,boolean isZ){
		double px=worldPosition.getX(), pz=worldPosition.getZ();
		double vx=isZ?sideVel:0, vz=isZ?0:sideVel;
		for(double along: EXTENDER_PARTICLE_OFFSETS){
			double x=isZ?px+sideOffset:px+along;
			double z=isZ?pz+along:pz+sideOffset;
			particle(new Vec3(x,y,z),new Vec3(vx,0.007,vz));
		}
	}
	public FuelType getFuelFromTank(){
		if(fluidTank.isEmpty()) return FuelType.INVALID;
		net.neoforged.neoforge.fluids.FluidStack stack=fluidTank.getFluid();
		if(stack.is(DIESEL_TAG)) return FuelType.DIESEL;
		if(stack.is(GASOLINE_TAG)) return FuelType.GASOLINE;
		if(stack.is(LPG_TAG)) return FuelType.LPG;
		if(stack.is(HEAVY_FUEL_OIL_TAG)) return FuelType.HEAVY_FUEL_OIL;
		return FuelType.INVALID;
	}
	public FuelType getFuelType(){
		if(fluidTank.isEmpty()) return FuelType.INVALID;
		Block ownBlock=getBlockState().getBlock();
		if(isEngineBlock(ownBlock)&&cachedExtenders==0) return FuelType.INVALID;
		FuelType fuel=getFuelFromTank();
		if(isEngineBlockPortable(ownBlock))
			return (fuel==FuelType.GASOLINE||fuel==FuelType.LPG)?fuel:FuelType.INVALID;
		return (fuel==FuelType.GASOLINE||fuel==FuelType.DIESEL||fuel==FuelType.HEAVY_FUEL_OIL)?fuel:FuelType.INVALID;
	}
	public boolean isEngineExtender(BlockPos pos){
		if(level==null||!level.isLoaded(pos)) return false;
		return level.getBlockState(pos).getBlock() instanceof EngineExtender;
	}
	private boolean isEngineBlock(Block block){
		return block.equals(ENGINE_BASE.get());
	}
	private boolean isEngineBlockPortable(Block block){
		return block.equals(ENGINE_PORTABLE.get());
	}
	public void particle(Vec3 pos,Vec3 velocity){
		if(level==null) return;
		if(DifMod.rouletteBoolean(4)) level.addParticle(ParticleTypes.SMOKE,pos.x,pos.y,pos.z,velocity.x,velocity.y,velocity.z);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip,boolean isPlayerSneaking){
		super.addToGoggleTooltip(tooltip,isPlayerSneaking);
		if(!fluidTank.isEmpty()){
			String fluidName=fluidTank.getFluid().getHoverName().getString();
			tooltip.add(Component.literal("     Fuel: ").withStyle(ChatFormatting.GRAY).append(Component.literal(fluidName+" ("+fluidTank.getFluidAmount()+" / "+fluidTank.getCapacity()+" mB)").withStyle(ChatFormatting.AQUA)));
		}else
			tooltip.add(Component.literal("     Fuel: ").withStyle(ChatFormatting.GRAY).append(Component.literal("Empty (0 / "+fluidTank.getCapacity()+" mB)").withStyle(ChatFormatting.DARK_GRAY)));
		FuelType fuel=getFuelType();
		if(fuel!=FuelType.INVALID&&redstoneSignal>0){
			boolean isPortable=isEngineBlockPortable(getBlockState().getBlock());
			double burnRatePerSec=calculateBurnRate(fuel,isPortable,cachedExtenders,redstoneSignal);
			String burnRateStr=String.format(Locale.US,"%.1f",burnRatePerSec);
			tooltip.add(Component.literal("     Burn Rate: ").withStyle(ChatFormatting.GRAY).append(Component.literal(burnRateStr+" mB/s").withStyle(ChatFormatting.GOLD)));
		}else
			tooltip.add(Component.literal("     Burn Rate: ").withStyle(ChatFormatting.GRAY).append(Component.literal("0.0 mB/s").withStyle(ChatFormatting.DARK_GRAY)));
		return true;
	}
}