package cz.maxtechnik.dif.item.modular.v2;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.other.DifModComponents;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.Locale;

import static cz.maxtechnik.dif.init.basic.DifModItems.*;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class ModularColorHandler{
	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item event){
		event.register((itemStack,tintIndex)->{
			if(itemStack.getItem() instanceof ModularTool){
				ModularToolProperties props=itemStack.get(DifModComponents.MODULAR_TOOL_PROPERTIES.get());
				if(props!=null){
					int color=-1;
					if(tintIndex==0) color=ModularMaterial.byName(props.handleMaterial()).getColor();
					if(tintIndex==1) color=ModularMaterial.byName(props.bindingMaterial()).getColor();
					if(tintIndex==2) color=ModularMaterial.byName(props.headMaterial()).getColor();
					if(color!=-1) return color|0xFF000000;
				}
			}else if(itemStack.getItem() instanceof ModularPart){
				ModularPartProperties props=itemStack.get(DifModComponents.MODULAR_PART_PROPERTIES.get());
				if(props!=null){
					int color=-1;
					if(props.castMold()){
						if(tintIndex==0) color=0xFFFFFF;
						if(tintIndex==1) color=ModularMaterial.byName(props.material()).getColor();
					}else if(tintIndex==0) color=ModularMaterial.byName(props.material()).getColor();
					if(color!=-1) return color|0xFF000000;
				}
			}
            return tintIndex;
        },MODULAR_TOOL,MODULAR_PART);
		event.register((itemStack,tintIndex)->{
			if(tintIndex==1){
				var contained = net.neoforged.neoforge.fluids.FluidUtil.getFluidContained(itemStack);
				if(contained.isPresent() && !contained.get().isEmpty()){
					return net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(contained.get().getFluid()).getTintColor();
				}
			}
			return -1;
		},MOLTEN_IRON_BUCKET,MOLTEN_COPPER_BUCKET,MOLTEN_GOLD_BUCKET,MOLTEN_STEEL_BUCKET,MOLTEN_OBSIDIAN_BUCKET,MOLTEN_ZINC_BUCKET,MOLTEN_BRASS_BUCKET,MOLTEN_NICKEL_BUCKET,MOLTEN_MITHRIL_BUCKET);
	}
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event){
		event.enqueueWork(()->ItemProperties.register(MODULAR_TOOL.get(),
				ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"tool_state"),
				(itemStack,level,entity,seed)->{
					if(!(itemStack.getItem() instanceof ModularTool tool)) return 0F;
					ModularToolProperties props=itemStack.get(DifModComponents.MODULAR_TOOL_PROPERTIES.get());
					if(props==null) return 0F;
					float base=ModularTools.byName(props.toolType().toLowerCase(Locale.ROOT)).getRenderIndex();
					if(base==0F) return 0F;
					return tool.isBroken(itemStack)?base+0.5F:base;
				}
		));
		event.enqueueWork(()->ItemProperties.register(MODULAR_PART.get(),
				ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"part_state"),
				(itemStack,level,entity,seed)->{
					if(!(itemStack.getItem() instanceof ModularPart)) return 0F;
					ModularPartProperties props=itemStack.get(DifModComponents.MODULAR_PART_PROPERTIES.get());
					if(props==null) return 0F;
					float base=ModularParts.byName(props.partType().toLowerCase(Locale.ROOT)).getRenderIndex();
					if(base==0F) return 0F;
					return ModularPart.isCast(itemStack)?base+0.5F:base;
				}
		));
	}
}