package cz.maxtechnik.dif.init.events.client;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
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
import net.neoforged.neoforge.client.event.ModelEvent; // NOVÝ IMPORT

@SuppressWarnings("removal")
@EventBusSubscriber(modid = DifMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModularToolClientHandler {

	// 1. REGISTRACE BARVENÍ VRSTEV (S FIXEM PRO PRŮHLEDNOST)
	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
		event.register((stack, tintIndex) -> {
			if (stack.getItem() instanceof ModularTool) {
				ModularToolProperties props = stack.get(DifModComponents.MODULAR_PROPERTIES.get());
				if (props != null) {
					int color = -1;
					if (tintIndex == 0) color = ModularMaterial.byName(props.handleMaterial()).getColor();
					if (tintIndex == 1) color = ModularMaterial.byName(props.bindingMaterial()).getColor();
					if (tintIndex == 2) color = ModularMaterial.byName(props.headMaterial()).getColor();

					// FIX: Pomocí bitového operátoru OR (|) vnutíne barvě plnou hodnotu Alpha kanálu (0xFF000000)
					if (color != -1) {
						return color | 0xFF000000;
					}
				}
			}
			return -1;
		}, DifModItems.MODULAR_TOOL.get());
	}

	// 2. FIX PRO MODELY: Registrace dodatečných modelů, aby je hra načetla a zapekla (bake) do RAM
	// Bez tohoto bloku kódu budou modely specifikované v overrides JSONu pro hru neviditelné!
	@SubscribeEvent
	public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
		event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
				ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "item/modular_pickaxe"), "inventory"));

		event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
				ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "item/modular_pickaxe_broken"), "inventory"));

		event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
				ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "item/modular_axe"), "inventory"));

		event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
				ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "item/modular_axe_broken"), "inventory"));

		// Až vytvoříš JSONy pro meč, lopatu a motyku, stačí je sem odkomentovat (přepsané na ModelResourceLocation):
		// event.register(new net.minecraft.client.resources.model.ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "item/modular_sword"), "inventory"));
		// event.register(new net.minecraft.client.resources.model.ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "item/modular_sword_broken"), "inventory"));
		// event.register(new net.minecraft.client.resources.model.ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "item/modular_shovel"), "inventory"));
		// event.register(new net.minecraft.client.resources.model.ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "item/modular_shovel_broken"), "inventory"));
		// event.register(new net.minecraft.client.resources.model.ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "item/modular_hoe"), "inventory"));
		// event.register(new net.minecraft.client.resources.model.ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "item/modular_hoe_broken"), "inventory"));
	}

	// 3. REGISTRACE PREDICATU PRO PŘEPÍNÁNÍ MODELŮ (ZŮSTÁVÁ STEJNÁ)
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> ItemProperties.register(
				DifModItems.MODULAR_TOOL.get(),
				ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "tool_state"),
				(stack, level, entity, seed) -> {
					if (!(stack.getItem() instanceof ModularTool tool)) return 0.0F;
					ModularToolProperties props = stack.get(DifModComponents.MODULAR_PROPERTIES.get());
					if (props == null) return 0.0F;

					String type = props.toolType().toLowerCase(java.util.Locale.ROOT);
					boolean broken = tool.isBroken(stack);

					float base = switch (type) {
						case "pickaxe" -> 1.0F;
						case "axe"     -> 2.0F;
						case "sword"   -> 3.0F;
						case "shovel"  -> 4.0F;
						case "hoe"     -> 5.0F;
						default        -> 0.0F;
					};

					if (base == 0.0F) return 0.0F;
					return broken ? base + 0.5F : base;
				}
		));
	}
}