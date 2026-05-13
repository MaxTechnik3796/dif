package cz.maxtechnik.dif.entity;

import cz.maxtechnik.dif.init.other.DifModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
public class NuclearCountdownEntity extends Entity{
	private static final EntityDataAccessor<Integer> DATA_COUNTDOWN=
			SynchedEntityData.defineId(NuclearCountdownEntity.class,EntityDataSerializers.INT);
	public NuclearCountdownEntity(EntityType<?> type,Level level){
		super(type,level);
		this.noPhysics=true;
	}
	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder){
		builder.define(DATA_COUNTDOWN,200);
	}
	public void setCountdown(int ticks){
		this.entityData.set(DATA_COUNTDOWN,ticks);
	}
	public int getCountdown(){
		return this.entityData.get(DATA_COUNTDOWN);
	}
	@Override
	public void tick(){
		super.tick();
		if(level().isClientSide){
			tickClient();
			return;
		}
		int countdown=getCountdown()-1;
		setCountdown(countdown);
		if(countdown>60&&countdown%20==0) playTick(1.0f);
		else if(countdown>20&&countdown%10==0) playTick(1.2f);
		else if(countdown>0&&countdown%5==0) playTick(1.5f);
		if(countdown<=0) transformToExplosion();
	}
	private void playTick(float pitch){
		level().playSound(null,getX(),getY(),getZ(),
				SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value(),SoundSource.BLOCKS,1.5f,pitch);
	}
	private void transformToExplosion(){
		level().playSound(null,getX(),getY(),getZ(),
				SoundEvents.GENERIC_EXPLODE.value(),SoundSource.BLOCKS,10.0f,0.5f);
		NuclearExplosionEntity explosion=new NuclearExplosionEntity(DifModEntities.NUCLEAR_EXPLOSION.get(),level());
		explosion.setPos(this.getX(),this.getY(),this.getZ());
		explosion.setRadius(40);
		level().addFreshEntity(explosion);
		this.discard();
	}
	private void tickClient(){
		int countdown=getCountdown();
		int blinkRate=countdown>100?20:countdown>40?10:countdown>10?5:1;
		if(this.tickCount%blinkRate==0){
			level().addParticle(ParticleTypes.FLAME,getX(),getY()+0.5,getZ(),0,0.05,0);
			level().addParticle(ParticleTypes.SMOKE,getX(),getY()+0.5,getZ(),0,0.05,0);
		}
		if(countdown<60){
			level().addParticle(ParticleTypes.LARGE_SMOKE,
					getX()+(random.nextDouble()-0.5)*0.5,getY()+0.5,
					getZ()+(random.nextDouble()-0.5)*0.5,0,0.1,0);
		}
	}
	@Override
	protected void readAdditionalSaveData(CompoundTag tag){
		setCountdown(tag.getInt("Countdown"));
	}
	@Override
	protected void addAdditionalSaveData(CompoundTag tag){
		tag.putInt("Countdown",getCountdown());
	}
	@Override
	public boolean isPickable(){
		return true;
	}
	@Override
	public boolean isAttackable(){
		return false;
	}
}