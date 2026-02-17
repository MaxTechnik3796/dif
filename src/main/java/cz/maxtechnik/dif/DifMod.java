package cz.maxtechnik.dif;

import com.mojang.logging.LogUtils;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.basic.DifModSounds;
import cz.maxtechnik.dif.init.basic.DifModTabs;
import cz.maxtechnik.dif.init.fluid.DifModFluidTypes;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import cz.maxtechnik.dif.init.gui.DifModMenus;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.init.other.DifModMobEffects;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.init.other.DifModDimensions;
import cz.maxtechnik.dif.init.events.JetpackHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.BiConsumer;

@SuppressWarnings("removal")
@Mod(DifMod.MODID)
public class DifMod {
	public static final String MODID = "dif";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(MODID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);
	private static int messageID = 0;

	public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
		PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
		messageID++;
	}

	public DifMod() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

		bus.addListener(this::commonSetup);

		// Registrace modulů
		DifModDimensions.register();
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

		// REGISTRACE EVENTŮ
		MinecraftForge.EVENT_BUS.register(this);
		// Registrujeme JetpackHandler, aby fungoval let
		MinecraftForge.EVENT_BUS.register(JetpackHandler.class);

		bus.addListener(DifModTabs::addCreative);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DifModCommonConfig.SPEC);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		LOGGER.info("DIF MOD: Common Setup");
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		LOGGER.info("DIF MOD: Server Starting");
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			LOGGER.info("DIF MOD: Client Setup");
		}
	}

	// --- POMOCNÉ METODY (Navráceny zpět) ---

	public static boolean rouletteBoolean(int range) {
		return 0 == Mth.nextInt(RandomSource.create(), 0, range);
	}

	/**
	 * Metoda pro detekci myši v určité oblasti (používá se v GUI/Screenu)
	 */
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
}