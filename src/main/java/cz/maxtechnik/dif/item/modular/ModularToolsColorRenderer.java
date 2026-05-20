package cz.maxtechnik.dif.item.modular;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = DifMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModularToolsColorRenderer {
	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
		// Nástroje — 3 tint vrstvy: 0=handle, 1=binding, 2=head
		event.register((stack, tintIndex) -> {
					ModularToolData data = ModularBase.getToolData(stack);
					return switch (tintIndex) {
						case 0 -> data.handle().color;
						case 1 -> data.binding().color;
						case 2 -> data.head().color;
						default -> -1;
					};
				},
				DifModItems.MODULAR_PICKAXE.get(),
				DifModItems.MODULAR_SWORD.get(),
				DifModItems.MODULAR_SHOVEL.get(),
				DifModItems.MODULAR_AXE.get());

		// Části nástroje — 1 tint vrstva podle typu části
		event.register((stack, tintIndex) -> {
					ModularToolData data = ModularBase.getToolData(stack);
					if (ModularBase.isHead(stack)) return data.head().color;
					if (ModularBase.isBinding(stack)) return data.binding().color;
					if (ModularBase.isHandle(stack)) return data.handle().color;
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