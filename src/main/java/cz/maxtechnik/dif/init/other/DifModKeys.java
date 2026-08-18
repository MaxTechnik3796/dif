package cz.maxtechnik.dif.init.other;

import com.mojang.blaze3d.platform.InputConstants;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.network.EnderOpenMessage;
import cz.maxtechnik.dif.network.JetpackFlyMessage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class DifModKeys{
	public static final String CATEGORY="key.categories.dif";
	// Původní klávesy
	public static final KeyMapping JETPACK_FLY=new KeyMapping("key.dif.jetpack_fly",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_SPACE,CATEGORY);
	public static final KeyMapping KEY_HOVER=new KeyMapping("key.dif.jetpack_hover",KeyConflictContext.IN_GAME,InputConstants.Type.KEYSYM,InputConstants.KEY_X,"key.categories.dif");
	public static final KeyMapping OPEN_ENDER_CHEST=new KeyMapping("key.dif.open_ender",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_B,CATEGORY);

	@SubscribeEvent
	public static void registerKeys(RegisterKeyMappingsEvent event){
		event.register(JETPACK_FLY);
		event.register(KEY_HOVER);
		event.register(OPEN_ENDER_CHEST);
	}
	@EventBusSubscriber(modid=DifMod.MODID, value=Dist.CLIENT, bus=EventBusSubscriber.Bus.GAME)
	public static class ClientTickHandler{
		@SubscribeEvent
		public static void onClientTick(ClientTickEvent.Post event){
			if(Minecraft.getInstance().player!=null){
				var player=Minecraft.getInstance().player;
				if(JETPACK_FLY.isDown()){
					PacketDistributor.sendToServer(new JetpackFlyMessage(0,0));
					JetpackFlyMessage.pressAction(player,0);
				}
				if(KEY_HOVER.consumeClick()){
					PacketDistributor.sendToServer(new JetpackFlyMessage(2,0));
				}
				// 3. Logika Ender Chesty
				while(OPEN_ENDER_CHEST.consumeClick()){
					PacketDistributor.sendToServer(new EnderOpenMessage(0,0));
				}
			}
		}
	}
}