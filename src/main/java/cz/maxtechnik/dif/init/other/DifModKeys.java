package cz.maxtechnik.dif.init.other;

import com.mojang.blaze3d.platform.InputConstants;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.network.EnderOpenMessage;
import cz.maxtechnik.dif.network.JetpackFlyMessage;
import cz.maxtechnik.dif.network.RemoteControlPacket;
import cz.maxtechnik.dif.network.ShiftGearPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = DifMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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

	@SubscribeEvent
	public static void registerKeys(RegisterKeyMappingsEvent event) {
		event.register(JETPACK_FLY);
		event.register(OPEN_ENDER_CHEST);
		event.register(MINE_FORWARD);
		event.register(MINE_BACKWARD);
		event.register(GEAR_UP);
		event.register(GEAR_DOWN);
	}

	@Mod.EventBusSubscriber(modid = DifMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ClientTickHandler {
		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event) {
			if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().player != null) {
				var player = Minecraft.getInstance().player;

				// 1. Logika řazení (pokud hráč sedí v autě)
				if (player.getVehicle() instanceof BaseCarEntity) {
					while (GEAR_UP.consumeClick()) {
						DifMod.PACKET_HANDLER.sendToServer(new ShiftGearPacket(+1));
					}
					while (GEAR_DOWN.consumeClick()) {
						DifMod.PACKET_HANDLER.sendToServer(new ShiftGearPacket(-1));
					}
				}

				// 2. Logika Jetpacku
				if (JETPACK_FLY.isDown()) {
					DifMod.PACKET_HANDLER.sendToServer(new JetpackFlyMessage(0, 0));
					JetpackFlyMessage.pressAction(player, 0);
				}

				// 3. Logika Ender Chesty
				while (OPEN_ENDER_CHEST.consumeClick()) {
					DifMod.PACKET_HANDLER.sendToServer(new EnderOpenMessage(0, 0));
				}

				// 4. Logika dálkového ovládání Minecartu
				if (player.getMainHandItem().is(DifModItems.REMOTE_CONTROLLER.get())) {
					if (MINE_FORWARD.isDown()) {
						DifMod.PACKET_HANDLER.sendToServer(new RemoteControlPacket(1.0, false));
					}
					if (MINE_BACKWARD.consumeClick()) {
						DifMod.PACKET_HANDLER.sendToServer(new RemoteControlPacket(0.0, true));
					}
				}
			}
		}
	}
}