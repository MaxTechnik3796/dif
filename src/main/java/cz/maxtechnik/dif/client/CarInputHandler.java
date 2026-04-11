package cz.maxtechnik.dif.client;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import cz.maxtechnik.dif.init.other.DifModKeys;
import cz.maxtechnik.dif.network.ModNetworking.ShiftGearPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Klientský handler vstupu pro vozidla – řazení.
 *
 * Sekvence: R(-1) ↔ N(0) ↔ 1 ↔ 2 ↔ … ↔ 7
 *   F (GEAR_DOWN) = řadit dolů  → N → R
 *   R (GEAR_UP)   = řadit nahoru → R → N → 1 → …
 *
 * Zpátečka (R = -1): pouze stojíš + máš palivo.
 * W v R = jede dozadu. S = nic.
 */
@Mod.EventBusSubscriber(modid = DifMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CarInputHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        if (!(mc.player.getVehicle() instanceof BaseCarEntity car)) return;

        // consumeClick() vrátí true právě jednou za každý stisk klávesy
        while (DifModKeys.GEAR_UP.consumeClick()) {
            handleShift(car, +1);
        }
        while (DifModKeys.GEAR_DOWN.consumeClick()) {
            handleShift(car, -1);
        }
    }

    @SubscribeEvent
    public static void onMovementInput(net.minecraftforge.client.event.MovementInputUpdateEvent event) {
        if (event.getEntity().getVehicle() instanceof BaseCarEntity) {
            net.minecraft.client.player.Input input = event.getInput();
            // Override vanilla inputs with custom car keys
            input.up = DifModKeys.CAR_GAS.isDown();
            input.jumping = DifModKeys.CAR_BRAKE.isDown();

            // Recompute forward impulse for client-side syncing and packet sending
            input.forwardImpulse = 0.0f;
            if (input.up) {
                input.forwardImpulse += 1.0f;
            }
            if (input.down) {
                input.forwardImpulse -= 1.0f;
            }
        }
    }

    /**
     * Optimistická klientská predikce řazení.
     * Pravidla jsou zrcadlem ShiftGearPacket na serveru.
     *
     * Sekvence: R(-1) – N(0) – 1 – 2 – … – maxGear
     * Zpátečka: jen při stání (< 0.5 km/h) a s palivem.
     */
    private static void handleShift(BaseCarEntity car, int direction) {
        int   current  = car.getCurrentGear();
        int   maxGear  = car.getGearRatios().length;
        float speedKmh = car.getSpeedKmh();

        int newGear = current + direction;

        // Ohraničení sekvence: R(-1) až maxGear
        if (newGear < -1)      newGear = -1;
        if (newGear > maxGear) newGear = maxGear;

        // R(-1): pouze stojíš A máš palivo
        if (newGear == -1 && (speedKmh > 0.5f || car.getFuelMb() <= 0.0f)) {
            newGear = 0; // fallback na N
        }

        // Downshift rev-protection (jen pro 3↓2, 4↓3 atd.)
        if (direction < 0 && current > 1 && newGear > 0) {
            float[] ratios = car.getGearRatios();
            float newRatio = ratios[newGear - 1];
            float rpmConv  = car.getMaxRPM() /
                    ((car.getMaxSpeedKmh() / 72.0f) * ratios[ratios.length - 1]);
            float estRPM   = (speedKmh / 72.0f) * newRatio * rpmConv;
            if (estRPM > car.getRedlineRPM() * 1.02f) return;
        }

        if (newGear != current) {
            // Okamžitá klientská predikce → HUD se okamžitě aktualizuje
            car.setCurrentGear(newGear);
            // Autoritativní potvrzení serveru
            DifMod.PACKET_HANDLER.sendToServer(new ShiftGearPacket(direction));
        }
    }
}