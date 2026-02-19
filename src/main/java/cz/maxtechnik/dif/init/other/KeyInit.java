package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import com.mojang.blaze3d.platform.InputConstants;
import cz.maxtechnik.dif.network.JetpackFlyMessage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = DifMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyInit {
	public static final KeyMapping JETPACK_FLY = new KeyMapping("key.dif.jetpack_fly",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_SPACE,"key.categories.dif"){
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				DifMod.PACKET_HANDLER.sendToServer(new JetpackFlyMessage(0, 0));
				assert Minecraft.getInstance().player!=null;
				JetpackFlyMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}

	};

	@SubscribeEvent
	public static void registerKeys(RegisterKeyMappingsEvent event) {
		event.register(JETPACK_FLY);
	}
	@Mod.EventBusSubscriber({Dist.CLIENT})
	public static class KeyEventListener {
		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event) {
			if (Minecraft.getInstance().screen == null) {
				JETPACK_FLY.consumeClick();
			}
		}
	}


}