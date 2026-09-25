package cz.maxtechnik.dif.init.fluid;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
@SuppressWarnings("removal")
public class FluidEntry{
	public static final List<FluidEntry> ALL_ENTRIES=new ArrayList<>();
	public final String name;
	public final DeferredHolder<FluidType,FluidType> type;
	public final DeferredHolder<Fluid,FlowingFluid> source;
	public final DeferredHolder<Fluid,FlowingFluid> flowing;
	public final DeferredBlock<LiquidBlock> block;
	public final DeferredItem<BucketItem> bucket;
	public final boolean isWaterLike;
	public final boolean isTranslucent;
	public FluidEntry(String name,boolean isWaterLike,boolean isTranslucent,Consumer<FluidType.Properties> typePropsModifier,Consumer<BaseFlowingFluid.Properties> fluidPropsModifier,DeferredRegister<Fluid> FLUIDS,DeferredRegister<FluidType> TYPES,DeferredRegister.Items ITEMS,DeferredRegister.Blocks BLOCKS){
		this.name=name;
		this.isWaterLike=isWaterLike;
		this.isTranslucent=isTranslucent;
		FluidType.Properties typeProps=FluidType.Properties.create();
		typePropsModifier.accept(typeProps);
		this.type=TYPES.register(name,()->new ModFluidType(typeProps,name));
		BaseFlowingFluid.Properties[] fluidProps=new BaseFlowingFluid.Properties[1];
		this.source=FLUIDS.register(name,()->new BaseFlowingFluid.Source(fluidProps[0]));
		this.flowing=FLUIDS.register("flowing_"+name,()->new BaseFlowingFluid.Flowing(fluidProps[0]));
		BlockBehaviour.Properties blockProps=BlockBehaviour.Properties.of()
				.noCollission().strength(100F).noLootTable().liquid()
				.pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable();
		this.block=BLOCKS.register(name,()->new LiquidBlock(this.source.get(),blockProps));
		Item.Properties bucketProps=new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1);
		this.bucket=ITEMS.register(name+"_bucket",()->new BucketItem(this.source.get(),bucketProps));
		fluidProps[0]=new BaseFlowingFluid.Properties(this.type,this.source,this.flowing).bucket(this.bucket).block(this.block);
		fluidPropsModifier.accept(fluidProps[0]);
		ALL_ENTRIES.add(this);
	}
	public static class ModFluidType extends FluidType{
		private final ResourceLocation stillTexture;
		private final ResourceLocation flowingTexture;
		public ModFluidType(Properties properties,String name){
			super(properties.sound(SoundActions.BUCKET_FILL,SoundEvents.BUCKET_FILL)
					.sound(SoundActions.BUCKET_EMPTY,SoundEvents.BUCKET_EMPTY)
					.sound(SoundActions.FLUID_VAPORIZE,SoundEvents.FIRE_EXTINGUISH));
			this.stillTexture=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/"+name+"_still");
			this.flowingTexture=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/"+name+"_flow");
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
		@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
		public static class ClientModEvents{
			@SubscribeEvent
			public static void onClientSetup(FMLClientSetupEvent event){
				event.enqueueWork(()->{
					for(FluidEntry entry: FluidEntry.ALL_ENTRIES){
						if(entry.isTranslucent){
							ItemBlockRenderTypes.setRenderLayer(entry.source.get(),RenderType.translucent());
							ItemBlockRenderTypes.setRenderLayer(entry.flowing.get(),RenderType.translucent());
						}
					}
				});
			}
		}
	}
}