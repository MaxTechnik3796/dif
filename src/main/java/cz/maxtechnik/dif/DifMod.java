package cz.maxtechnik.dif;

import com.mojang.logging.LogUtils;
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
import cz.maxtechnik.dif.renderer.*;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
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
public class DifMod{
	public static final String MODID="dif";
	public static final Logger LOGGER=LogUtils.getLogger();
	public static final String PROTOCOL_VERSION="1";
	public static final SimpleChannel PACKET_HANDLER=NetworkRegistry.newSimpleChannel(
			new ResourceLocation(MODID,"main"),
			()->PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);
	private static int messageID=0;
	public static <T> void addNetworkMessage(Class<T> messageType,BiConsumer<T,FriendlyByteBuf> encoder,Function<FriendlyByteBuf,T> decoder,BiConsumer<T,Supplier<NetworkEvent.Context>> messageConsumer){
		PACKET_HANDLER.registerMessage(messageID,messageType,encoder,decoder,messageConsumer);
		messageID++;
	}
	public DifMod(){
		IEventBus bus=FMLJavaModLoadingContext.get().getModEventBus();
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
		DifModRecipes.TYPE_REGISTRY.register(bus);
		DifModEntities.REGISTRY.register(bus);
		// REGISTRACE EVENTŮ
		MinecraftForge.EVENT_BUS.register(this);
		// Registrujeme JetpackHandler, aby fungoval let
		MinecraftForge.EVENT_BUS.register(JetpackHandler.class);
		bus.addListener(DifModTabs::addCreative);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,DifModCommonConfig.SPEC);
	}
	private void commonSetup(final FMLCommonSetupEvent event){
		LOGGER.info("DIF MOD: Common Setup");
		event.enqueueWork(() ->addNetworkMessage(
				RemoteControlPacket.class,
				RemoteControlPacket::encode,
				RemoteControlPacket::decode,
				RemoteControlPacket::handle
		));
		event.enqueueWork(() ->addNetworkMessage(
				CameraExitPacket.class,
				CameraExitPacket::encode,
				CameraExitPacket::decode,
				CameraExitPacket::handle
		));

		// PŘIDÁNO: Registrace řazení
		event.enqueueWork(() ->addNetworkMessage(
				cz.maxtechnik.dif.network.ModNetworking.ShiftGearPacket.class,
				cz.maxtechnik.dif.network.ModNetworking.ShiftGearPacket::encode,
				cz.maxtechnik.dif.network.ModNetworking.ShiftGearPacket::decode,
				cz.maxtechnik.dif.network.ModNetworking.ShiftGearPacket::handle
		));
		event.enqueueWork(() -> addNetworkMessage(
				SyncCarPositionPacket.class,
				SyncCarPositionPacket::encode,
				SyncCarPositionPacket::decode,
				SyncCarPositionPacket::handle
		));
	}
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event){
		LOGGER.info("DIF MOD: Server Starting");
	}
	@SubscribeEvent
	public void onCommandsRegister(RegisterCommandsEvent event){
		ChunkLoaderCommand.register(event.getDispatcher());
		IsChunkLoadedCommand.register(event.getDispatcher());
		ConfigReloadCommand.register(event.getDispatcher());
	}
	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ModBusEvents {
		@SubscribeEvent
		public static void entityAttributes(net.minecraftforge.event.entity.EntityAttributeCreationEvent event) {
			event.put(DifModEntities.WITHER_TITAN.get(), cz.maxtechnik.dif.entity.WitherTitanEntity.createAttributes().build());
		}
	}
	@SubscribeEvent
	public void onRenderGui(RenderGuiOverlayEvent.Post event) {
		if (event.getOverlay().id().getPath().equals("hotbar")) {
			CarHudOverlay.render(event.getGuiGraphics());
		}
	}
	@Mod.EventBusSubscriber(modid=MODID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
	public static class ClientModEvents{
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event){
			LOGGER.info("DIF MOD: Client Setup");
		}
		@SubscribeEvent
		public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event){
			event.registerBlockEntityRenderer(DifModBlockEntities.FRYING_TABLE.get(),context->new FryingTableRenderer());
			event.registerBlockEntityRenderer(DifModBlockEntities.QUARRY.get(),context->new QuarryRenderer());
			event.registerBlockEntityRenderer(DifModBlockEntities.CHUNK_LOADER_BE.get(),context->new ChunkLoaderRenderer());
			event.registerEntityRenderer(DifModEntities.WITHER_TITAN.get(), WitherTitanRenderer::new);
			event.registerEntityRenderer(DifModEntities.FORMULA.get(), CarRenderer::new);
			event.registerEntityRenderer(DifModEntities.REMOTE_MINECART.get(),
					context -> new net.minecraft.client.renderer.entity.MinecartRenderer<>(
							context,
							net.minecraft.client.model.geom.ModelLayers.MINECART
					)
			);
		}
	}
	/**
	 * Fixing space in goggle tooltip
	 */
	public static String goggleTooltipFix="    ";
	/**
	 * Radom chance generator
	 */
	public static boolean rouletteBoolean(int range){
		return 0==Mth.nextInt(RandomSource.create(),0,range);
	}
	/**
	 * Method for detecting mouse in a specific area (used in GUI/Screen)
	 * @param  mouseX Mouse X position.
	 * @param mouseY Mouse Y position.
	 * @param x X offset from 0. (top left corner)
	 * @param y Y offset from 0. (top left corner)
	 * @param w Width of area. (bottom right corner)
	 * @param h Height of area. (bottom right corner)
	 */
	public static boolean mouseIn(int mouseX,int mouseY,int x,int y,int w,int h){
		return mouseX>=x&&mouseX<x+w&&mouseY>=y&&mouseY<y+h;
	}
	/**
	 * Send message to player with mod prefix
	 */
	public static void sendMessageToPlayer(Player player,MutableComponent message){
		MutableComponent messageTemplate=Component.empty();
		messageTemplate.append(Component.translatable("chat.dif.mod_prefix"));
		messageTemplate.append(CommonComponents.space());
		messageTemplate.append(message);
		player.sendSystemMessage(messageTemplate);
	}
	/**
	 * Check if is player in creative based gamemode
	 */
	public static boolean playerGameModeIsCreativeCategory(ServerPlayer player){
		return player.gameMode.isCreative()||player.gameMode.getGameModeForPlayer().equals(GameType.SPECTATOR);
	}
	/**
	 *Insert several ItemStacks in a row in one creative tab behind an ItemStack without much code.
	 *@param tabData Tab builder. (event)
	 *@param startStack Stack after which insertion will begin.
	 *@param addStacks List of ItemStacks you want to insert.
	 */
	public static void addItemStacksBehind(BuildCreativeModeTabContentsEvent tabData,ItemStack startStack,ItemStack[] addStacks){
		for(int i=0;i<addStacks.length;i++){
			if(i==0) tabData.getEntries().putAfter(startStack,addStacks[i],CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			else tabData.getEntries().putAfter(addStacks[i-1],addStacks[i],CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}
	}
}