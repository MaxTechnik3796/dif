package cz.maxtechnik.dif.fluid.template;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
public class BaseMoltenFluidType extends BaseFluidType{
	private final ResourceLocation stillTexture;
	private final ResourceLocation flowingTexture;
	private final int color;
	public BaseMoltenFluidType(Properties properties,String name,int color){
		super(properties,name);
		this.stillTexture=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/molten_fluid_still");
		this.flowingTexture=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/molten_fluid_flow");
		this.color=color;
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
			@Override
			public int getTintColor(){
				return color|0xFF000000;
			}
			@Override
			public int getTintColor(@NotNull FluidState fluidState,@NotNull BlockAndTintGetter world,@NotNull BlockPos pos){
				return color|0xFF000000;
			}
		});
	}
}