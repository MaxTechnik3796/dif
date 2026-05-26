package cz.maxtechnik.dif.fluid;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
@SuppressWarnings("removal")
public class BaseFluidType extends FluidType{
	private final ResourceLocation stillTexture;
	private final ResourceLocation flowingTexture;
	/**
	 * @param properties Základní vlastnosti (motionScale, density, temperature...)
	 * @param name Název tekutiny pro automatické dohledání textury (např. "beer", "fuel")
	 */
	public BaseFluidType(Properties properties,String name){
		super(properties.sound(SoundActions.BUCKET_FILL,SoundEvents.BUCKET_FILL)
				.sound(SoundActions.BUCKET_EMPTY,SoundEvents.BUCKET_EMPTY)
				.sound(SoundActions.FLUID_VAPORIZE,SoundEvents.FIRE_EXTINGUISH));
		// Automaticky sestaví cesty k texturám z tvého modu
		this.stillTexture=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/"+name+"_fluid_still");
		this.flowingTexture=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/"+name+"_fluid_flow");
	}
	@Override
	public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer){
		consumer.accept(new IClientFluidTypeExtensions(){
			@Override
			public @NotNull ResourceLocation getStillTexture(){
				return stillTexture;
			}
			@Override
			public @NotNull ResourceLocation getFlowingTexture(){
				return flowingTexture;
			}
		});
	}
}