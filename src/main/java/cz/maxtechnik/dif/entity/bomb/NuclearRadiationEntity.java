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
			// Exploze o poloměru 64 zasáhne entity až do vzdálenosti 128 bloků
			// Používáme vanilla explozi (NONE - neničí bloky), takže totemy, brnění a štíty fungují 100% správně!
			level().explode(null,getX(),getY()+2.0,getZ(),64,Level.ExplosionInteraction.NONE);

			AABB area=new AABB(getX()-220,getY()-220,getZ()-220,getX()+220,getY()+220,getZ()+220);
			for(LivingEntity entity: level().getEntitiesOfClass(LivingEntity.class,area)){
				if(entity.isSpectator()) continue;
				if(entity instanceof Player player&&player.isCreative()) continue;
				double dist=entity.distanceTo(this);
				// Wither efekt
				if(dist<=220.0)
					entity.addEffect(new MobEffectInstance(MobEffects.WITHER,2400,1));
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
