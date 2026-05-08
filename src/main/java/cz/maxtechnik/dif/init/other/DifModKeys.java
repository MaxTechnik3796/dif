package cz.maxtechnik.dif.init.other;

import com.mojang.blaze3d.platform.InputConstants;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.network.EnderOpenMessage;
import cz.maxtechnik.dif.network.JetpackFlyMessage;
import cz.maxtechnik.dif.network.MegaBackpackOpenPacket;
import cz.maxtechnik.dif.network.RemoteControlPacket;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
@SuppressWarnings("removal")
@EventBusSubscriber(modid = DifMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DifModKeys {
	public static final String CATEGORY = "key.categories.dif";

	// Původní klávesy
	public static final KeyMapping JETPACK_FLY = new KeyMapping("key.dif.jetpack_fly", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SPACE, CATEGORY);
	public static final KeyMapping OPEN_ENDER_CHEST = new KeyMapping("key.dif.open_ender", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);

	// Původní Minecart ovladač
	public static final KeyMapping MINE_FORWARD = new KeyMapping("key.dif.remote_minecart_boost", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, CATEGORY);
	public static final KeyMapping MINE_BACKWARD = new KeyMapping("key.dif.remote_minecart_invert", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);

	// NOVÉ KLÁVESY PRO AUTO
	public static final KeyMapping GEAR_UP = new KeyMapping("key.dif.gear_up", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
	public static final KeyMapping GEAR_DOWN = new KeyMapping("key.dif.gear_down", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F, CATEGORY);
	public static final KeyMapping CAMERA_LOCK = new KeyMapping("key.dif.camera_lock", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY);
	public static final KeyMapping CAR_GAS = new KeyMapping("key.dif.car_gas", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_W, CATEGORY);
	public static final KeyMapping CAR_BRAKE = new KeyMapping("key.dif.car_brake", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SPACE, CATEGORY);

	public static final KeyMapping MEGA_BACKPACK_KEY = new KeyMapping(
			"key.dif.mega_backpack",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_B, // Výchozí klávesa B
			CATEGORY
	);

	@SubscribeEvent
	public static void registerKeys(RegisterKeyMappingsEvent event) {
		event.register(JETPACK_FLY);
		event.register(OPEN_ENDER_CHEST);
		event.register(MINE_FORWARD);
		event.register(MINE_BACKWARD);
		event.register(GEAR_UP);
		event.register(GEAR_DOWN);
		event.register(CAMERA_LOCK);
		event.register(CAR_GAS);
		event.register(CAR_BRAKE);
		event.register(MEGA_BACKPACK_KEY);
	}

	@EventBusSubscriber(modid = DifMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
	public static class ClientTickHandler {
		@SubscribeEvent
		public static void onClientTick(ClientTickEvent.Post event) {
			if (Minecraft.getInstance().player != null) {
				var player = Minecraft.getInstance().player;

				// 1. Řazení obsluhuje CarInputHandler přes InputEvent.Key
				//    (má správnou logiku R/N/1-7 a klientskou predikci)

				// 2. Logika Jetpacku
				if (JETPACK_FLY.isDown()) {
					PacketDistributor.sendToServer(new JetpackFlyMessage(0, 0));
					JetpackFlyMessage.pressAction(player, 0);
				}

				// 3. Logika Ender Chesty
				while (OPEN_ENDER_CHEST.consumeClick()) {
					PacketDistributor.sendToServer(new EnderOpenMessage(0, 0));
				}

				// 4. Logika dálkového ovládání Minecartu
				if (player.getMainHandItem().is(DifModItems.REMOTE_CONTROLLER.get())) {
					if (MINE_FORWARD.isDown()) {
						PacketDistributor.sendToServer(new RemoteControlPacket(1.0, false));
					}
					if (MINE_BACKWARD.consumeClick()) {
						PacketDistributor.sendToServer(new RemoteControlPacket(0.0, true));
					}
				}

				while(CAMERA_LOCK.consumeClick()) {
					cz.maxtechnik.dif.init.events.client.VehicleCameraHandler.vehicleCameraLocked = !cz.maxtechnik.dif.init.events.client.VehicleCameraHandler.vehicleCameraLocked;
				}
				while (MEGA_BACKPACK_KEY.consumeClick()) {
					//Only Creative (TEMP)
					if(player.getAbilities().instabuild) PacketDistributor.sendToServer(new MegaBackpackOpenPacket(0, 0));
				}
			}
		}
	}
}