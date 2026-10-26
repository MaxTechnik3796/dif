package cz.maxtechnik.dif.init.event.nuke;

import cz.maxtechnik.dif.config.DifModServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
@SuppressWarnings("deprecation")
public class NukeBlastHandler{
	private static final int BLOCK_UPDATE_FLAGS=2|16|64;
	public static void destroyInitialArea(ServerLevel level,BlockPos center,int radius,BlockPos.MutableBlockPos mutablePos){
		double maxResistance=DifModServerConfig.NUKE_MAX_DESTROYABLE_RESISTANCE.get();
		int cx=center.getX(), cy=center.getY(), cz=center.getZ();
		int rSq=radius*radius;
		for(int dx=-radius;dx<=radius;dx++){
			for(int dy=-radius;dy<=radius;dy++){
				for(int dz=-radius;dz<=radius;dz++){
					if(dx*dx+dy*dy+dz*dz<=rSq){
						int y=cy+dy;
						if(y>=level.getMinBuildHeight()&&y<level.getMaxBuildHeight()){
							mutablePos.set(cx+dx,y,cz+dz);
							BlockState state=level.getBlockState(mutablePos);
							if(!state.isAir()&&state.getBlock().getExplosionResistance()<=maxResistance) level.setBlock(mutablePos,Blocks.AIR.defaultBlockState(),BLOCK_UPDATE_FLAGS);
						}
					}
				}
			}
		}
	}
	public static void pullEntities(ServerLevel level,double x,double y,double z,double pullRadius){
		AABB area=new AABB(x-pullRadius,y-pullRadius,z-pullRadius,x+pullRadius,y+pullRadius,z+pullRadius);
		for(LivingEntity entity: level.getEntitiesOfClass(LivingEntity.class,area)){
			if(entity.isSpectator()) continue;
			if(entity instanceof Player player&&player.isCreative()) continue;
			Vec3 toCenter=new Vec3(x-entity.getX(),(y+1)-entity.getY(),z-entity.getZ());
			double dist=toCenter.length();
			if(dist>1&&dist<=pullRadius){
				Vec3 pull=toCenter.normalize().scale(0.1);
				entity.setDeltaMovement(entity.getDeltaMovement().add(pull));
				entity.hurtMarked=true;
			}
		}
	}
	public static void detonateEntities(ServerLevel level,double x,double y,double z){
		int craterRadius=DifModServerConfig.NUKE_RADIUS.get();
		double damageRadius=DifModServerConfig.NUKE_DAMAGE_RADIUS.get();
		double maxRadius=Math.max(craterRadius,damageRadius);
		AABB area=new AABB(x-maxRadius,y-maxRadius,z-maxRadius,x+maxRadius,y+maxRadius,z+maxRadius);
		for(LivingEntity entity: level.getEntitiesOfClass(LivingEntity.class,area)){
			if(entity.isSpectator()) continue;
			if(entity instanceof Player player&&player.isCreative()) continue;
			double dist=Math.sqrt(entity.distanceToSqr(x,y,z));
			if(dist>maxRadius) continue;
			if(dist<=craterRadius){
				entity.hurt(level.damageSources().wither(),100F);
				entity.addEffect(new MobEffectInstance(MobEffects.WITHER,600,1));
				entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS,200));
			}else{
				entity.addEffect(new MobEffectInstance(MobEffects.WITHER,300));
				entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS,120));
			}
			Vec3 dir=entity.position().subtract(x,y,z);
			if(dir.lengthSqr()>0.01){
				double strength=Math.max(0.5,1-(dist/maxRadius))*2.5;
				Vec3 push=dir.normalize().scale(strength).add(0,0.6,0);
				entity.setDeltaMovement(entity.getDeltaMovement().add(push));
				entity.hurtMarked=true;
			}
		}
	}
	public static int tickCrater(ServerLevel level,BlockPos center,int currentY,BlockPos.MutableBlockPos mutablePos){
		int radius=DifModServerConfig.NUKE_RADIUS.get();
		int blocksPerTick=DifModServerConfig.NUKE_BLOCKS_PER_TICK.get();
		double maxResistance=DifModServerConfig.NUKE_MAX_DESTROYABLE_RESISTANCE.get();
		int cx=center.getX(), cy=center.getY(), cz=center.getZ();
		int processed=0;
		while(currentY>=-radius&&processed<blocksPerTick){
			int y=cy+currentY;
			if(y>=level.getMinBuildHeight()&&y<level.getMaxBuildHeight()){
				int rSq=radius*radius-currentY*currentY;
				int layerRadius=(int)Math.sqrt(rSq);
				for(int dx=-layerRadius;dx<=layerRadius;dx++){
					for(int dz=-layerRadius;dz<=layerRadius;dz++){
						if(dx*dx+dz*dz<=rSq){
							mutablePos.set(cx+dx,y,cz+dz);
							BlockState state=level.getBlockState(mutablePos);
							if(!state.isAir()&&state.getBlock().getExplosionResistance()<=maxResistance) level.setBlock(mutablePos,Blocks.AIR.defaultBlockState(),BLOCK_UPDATE_FLAGS);
							processed++;
						}
					}
				}
			}
			currentY--;
		}
		return currentY;
	}
}