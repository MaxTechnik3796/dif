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
 * Design:
 *  - Žádné pozadí (průhledné) — svět je vidět skrz
 *  - Segmenty jsou plné s fluid barvou, zvýrazněný segment je světlejší
 *  - Tenká světlá mezera mezi segmenty
 *  - Vybraný segment má zlatý okraj
 *  - Uprostřed malý kruh s názvem vybraného fluidu
 *  - Nad kruhem tooltip při hoveru
 *  - Jemný pulzující efekt na vybraném segmentu (přes animaci alpha)
 */
public class ForgeRadialScreen extends Screen {

    private static final int[] RING_RADII_OUTER = {90, 58, 30};
    private static final int[] RING_RADII_INNER = {64, 36, 12};
    private static final int   ICON_SIZE        = 16;
    private static final int   ICON_HALF        = ICON_SIZE / 2;
    private static final int[] MAX_PER_RING     = {12, 12, 8};

    private final ForgeControllerBlockEntity be;
    private final BlockPos ctrlPos;
    private final List<int[]> activeTanks = new ArrayList<>();
    private int hoveredIdx  = -1;
    private int selectedIdx = -1;
    private long openTime   = 0;

    public ForgeRadialScreen(ForgeControllerBlockEntity be) {
        super(Component.empty());
        this.be      = be;
        this.ctrlPos = be.getBlockPos();

        for (int i = 0; i < ForgeControllerBlockEntity.FLUID_TANK_COUNT; i++) {
            if (!be.fluidTanks[i].isEmpty()) activeTanks.add(new int[]{i});
        }
        int pref = be.getPreferredOutputTank();
        for (int i = 0; i < activeTanks.size(); i++) {
            if (activeTanks.get(i)[0] == pref) { selectedIdx = i; break; }
        }
    }

    @Override
    protected void init() {
        super.init();
        openTime = System.currentTimeMillis();
    }

    @Override
    public void render(@NotNull GuiGraphics gfx, int mx, int my, float partial) {
        int cx = width / 2, cy = height / 2;
        long elapsed = System.currentTimeMillis() - openTime;
        float pulse  = (float)(Math.sin(elapsed / 400.0) * 0.12 + 0.88); // 0.76 – 1.00

        int total = activeTanks.size();
        if (total == 0) {
            // Malý temný rámeček uprostřed
            int bw = 140, bh = 32;
            gfx.fill(cx - bw/2, cy - bh/2, cx + bw/2, cy + bh/2, 0xCC111111);
            gfx.fill(cx - bw/2, cy - bh/2, cx + bw/2, cy - bh/2 + 1, 0xFF6B3D1A);
            gfx.drawCenteredString(font, "§7No fluids in forge", cx, cy - 4, 0xFFCCCCCC);
            super.render(gfx, mx, my, partial);
            return;
        }

        hoveredIdx = getHoveredIdx(mx - cx, my - cy);

        // Segmenty
        int drawn = 0;
        for (int ring = 0; ring < MAX_PER_RING.length && drawn < total; ring++) {
            int countInRing = Math.min(MAX_PER_RING[ring], total - drawn);
            for (int s = 0; s < countInRing; s++) {
                int gi = drawn + s;
                drawSegment(gfx, cx, cy, s, countInRing, gi,
                        RING_RADII_INNER[ring], RING_RADII_OUTER[ring],
                        hoveredIdx == gi, selectedIdx == gi, pulse);
            }
            drawn += countInRing;
        }

        // Střední kruh — jen jemný tmavý disk, žádný rámeček
        int cr = RING_RADII_INNER[RING_RADII_INNER.length - 1] - 1;
        drawDisk(gfx, cx, cy, cr, 0xCC0A0A0A);

        // Text uprostřed — vybraný fluid nebo "select"
        if (selectedIdx >= 0 && selectedIdx < total) {
            FluidStack sf = be.fluidTanks[activeTanks.get(selectedIdx)[0]].getFluid();
            String sname  = shortenName(sf.getHoverName().getString(), 7);
            gfx.drawCenteredString(font, "§6" + sname, cx, cy - 4, 0xFFFFFFFF);
        } else {
            gfx.drawCenteredString(font, "§8select", cx, cy - 4, 0xFF555555);
        }

        // Tooltip nad kruhem při hoveru
        if (hoveredIdx >= 0 && hoveredIdx < total) {
            FluidStack hfs  = be.fluidTanks[activeTanks.get(hoveredIdx)[0]].getFluid();
            String hname    = hfs.getHoverName().getString();
            String hamount  = formatMb(hfs.getAmount());
            int tw = Math.max(font.width(hname), font.width(hamount)) + 16;
            int tx = cx - tw / 2;
            int ty = cy - RING_RADII_OUTER[0] - 30;
            // Tooltip box bez okraje, jen jemné pozadí
            gfx.fill(tx, ty, tx + tw, ty + 22, 0xBB111111);
            // Zlatá linka nahoře
            gfx.fill(tx, ty, tx + tw, ty + 1, 0xFFD4891A);
            gfx.drawString(font, hname,   tx + 8, ty + 3,  0xFFEEEEEE, false);
            gfx.drawString(font, hamount, tx + 8, ty + 13, 0xFF888888, false);
        }

        // Jemná instrukce dole
        gfx.drawCenteredString(font, "§8click to select  ·  esc to cancel",
                cx, cy + RING_RADII_OUTER[0] + 14, 0xFF444444);

        super.render(gfx, mx, my, partial);
    }

