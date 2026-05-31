package cz.maxtechnik.dif.init.events.client;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems; // Předpokládám registr tvých itemů
import cz.maxtechnik.dif.init.other.DifModComponents;
import cz.maxtechnik.dif.item.modular.v2.ModularMaterial;
import cz.maxtechnik.dif.item.modular.v2.ModularTool;
import cz.maxtechnik.dif.item.modular.v2.ModularToolProperties;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
@SuppressWarnings("removal")
@EventBusSubscriber(modid = DifMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModularToolClientHandler {

    // 1. REGISTRACE BARVENÍ VRSTEV (TINT INDEXY)
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            if (stack.getItem() instanceof ModularTool) {
                ModularToolProperties props = stack.get(DifModComponents.MODULAR_PROPERTIES.get());
                if (props != null) {
                    // tintIndex odpovídá pořadí vrstev v JSON modelu (layer0, layer1, layer2)
                    if (tintIndex == 0) return ModularMaterial.byName(props.handleMaterial()).getColor();
                    if (tintIndex == 1) return ModularMaterial.byName(props.bindingMaterial()).getColor();
                    if (tintIndex == 2) return ModularMaterial.byName(props.headMaterial()).getColor();
                }
            }
            return -1; // Žádné zabarvení pro ostatní indexy
        }, DifModItems.MODULAR_TOOL.get()); // Tvůj zaregistrovaný modulární item
    }

    // 2. REGISTRACE PREDICATU PRO PŘEPÍNÁNÍ MODELŮ (TYP + STAV)
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->ItemProperties.register(
			DifModItems.MODULAR_TOOL.get(),
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "tool_state"),
			(stack, level, entity, seed) -> {
				if (!(stack.getItem() instanceof ModularTool tool)) return 0.0F;
				ModularToolProperties props = stack.get(DifModComponents.MODULAR_PROPERTIES.get());
				if (props == null) return 0.0F;

				String type = props.toolType().toLowerCase(java.util.Locale.ROOT);
				boolean broken = tool.isBroken(stack);

				// Zakódujeme stavy do jednoduchých čísel
				float base = switch (type) {
					case "pickaxe" -> 1.0F;
					case "axe"     -> 2.0F;
					case "sword"   -> 3.0F;
					case "shovel"  -> 4.0F;
					case "hoe"     -> 5.0F;
					default        -> 0.0F;
				};

				if (base == 0.0F) return 0.0F;
				// Pokud je nástroj zlomený, přičteme 0.5F (přepne model na broken variantu)
				return broken ? base + 0.5F : base;
			}
		));
    }
}