package cz.maxtechnik.dif.gui.screen;

import cz.maxtechnik.dif.block.entity.ForgeControllerBlockEntity;
import cz.maxtechnik.dif.network.ForgeSelectFluidPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Radial menu pro výběr preferované výstupní kapaliny.
 *
 * Layout: dynamické kruhy podle počtu neprázdných tanků.
 *   1–12  kapalin → 1 vnější kruh
 *   13–24 kapalin → vnější + střední kruh
 *   25–32 kapalin → vnější + střední + vnitřní kruh
 *
 * Každý segment zobrazuje fluid texturu s tint barvou.
 * Hover → tooltip s názvem a množstvím.
 * Klik  → potvrzení výběru + packet na server.
 */
public class ForgeRadialScreen extends Screen {

    // ── Layout konstanty ──────────────────────────────────────────────────────
    private static final int[] RING_RADII_OUTER = {90, 58, 30};  // vnější poloměr každého kruhu
    private static final int[] RING_RADII_INNER = {62, 34, 10};  // vnitřní poloměr každého kruhu
    private static final int   ICON_SIZE        = 16;
    private static final int   ICON_HALF        = ICON_SIZE / 2;

    // Max segmentů na každý kruh (zvenku dovnitř)
    private static final int[] MAX_PER_RING = {12, 12, 8};

    // ── Data ──────────────────────────────────────────────────────────────────
    private final ForgeControllerBlockEntity be;
    private final BlockPos ctrlPos;

    /** Pouze neprázdné tanky — (tankIndex, fluidStack) */
    private final List<int[]> activeTanks = new ArrayList<>();

    /** activeTanks index který je hovered, nebo -1 */
    private int hoveredIdx = -1;

    /** activeTanks index aktuálně vybraného (preferredOutputTank) */
    private int selectedIdx = -1;

