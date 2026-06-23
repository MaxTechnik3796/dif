package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.modular.v2.ModularTool;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = DifMod.MODID)
public class ModularCraftingHandler {

	/**
	 * Blokuje opravování modular toolů v crafting gridu.
	 * Minecraft's RepairItemRecipe kombinuje dva identické itemy a slučuje jejich durability.
	 * Tato událost se spustí, jakmile hráč změní obsah crafting gridu -
	 * pokud výsledkem je modular tool a v gridu jsou dva modular tooly, výsledek vymažeme.
	 */
	@SubscribeEvent
	public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
		ItemStack result = event.getCrafting();
		if (!(result.getItem() instanceof ModularTool)) return;

		// Zkontroluj, zda je v crafting containeru více než jeden ModularTool
		// (repair recipe kombinuje dva stejné itemy)
		if (!(event.getInventory() instanceof CraftingContainer craftingContainer)) return;

		int modularToolCount = 0;
		for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
			ItemStack slotStack = craftingContainer.getItem(i);
			if (!slotStack.isEmpty() && slotStack.getItem() instanceof ModularTool) {
				modularToolCount++;
			}
		}

		// Pokud jsou v gridu 2+ modular tooly, je to repair recipe - zablokujeme výsledek
		if (modularToolCount >= 2) {
			event.getCrafting().setCount(0);
		}
	}
}
