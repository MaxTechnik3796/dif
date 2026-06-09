package cz.maxtechnik.dif.init.events.client;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.other.DifModComponents;
import cz.maxtechnik.dif.item.modular.v2.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.Locale;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class ModularClientHandler{
	// 1. REGISTRACE BARVENÍ VRSTEV (S FIXEM PRO PRŮHLEDNOST)
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
					// FIX: Pomocí bitového operátoru OR (|) vnutíne barvě plnou hodnotu Alpha kanálu (0xFF000000)
					if(color!=-1) return color|0xFF000000;
				}
			}else if(itemStack.getItem() instanceof ModularPart){
				ModularPartProperties props=itemStack.get(DifModComponents.MODULAR_PART_PROPERTIES.get());
				if(props!=null){
					int color=-1;
					if(tintIndex==0) color=ModularMaterial.byName(props.material()).getColor();
					if(color!=-1) return color|0xFF000000;
				}
			}
			return -1;
		},DifModItems.MODULAR_TOOL,DifModItems.MODULAR_PART);
	}
	// 3. REGISTRACE PREDICATU PRO PŘEPÍNÁNÍ MODELŮ (ZŮSTÁVÁ STEJNÁ)
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event){
		event.enqueueWork(()->ItemProperties.register(DifModItems.MODULAR_TOOL.get(),
				ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"tool_state"),
				(itemStack,level,entity,seed)->{
					if(!(itemStack.getItem() instanceof ModularTool tool)) return 0.0F;
					ModularToolProperties props=itemStack.get(DifModComponents.MODULAR_TOOL_PROPERTIES.get());
					if(props==null) return 0.0F;
					String type=props.toolType().toLowerCase(Locale.ROOT);
					boolean broken=tool.isBroken(itemStack);
					float base=switch(type){
						case "pickaxe" -> 1.0F;
						case "axe" -> 2.0F;
						case "sword" -> 3.0F;
						case "shovel" -> 4.0F;
						case "hoe" -> 5.0F;
						default -> 0.0F;
					};
					if(base==0.0F) return 0.0F;
					return broken?base+0.5F:base;
				}
		));
		event.enqueueWork(()->ItemProperties.register(DifModItems.MODULAR_PART.get(),
				ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"part_state"),
				(itemStack,level,entity,seed)->{
					if(!(itemStack.getItem() instanceof ModularPart)) return 0.0F;
					ModularPartProperties props=itemStack.get(DifModComponents.MODULAR_PART_PROPERTIES.get());
					if(props==null) return 0.0F;
					String type=props.partType().toLowerCase(Locale.ROOT);
					return switch(type){
						case "handle" -> 1.0F;
						case "binding" -> 2.0F;
						case "axe_head" -> 3.0F;
						case "pickaxe_head" -> 4.0F;
						case "sword_head" -> 5.0F;
						case "shovel_head" -> 6.0F;
						case "sword_binding" -> 7.0F;
						case "hoe_head" -> 8.0F;
						default -> 0.0F;
					};
				}
		));
	}
}