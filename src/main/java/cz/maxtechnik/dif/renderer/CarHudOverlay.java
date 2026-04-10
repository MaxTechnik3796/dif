package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * HUD přístrojová deska – design převzat z Old_CarHudOverlay, bez spojky.
 *
 * ═══ ROZVRŽENÍ (střed spodní části obrazovky) ══════════════════════════════
 *
 *   ┌─────────────────────────────────────────────────────────────────┐
 *   │  [●●●●●●●●●●○○○○○]  ← LED pruh (15 diod)                      │
 *   │                                                                  │
 *   │              3      ← stupeň (velký, 2×)                       │
 *   │           235 km/h  ← rychlost                                  │
 *   │  Fuel 45%           ← palivo                                    │
 *   └─────────────────────────────────────────────────────────────────┘
 *
 * Stupně: R = červená, N = žlutá, 1–7 = bílá.
 */
public class CarHudOverlay {

    private static float smoothedSpeed = 0.0f;
    private static final float SPEED_LERP = 0.20f;
    private static final int   LED_COUNT  = 15;

    public static void render(GuiGraphics gui, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (!(mc.player.getVehicle() instanceof BaseCarEntity car)) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        int cx = sw / 2;

        // Vyhlazení rychlosti (LERP) pro plynulý tachometr
        smoothedSpeed += (car.getSpeedKmh() - smoothedSpeed) * SPEED_LERP;

        // ── Základní hodnoty ──────────────────────────────────────────────────
        float rpm      = car.getRPM();
        float maxRPM   = car.getMaxRPM();
        float idleRPM  = car.getIdleRPM();
        float fuelPct  = car.getFuelPercent();
        int   gear     = car.getCurrentGear();

        // Rev limit (fallback na maxRPM pokud getRedlineRPM chybí)
        float revLimit = car.getRedlineRPM();

        // Blikací takt pro rev limiter
        long ms = System.currentTimeMillis();
        boolean blinkFast = (ms % 150L) < 75L;   // ~6.7 Hz – rev limiter

        // ── Pozice panelu ─────────────────────────────────────────────────────
        int panelW = 180;
        int panelH = 76;
        int panelX = cx - panelW / 2;
        int panelY = sh - panelH - 56;


        // Poloprůhledné pozadí panelu
        gui.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xAA000000);
        // Tenký rámeček
        gui.fill(panelX,              panelY,              panelX + panelW, panelY + 1,      0x88FFFFFF);
        gui.fill(panelX,              panelY + panelH - 1, panelX + panelW, panelY + panelH, 0x88FFFFFF);
        gui.fill(panelX,              panelY,              panelX + 1,      panelY + panelH, 0x88FFFFFF);
        gui.fill(panelX + panelW - 1, panelY,              panelX + panelW, panelY + panelH, 0x88FFFFFF);

        // ── LED PRUH ─────────────────────────────────────────────────────────
        int ledBarY      = panelY + 7;
        int ledW         = 9;
        int ledH         = 10;
        int ledGap       = 2;
        int ledStep      = ledW + ledGap;
        int ledBarTotalW = LED_COUNT * ledStep - ledGap;
        int ledBarStartX = cx - ledBarTotalW / 2;

        // RPM práh pro rozsvícení každé diody (lineární od idleRPM do revLimit)
        float rpmRange = revLimit - idleRPM;
        boolean revLimiting = rpm >= revLimit;

        for (int i = 0; i < LED_COUNT; i++) {
            float threshold = idleRPM + (i / (float)(LED_COUNT - 1)) * rpmRange;
            boolean lit = rpm >= threshold;

            int color;

            if (revLimiting) {
                // Omezovač: všechny diody blikají červeně rychle
                color = blinkFast ? 0xFFFF1111 : 0x55FF1111;
            } else if (lit) {
                // Normální "fill" chování
                if (i < 5) {
                    color = 0xFF00DD00;  // Zelená – zahřívání
                } else if (i < 10) {
                    color = 0xFFFF5500;  // Oranžová-červená – výkonová zóna
                } else {
                    color = 0xFF4455FF;  // Modrá – těsně před limitem
                }
            } else {
                color = 0x22FFFFFF;     // Vypnuto (tmavá)
            }

            int lx = ledBarStartX + i * ledStep;
            gui.fill(lx, ledBarY, lx + ledW, ledBarY + ledH, color);

            // Malý lesk na horní hraně diody
            if (color != 0x22FFFFFF) {
                gui.fill(lx, ledBarY, lx + ledW, ledBarY + 1, 0x44FFFFFF);
            }
        }

        // ── VELKÝ STUPEŇ (2× zvětšeno) ───────────────────────────────────────
        String gearText = switch (gear) {
            case -1 -> "R";
            case  0 -> "N";
            default -> String.valueOf(gear);
        };

        int gearColor;
        if (revLimiting && blinkFast) {
            gearColor = 0xFFFF1111; // Bliká červeně při omezovači
        } else if (revLimiting) {
            gearColor = 0xFFAA0000;
        } else {
            gearColor = switch (gear) {
                case -1 -> 0xFFFF4444; // R – červená
                case  0 -> 0xFFFFDD00; // N – žlutá
                default -> 0xFFFFFFFF; // 1–7 – bílá
            };
        }

        int gearY = panelY + 22;
        PoseStack ps = gui.pose();
        ps.pushPose();
        ps.translate(cx, gearY, 0);
        ps.scale(2.0f, 2.0f, 1.0f);
        gui.drawCenteredString(mc.font, gearText, 0, 0, gearColor);
        ps.popPose();

        // ── RYCHLOST ─────────────────────────────────────────────────────────
        int speedY = panelY + 44;
        String speedText = (int) smoothedSpeed + " km/h";
        gui.drawCenteredString(mc.font, speedText, cx, speedY, 0xFFCCCCCC);

        // ── PALIVO (spodní řádek panelu) ──────────────────────────────────────
        int bottomY = panelY + 59;
        int fuelColor;
        if (fuelPct > 0.50f)       fuelColor = 0xFF00CC00;
        else if (fuelPct > 0.25f)  fuelColor = 0xFFFFDD00;
        else                       fuelColor = 0xFFFF4444;
        String fuelText = "Fuel " + (int)(fuelPct * 100f) + "%";
        gui.drawString(mc.font, fuelText, panelX + 8, bottomY, fuelColor);
    }
}