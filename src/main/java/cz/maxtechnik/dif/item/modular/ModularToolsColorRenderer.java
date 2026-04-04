package cz.maxtechnik.dif.item.modular;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=DifMod.MODID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public class ModularToolsColorRenderer{
	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item event){
		event.register((itemStack,tintIndex)->{
			if(itemStack.getTag()==null) return -1;
			return switch(tintIndex){
				case 0 -> itemStack.getTag().contains("HandleColor")?itemStack.getTag().getInt("HandleColor"):-1;
				case 1 -> itemStack.getTag().contains("BindingColor")?itemStack.getTag().getInt("BindingColor"):-1;
				case 2 -> itemStack.getTag().contains("HeadColor")?itemStack.getTag().getInt("HeadColor"):-1;
				default -> -1;
			};
		},
				DifModItems.MODULAR_PICKAXE.get(),
				DifModItems.MODULAR_SWORD.get(),
				DifModItems.MODULAR_SHOVEL.get(),
				DifModItems.MODULAR_AXE.get());
		event.register((itemStack,tintIndex)->{
					if(itemStack.getTag()==null) return -1;
					if(itemStack.getTag().contains("HeadColor")){
						return itemStack.getTag().getInt("HeadColor");
					}else if(itemStack.getTag().contains("BindingColor")){
						return itemStack.getTag().getInt("BindingColor");
					}else if(itemStack.getTag().contains("HandleColor")){
						return itemStack.getTag().getInt("HandleColor");
					}else{
						return -1;
					}
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