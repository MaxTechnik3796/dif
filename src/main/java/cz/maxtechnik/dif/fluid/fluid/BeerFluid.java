package cz.maxtechnik.dif.fluid.fluid;

import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.fluid.DifModFluidTypes;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.NotNull;
public abstract class BeerFluid extends ForgeFlowingFluid{
	private BeerFluid(){
		super(new Properties(DifModFluidTypes.BEER_TYPE,DifModFluids.BEER,DifModFluids.FLOWING_BEER).explosionResistance(100F).bucket(DifModItems.BEER_BUCKET).block(()->(LiquidBlock)DifModBlocks.BEER_FLUID.get()));
	}
	public static class Source extends BeerFluid{
		public int getAmount(@NotNull FluidState state){
			return 8;
		}
		public boolean isSource(@NotNull FluidState state){
			return true;
		}
	}
	public static class Flowing extends BeerFluid{
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
