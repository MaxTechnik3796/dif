package cz.maxtechnik.dif.item.modular;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import static cz.maxtechnik.dif.item.modular.ModularBase.D;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class ModularToolsColorRenderer{
	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item event){
		event.register((itemStack,tintIndex)->{
					var data=itemStack.get(D);
					if(data==null) return -1;
					var tag=data.copyTag();
					return switch(tintIndex){
						case 0 -> tag.contains("HandleColor")?tag.getInt("HandleColor"):-1;
						case 1 -> tag.contains("BindingColor")?tag.getInt("BindingColor"):-1;
						case 2 -> tag.contains("HeadColor")?tag.getInt("HeadColor"):-1;
						default -> -1;
					};
				},
				DifModItems.MODULAR_PICKAXE.get(),
				DifModItems.MODULAR_SWORD.get(),
				DifModItems.MODULAR_SHOVEL.get(),
				DifModItems.MODULAR_AXE.get());
		event.register((itemStack,tintIndex)->{
					var data=itemStack.get(D);
					if(data==null) return -1;
					var tag=data.copyTag();
					if(tintIndex==0){
						if(tag.contains("HeadColor")) return tag.getInt("HeadColor");
						if(tag.contains("BindingColor")) return tag.getInt("BindingColor");
						if(tag.contains("HandleColor")) return tag.getInt("HandleColor");
					}
					return -1;
				},
				DifModItems.MODULAR_PART_PICKAXE_HEAD.get(),
				DifModItems.MODULAR_PART_AXE_HEAD.get(),
				DifModItems.MODULAR_PART_SHOVEL_HEAD.get(),
				DifModItems.MODULAR_PART_SWORD_HEAD.get(),
				DifModItems.MODULAR_PART_BINDING.get(),
				DifModItems.MODULAR_PART_SWORD_BINDING.get(),
				DifModItems.MODULAR_PART_HANDLE.get());
	}
}