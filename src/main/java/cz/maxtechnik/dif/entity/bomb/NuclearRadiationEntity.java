package cz.maxtechnik.dif.entity.bomb;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class NuclearRadiationEntity extends Entity{
	private boolean processed=false;

	public NuclearRadiationEntity(EntityType<?> type,Level level){
		super(type,level);
		this.noPhysics=true;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder){
	}

	@Override
	public void tick(){
		super.tick();
		if(level().isClientSide) return;
		if(!processed){
			processed=true;
			double ex=getX(), ey=getY()+1.5, ez=getZ();

			// Exploze o poloměru 32 (pro 64-blokový kráter) - vanilla exploze bez ničení bloků (NONE),
			// takže totemy, brnění, odolnosti a štíty fungují 100% správně!
			level().explode(null,ex,ey,ez,32.0F,Level.ExplosionInteraction.NONE);

			double radRadius=96.0;
			AABB area=new AABB(ex-radRadius,ey-radRadius,ez-radRadius,ex+radRadius,ey+radRadius,ez+radRadius);

			for(LivingEntity entity: level().getEntitiesOfClass(LivingEntity.class,area)){
				if(entity.isSpectator()) continue;
				if(entity instanceof Player player&&player.isCreative()) continue;

				double dist=entity.distanceTo(this);
				if(dist<=radRadius){
					// Smrtící radiace a otřes
					if(dist<=36.0){
						entity.addEffect(new MobEffectInstance(MobEffects.WITHER,1200,1));
						entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,400,2));
						entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS,200,0));
					}else{
						entity.addEffect(new MobEffectInstance(MobEffects.WITHER,800,0));
						entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION,200,0));
					}

					// Masivní kinetický impuls směrem ven
					double dx=entity.getX()-ex;
					double dz=entity.getZ()-ez;
					double horizDist=Math.sqrt(dx*dx+dz*dz);
					if(horizDist>0.1){
						double impulse=Math.max(0.4,1.0-(dist/radRadius))*2.2;
						Vec3 push=new Vec3((dx/horizDist)*impulse,0.45,(dz/horizDist)*impulse);
						entity.setDeltaMovement(entity.getDeltaMovement().add(push));
						entity.hurtMarked=true;
					}
				}
			}
			discard();
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag){
		processed=tag.getBoolean("Processed");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag){
		tag.putBoolean("Processed",processed);
	}

	@Override
	public boolean isAttackable(){
		return false;
	}

	@Override
	public boolean isPickable(){
		return false;
	}
}
