package cz.maxtechnik.dif.fluid.template;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import org.jetbrains.annotations.NotNull;
public class MoltenBlock extends LiquidBlock{
	public MoltenBlock(FlowingFluid flowingFluid,Properties properties){
		super(flowingFluid,properties);
	}
	@Override
	public void entityInside(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,@NotNull Entity entity){
		super.entityInside(blockState,world,pos,entity);
		entity.igniteForSeconds(20);
	}
}
