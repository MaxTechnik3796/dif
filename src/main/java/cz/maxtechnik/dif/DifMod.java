package cz.maxtechnik.dif;

import com.mojang.logging.LogUtils;
import cz.maxtechnik.dif.item.armor.ElectroRunners;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import cz.maxtechnik.dif.command.ChunkLoaderCommand;
import cz.maxtechnik.dif.command.ConfigReloadCommand;
import cz.maxtechnik.dif.command.IsChunkLoadedCommand;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.basic.DifModSounds;
import cz.maxtechnik.dif.init.basic.DifModTabs;
import cz.maxtechnik.dif.init.fluid.DifModFluidTypes;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import cz.maxtechnik.dif.init.gui.DifModMenus;
import cz.maxtechnik.dif.init.other.*;
import cz.maxtechnik.dif.init.events.JetpackHandler;
import cz.maxtechnik.dif.renderer.*;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
@SuppressWarnings({"removal","deprecation"})
@Mod(DifMod.MODID)
public class DifMod {
	public static final String MODID = "dif";
	public static final Logger LOGGER = LogUtils.getLogger();

	public DifMod(IEventBus bus, ModContainer modContainer) {
		// Registrace modulů
		DifModBlocks.REGISTRY.register(bus);
		DifModItems.REGISTRY.register(bus);
		DifModItems.V_REGISTRY.register(bus);
		DifModTabs.REGISTER.register(bus);
		DifModSounds.REGISTRY.register(bus);
		DifModMobEffects.REGISTRY.register(bus);
		DifModBlockEntities.REGISTRY.register(bus);
		DifModMenus.REGISTRY.register(bus);
		DifModFluids.REGISTRY.register(bus);
		DifModFluidTypes.REGISTRY.register(bus);
		DifModRecipes.REGISTRY.register(bus);
		DifModRecipes.TYPE_REGISTRY.register(bus);
		DifModEntities.REGISTRY.register(bus);
		DifModFeatures.REGISTRY.register(bus);
		// REGISTRACE EVENTŮ
		NeoForge.EVENT_BUS.register(this);
		NeoForge.EVENT_BUS.register(JetpackHandler.class);
		NeoForge.EVENT_BUS.addListener(DifMod::onRenderGui);
		bus.addListener(DifModTabs::addCreative);
		modContainer.registerConfig(ModConfig.Type.COMMON, DifModCommonConfig.SPEC);
		bus.addListener(DifMod::registerCapabilities);
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event){
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DifModBlockEntities.QUARRY.get(),
			(be, side) -> be.getInventory());
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, DifModBlockEntities.QUARRY.get(),
			(be, side) -> be.getEnergyStorage());
		ElectroRunners.Boots.registerCapability(event, DifModItems.ELECTRO_RUNNERS.get());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DifModBlockEntities.ANDESITE_BARREL.get(),
				(be, side) -> be.getInventory());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DifModBlockEntities.COPPER_BARREL.get(),
				(be, side) -> be.getInventory());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DifModBlockEntities.BRASS_BARREL.get(),
				(be, side) -> be.getInventory());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DifModBlockEntities.SUPER_BOX.get(),
				(be, side) -> be.getInventory());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DifModBlockEntities.OLD_CHEST.get(),
				(be, side) -> be.getInventory());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DifModBlockEntities.BURNING_GENERATOR.get(),
				(be, side) -> be.getItemHandler());
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, DifModBlockEntities.BURNING_GENERATOR.get(),
				(be, side) -> be.getEnergyStorage());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DifModBlockEntities.SPACE_CRATE.get(),
				(be, side) -> be.getInventory());
	}


	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		LOGGER.info("DIF MOD: Server Starting");
	}

	@SubscribeEvent
	public void onCommandsRegister(RegisterCommandsEvent event) {
		ChunkLoaderCommand.register(event.getDispatcher());
		IsChunkLoadedCommand.register(event.getDispatcher());
		ConfigReloadCommand.register(event.getDispatcher());
	}
	public static void onRenderGui(RenderGuiLayerEvent.Post event) {
		if (event.getName().equals(net.neoforged.neoforge.client.gui.VanillaGuiLayers.HOTBAR)) {
			CarHudOverlay.render(event.getGuiGraphics());
		}
	}

	@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {


		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			LOGGER.info("DIF MOD: Client Setup");
			event.enqueueWork(() -> {
				try {
					LOGGER.info("DIF MOD: Flywheel BER fallback setup pro BrassLargeWaterWheel");

					ItemBlockRenderTypes.setRenderLayer(DifModBlocks.QUARRY_LANDMARK.get(), RenderType.cutout());
				} catch (Exception e) {
					LOGGER.error("DIF MOD: Chyba při Flywheel setup", e);
				}
			});
		}

		@SubscribeEvent
		public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
			event.registerBlockEntityRenderer(DifModBlockEntities.FRYING_TABLE.get(), context -> new FryingTableRenderer());
			event.registerBlockEntityRenderer(DifModBlockEntities.QUARRY.get(), context -> new QuarryRenderer());
			event.registerBlockEntityRenderer(DifModBlockEntities.CHUNK_LOADER_BE.get(), context -> new ChunkLoaderRenderer());
			event.registerEntityRenderer(DifModEntities.FORMULA.get(), CarRenderer::new);
			event.registerEntityRenderer(DifModEntities.REMOTE_MINECART.get(),context -> new MinecartRenderer<>(context,ModelLayers.MINECART)
			);
		}
	}

	public static String goggleTooltipFix = "    ";

	public static boolean rouletteBoolean(int range) {
		return 0 == Mth.nextInt(RandomSource.create(), 0, range);
	}

	public static boolean mouseIn(int mouseX, int mouseY, int x, int y, int w, int h) {
		return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
	}

	public static void sendMessageToPlayer(Player player, MutableComponent message) {
		MutableComponent messageTemplate = Component.empty();
		messageTemplate.append(Component.translatable("chat.dif.mod_prefix"));
		messageTemplate.append(CommonComponents.space());
		messageTemplate.append(message);
		player.sendSystemMessage(messageTemplate);
	}

	public static boolean playerGameModeIsCreativeCategory(ServerPlayer player) {
		return player.gameMode.isCreative()||player.gameMode.getGameModeForPlayer().equals(GameType.SPECTATOR);
	}
	public static void addItemStacksBehind(BuildCreativeModeTabContentsEvent tabData, ItemStack startStack, ItemStack[] addStacks) {
		for(ItemStack addStack:addStacks) tabData.insertAfter(startStack,addStack,CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}
}