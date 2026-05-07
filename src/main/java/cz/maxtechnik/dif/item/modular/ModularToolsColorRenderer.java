package cz.maxtechnik.dif.item.modular;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.Objects;

import static cz.maxtechnik.dif.item.modular.ModularBase.D;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class ModularToolsColorRenderer{
	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item event){
		event.register((itemStack,tintIndex)->switch(tintIndex){
			case 0 -> Objects.requireNonNull(itemStack.get(D)).copyTag().contains("HandleColor")?Objects.requireNonNull(itemStack.get(D)).copyTag().getInt("HandleColor"):-1;
			case 1 -> Objects.requireNonNull(itemStack.get(D)).copyTag().contains("BindingColor")?Objects.requireNonNull(itemStack.get(D)).copyTag().getInt("BindingColor"):-1;
			case 2 -> Objects.requireNonNull(itemStack.get(D)).copyTag().contains("HeadColor")?Objects.requireNonNull(itemStack.get(D)).copyTag().getInt("HeadColor"):-1;
			default -> -1;
		},
				DifModItems.MODULAR_PICKAXE.get(),
				DifModItems.MODULAR_SWORD.get(),
				DifModItems.MODULAR_SHOVEL.get(),
				DifModItems.MODULAR_AXE.get());
		event.register((itemStack,tintIndex)->{
					if(Objects.requireNonNull(itemStack.get(D)).copyTag().contains("HeadColor")){
						return Objects.requireNonNull(itemStack.get(D)).copyTag().getInt("HeadColor");
					}else if(Objects.requireNonNull(itemStack.get(D)).copyTag().contains("BindingColor")){
						return Objects.requireNonNull(itemStack.get(D)).copyTag().getInt("BindingColor");
					}else if(Objects.requireNonNull(itemStack.get(D)).copyTag().contains("HandleColor")){
						return Objects.requireNonNull(itemStack.get(D)).copyTag().getInt("HandleColor");
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