    public ForgeRadialScreen(ForgeControllerBlockEntity be) {
        super(Component.empty());
        this.be     = be;
        this.ctrlPos = be.getBlockPos();

        // Sbírej neprázdné tanky
        for (int i = 0; i < ForgeControllerBlockEntity.FLUID_TANK_COUNT; i++) {
            if (!be.fluidTanks[i].isEmpty()) {
                activeTanks.add(new int[]{i});
            }
        }

        // Nastav selectedIdx podle aktuálního preferredOutputTank
        int pref = be.getPreferredOutputTank();
        for (int i = 0; i < activeTanks.size(); i++) {
            if (activeTanks.get(i)[0] == pref) { selectedIdx = i; break; }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  RENDER
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void render(@NotNull GuiGraphics gfx, int mx, int my, float partial) {
        int cx = width / 2, cy = height / 2;

        // Poloprůhledné tmavé pozadí pouze přes celý screen (velmi jemné)
        gfx.fill(0, 0, width, height, 0x55000000);

        int total = activeTanks.size();
        if (total == 0) {
            gfx.drawCenteredString(font, "§7No fluids in forge", cx, cy - 4, 0xFFFFFFFF);
            gfx.drawCenteredString(font, "§8[Esc] close", cx, cy + 8, 0xFF555555);
            super.render(gfx, mx, my, partial);
            return;
        }

        // Aktualizuj hover
        hoveredIdx = getHoveredIdx(mx - cx, my - cy);

        // Kresli kruhy zvenku dovnitř
        int drawn = 0;
        for (int ring = 0; ring < MAX_PER_RING.length && drawn < total; ring++) {
            int countInRing = Math.min(MAX_PER_RING[ring], total - drawn);
            int rOuter = RING_RADII_OUTER[ring];
            int rInner = RING_RADII_INNER[ring];

            for (int s = 0; s < countInRing; s++) {
                int globalIdx = drawn + s;
                boolean hovered  = hoveredIdx == globalIdx;
                boolean selected = selectedIdx == globalIdx;
                drawSegment(gfx, cx, cy, s, countInRing, globalIdx, rInner, rOuter, hovered, selected);
            }
            drawn += countInRing;
        }

        // Střední tečka
        gfx.fill(cx - 4, cy - 4, cx + 4, cy + 4, 0xFF6B3D1A);
        gfx.fill(cx - 3, cy - 3, cx + 3, cy + 3, 0xFF1A0F05);

        // Tooltip pro hovered
        if (hoveredIdx >= 0 && hoveredIdx < total) {
            FluidStack fs = be.fluidTanks[activeTanks.get(hoveredIdx)[0]].getFluid();
            String name = fs.getHoverName().getString();
            String amount = formatMb(fs.getAmount());
            int tw = Math.max(font.width(name), font.width(amount)) + 8;
            int tx = cx - tw / 2;
            int ty = cy - RING_RADII_OUTER[0] - 26;
            gfx.fill(tx - 2, ty - 2, tx + tw + 2, ty + 20, 0xDD000000);
            gfx.fill(tx - 2, ty - 2, tx + tw + 2, ty - 1, 0xFF6B3D1A);
            gfx.drawString(font, name,   tx + 4, ty + 2,  0xFFFFFFFF, false);
            gfx.drawString(font, amount, tx + 4, ty + 12, 0xFF888888, false);
        }

        // Instrukce
        gfx.drawCenteredString(font, "§7Click to select output  §8[Esc] cancel",
                cx, cy + RING_RADII_OUTER[0] + 12, 0xFF555555);

        super.render(gfx, mx, my, partial);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  SEGMENT KRESLENÍ
    // ═══════════════════════════════════════════════════════════════════════════

    private void drawSegment(GuiGraphics gfx, int cx, int cy,
                             int segInRing, int totalInRing, int globalIdx,
                             int rInner, int rOuter, boolean hovered, boolean selected) {

        FluidStack fs   = be.fluidTanks[activeTanks.get(globalIdx)[0]].getFluid();
        int fluidColor  = getFluidColor(fs);
        int alpha       = hovered ? 0xDD : selected ? 0xBB : 0x88;
        int segColor    = (alpha << 24) | (fluidColor & 0x00FFFFFF);
        int borderColor = selected ? 0xFFFFD700 : hovered ? 0xFFFFFFFF : 0x44FFFFFF;

        float segAngle  = (float)(Math.PI * 2 / totalInRing);
        float startA    = segAngle * segInRing - (float)(Math.PI / 2) + 0.03f;
        float endA      = startA + segAngle - 0.06f;
        int   rEnd      = hovered || selected ? rOuter + 5 : rOuter;
        int   steps     = Math.max(8, (int)(rOuter * segAngle / 3));

        // Výplň segmentu
        gfx.pose().pushPose();
        var buffer = gfx.bufferSource().getBuffer(RenderType.gui());
        var mat    = gfx.pose().last().pose();

        for (int s = 0; s < steps; s++) {
            float a0 = startA + (endA - startA) * s / steps;
            float a1 = startA + (endA - startA) * (s + 1) / steps;
            float r = ((segColor >> 16) & 0xFF) / 255f;
            float g = ((segColor >>  8) & 0xFF) / 255f;
            float b = ((segColor      ) & 0xFF) / 255f;
            float a = ((segColor >> 24) & 0xFF) / 255f;

            float x0i = cx + (float)Math.cos(a0) * rInner;
            float y0i = cy + (float)Math.sin(a0) * rInner;
            float x1i = cx + (float)Math.cos(a1) * rInner;
            float y1i = cy + (float)Math.sin(a1) * rInner;
            float x0o = cx + (float)Math.cos(a0) * rEnd;
            float y0o = cy + (float)Math.sin(a0) * rEnd;
            float x1o = cx + (float)Math.cos(a1) * rEnd;
            float y1o = cy + (float)Math.sin(a1) * rEnd;

            // Trapézoid = 2 trojúhelníky (každý jako quad se zdvojeným vrcholem)
            buffer.addVertex(mat, x0i, y0i, 0).setColor(r, g, b, a);
            buffer.addVertex(mat, x0o, y0o, 0).setColor(r, g, b, a);
            buffer.addVertex(mat, x1o, y1o, 0).setColor(r, g, b, a);
            buffer.addVertex(mat, x0i, y0i, 0).setColor(r, g, b, a);

            buffer.addVertex(mat, x0i, y0i, 0).setColor(r, g, b, a);
            buffer.addVertex(mat, x1o, y1o, 0).setColor(r, g, b, a);
            buffer.addVertex(mat, x1i, y1i, 0).setColor(r, g, b, a);
            buffer.addVertex(mat, x0i, y0i, 0).setColor(r, g, b, a);
        }
        gfx.pose().popPose();

        // Fluid textura ikona uprostřed segmentu
        float midA   = (startA + endA) / 2f;
        float midR   = (rInner + rEnd) / 2f;
        int   iconX  = cx + (int)(Math.cos(midA) * midR) - ICON_HALF;
        int   iconY  = cy + (int)(Math.sin(midA) * midR) - ICON_HALF;
        renderFluidIcon(gfx, fs, iconX, iconY, fluidColor);

        // Border (okraj segmentu — jen čáry po obvodu)
        drawArcLine(gfx, cx, cy, rEnd,   startA, endA, borderColor, steps);
        drawArcLine(gfx, cx, cy, rInner, startA, endA, 0x22FFFFFF,  steps);
    }

    /** Kreslí fluid sprite s tint barvou jako čtvereček ICON_SIZE × ICON_SIZE. */
    private void renderFluidIcon(GuiGraphics gfx, FluidStack fs, int x, int y, int color) {
        try {
            IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fs.getFluid());
            ResourceLocation texLoc = ext.getStillTexture(fs);
            if (texLoc == null) { drawColorSquare(gfx, x, y, color); return; }

            var atlas  = Minecraft.getInstance().getTextureAtlas(
                    net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS);
            TextureAtlasSprite sprite = atlas.apply(texLoc);

            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >>  8) & 0xFF) / 255f;
            float b = ((color      ) & 0xFF) / 255f;

            RenderSystem.setShaderColor(r, g, b, 1f);
            gfx.blit(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS,
                    x, y, ICON_SIZE, ICON_SIZE,
                    sprite.getU0(), sprite.getV0(),
                    (int) (sprite.getU1() - sprite.getU0()),
                    (int) (sprite.getV1() - sprite.getV0()),
                    1, 1);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        } catch (Exception e) {
            drawColorSquare(gfx, x, y, color);
        }
    }

