package cz.maxtechnik.dif.block.rails;

import cz.maxtechnik.dif.DifModCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
public class FastRailBlock extends RailBlock{
	public FastRailBlock(Properties properties){
		super(properties);
	}
	@Override
	public float getRailMaxSpeed(BlockState state,Level world,BlockPos pos,AbstractMinecart cart){
		// Nastavíme stejný limit jako u tvého FastPoweredRailu (1.2f)
		// Díky tomu vozík v zatáčce "necukne" a nezpomalí.
		return (float)DifModCommonConfig.fastRailTopSpeed;
	}
}