    private void drawSegment(GuiGraphics gfx, int cx, int cy,
                             int segInRing, int totalInRing, int globalIdx,
                             int rInner, int rOuter,
                             boolean hovered, boolean selected, float pulse) {

        FluidStack fs  = be.fluidTanks[activeTanks.get(globalIdx)[0]].getFluid();
        int fc         = getFluidColor(fs);
        int fr         = (fc >> 16) & 0xFF;
        int fg         = (fc >>  8) & 0xFF;
        int fb         =  fc        & 0xFF;

        // Alpha: normal=0.70, hover=0.90, selected=pulse(0.76–1.00)
        float baseA    = selected ? pulse : hovered ? 0.90f : 0.70f;
        int   rEnd     = hovered || selected ? rOuter + 6 : rOuter;

        // Mezera mezi segmenty: 0.05 rad na každé straně
        float segAngle = (float)(Math.PI * 2 / totalInRing);
        float gap      = totalInRing > 1 ? 0.04f : 0f;
        float startA   = segAngle * segInRing - (float)(Math.PI / 2) + gap;
        float endA     = startA + segAngle - 2 * gap;
        int   steps    = Math.max(10, (int)(rOuter * segAngle / 2.5f));

        // Výplň segmentu
        var buffer = gfx.bufferSource().getBuffer(RenderType.gui());
        var mat    = gfx.pose().last().pose();

        for (int s = 0; s < steps; s++) {
            float a0 = startA + (endA - startA) * s / steps;
            float a1 = startA + (endA - startA) * (s + 1) / steps;

            float x0i = cx + (float)Math.cos(a0) * rInner;
            float y0i = cy + (float)Math.sin(a0) * rInner;
            float x1i = cx + (float)Math.cos(a1) * rInner;
            float y1i = cy + (float)Math.sin(a1) * rInner;
            float x0o = cx + (float)Math.cos(a0) * rEnd;
            float y0o = cy + (float)Math.sin(a0) * rEnd;
            float x1o = cx + (float)Math.cos(a1) * rEnd;
            float y1o = cy + (float)Math.sin(a1) * rEnd;

            float r = fr / 255f, g = fg / 255f, b = fb / 255f;

            buffer.addVertex(mat, x0i, y0i, 0).setColor(r, g, b, baseA);
            buffer.addVertex(mat, x0o, y0o, 0).setColor(r, g, b, baseA);
            buffer.addVertex(mat, x1o, y1o, 0).setColor(r, g, b, baseA);
            buffer.addVertex(mat, x0i, y0i, 0).setColor(r, g, b, baseA);

            buffer.addVertex(mat, x0i, y0i, 0).setColor(r, g, b, baseA);
            buffer.addVertex(mat, x1o, y1o, 0).setColor(r, g, b, baseA);
            buffer.addVertex(mat, x1i, y1i, 0).setColor(r, g, b, baseA);
            buffer.addVertex(mat, x0i, y0i, 0).setColor(r, g, b, baseA);
        }

        // Okraj: zlatý pro vybraný, bílý poloprůhledný pro hover, nic pro normal
        if (selected) {
            drawArcLine(gfx, cx, cy, rEnd,   startA, endA, 0xFFD4891A, steps);
            drawArcLine(gfx, cx, cy, rInner, startA, endA, 0xFFD4891A, steps);
        } else if (hovered) {
            drawArcLine(gfx, cx, cy, rEnd,   startA, endA, 0xAAFFFFFF, steps);
        }

        // Fluid ikona uprostřed segmentu
        float midA  = (startA + endA) / 2f;
        float midR  = (rInner + rEnd) / 2f;
        int   iconX = cx + (int)(Math.cos(midA) * midR) - ICON_HALF;
        int   iconY = cy + (int)(Math.sin(midA) * midR) - ICON_HALF;
        renderFluidIcon(gfx, fs, iconX, iconY, fc);
    }

