package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class CarHudOverlay {
    private static float smoothedSpeed = 0.0f;

    public static void render(GuiGraphics gui, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !(mc.player.getVehicle() instanceof BaseCarEntity c)) return;
        int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight(), cx = sw / 2;
        smoothedSpeed += (c.getSpeedKmh() - smoothedSpeed) * 0.20f;
        
        float rpm = c.getRPM(), rL = c.getRedlineRPM(), fp = c.getFuelPercent(); int g = c.getCurrentGear();
        boolean blink = (System.currentTimeMillis() % 150L) < 75L, revLimit = rpm >= rL;
        int pW = 180, pH = 76, px = cx - pW / 2, py = sh - pH - 56;

        gui.fill(px, py, px + pW, py + pH, 0xAA000000);
        gui.fill(px, py, px + pW, py + 1, 0x88FFFFFF); gui.fill(px, py + pH - 1, px + pW, py + pH, 0x88FFFFFF);
        gui.fill(px, py, px + 1, py + pH, 0x88FFFFFF); gui.fill(px + pW - 1, py, px + pW, py + pH, 0x88FFFFFF);

        for (int i = 0; i < 15; i++) {
            int col = 0x22FFFFFF, lx = cx - (15 * 11 - 2) / 2 + i * 11;
            if (revLimit) col = blink ? 0xFFFF1111 : 0x55FF1111;
            else if (rpm >= c.getIdleRPM() + (i / 14f) * (rL - c.getIdleRPM())) col = i < 5 ? 0xFF00DD00 : i < 10 ? 0xFFFF5500 : 0xFF4455FF;
            gui.fill(lx, py + 7, lx + 9, py + 17, col); if (col != 0x22FFFFFF) gui.fill(lx, py + 7, lx + 9, py + 8, 0x44FFFFFF);
        }

        String gt = g == -1 ? "R" : g == 0 ? "N" : String.valueOf(g);
        int gc = revLimit ? (blink ? 0xFFFF1111 : 0xFFAA0000) : g == -1 ? 0xFFFF4444 : g == 0 ? 0xFFFFDD00 : 0xFFFFFFFF;
        
        PoseStack ps = gui.pose(); ps.pushPose(); ps.translate(cx, py + 22, 0); ps.scale(2f, 2f, 1f);
        gui.drawCenteredString(mc.font, gt, 0, 0, gc); ps.popPose();

        gui.drawCenteredString(mc.font, (int) smoothedSpeed + " km/h", cx, py + 44, 0xFFCCCCCC);
        gui.drawString(mc.font, "Fuel " + (int)(fp * 100f) + "%", px + 8, py + 59, fp > 0.5f ? 0xFF00CC00 : fp > 0.25f ? 0xFFFFDD00 : 0xFFFF4444);
    }
}