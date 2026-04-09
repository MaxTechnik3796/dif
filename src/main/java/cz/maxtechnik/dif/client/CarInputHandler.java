package cz.maxtechnik.dif.client;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import cz.maxtechnik.dif.init.other.DifModKeys;
import cz.maxtechnik.dif.network.ShiftGearPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DifMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CarInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();

        // Základní kontroly: hráč musí existovat, nesmí být v menu a musí sedět v autě
        if (mc.player == null || mc.screen != null) return;
        if (!(mc.player.getVehicle() instanceof BaseCarEntity car)) return;

        // Reagujeme pouze na stisk (Action 1), ne na držení nebo puštění[cite: 16]
        if (event.getAction() != 1) return;

        // Propojení na KeyMappingy z DifModKeys místo GLFW konstant
        if (DifModKeys.GEAR_UP.consumeClick()) {
            handleShift(car, 1);
        } else if (DifModKeys.GEAR_DOWN.consumeClick()) {
            handleShift(car, -1);
        }
    }

    private static void handleShift(BaseCarEntity car, int direction) {
        int currentGear = car.getCurrentGear(); //[cite: 16]
        int maxGear = car.getGearRatios().length; //[cite: 16]

        if (direction == 1 && currentGear < maxGear) {
            // Logika pro přeřazení nahoru[cite: 16]
            car.setCurrentGear(currentGear + 1);
            sendPacket(1);
        } else if (direction == -1 && currentGear > 1) {
            // Logika pro přeřazení dolů[cite: 16]
            car.setCurrentGear(currentGear - 1);
            sendPacket(-1);
        }
    }

    private static void sendPacket(int direction) {
        // Odeslání packetu přes tvůj PACKET_HANDLER[cite: 16]
        DifMod.PACKET_HANDLER.sendToServer(new ShiftGearPacket(direction));
    }
}