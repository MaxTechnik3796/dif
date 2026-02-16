package cz.maxtechnik.dif.fluid;

import cz.maxtechnik.dif.block.DifModBlocks;
import cz.maxtechnik.dif.item.DifModItems;
import cz.maxtechnik.dif.fluid.types.DifModFluidTypes;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.NotNull;
public abstract class XpFluid extends ForgeFlowingFluid{
	public static final Properties PROPERTIES=new Properties(DifModFluidTypes.XP_TYPE,DifModFluids.XP,DifModFluids.FLOWING_XP).explosionResistance(100f).bucket(DifModItems.XP_BUCKET).block(()->(LiquidBlock)DifModBlocks.XP_FLUID.get());
	private XpFluid(){
		super(PROPERTIES);
	}
	public static class Source extends XpFluid{
		public int getAmount(@NotNull FluidState state){
			return 8;
		}
		public boolean isSource(@NotNull FluidState state){
			return true;
		}
	}
	public static class Flowing extends XpFluid{
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
