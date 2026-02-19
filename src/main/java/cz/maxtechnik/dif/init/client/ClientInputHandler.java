package cz.maxtechnik.dif.init.client;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.network.Messages;
import cz.maxtechnik.dif.network.PacketJetpackKey;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DifMod.MODID, value = Dist.CLIENT)
public class ClientInputHandler {
	private static boolean lastState = false;

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (Minecraft.getInstance().player == null) return;

		boolean isPressed = KeyInit.JETPACK_FLY.isDown();
		if (isPressed != lastState) {
			Messages.sendToServer(new PacketJetpackKey(isPressed));
			lastState = isPressed;
		}
	}
}