    private void drawColorSquare(GuiGraphics gfx, int x, int y, int color) {
        gfx.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, color | 0xFF000000);
    }

    private void drawArcLine(GuiGraphics gfx, int cx, int cy, int r,
                             float startA, float endA, int color, int steps) {
        for (int s = 0; s < steps; s++) {
            float a = startA + (endA - startA) * s / steps;
            int px = cx + (int)(Math.cos(a) * r);
            int py = cy + (int)(Math.sin(a) * r);
            gfx.fill(px, py, px + 1, py + 1, color);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  HIT DETECTION
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Vrátí globalIdx segmentu pod kurzorem, nebo -1.
     * dx, dy jsou relativní ke středu screenu.
     */
    private int getHoveredIdx(int dx, int dy) {
        double dist  = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.atan2(dy, dx) + Math.PI / 2;
        if (angle < 0) angle += Math.PI * 2;

        int total = activeTanks.size();
        int drawn = 0;

        for (int ring = 0; ring < MAX_PER_RING.length && drawn < total; ring++) {
            int countInRing = Math.min(MAX_PER_RING[ring], total - drawn);
            int rOuter = RING_RADII_OUTER[ring] + 6; // +6 pro hover expand
            int rInner = RING_RADII_INNER[ring];

            if (dist >= rInner && dist <= rOuter) {
                float segAngle = (float)(Math.PI * 2 / countInRing);
                int seg = (int)(angle / segAngle) % countInRing;
                return drawn + seg;
            }
            drawn += countInRing;
        }
        return -1;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  INPUT
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);
        int seg = getHoveredIdx((int)mx - width / 2, (int)my - height / 2);
        if (seg >= 0 && seg < activeTanks.size()) {
            selectedIdx = seg;
            int tankIdx = activeTanks.get(seg)[0];
            PacketDistributor.sendToServer(new ForgeSelectFluidPacket(ctrlPos, tankIdx));
            onClose();
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == 256) { onClose(); return true; } // Escape
        return super.keyPressed(key, scan, mods);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  UTILITY
    // ═══════════════════════════════════════════════════════════════════════════

    private static int getFluidColor(FluidStack fs) {
        try {
            return IClientFluidTypeExtensions.of(fs.getFluid()).getTintColor(fs);
        } catch (Exception e) {
            return 0xFFFF6600;
        }
    }

    private static String formatMb(int mb) {
        return mb >= 1000 ? String.format("%.1fB", mb / 1000f) : mb + " mB";
    }

    @Override public boolean isPauseScreen() { return false; }
}