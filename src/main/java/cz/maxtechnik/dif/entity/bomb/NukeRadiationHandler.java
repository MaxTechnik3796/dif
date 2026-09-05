package cz.maxtechnik.dif.entity.bomb;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NukeRadiationHandler{
	private static final double RADIATION_RADIUS=96.0;

	public static void apply(Level level,BlockPos center){
		if(level.isClientSide) return;

		double ex=center.getX()+0.5;
		double ey=center.getY()+1.5;
		double ez=center.getZ()+0.5;

		// 1. Vanilla exploze pro poškození, odolnosti a štíty bez ničení bloků
		level.explode(null,ex,ey,ez,32.0F,Level.ExplosionInteraction.NONE);

		// 2. Smrtící radiace a kinetický odhoz entit
		AABB area=new AABB(ex-RADIATION_RADIUS,ey-RADIATION_RADIUS,ez-RADIATION_RADIUS,
				ex+RADIATION_RADIUS,ey+RADIATION_RADIUS,ez+RADIATION_RADIUS);

		for(LivingEntity entity: level.getEntitiesOfClass(LivingEntity.class,area)){
			if(entity.isSpectator()) continue;
			if(entity instanceof Player player&&player.isCreative()) continue;

			double dist=Math.sqrt(entity.distanceToSqr(ex,ey,ez));
			if(dist<=RADIATION_RADIUS){
				if(dist<=36.0){
					entity.addEffect(new MobEffectInstance(MobEffects.WITHER,1200,1));
					entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,400,2));
					entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS,200,0));
				}else{
					entity.addEffect(new MobEffectInstance(MobEffects.WITHER,800,0));
					entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION,200,0));
				}

				// Kinetický impuls
				double dx=entity.getX()-ex;
				double dz=entity.getZ()-ez;
				double horizDist=Math.sqrt(dx*dx+dz*dz);
				if(horizDist>0.1){
					double impulse=Math.max(0.4,1.0-(dist/RADIATION_RADIUS))*2.2;
					Vec3 push=new Vec3((dx/horizDist)*impulse,0.45,(dz/horizDist)*impulse);
					entity.setDeltaMovement(entity.getDeltaMovement().add(push));
					entity.hurtMarked=true;
				}
			}
		}
	}
}