    /** Vykreslí plný disk (aproximace přes čtverce, stačí pro střed). */
    private void drawDisk(GuiGraphics gfx, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int hw = (int)Math.sqrt(r * r - dy * dy);
            gfx.fill(cx - hw, cy + dy, cx + hw, cy + dy + 1, color);
        }
    }

    private void renderFluidIcon(GuiGraphics gfx, FluidStack fs, int x, int y, int color) {
        try {
            IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fs.getFluid());
            ResourceLocation texLoc = ext.getStillTexture(fs);
            if (texLoc == null) { drawColorDot(gfx, x, y, color); return; }

            var atlas  = Minecraft.getInstance().getTextureAtlas(
                    net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS);
            TextureAtlasSprite sprite = atlas.apply(texLoc);

            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >>  8) & 0xFF) / 255f;
            float b = ((color      ) & 0xFF) / 255f;

            RenderSystem.setShaderTexture(0, net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS);
            RenderSystem.setShaderColor(r, g, b, 1f);

            var pose  = gfx.pose();
            var tess  = com.mojang.blaze3d.vertex.Tesselator.getInstance();
            var buf   = tess.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
                    com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX);
            var mat   = pose.last().pose();

            buf.addVertex(mat, x,             y + ICON_SIZE, 0).setUv(sprite.getU0(), sprite.getV1());
            buf.addVertex(mat, x + ICON_SIZE, y + ICON_SIZE, 0).setUv(sprite.getU1(), sprite.getV1());
            buf.addVertex(mat, x + ICON_SIZE, y,             0).setUv(sprite.getU1(), sprite.getV0());
            buf.addVertex(mat, x,             y,             0).setUv(sprite.getU0(), sprite.getV0());

            RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
            com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buf.buildOrThrow());
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        } catch (Exception e) {
            drawColorDot(gfx, x, y, color);
        }
    }

    /** Fallback — malý barevný kruh místo textury. */
    private void drawColorDot(GuiGraphics gfx, int x, int y, int color) {
        int cx = x + ICON_HALF, cy = y + ICON_HALF, r = ICON_HALF - 1;
        for (int dy = -r; dy <= r; dy++) {
            int hw = (int)Math.sqrt(r * r - dy * dy);
            gfx.fill(cx - hw, cy + dy, cx + hw, cy + dy + 1, color | 0xFF000000);
        }
    }

    private void drawArcLine(GuiGraphics gfx, int cx, int cy, int r,
                             float startA, float endA, int color, int steps) {
        for (int s = 0; s <= steps; s++) {
            float a  = startA + (endA - startA) * s / steps;
            int   px = cx + (int)(Math.cos(a) * r);
            int   py = cy + (int)(Math.sin(a) * r);
            gfx.fill(px, py, px + 1, py + 1, color);
        }
    }

    private int getHoveredIdx(int dx, int dy) {
        double dist  = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.atan2(dy, dx) + Math.PI / 2;
        if (angle < 0) angle += Math.PI * 2;
        int total = activeTanks.size(), drawn = 0;
        for (int ring = 0; ring < MAX_PER_RING.length && drawn < total; ring++) {
            int countInRing = Math.min(MAX_PER_RING[ring], total - drawn);
            int rOuter = RING_RADII_OUTER[ring] + 7;
            int rInner = RING_RADII_INNER[ring];
            if (dist >= rInner && dist <= rOuter) {
                float segAngle = (float)(Math.PI * 2 / countInRing);
                return drawn + (int)(angle / segAngle) % countInRing;
            }
            drawn += countInRing;
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);
        int seg = getHoveredIdx((int)mx - width / 2, (int)my - height / 2);
        if (seg >= 0 && seg < activeTanks.size()) {
            selectedIdx = seg;
            PacketDistributor.sendToServer(new ForgeSelectFluidPacket(ctrlPos, activeTanks.get(seg)[0]));
            onClose();
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == 256) { onClose(); return true; }
        return super.keyPressed(key, scan, mods);
    }

    private static int getFluidColor(FluidStack fs) {
        try { return IClientFluidTypeExtensions.of(fs.getFluid()).getTintColor(fs); }
        catch (Exception e) { return 0xFFFF6600; }
    }

    private static String shortenName(String name, int max) {
        return name.length() > max ? name.substring(0, max - 1) + "…" : name;
    }

    private static String formatMb(int mb) {
        return mb >= 1000 ? String.format("%.1fB", mb / 1000f) : mb + " mB";
    }

    @Override public boolean isPauseScreen() { return false; }
}