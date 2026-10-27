package cz.maxtechnik.dif.entity.bomb;

import cz.maxtechnik.dif.config.DifModServerConfig;
import cz.maxtechnik.dif.init.event.nuke.NukeBlastHandler;
import cz.maxtechnik.dif.init.event.nuke.NukeVisualHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
public class NuclearExplosionEntity extends Entity{
	private static final EntityDataAccessor<Integer> DATA_AGE=SynchedEntityData.defineId(NuclearExplosionEntity.class,EntityDataSerializers.INT);
	public static final int INITIAL_BLAST_RADIUS=5;
	public static final int CHARGE_TICKS=20;
	public static final int CRATER_DELAY_TICKS=70;
	public static final int TOTAL_LIFETIME=240;
	private int age=0;
	private int currentY=Integer.MIN_VALUE;
	private boolean craterFinished=false;
	private final BlockPos.MutableBlockPos mutablePos=new BlockPos.MutableBlockPos();
	public NuclearExplosionEntity(EntityType<?> type,Level level){
		super(type,level);
		this.noPhysics=true;
	}
	@Override
	protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder){
		builder.define(DATA_AGE,0);
	}
	public int getAge(){
		return level().isClientSide?this.entityData.get(DATA_AGE):this.age;
	}
	@Override
	public void tick(){
		super.tick();
		if(!(level() instanceof ServerLevel sl)) return;
		if(currentY==Integer.MIN_VALUE) currentY=DifModServerConfig.NUKE_RADIUS.get();
		double x=getX(), y=getY(), z=getZ();
		if(age>=CHARGE_TICKS){
			if(age==CHARGE_TICKS){
				NukeBlastHandler.destroyInitialArea(sl,blockPosition(),INITIAL_BLAST_RADIUS,mutablePos);
				NukeVisualHandler.triggerMainDetonation(sl,x,y,z);
				NukeBlastHandler.detonateEntities(sl,x,y,z);
			}
			if(!craterFinished&&age>=CHARGE_TICKS+CRATER_DELAY_TICKS){
				currentY=NukeBlastHandler.tickCrater(sl,blockPosition(),currentY,mutablePos);
				if(currentY<-DifModServerConfig.NUKE_RADIUS.get()) craterFinished=true;
			}
		}
		age++;
		this.entityData.set(DATA_AGE,age);
		if(craterFinished&&age>TOTAL_LIFETIME) discard();
	}
	@Override
	protected void readAdditionalSaveData(CompoundTag tag){
		age=tag.getInt("ExplosionAge");
		currentY=tag.getInt("CurrentY");
		craterFinished=tag.getBoolean("CraterFinished");
		this.entityData.set(DATA_AGE,age);
	}
	@Override
	protected void addAdditionalSaveData(CompoundTag tag){
		tag.putInt("ExplosionAge",age);
		tag.putInt("CurrentY",currentY);
		tag.putBoolean("CraterFinished",craterFinished);
	}
	@Override
	public boolean isAttackable(){
		return false;
	}
}
