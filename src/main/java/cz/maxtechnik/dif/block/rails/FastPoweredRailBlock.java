package cz.maxtechnik.dif.block.rails;

import cz.maxtechnik.dif.config.DifModServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
public class FastPoweredRailBlock extends PoweredRailBlock {
	public FastPoweredRailBlock(Properties properties) {
		super(properties, true);
	}
	@Override
	public float getRailMaxSpeed(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull AbstractMinecart cart) {
		float topSpeed = DifModServerConfig.FAST_RAIL_TOP_SPEED.get().floatValue();
		return state.getValue(getShapeProperty()).isAscending() ? Math.min(topSpeed, 0.6F) : topSpeed;
	}
	@Override
	public void onMinecartPass(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull AbstractMinecart cart) {
		if (!cart.shouldDoRailFunctions()) return;

		Vec3 motion = cart.getDeltaMovement();
		double speed = motion.horizontalDistance();
		// Kolej vypnutá = brzdění
		if (!state.getValue(POWERED)) {
			cart.setDeltaMovement(speed < 0.03D ? Vec3.ZERO : motion.multiply(0.5D, 0.0D, 0.5D));
			return;
		}
		if (speed > 0.01D) {
			double accel = DifModServerConfig.FAST_POWERED_RAIL_ACCELERATION.get();
			cart.setDeltaMovement(motion.add((motion.x / speed) * accel, 0.0D, (motion.z / speed) * accel));
		} else {
			launchFromWall(level, pos, state.getValue(getShapeProperty()), cart);
		}
	}
	private void launchFromWall(Level level, BlockPos pos, RailShape shape, AbstractMinecart cart) {
		double dx = 0, dz = 0;
		if (shape == RailShape.EAST_WEST) {
			if (level.getBlockState(pos.west()).isRedstoneConductor(level, pos.west())) dx = 0.02D;
			else if (level.getBlockState(pos.east()).isRedstoneConductor(level, pos.east())) dx = -0.02D;
		} else if (shape == RailShape.NORTH_SOUTH) {
			if (level.getBlockState(pos.north()).isRedstoneConductor(level, pos.north())) dz = 0.02D;
			else if (level.getBlockState(pos.south()).isRedstoneConductor(level, pos.south())) dz = -0.02D;
		}
		if (dx != 0 || dz != 0) {
			cart.setDeltaMovement(dx, cart.getDeltaMovement().y, dz);
		}
	}
}