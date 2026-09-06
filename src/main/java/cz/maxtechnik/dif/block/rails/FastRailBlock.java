package cz.maxtechnik.dif.block.rails;

import cz.maxtechnik.dif.config.DifModServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.jetbrains.annotations.NotNull;
public class FastRailBlock extends RailBlock{
	public FastRailBlock(Properties properties){
		super(properties);
	}
	@Override
	public float getRailMaxSpeed(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,@NotNull AbstractMinecart cart){
		float topSpeed=DifModServerConfig.FAST_RAIL_TOP_SPEED.get().floatValue();
		RailShape shape=blockState.getValue(getShapeProperty());
		if(shape!=RailShape.NORTH_SOUTH&&shape!=RailShape.EAST_WEST){
			return Math.min(topSpeed,0.6F);
		}
		return topSpeed;
	}
}