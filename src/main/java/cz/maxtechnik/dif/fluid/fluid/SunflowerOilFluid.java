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
public abstract class SunflowerOilFluid extends ForgeFlowingFluid{
	public static final Properties PROPERTIES=new Properties(DifModFluidTypes.SUNFLOWER_OIL_TYPE,DifModFluids.SUNFLOWER_OIL,DifModFluids.FLOWING_SUNFLOWER_OIL)
			.explosionResistance(100f).bucket(DifModItems.SUNFLOWER_OIL_BUCKET).block(()->(LiquidBlock)DifModBlocks.SUNFLOWER_OIL_FLUID.get());
	private SunflowerOilFluid(){
		super(PROPERTIES);
	}
	public static class Source extends SunflowerOilFluid{
		public int getAmount(@NotNull FluidState state){
			return 8;
		}
		public boolean isSource(@NotNull FluidState state){
			return true;
		}
	}
	public static class Flowing extends SunflowerOilFluid{
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
