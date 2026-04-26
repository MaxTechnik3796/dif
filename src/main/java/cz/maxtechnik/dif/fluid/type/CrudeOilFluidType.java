
package cz.maxtechnik.dif.fluid.type;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;
public class CrudeOilFluidType extends FluidType{
	public CrudeOilFluidType(){
		super(Properties.create().fallDistanceModifier(0F).canExtinguish(true).supportsBoating(true).canHydrate(false).canDrown(true).motionScale(0.002D).sound(SoundActions.BUCKET_FILL,SoundEvents.BUCKET_FILL).sound(SoundActions.BUCKET_EMPTY,SoundEvents.BUCKET_EMPTY).sound(SoundActions.FLUID_VAPORIZE,SoundEvents.FIRE_EXTINGUISH));
	}
	@Override
	public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer){
		consumer.accept(new IClientFluidTypeExtensions(){
			@Override
			public ResourceLocation getStillTexture(){
				return ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/crude_oil_fluid_still");
			}
			@Override
			public ResourceLocation getFlowingTexture(){
				return ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/crude_oil_fluid_flow");
			}
		});
	}
}
