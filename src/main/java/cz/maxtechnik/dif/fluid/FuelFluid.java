package cz.maxtechnik.dif.fluid;

import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.fluid.DifModFluidTypes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.LiquidBlock;

import org.jetbrains.annotations.NotNull;

public abstract class FuelFluid extends ForgeFlowingFluid {
	public static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(DifModFluidTypes.FUEL_TYPE, DifModFluids.FUEL,DifModFluids.FLOWING_FUEL)
			.explosionResistance(100f).bucket(DifModItems.FUEL_BUCKET).block(() -> (LiquidBlock) DifModBlocks.FUEL_FLUID.get());

	private FuelFluid() {
		super(PROPERTIES);
	}

	public static class Source extends FuelFluid{
		public int getAmount(@NotNull FluidState state) {
			return 8;
		}

		public boolean isSource(@NotNull FluidState state) {
			return true;
		}
	}

	public static class Flowing extends FuelFluid{
		protected void createFluidStateDefinition(StateDefinition.@NotNull Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		public int getAmount(FluidState state) {
			return state.getValue(LEVEL);
		}

		public boolean isSource(@NotNull FluidState state) {
			return false;
		}
	}
}
