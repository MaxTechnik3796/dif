package cz.maxtechnik.dif.entity.vehicle;

import cz.maxtechnik.dif.init.events.client.CarEngineSoundInstance;
import cz.maxtechnik.dif.network.ModNetworking.SyncCarPositionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
public abstract class BaseCarEntity extends Entity{
	protected static final EntityDataAccessor<Float> DATA_RPM=SynchedEntityData.defineId(BaseCarEntity.class,EntityDataSerializers.FLOAT);
	protected static final EntityDataAccessor<Integer> DATA_GEAR=SynchedEntityData.defineId(BaseCarEntity.class,EntityDataSerializers.INT);
	protected static final EntityDataAccessor<Float> DATA_SPEED=SynchedEntityData.defineId(BaseCarEntity.class,EntityDataSerializers.FLOAT);
	protected static final EntityDataAccessor<Boolean> DATA_ENGINE_ON=SynchedEntityData.defineId(BaseCarEntity.class,EntityDataSerializers.BOOLEAN);
	protected static final EntityDataAccessor<Float> DATA_FUEL=SynchedEntityData.defineId(BaseCarEntity.class,EntityDataSerializers.FLOAT);
	protected float velocity=0f;
	protected int shiftCooldown=0;
	private float prevVelocity=0f, fuelAccumulator=0f;
	private int crashDamageCooldown=0, fuelSyncTick=0;
	protected float currentSteering=0f;
	protected float motionYaw=0f;
	protected int spinoutTimer=0;
	protected int oversteerTimer=0;
	protected int driftRecoveryTimer=0;
	protected float spinoutDirection=1f;
	public boolean isSoundPlaying=false;
	public enum SurfaceType{NORMAL,SOUL_SAND,ICE,CARPET,GRASS}
	private static Field jumpingField;
	static{
		try{
			try{
				jumpingField=LivingEntity.class.getDeclaredField("f_20899_");
			}catch(NoSuchFieldException e){
				jumpingField=LivingEntity.class.getDeclaredField("jumping");
			}
			jumpingField.setAccessible(true);
		}catch(Exception ignored){
		}
	}
	public BaseCarEntity(EntityType<?> type,Level level){
		super(type,level);
		this.blocksBuilding=true;
	}
	@Override
	public float maxUpStep(){
		return getCustomStepHeight();
	}
	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder){
		builder.define(DATA_RPM,getIdleRPM());
		builder.define(DATA_GEAR,0);
		builder.define(DATA_SPEED,0F);
		builder.define(DATA_ENGINE_ON,false);
		builder.define(DATA_FUEL,getInitialFuelMb());
	}
	@Override
	public @NotNull InteractionResult interact(Player player,@NotNull InteractionHand hand){
		if(player.isSecondaryUseActive()){
			ItemStack itemStack=player.getItemInHand(hand);
			if(!level().isClientSide){
				if(itemStack.is(Items.LAVA_BUCKET)){
					if(getFuelMb()>=getMaxFuelMb())
						player.displayClientMessage(Component.literal("Nádrž je plná!"),true);
					else{
						setFuelMb(getFuelMb()+1000F);
						if(!player.getAbilities().instabuild){
							itemStack.shrink(1);
							if(!player.getInventory().add(new ItemStack(Items.BUCKET)))
								player.drop(new ItemStack(Items.BUCKET),false);
						}
					}
				}else if(itemStack.is(Items.BUCKET)){
					if(getFuelMb()<1000F)
						player.displayClientMessage(Component.literal("Nestačí palivo na odebrání celého bucketu!"),true);
					else{
						setFuelMb(getFuelMb()-1000F);
						if(!player.getAbilities().instabuild){
							itemStack.shrink(1);
							if(!player.getInventory().add(new ItemStack(Items.LAVA_BUCKET)))
								player.drop(new ItemStack(Items.LAVA_BUCKET),false);
						}
					}
				}
			}
			return InteractionResult.SUCCESS;
		}
		if(!level().isClientSide&&player.startRiding(this)){
			setEngineOn(true);
			setCurrentGear(0);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.SUCCESS;
	}
	@Override
	public void removePassenger(@NotNull Entity entity){
		super.removePassenger(entity);
		if(!isVehicle()){
			if(!level().isClientSide()){
				setEngineOn(false);
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(this,new SyncCarPositionPacket(getId(),getX(),getY(),getZ(),getYRot(),0F));
			}
			velocity=prevVelocity=0F;
			setDeltaMovement(Vec3.ZERO);
			hasImpulse=true;
		}
	}
	@Override
	public boolean hurt(@NotNull DamageSource damageSource,float amount){
		if(isInvulnerableTo(damageSource)) return false;
		if(!level().isClientSide&&!isRemoved()){
			if(damageSource.getEntity() instanceof Player p&&!p.getAbilities().instabuild)
				spawnAtLocation(getDropItem());
			discard();
			return true;
		}
		return false;
	}
	public abstract Item getDropItem();
	@Override
	public void tick(){
		super.tick();
		if(shiftCooldown>0) shiftCooldown--;
		if(crashDamageCooldown>0) crashDamageCooldown--;
		LivingEntity d=getControllingPassenger();
		prevVelocity=velocity;
		if(isVehicle()&&d!=null) simulateActivePhysics(d);
		else simulateIdlePhysics();
		if(!level().isClientSide){
			if(isVehicle()&&isEngineOn()){
				if(getFuelMb()>0F&&(fuelAccumulator+=(getSpeedKmh()>=getFuelSpeedThresholdKmh()||getCurrentGear()==-1?getFuelConsumptionHighMbPerTick():getFuelConsumptionLowMbPerTick()))>=1F||++fuelSyncTick>=5){
					setFuelMb(getFuelMb()-fuelAccumulator);
					fuelAccumulator=fuelSyncTick=0;
				}
			}
			PacketDistributor.sendToPlayersTrackingEntity(this,new SyncCarPositionPacket(getId(),getX(),getY(),getZ(),getYRot(),velocity));
		}else{
			if(isEngineOn()&&!isSoundPlaying&&getEngineSound()!=null){
				CarEngineSoundInstance.play(this);
				isSoundPlaying=true;
			}
			if(!isEngineOn()) isSoundPlaying=false;
		}
		hasImpulse=true;
		entityData.set(DATA_SPEED,velocity);
	}
	private void simulateIdlePhysics(){
		motionYaw=getYRot();
		velocity=Math.abs(velocity)<0.0005F?0F:velocity*0.88F;
		setRPM(Math.max(0F,getRPM()-getMaxRPM()*0.04F));
		double yaw=Math.toRadians(getYRot()), yMot=Math.max(-1.5,onGround()?-0.05:getDeltaMovement().y-0.04);
		setDeltaMovement(-Math.sin(yaw)*velocity,yMot,Math.cos(yaw)*velocity);
		move(MoverType.SELF,getDeltaMovement());
	}
	protected void simulateActivePhysics(LivingEntity d){
		float throttle=Math.max(0F,d.zza), targetInput=-d.xxa;
		if(spinoutTimer>0){
			spinoutTimer--;
			if(spinoutTimer>60){
				throttle=0F;
				targetInput=0F;
			}
			setYRot(getYRot()+6F*spinoutDirection);
			velocity*=0.985F;
		}else{
			if(targetInput!=0F){
				if(Math.abs(currentSteering)<0.25F*Math.abs(targetInput)){
					currentSteering=Math.signum(targetInput)*0.25F*Math.abs(targetInput);
				}
				currentSteering+=(targetInput-currentSteering)*0.15F;
			}else{
				currentSteering+=(0F-currentSteering)*0.35F;
				if(Math.abs(currentSteering)<0.05F) currentSteering=0F;
			}
		}
		SurfaceType s=detectSurface();
		int g=getCurrentGear();
		float sMult=s==SurfaceType.SOUL_SAND?0.35F:s==SurfaceType.CARPET?0.88F:1F, lGrip=s==SurfaceType.ICE?0.1F:s==SurfaceType.GRASS?0.7F:s==SurfaceType.SOUL_SAND?0.5F:s==SurfaceType.CARPET?0.75F:1F, rRes=s==SurfaceType.SOUL_SAND?0.025F:s==SurfaceType.ICE?0.0004F:s==SurfaceType.GRASS?0.0015F:s==SurfaceType.CARPET?0.006F:0.002F;
		float msBT=(getMaxSpeedKmh()*sMult)/72F, rpmC=getMaxRPM()/((getMaxSpeedKmh()/72F)*getGearRatios()[getGearRatios().length-1]);
		if(g==0) setRPM(getRPM()+(getIdleRPM()+(getMaxRPM()-getIdleRPM())*throttle*0.25F-getRPM())*0.15F);
		else{
			float tRPM=Math.abs(velocity)*getGearRatios()[g==-1?0:g-1]*rpmC;
			setRPM(Math.clamp(Math.abs(velocity)<0.05F&&throttle>0F?Math.max(tRPM,getIdleRPM()+(getMaxRPM()-getIdleRPM())*throttle*0.25F):tRPM, getIdleRPM(), getMaxRPM()));
		}
		float thrust=0F;
		if(g!=0&&throttle>0f&&getFuelMb()>0F){
			float base=throttle*getBaseAcceleration()*(getGearRatios()[g==-1?0:g-1]/getGearRatios()[0])*Math.clamp((float)(-4.0*Math.pow(getRPM()/getMaxRPM()-0.75,2)+1.0),0.25F,1F)*(getRPM()>=getMaxRPM()*0.999F?0F:shiftCooldown>0?(float)shiftCooldown/getShiftCooldownTicks():1F);
			thrust=g==-1?-base*0.4F:base;
		}else if(getFuelMb()<=0F) setRPM(Math.max(0F,getRPM()-getMaxRPM()*0.04F));
		if(getJumping(d)){
			velocity*=0.97F;
			velocity=Math.signum(velocity)*Math.max(0F,Math.abs(velocity)-getBrakingDeceleration()*0.15F);
			lGrip*=0.6F;
		}
		velocity=Math.clamp(msBT,-0.25F,velocity+thrust-velocity*Math.abs(velocity)*getAeroDrag()-velocity*rRes);
		if(s==SurfaceType.SOUL_SAND&&Math.abs(velocity)>22F/72F)
			velocity=velocity*0.82F+Math.signum(velocity)*(22F/72F)*0.18F;
		if(Math.abs(velocity)>0.015F){
			float speedKmh=Math.abs(velocity)*72F;
			float steeringMult=Math.max(0.2F,1F-(speedKmh/400F));
			setYRot(getYRot()+getBaseHandling()*(1F+getDownforceCoefficient()*(velocity/msBT)*(velocity/msBT))*lGrip*currentSteering*steeringMult);
			float safeLimit;
			if(speedKmh<=150F) safeLimit=1F;
			else if(speedKmh<=250F) safeLimit=1F-((speedKmh-150F)/100F)*0.5F;
			else if(speedKmh<=300F) safeLimit=0.5F-((speedKmh-250F)/50F)*0.3F;
			else safeLimit=0.15F;
			float absSteer=Math.abs(currentSteering);
			boolean oversteerDanger=absSteer>safeLimit;
			if(oversteerDanger&&spinoutTimer==0){
				oversteerTimer++;
				int timeLimit=getJumping(d)?30:10;
				if(oversteerTimer>timeLimit){
					// HODINY (Spinout) - striktně překročení limitů přináší hodiny, zrušen lehký drift
					spinoutTimer=120;
					spinoutDirection=Math.signum(currentSteering);
				}
			}else{
				oversteerTimer=Math.max(0,oversteerTimer-2);
			}
		}
		if(driftRecoveryTimer>0) driftRecoveryTimer--;
		float yawDiff=net.minecraft.util.Mth.wrapDegrees(getYRot()-motionYaw);
		float alignSpeed=0.2F;
		if(spinoutTimer>0) alignSpeed=0.01F;
		else if(driftRecoveryTimer>0) alignSpeed=0.04F;
		motionYaw+=yawDiff*alignSpeed;
		double yaw=Math.toRadians(motionYaw), preX=getX(), preY=getY(), preZ=getZ();
		Vec3 mot=new Vec3(-Math.sin(yaw)*velocity,Math.max(-1.5,onGround()?-0.05:getDeltaMovement().y-0.04),Math.cos(yaw)*velocity);
		setDeltaMovement(mot);
		move(MoverType.SELF,getDeltaMovement());
		if(horizontalCollision&&getY()-preY<=0.001){
			double cs=Math.sqrt(Math.pow(getX()-preX,2)+Math.pow(getZ()-preZ,2));
			velocity=(float)cs*Math.signum(velocity);
			spinoutTimer=0;
			oversteerTimer=0;
			double v=(Math.sqrt(mot.x*mot.x+mot.z*mot.z)-cs)*72.0;
			if(!level().isClientSide&&crashDamageCooldown==0&&v>getCrashDamageThresholdKmh()){
				d.hurt(level().damageSources().generic(),(float)(v-getCrashDamageThresholdKmh())*getCrashDamageMultiplier());
				crashDamageCooldown=10;
			}
		}
	}
	protected SurfaceType detectSurface(){
		BlockPos f=blockPosition();
		Block bf=level().getBlockState(f).getBlock(), bb=level().getBlockState(f.below()).getBlock();
		if(bf instanceof CarpetBlock||bb instanceof CarpetBlock) return SurfaceType.CARPET;
		if(bb==Blocks.GRASS_BLOCK||bb==Blocks.DIRT_PATH||bf==Blocks.GRASS_BLOCK||bf==Blocks.DIRT_PATH)
			return SurfaceType.GRASS;
		if(bb==Blocks.ICE||bb==Blocks.PACKED_ICE||bb==Blocks.BLUE_ICE||bb==Blocks.FROSTED_ICE||bf==Blocks.ICE||bf==Blocks.PACKED_ICE||bf==Blocks.BLUE_ICE||bf==Blocks.FROSTED_ICE)
			return SurfaceType.ICE;
		if(bb==Blocks.SOUL_SAND||bb==Blocks.SOUL_SOIL||bf==Blocks.SOUL_SAND||bf==Blocks.SOUL_SOIL)
			return SurfaceType.SOUL_SAND;
		return SurfaceType.NORMAL;
	}
	private boolean getJumping(LivingEntity e){
		try{
			return jumpingField!=null&&jumpingField.getBoolean(e);
		}catch(Exception ex){
			return false;
		}
	}
	public void setVelocityFromPacket(float v){
		entityData.set(DATA_SPEED,velocity=v);
	}
	public abstract SoundEvent getEngineSound();
	public abstract float getCustomStepHeight();
	public abstract float getMaxSpeedKmh();
	public abstract float getBaseAcceleration();
	public abstract float[] getGearRatios();
	public abstract float getBaseHandling();
	public abstract float getIdleRPM();
	public abstract float getMaxRPM();
	public abstract float getRedlineRPM();
	public abstract float getMaxFuelMb();
	public abstract float getFuelConsumptionLowMbPerTick();
	public abstract float getFuelConsumptionHighMbPerTick();
	public abstract float getFuelSpeedThresholdKmh();
	public abstract Fluid getFuelFluid();
	public float getInitialFuelMb(){
		return getMaxFuelMb();
	}
	public float getDownforceCoefficient(){
		return 0F;
	}
	public float getAeroDrag(){
		return 0.0002F;
	}
	public float getBrakingDeceleration(){
		return 0.05F;
	}
	public float getHighSpeedSteerReduction(){
		return 0.55F;
	}
	public int getShiftCooldownTicks(){
		return 8;
	}
	public void applyShiftCooldown(){
		shiftCooldown=getShiftCooldownTicks();
	}
	public float getCrashDamageThresholdKmh(){
		return 40F;
	}
	public float getCrashDamageMultiplier(){
		return 0.15F;
	}
	public float getRPM(){
		return entityData.get(DATA_RPM);
	}
	public void setRPM(float v){
		entityData.set(DATA_RPM,v);
	}
	public int getCurrentGear(){
		return entityData.get(DATA_GEAR);
	}
	public void setCurrentGear(int v){
		entityData.set(DATA_GEAR,v);
	}
	public float getSpeedKmh(){
		return Math.abs(entityData.get(DATA_SPEED))*72F;
	}
	public boolean isEngineOn(){
		return entityData.get(DATA_ENGINE_ON);
	}
	public void setEngineOn(boolean v){
		entityData.set(DATA_ENGINE_ON,v);
	}
	public float getFuelMb(){
		return entityData.get(DATA_FUEL);
	}
	public void setFuelMb(float v){
		entityData.set(DATA_FUEL, Math.clamp(v, 0F, getMaxFuelMb()));
	}
	public float getFuelPercent(){
		return getMaxFuelMb()>0?getFuelMb()/getMaxFuelMb():0F;
	}
	@Override
	public LivingEntity getControllingPassenger(){
		return getFirstPassenger() instanceof LivingEntity le?le:null;
	}
	@Override
	public boolean isPickable(){
		return !isRemoved();
	}
	@Override
	public boolean isPushable(){
		return true;
	}
	@Override
	protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag n){
		if(n.contains("FuelMb")) setFuelMb(n.getFloat("FuelMb"));
	}
	@Override
	protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag n){
		n.putFloat("FuelMb",getFuelMb());
	}
	public float getCurrentSteering(){
		return currentSteering;
	}
}