package cz.maxtechnik.dif.block.rails;

import cz.maxtechnik.dif.config.DifModCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
public class FastPoweredRailBlock extends PoweredRailBlock{
	public FastPoweredRailBlock(Properties properties){
		super(properties,true);
	}
	@Override
	public float getRailMaxSpeed(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,@NotNull AbstractMinecart cart){
		return DifModCommonConfig.FAST_RAIL_TOP_SPEED.get().floatValue();
	}
	@Override
	public void onMinecartPass(BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,@NotNull AbstractMinecart cart){
		double multiplier=DifModCommonConfig.FAST_POWERED_RAIL_ACCELERATION.get();
		if(blockState.getValue(POWERED)){
			Vec3 motion=cart.getDeltaMovement();
			double speed=motion.horizontalDistance();
			if(speed>0.01) cart.setDeltaMovement(motion.add(motion.x/speed*multiplier,0,motion.z/speed*multiplier));
			else super.onMinecartPass(blockState,world,pos,cart);
		}
	}
}