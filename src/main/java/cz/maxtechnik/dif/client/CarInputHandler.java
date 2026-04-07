package cz.maxtechnik.dif.client;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import cz.maxtechnik.dif.network.ShiftGearPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class CarInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        if (!(mc.player.getVehicle() instanceof BaseCarEntity car)) return;
        if (event.getAction() != 1) return;

        int currentGear = car.getCurrentGear();
        int maxGear = car.getGearRatios().length;

        if (event.getKey() == GLFW.GLFW_KEY_R) { // GEAR UP
            if (currentGear < maxGear) {
                car.setCurrentGear(currentGear + 1);
                sendPacket(+1);
            }
        } else if (event.getKey() == GLFW.GLFW_KEY_F) { // GEAR DOWN
            if (currentGear > 1) {
                car.setCurrentGear(currentGear - 1);
                sendPacket(-1);
            }
        }
    }

    private static void sendPacket(int direction) {
        // Použijeme tvůj vlastní PACKET_HANDLER z DifMod.java
        DifMod.PACKET_HANDLER.sendToServer(new ShiftGearPacket(direction));
    }
}