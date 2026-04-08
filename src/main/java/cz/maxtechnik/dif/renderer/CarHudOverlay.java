package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class CarHudOverlay {

    private static float smoothedSpeed = 0.0f;
    private static final float SPEED_LERP = 0.20f;
    private static final float CLUSTER_OFFSET = 65.0f;

    public static void render(GuiGraphics gui, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (!(mc.player.getVehicle() instanceof BaseCarEntity car)) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        float arcCY = sh - 70.0f;
        float rpmCX  = sw / 2.0f - CLUSTER_OFFSET;
        float fuelCX = sw / 2.0f + CLUSTER_OFFSET;

        // Vyhlazení rychlosti
        smoothedSpeed += (car.getSpeedKmh() - smoothedSpeed) * SPEED_LERP;
        float fuelPercent = car.getFuelPercent();

        // Vykreslení obou oblouků přes sjednocenou metodu
        drawGauge(gui, rpmCX, arcCY, car.getRPM() / 18000f, true);
        drawGauge(gui, fuelCX, arcCY, fuelPercent, false);

        // Texty otáčkoměru
        String gear = car.getCurrentGear() == 0 ? "N" : String.valueOf(car.getCurrentGear());
        gui.drawCenteredString(mc.font, gear, (int) rpmCX, (int) arcCY - 15, 0xFFFFFFFF);
        gui.drawCenteredString(mc.font, (int) smoothedSpeed + " km/h", (int) rpmCX, (int) arcCY + 10, 0xFFFFFFFF);

        // Texty paliva (barva reaguje na stav nádrže)
        int fuelColor = fuelPercent > 0.50f ? 0xFF00FF00 : (fuelPercent > 0.25f ? 0xFFFFFF00 : 0xFFFF0000);
        gui.drawCenteredString(mc.font, (int)(fuelPercent * 100f) + " %", (int) fuelCX, (int) arcCY + 10, fuelColor);
    }

    // Sjednocená metoda pro vykreslení 14 segmentů
    private static void drawGauge(GuiGraphics gui, float cx, float cy, float percent, boolean isRpm) {
        for (int i = 0; i < 14; i++) {
            int color = 0x44FFFFFF; // Výchozí šedá (vypnuto)

            // Pokud má být segment rozsvícený
            if (percent > i / 14.0f) {
                if (isRpm) {
                    color = (i < 8) ? 0xFF00FF00 : ((i < 11) ? 0xFFFFFF00 : 0xFFFF0000);
                } else {
                    color = (percent > 0.50f) ? 0xFF00FF00 : ((percent > 0.25f) ? 0xFFFFFF00 : 0xFFFF0000);
                }
            }

            float angle = -100f + (i * 200f / 13f);

            // Vykreslení jednoho obdélníčku
            PoseStack ps = gui.pose();
            ps.pushPose();
            ps.translate(cx, cy, 0);
            ps.mulPose(Axis.ZP.rotationDegrees(angle));
            ps.translate(0, -40, 0);
            gui.fill(-3, -1, 3, 1, color); // šířka 6, výška 3 od středu
            ps.popPose();
        }
    }
}