package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class CarHudOverlay {

    public static void render(GuiGraphics gui, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (!(mc.player.getVehicle() instanceof BaseCarEntity car)) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        // Střed HUDu posunutý nahoru od spodku obrazovky
        float arcCX = sw / 2.0f;
        float arcCY = sh - 50.0f;

        float rpmPercent = car.getRPM() / 18000f;

        // Vykreslení 14 segmentů otáčkoměru
        for (int i = 0; i < 14; i++) {
            float segStart = i / 14.0f;
            int color = 0x44FFFFFF; // Průhledná bílá (vypnuto)

            if (rpmPercent > segStart) {
                if (i < 8) color = 0xFF00FF00;      // Zelená
                else if (i < 11) color = 0xFFFFFF00; // Žlutá
                else color = 0xFFFF0000;             // Červená
            }

            float angle = -100f + (i * 200f / 13f);
            drawSegment(gui, arcCX, arcCY, 6, 3, angle, color);
        }

        // Rychlost a stupeň
        String gear = car.getCurrentGear() == 0 ? "N" : String.valueOf(car.getCurrentGear());
        gui.drawCenteredString(mc.font, gear, (int)arcCX, (int)arcCY - 15, 0xFFFFFFFF);

        String speed = (int)car.getSpeedKmh() + " km/h";
        gui.drawCenteredString(mc.font, speed, (int)arcCX, (int)arcCY + 10, 0xFFFFFFFF);
    }

    private static void drawSegment(GuiGraphics gui, float cx, float cy, float w, float h, float rot, int color) {
        PoseStack ps = gui.pose();
        ps.pushPose();
        ps.translate(cx, cy, 0);
        ps.mulPose(Axis.ZP.rotationDegrees(rot));
        ps.translate(0, -40, 0); // Poloměr oblouku
        gui.fill((int)(-w/2), (int)(-h/2), (int)(w/2), (int)(h/2), color);
        ps.popPose();
    }
}