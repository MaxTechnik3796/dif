package cz.maxtechnik.dif.entity.bomb;

import cz.maxtechnik.dif.init.event.nuke.NukeCraterHandler;
import cz.maxtechnik.dif.init.event.nuke.NukeEffectHandler;
import cz.maxtechnik.dif.init.event.nuke.NukeMushroomHandler;
import cz.maxtechnik.dif.init.event.nuke.NukeScorchHandler;
import cz.maxtechnik.dif.init.event.nuke.NukeShockwaveHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
public class NuclearExplosionEntity extends Entity{
	public static final int MUSHROOM_TICKS=NukeMushroomHandler.TOTAL_MUSHROOM_TICKS;
	private int age=0;
	private int currentY=Integer.MIN_VALUE;
	private int currentScorchRadius=Integer.MIN_VALUE;
	private boolean craterFinished=false;
	private boolean scorchFinished=false;
	private final BlockPos.MutableBlockPos mutablePos=new BlockPos.MutableBlockPos();
	public NuclearExplosionEntity(EntityType<?> type,Level level){
		super(type,level);
		this.noPhysics=true;
	}
	@Override
	protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder){
	}
	@Override
	public void tick(){
		super.tick();
		if(!(level() instanceof ServerLevel sl)) return;
		int radius=NukeCraterHandler.getRadius();
		if(currentY==Integer.MIN_VALUE)
			currentY=NukeCraterHandler.getCraterUpRadius(radius);
		if(currentScorchRadius==Integer.MIN_VALUE)
			currentScorchRadius=NukeScorchHandler.getStartRadius();
		double x=getX(), y=getY(), z=getZ();
		if(age==0){
			NukeMushroomHandler.spawnInitialFlash(sl,x,y,z,random);
			NukeEffectHandler.applyDetonationEffects(sl,x,y,z);
		}
		if(!craterFinished){
			currentY=NukeCraterHandler.tickCrater(sl,blockPosition(),currentY,mutablePos);
			if(currentY<-NukeCraterHandler.getCraterDownRadius(radius))
				craterFinished=true;
		}
		int waveRadius=currentScorchRadius;
		if(!scorchFinished){
			currentScorchRadius=NukeScorchHandler.tickScorchRing(sl,blockPosition(),currentScorchRadius,mutablePos);
			if(currentScorchRadius>NukeScorchHandler.getMaxScorchRadius())
				scorchFinished=true;
		}
		NukeShockwaveHandler.tickShockwaves(sl,x,y,z,age,waveRadius,random);
		if(age<=MUSHROOM_TICKS)
			NukeMushroomHandler.tickMushroomCloud(sl,x,y,z,age,random);
		if(age%20==0)
			NukeEffectHandler.tickFalloutRadiation(sl,x,y,z);
		age++;
		if(craterFinished&&scorchFinished&&age>MUSHROOM_TICKS)
			discard();
	}
	@Override
	protected void readAdditionalSaveData(CompoundTag tag){
		age=tag.getInt("ExplosionAge");
		currentY=tag.getInt("CurrentY");
		currentScorchRadius=tag.getInt("CurrentScorchRadius");
		craterFinished=tag.getBoolean("CraterFinished");
		scorchFinished=tag.getBoolean("ScorchFinished");
	}
	@Override
	protected void addAdditionalSaveData(CompoundTag tag){
		tag.putInt("ExplosionAge",age);
		tag.putInt("CurrentY",currentY);
		tag.putInt("CurrentScorchRadius",currentScorchRadius);
		tag.putBoolean("CraterFinished",craterFinished);
		tag.putBoolean("ScorchFinished",scorchFinished);
	}
	@Override
	public boolean isAttackable(){
		return false;
	}
}
