package cz.maxtechnik.dif.fluid;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import org.jetbrains.annotations.NotNull;
public class BaseFluidBlocks{
	// Univerzální zdrojová tekutina (Source)
	public static class Source extends BaseFlowingFluid{
		public Source(Properties properties){
			super(properties);
		}
		@Override
		public int getAmount(@NotNull FluidState state){
			return 8;
		}
		@Override
		public boolean isSource(@NotNull FluidState state){
			return true;
		}
	}
	// Univerzální tekoucí tekutina (Flowing)
	public static class Flowing extends BaseFlowingFluid{
		public Flowing(Properties properties){
			super(properties);
		}
		@Override
		protected void createFluidStateDefinition(StateDefinition.@NotNull Builder<Fluid,FluidState> builder){
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}
		@Override
		public int getAmount(@NotNull FluidState state){
			return state.getValue(LEVEL);
		}
		@Override
		public boolean isSource(@NotNull FluidState state){
			return false;
		}
	}
}