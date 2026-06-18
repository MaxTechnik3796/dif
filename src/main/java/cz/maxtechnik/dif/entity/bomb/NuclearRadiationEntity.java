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
			level().explode(null,getX(),getY()+2.0,getZ(),30,Level.ExplosionInteraction.NONE);
			AABB area=new AABB(getX()-320,getY()-320,getZ()-320,getX()+320,getY()+320,getZ()+320);
			for(LivingEntity entity: level().getEntitiesOfClass(LivingEntity.class,area)){
				if(entity.isSpectator()) continue;
				if(entity instanceof Player player&&player.isCreative()) continue;
				double dist=entity.distanceTo(this);
				// Wither efekt
				if(dist<=220.0)
					entity.addEffect(new MobEffectInstance(MobEffects.WITHER,2400,1));
				// Damage do 128 bloků
				if(dist<=128.0){
					float damage=(float)(200.0*(1.0-dist/128.0));
					entity.invulnerableTime=0;
					entity.setHealth(entity.getHealth()-damage);
					if(entity.getHealth()<=0) entity.kill();
				}
			}
			// Nyní se entita smaže. Pokud byste chtěl dělat dlouhodobou radiaci,
			// můžete discard() odebrat a přidat logiku pro opakovaný dmg/wither.
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
