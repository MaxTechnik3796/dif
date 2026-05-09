package cz.maxtechnik.dif.fluid;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.fluid.DifModFluidTypes;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
@SuppressWarnings("removal")
public class Cider{
	public abstract static class Fluid extends BaseFlowingFluid{
		private Fluid(){
			super(new Properties(DifModFluidTypes.CIDER_TYPE,DifModFluids.CIDER,DifModFluids.FLOWING_CIDER).explosionResistance(100F).bucket(DifModItems.CIDER_BUCKET).block(()->(LiquidBlock)DifModBlocks.CIDER_FLUID.get()));
		}
		public static class Source extends Fluid{
			public int getAmount(@NotNull FluidState state){
				return 8;
			}
			public boolean isSource(@NotNull FluidState state){
				return true;
			}
		}
		public static class Flowing extends Fluid{
			protected void createFluidStateDefinition(StateDefinition.@NotNull Builder<net.minecraft.world.level.material.Fluid,FluidState> builder){
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
	public static class FluidType extends net.neoforged.neoforge.fluids.FluidType{
		public FluidType(){
			super(Properties.create().fallDistanceModifier(0F).canExtinguish(true).supportsBoating(true).canHydrate(true).canDrown(true).motionScale(0.007D).sound(SoundActions.BUCKET_FILL,SoundEvents.BUCKET_FILL).sound(SoundActions.BUCKET_EMPTY,SoundEvents.BUCKET_EMPTY).sound(SoundActions.FLUID_VAPORIZE,SoundEvents.FIRE_EXTINGUISH));
		}
		@Override
		public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer){
			consumer.accept(new IClientFluidTypeExtensions(){
				@Override
				public @NotNull ResourceLocation getStillTexture(){
					return ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/cider_fluid_still");
				}
				@Override
				public @NotNull ResourceLocation getFlowingTexture(){
					return ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/cider_fluid_flow");
				}
			});
		}
	}
}
