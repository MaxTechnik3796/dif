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
public abstract class JetpackTurboFuelFluid extends ForgeFlowingFluid{
	private JetpackTurboFuelFluid(){
		super(new Properties(DifModFluidTypes.JETPACK_TURBO_FUEL_TYPE,DifModFluids.JETPACK_TURBO_FUEL,DifModFluids.FLOWING_JETPACK_TURBO_FUEL).explosionResistance(100F).bucket(DifModItems.JETPACK_TURBO_FUEL_BUCKET).tickRate(7).block(()->(LiquidBlock)DifModBlocks.JETPACK_TURBO_FUEL_FLUID.get()));
	}
	public static class Source extends JetpackTurboFuelFluid{
		public int getAmount(@NotNull FluidState state){
			return 8;
		}
		public boolean isSource(@NotNull FluidState state){
			return true;
		}
	}
	public static class Flowing extends JetpackTurboFuelFluid{
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
