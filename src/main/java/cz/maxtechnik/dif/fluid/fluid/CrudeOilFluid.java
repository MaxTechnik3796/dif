package cz.maxtechnik.dif.fluid.fluid;

import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.fluid.DifModFluidTypes;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.NotNull;
public abstract class CrudeOilFluid extends ForgeFlowingFluid{
	private CrudeOilFluid(){
		super(new Properties(DifModFluidTypes.CRUDE_OIL_TYPE,DifModFluids.CRUDE_OIL,DifModFluids.FLOWING_CRUDE_OIL).explosionResistance(100F).bucket(DifModItems.CRUDE_OIL_BUCKET).tickRate(28).block(()->(LiquidBlock)DifModBlocks.CRUDE_OIL_FLUID.get()));
	}
	public static class Source extends CrudeOilFluid{
		public int getAmount(@NotNull FluidState state){
			return 8;
		}
		public boolean isSource(@NotNull FluidState state){
			return true;
		}
	}
	public static class Flowing extends CrudeOilFluid{
		protected void createFluidStateDefinition(StateDefinition.@NotNull Builder<Fluid,FluidState> builder){
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}
		public int getAmount(FluidState state){
			return state.getValue(LEVEL);
		}
		public boolean isSource(@NotNull FluidState state){
			return false;
		}
	}
}
