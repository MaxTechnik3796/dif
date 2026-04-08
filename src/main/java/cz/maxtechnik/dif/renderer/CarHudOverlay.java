package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class CarHudOverlay {

    // ── Vyhlazení zobrazované rychlosti ──────────────────────────────────────
    // Lerp faktor 0.20 → ~0.5s doby odezvy na 90 % změny.
    // Díky tomu číslo neskáče 200↔201 pětkrát za sekundu, ale při zrychlení
    // stále reaguje dostatečně rychle.
    private static float smoothedSpeed = 0.0f;
    private static final float SPEED_LERP = 0.20f;

    // ── Vzájemný offset levého (RPM) a pravého (palivo) oblouku od středu ───
    private static final float CLUSTER_OFFSET = 65.0f;

    public static void render(GuiGraphics gui, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (!(mc.player.getVehicle() instanceof BaseCarEntity car)) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        float arcCY = sh - 70.0f;

        // Středy dvou clusterů
        float rpmCX  = sw / 2.0f - CLUSTER_OFFSET; // levý – otáčkoměr + rychlost
        float fuelCX = sw / 2.0f + CLUSTER_OFFSET;  // pravý – palivo

        // ── Vyhlazená rychlost ────────────────────────────────────────────────
        float actualSpeed = car.getSpeedKmh();
        smoothedSpeed = smoothedSpeed + (actualSpeed - smoothedSpeed) * SPEED_LERP;

        float rpmPercent  = car.getRPM() / 18000f;
        float fuelPercent = car.getFuelPercent(); // 0–1

        // ── Levý oblouk – otáčkoměr (14 segmentů) ────────────────────────────
        for (int i = 0; i < 14; i++) {
            float segStart = i / 14.0f;
            int color = 0x44FFFFFF; // vypnuto

            if (rpmPercent > segStart) {
                if      (i < 8)  color = 0xFF00FF00; // zelená
                else if (i < 11) color = 0xFFFFFF00; // žlutá
                else             color = 0xFFFF0000; // červená
            }

            float angle = -100f + (i * 200f / 13f);
            drawSegment(gui, rpmCX, arcCY, 6, 3, angle, color);
        }

        // ── Pravý oblouk – palivo (14 segmentů) ──────────────────────────────
        for (int i = 0; i < 14; i++) {
            float segStart = i / 14.0f;
            int color = 0x44FFFFFF; // vypnuto

            if (fuelPercent > segStart) {
                if      (fuelPercent > 0.50f) color = 0xFF00FF00; // zelená
                else if (fuelPercent > 0.25f) color = 0xFFFFFF00; // žlutá
                else                          color = 0xFFFF0000; // červená
            }

            float angle = -100f + (i * 200f / 13f);
            drawSegment(gui, fuelCX, arcCY, 6, 3, angle, color);
        }

        // ── Text – levý cluster: stupeň + rychlost ───────────────────────────
        String gear  = car.getCurrentGear() == 0 ? "N" : String.valueOf(car.getCurrentGear());
        gui.drawCenteredString(mc.font, gear, (int) rpmCX, (int) arcCY - 15, 0xFFFFFFFF);

        String speed = (int) smoothedSpeed + " km/h";
        gui.drawCenteredString(mc.font, speed, (int) rpmCX, (int) arcCY + 10, 0xFFFFFFFF);

        // ── Text – pravý cluster: procenta paliva ─────────────────────────────
        // Barva textu odpovídá stavu nádrže.
        int fuelTextColor;
        if      (fuelPercent > 0.50f) fuelTextColor = 0xFF00FF00;
        else if (fuelPercent > 0.25f) fuelTextColor = 0xFFFFFF00;
        else                          fuelTextColor = 0xFFFF0000;

        String fuelStr = (int)(fuelPercent * 100f) + " %";
        gui.drawCenteredString(mc.font, fuelStr, (int) fuelCX, (int) arcCY + 10, fuelTextColor);
    }

    private static void drawSegment(GuiGraphics gui, float cx, float cy, float w, float h, float rot, int color) {
        PoseStack ps = gui.pose();
        ps.pushPose();
        ps.translate(cx, cy, 0);
        ps.mulPose(Axis.ZP.rotationDegrees(rot));
        ps.translate(0, -40, 0); // Poloměr oblouku
        gui.fill((int)(-w / 2), (int)(-h / 2), (int)(w / 2), (int)(h / 2), color);
        ps.popPose();
    }
}