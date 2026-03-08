package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import com.mojang.blaze3d.platform.InputConstants;
import cz.maxtechnik.dif.network.JetpackFlyMessage;
import cz.maxtechnik.dif.network.EnderOpenMessage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid=DifMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class DifModKeys {
	public static final KeyMapping JETPACK_FLY = new KeyMapping("key.dif.jetpack_fly", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SPACE, "key.categories.dif");
	public static final KeyMapping OPEN_ENDER_CHEST = new KeyMapping("key.dif.open_ender", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.dif");

	@SubscribeEvent
	public static void registerKeys(RegisterKeyMappingsEvent event){
		event.register(JETPACK_FLY);
		event.register(OPEN_ENDER_CHEST);
	}

	@Mod.EventBusSubscriber(modid=DifMod.MODID, value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.FORGE)
	public static class ClientTickHandler {
		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event){
			if(event.phase==TickEvent.Phase.END && Minecraft.getInstance().player!=null){
				if(JETPACK_FLY.isDown()){
					DifMod.PACKET_HANDLER.sendToServer(new JetpackFlyMessage(0,0));
					JetpackFlyMessage.pressAction(Minecraft.getInstance().player,0);
				}
				while(OPEN_ENDER_CHEST.consumeClick()){
					DifMod.PACKET_HANDLER.sendToServer(new EnderOpenMessage(0,0));
				}
			}
		}
	}
}