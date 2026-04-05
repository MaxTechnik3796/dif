package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import com.mojang.blaze3d.platform.InputConstants;
import cz.maxtechnik.dif.network.JetpackFlyMessage;
import cz.maxtechnik.dif.network.EnderOpenMessage;
import cz.maxtechnik.dif.network.RemoteControlPacket;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
@Mod.EventBusSubscriber(modid=DifMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class DifModKeys{
	public static final String CATEGORY="key.categories.dif";
	public static final KeyMapping JETPACK_FLY=new KeyMapping("key.dif.jetpack_fly",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_SPACE,CATEGORY);
	public static final KeyMapping OPEN_ENDER_CHEST=new KeyMapping("key.dif.open_ender",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_V,CATEGORY);
	// NOVÉ KLÁVESY PRO MINECART
	public static final KeyMapping MINE_FORWARD=new KeyMapping("key.dif.remote_minecart_boost",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_I,CATEGORY);
	public static final KeyMapping MINE_BACKWARD=new KeyMapping("key.dif.remote_minecart_invert",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_K,CATEGORY);
	@SubscribeEvent
	public static void registerKeys(RegisterKeyMappingsEvent event){
		event.register(JETPACK_FLY);
		event.register(OPEN_ENDER_CHEST);
		event.register(MINE_FORWARD);
		event.register(MINE_BACKWARD);
	}
	@Mod.EventBusSubscriber(modid=DifMod.MODID, value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.FORGE)
	public static class ClientTickHandler{
		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event){
			if(event.phase==TickEvent.Phase.END&&Minecraft.getInstance().player!=null){
				var player=Minecraft.getInstance().player;
				// Existující logika Jetpacku a Ender chesty
				if(JETPACK_FLY.isDown()){
					DifMod.PACKET_HANDLER.sendToServer(new JetpackFlyMessage(0,0));
					JetpackFlyMessage.pressAction(player,0);
				}
				while(OPEN_ENDER_CHEST.consumeClick()){
					DifMod.PACKET_HANDLER.sendToServer(new EnderOpenMessage(0,0));
				}
				// NOVÁ LOGIKA: Ovládání Minecartu jako vozidla
				// V ClientTickHandler v DifModKeys.java:
				if (player.getMainHandItem().is(DifModItems.REMOTE_CONTROLLER.get())) {
					// PLYN (I) - Posíláme 1.0 dokud se drží
					if (MINE_FORWARD.isDown()) {
						DifMod.PACKET_HANDLER.sendToServer(new RemoteControlPacket(1.0, false));
					}

					// REVERZ (K) - Pošle signál k otočení jen při stisku
					if (MINE_BACKWARD.consumeClick()) {
						DifMod.PACKET_HANDLER.sendToServer(new RemoteControlPacket(0.0, true));
					}
				}
			}
		}
	}
}