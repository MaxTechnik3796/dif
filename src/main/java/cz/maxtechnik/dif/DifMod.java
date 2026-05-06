package cz.maxtechnik.dif;

import com.mojang.logging.LogUtils;
import cz.maxtechnik.dif.renderer.BrassWaterWheelRenderer;
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
import cz.maxtechnik.dif.network.CameraExitPacket;
import cz.maxtechnik.dif.network.RemoteControlPacket;
import cz.maxtechnik.dif.network.ModNetworking.SyncCarPositionPacket;
import cz.maxtechnik.dif.network.ModNetworking.ShiftGearPacket;
import cz.maxtechnik.dif.renderer.*;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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
import net.neoforged.neoforge.client.event.RenderGuiOverlayEvent;
import net.neoforged.neoforge.network.PacketDistributor;

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
		bus.addListener(DifModTabs::addCreative);
		modContainer.registerConfig(ModConfig.Type.COMMON, DifModCommonConfig.SPEC);
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
	@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.NEOFORGE, value = Dist.CLIENT)
	public static class ClientForgeEvents {
		@SubscribeEvent
		public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
			if (event.getOverlay().id().getPath().equals("hotbar")) {
				CarHudOverlay.render(event.getGuiGraphics());
			}
		}
	}

	@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {
		public static final PartialModel BRASS_PRESS_HEAD = PartialModel.of(new ResourceLocation(MODID,"block/brass_mechanical_press_head"));
		public static final PartialModel BRASS_MIXER_POLE = PartialModel.of(new ResourceLocation(MODID, "block/brass_mechanical_mixer_pole"));
		public static final PartialModel BRASS_MIXER_HEAD = PartialModel.of(new ResourceLocation(MODID, "block/brass_mechanical_mixer_head"));


		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			LOGGER.info("DIF MOD: Client Setup");

			// KLÍČOVÉ: Říkáme Flywheel, aby pro náš BE nepoužíval instancing visual,
			// ale přenechal rendering našemu BlockEntityRenderer (BrassWaterWheelRenderer).
			// Bez tohoto Flywheel buď nic nevykreslí (nenajde visual pro náš typ),
			// nebo se pokusí použít Create's LargeWaterWheel visual, který generuje
			// dynamický model ze dřeva – ne náš OBJ.
			event.enqueueWork(() -> {
				try {
					// Flywheel 1.0.5 API: VisualizationManager nemá přímé "skip" API,
					// ale pokud pro daný BlockEntityType není registrován žádný VisualType,
					// Flywheel automaticky fallbackuje na standardní BER rendering.
					// Náš BRASS_LARGE_WATER_WHEEL BlockEntityType NENÍ registrován
					// v Create's AllBlockEntityTypes, takže by měl projít BER automaticky.
					// Tento log nám pomůže potvrdit že se client setup zavolal.
					LOGGER.info("DIF MOD: Flywheel BER fallback setup pro BrassLargeWaterWheel");

					ItemBlockRenderTypes.setRenderLayer(DifModBlocks.BRASS_LARGE_WATER_WHEEL.get(), RenderType.cutout());
					ItemBlockRenderTypes.setRenderLayer(DifModBlocks.BRASS_WATER_WHEEL.get(), RenderType.cutout());
					ItemBlockRenderTypes.setRenderLayer(DifModBlocks.BRASS_MECHANICAL_PRESS.get(), RenderType.cutout());
					ItemBlockRenderTypes.setRenderLayer(DifModBlocks.BRASS_MECHANICAL_MIXER.get(), RenderType.cutout());
					ItemBlockRenderTypes.setRenderLayer(DifModBlocks.QUARRY_LANDMARK.get(), RenderType.cutout());
				} catch (Exception e) {
					LOGGER.error("DIF MOD: Chyba při Flywheel setup", e);
				}
			});
		}

		@SubscribeEvent
		public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
			event.register(new ResourceLocation("dif", "block/brass_large_water_wheel"));
			event.register(new ResourceLocation("dif", "block/brass_large_water_wheel_extension"));
			event.register(new ResourceLocation("dif", "block/brass_water_wheel"));
			event.register(new ResourceLocation("dif", "block/brass_water_wheel_wheel"));
			event.register(new ResourceLocation("dif", "block/brass_mechanical_press_head"));
			event.register(new ResourceLocation("dif", "block/brass_mechanical_mixer_pole"));
			event.register(new ResourceLocation("dif", "block/brass_mechanical_mixer_head"));
		}

		@SubscribeEvent
		public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
			event.registerBlockEntityRenderer(DifModBlockEntities.FRYING_TABLE.get(), context -> new FryingTableRenderer());
			event.registerBlockEntityRenderer(DifModBlockEntities.QUARRY.get(), context -> new QuarryRenderer());
			event.registerBlockEntityRenderer(DifModBlockEntities.CHUNK_LOADER_BE.get(), context -> new ChunkLoaderRenderer());
			event.registerBlockEntityRenderer(DifModBlockEntities.BRASS_LARGE_WATER_WHEEL.get(), BrassWaterWheelRenderer::new);
			event.registerBlockEntityRenderer(DifModBlockEntities.BRASS_WATER_WHEEL.get(), BrassWaterWheelRenderer::new);
			event.registerBlockEntityRenderer(DifModBlockEntities.BRASS_MECHANICAL_PRESS.get(), cz.maxtechnik.dif.renderer.BrassMechanicalPressRenderer::new);
			event.registerBlockEntityRenderer(DifModBlockEntities.BRASS_MECHANICAL_MIXER.get(), cz.maxtechnik.dif.renderer.BrassMechanicalMixerRenderer::new);
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
		return player.gameMode.isCreative() || player.gameMode.getGameModeForPlayer().equals(GameType.SPECTATOR);
	}

	public static void addItemStacksBehind(BuildCreativeModeTabContentsEvent tabData, ItemStack startStack, ItemStack[] addStacks) {
		for (int i = 0; i < addStacks.length; i++) {
			if (i == 0) tabData.getEntries().putAfter(startStack, addStacks[i], CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			else tabData.getEntries().putAfter(addStacks[i - 1], addStacks[i], CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}
	}
}