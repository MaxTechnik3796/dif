package cz.maxtechnik.dif.gui.screen;

import cz.maxtechnik.dif.block.entity.ForgeControllerBlockEntity;
import cz.maxtechnik.dif.network.ForgeSelectFluidPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
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

@SuppressWarnings("deprecation")
public class ForgeRadialScreen extends Screen {

	// ── Layout constants ──────────────────────────────────────────────────
	private static final int COLS     = 8;
	private static final int CARD_W   = 40;
	private static final int CARD_H   = 40;
	private static final int CARD_GAP = 4;
	private static final int CORNER_R = 4;
	private static final int BORDER_W = 2;

	private final ForgeControllerBlockEntity be;
	private final BlockPos ctrlPos;
	private final List<Integer> tankIndices = new ArrayList<>();
	private int selectedIdx = -1;
	private long openTime;

	public ForgeRadialScreen(ForgeControllerBlockEntity be) {
		super(Component.empty());
		this.be      = be;
		this.ctrlPos = be.getBlockPos();
		for (int i = 0; i < ForgeControllerBlockEntity.FLUID_TANK_COUNT; i++)
			if (!be.fluidTanks[i].isEmpty()) tankIndices.add(i);
		int pref = be.getPreferredOutputTank();
		for (int i = 0; i < tankIndices.size(); i++)
			if (tankIndices.get(i) == pref) { selectedIdx = i; break; }
	}

	@Override protected void init() { super.init(); openTime = System.currentTimeMillis(); }

	// ── Top-left corner of the whole grid (centred on screen) ─────────────
	private int gridX() {
		int total = tankIndices.size();
		int cols  = Math.min(COLS, total);
		return width  / 2 - (cols * (CARD_W + CARD_GAP) - CARD_GAP) / 2;
	}
	private int gridY() {
		int total = tankIndices.size();
		if (total == 0) return height / 2;
		int cols = Math.min(COLS, total);
		int rows = (int) Math.ceil((double) total / cols);
		return height / 2 - (rows * (CARD_H + CARD_GAP) - CARD_GAP) / 2 - 12;
	}

	// ── Card top-left for index i ─────────────────────────────────────────
	private int cardX(int i) { return gridX() + (i % COLS) * (CARD_W + CARD_GAP); }
	private int cardY(int i) { return gridY() + (i / COLS) * (CARD_H + CARD_GAP); }

	// ── Main render ───────────────────────────────────────────────────────
	@Override
	public void render(@NotNull GuiGraphics gfx, int mx, int my, float partial) {
		int total = tankIndices.size();

		// Poloprůhledné tmavé pozadí (MC GUI styl)
		gfx.fill(0, 0, width, height, 0x88000000);

		if (total == 0) {
			gfx.drawCenteredString(font, "§7No fluids in forge", width / 2, height / 2 - 4, 0xFFCCCCCC);
			super.render(gfx, mx, my, partial);
			return;
		}

		long elapsed  = System.currentTimeMillis() - openTime;
		float pulse   = (float)(Math.sin(elapsed / 350.0) * 0.18 + 0.82);
		int hoveredIdx = getHoveredIdx(mx, my);

		for (int i = 0; i < total; i++) {
			boolean hov = hoveredIdx == i;
			boolean sel = selectedIdx == i;
			drawCard(gfx, i, hov, sel, pulse, elapsed);
		}

		// Tooltip
		if (hoveredIdx >= 0) drawTooltip(gfx, hoveredIdx, mx, my);

		// Selected label
		int labelY = gridY() + getGridHeight() + 10;
		if (selectedIdx >= 0) {
			FluidStack sf = be.fluidTanks[tankIndices.get(selectedIdx)].getFluid();
			gfx.drawCenteredString(font,
					"§6Selected: §f" + sf.getHoverName().getString(),
					width / 2, labelY, 0xFFFFFFFF);
		}
		gfx.drawCenteredString(font, "§8click to select  ·  esc to cancel",
				width / 2, labelY + 12, 0xFF555555);

		super.render(gfx, mx, my, partial);
	}

	private int getGridHeight() {
		int total = tankIndices.size();
		if (total == 0) return 0;
		int rows = (int) Math.ceil((double) total / Math.min(COLS, total));
		return rows * (CARD_H + CARD_GAP) - CARD_GAP;
	}

	// ── Draw one card ─────────────────────────────────────────────────────
	private void drawCard(GuiGraphics gfx, int idx,
	                      boolean hovered, boolean selected,
	                      float pulse, long elapsed) {
		int x  = cardX(idx);
		int y  = cardY(idx);
		FluidStack fs = be.fluidTanks[tankIndices.get(idx)].getFluid();
		int fc = getFluidColor(fs);

		// Hover: lehký pulz alpha
		float hoverA = hovered
				? (float)(Math.sin(elapsed / 200.0) * 0.1 + 0.9)
				: 1.0f;

		// 1) Tmavé zaoblené pozadí karty
		fillRoundRect(gfx, x, y, CARD_W, CARD_H, CORNER_R, 0xCC111111);

		// 2) Fluid textura přes celou kartu (ořezaná zaoblením)
		renderFluidTiled(gfx, fs, x + BORDER_W, y + BORDER_W,
				CARD_W - BORDER_W * 2, CARD_H - BORDER_W * 2 - 12, fc, hoverA);

		// 3) Tmavý gradient overlay dole (pro čitelnost textu)
		fillRoundRect(gfx, x, y + CARD_H - 16, CARD_W, 16, 0, 0xBB000000);

		// 4) Množství
		String amt = formatMbShort(fs.getAmount());
		gfx.drawCenteredString(font, "§f" + amt, x + CARD_W / 2, y + CARD_H - 11, 0xFFFFFFFF);

		// 5) Border — barva fluidu
		int borderCol = (fc & 0x00FFFFFF) | 0xDD000000;
		strokeRoundRect(gfx, x, y, CARD_W, CARD_H, CORNER_R, BORDER_W, borderCol);

		// 6) Selected overlay — zlatý border + pulzující vnitřní glow
		if (selected) {
			strokeRoundRect(gfx, x - 1, y - 1, CARD_W + 2, CARD_H + 2,
					CORNER_R + 1, BORDER_W, 0xFFD4891A);
			// Vnitřní glow (druhý border těsně uvnitř)
			int glowA = (int)(pulse * 180);
			strokeRoundRect(gfx, x + 1, y + 1, CARD_W - 2, CARD_H - 2,
					CORNER_R - 1, 1, (glowA << 24) | 0xD4891A);
		}

		// 7) Hover highlight
		if (hovered && !selected) {
			strokeRoundRect(gfx, x - 1, y - 1, CARD_W + 2, CARD_H + 2,
					CORNER_R + 1, BORDER_W, 0x99FFFFFF);
		}
	}

	// ── Tooltip ───────────────────────────────────────────────────────────
	private void drawTooltip(GuiGraphics gfx, int idx, int mx, int my) {
		FluidStack fs  = be.fluidTanks[tankIndices.get(idx)].getFluid();
		String name    = fs.getHoverName().getString();
		String amount  = formatMb(fs.getAmount());
		int tw  = Math.max(font.width(name), font.width(amount)) + 16;
		int th  = 24;
		int tx  = mx + 10;
		int ty  = my - th - 4;
		if (tx + tw > width  - 4) tx = mx - tw - 10;
		if (ty < 4)               ty = my + 10;
		// Pozadí
		gfx.fill(tx, ty, tx + tw, ty + th, 0xEE0D0D0D);
		// Top border zlatý
		gfx.fill(tx, ty, tx + tw, ty + 1, 0xFFD4891A);
		// Bottom border dim
		gfx.fill(tx, ty + th - 1, tx + tw, ty + th, 0x66D4891A);
		gfx.drawString(font, name,   tx + 8, ty + 3,  0xFFEEEEEE, false);
		gfx.drawString(font, amount, tx + 8, ty + 13, 0xFF999999, false);
	}

	// ── Filled rounded rectangle ──────────────────────────────────────────
	private void fillRoundRect(GuiGraphics gfx, int x, int y, int w, int h, int r, int color) {
		if (r <= 0) { gfx.fill(x, y, x + w, y + h, color); return; }
		// Centre body
		gfx.fill(x + r, y,     x + w - r, y + h,     color);
		gfx.fill(x,     y + r, x + r,     y + h - r, color);
		gfx.fill(x + w - r, y + r, x + w, y + h - r, color);
		// Corners (quarter-circles)
		fillCorner(gfx, x + r,         y + r,         r, color, 180);
		fillCorner(gfx, x + w - r - 1, y + r,         r, color, 270);
		fillCorner(gfx, x + r,         y + h - r - 1, r, color,  90);
		fillCorner(gfx, x + w - r - 1, y + h - r - 1, r, color,   0);
	}

	/** Vyplní čtvrt-kruh v rohu. quadrant: 0=BR,90=BL,180=TL,270=TR */
	private void fillCorner(GuiGraphics gfx, int cx, int cy, int r, int color, int quadrant) {
		for (int dy = 0; dy < r; dy++) {
			int hw = (int) Math.sqrt((double)(r * r) - (double)((r - 1 - dy) * (r - 1 - dy)));
			int px, py, pw;
			switch (quadrant) {
				case 180 -> { px = cx - hw; py = cy - r + dy; pw = hw; }  // TL
				case 270 -> { px = cx + 1;  py = cy - r + dy; pw = hw; }  // TR
				case  90 -> { px = cx - hw; py = cy + dy + 1; pw = hw; }  // BL
				default  -> { px = cx + 1;  py = cy + dy + 1; pw = hw; }  // BR
			}
			gfx.fill(px, py, px + pw, py + 1, color);
		}
	}

	// ── Stroked rounded rectangle ─────────────────────────────────────────
	private void strokeRoundRect(GuiGraphics gfx, int x, int y, int w, int h, int r, int bw, int color) {
		for (int t = 0; t < bw; t++) {
			int xi = x + t, yi = y + t, wi = w - t * 2, hi = h - t * 2, ri = Math.max(0, r - t);
			// Top / bottom edges
			gfx.fill(xi + ri, yi,          xi + wi - ri, yi + 1,          color);
			gfx.fill(xi + ri, yi + hi - 1, xi + wi - ri, yi + hi,         color);
			// Left / right edges
			gfx.fill(xi,          yi + ri, xi + 1,          yi + hi - ri, color);
			gfx.fill(xi + wi - 1, yi + ri, xi + wi,         yi + hi - ri, color);
			// Corner arcs
			strokeCornerArc(gfx, xi + ri,          yi + ri,          ri, color, 180);
			strokeCornerArc(gfx, xi + wi - ri - 1, yi + ri,          ri, color, 270);
			strokeCornerArc(gfx, xi + ri,          yi + hi - ri - 1, ri, color,  90);
			strokeCornerArc(gfx, xi + wi - ri - 1, yi + hi - ri - 1, ri, color,   0);
		}
	}

	private void strokeCornerArc(GuiGraphics gfx, int cx, int cy, int r, int color, int quadrant) {
		if (r <= 0) return;
		for (int i = 0; i <= 90; i += 2) {
			double a = Math.toRadians(i);
			int ox = (int) Math.round(Math.cos(a) * r);
			int oy = (int) Math.round(Math.sin(a) * r);
			int px, py;
			switch (quadrant) {
				case 180 -> { px = cx - ox; py = cy - oy; }
				case 270 -> { px = cx + ox; py = cy - oy; }
				case  90 -> { px = cx - ox; py = cy + oy; }
				default  -> { px = cx + ox; py = cy + oy; }
			}
			gfx.fill(px, py, px + 1, py + 1, color);
		}
	}

	// ── Fluid texture tiled across card area ──────────────────────────────
	private void renderFluidTiled(GuiGraphics gfx, FluidStack fs,
	                              int x, int y, int w, int h,
	                              int color, float alpha) {
		try {
			IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fs.getFluid());
			ResourceLocation texLoc = ext.getStillTexture(fs);
			TextureAtlasSprite sprite = Minecraft.getInstance()
					.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texLoc);

			float r = ((color >> 16) & 0xFF) / 255f;
			float g = ((color >>  8) & 0xFF) / 255f;
			float b = ( color        & 0xFF) / 255f;

			RenderSystem.enableBlend();
			RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
			RenderSystem.setShaderColor(r, g, b, alpha);

			var tess = Tesselator.getInstance();
			var buf  = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			var mat  = gfx.pose().last().pose();

			// Tile 16×16 sprite across the card area
			int tileSize = 16;
			for (int ty = 0; ty < h; ty += tileSize) {
				for (int tx = 0; tx < w; tx += tileSize) {
					int tw = Math.min(tileSize, w - tx);
					int th = Math.min(tileSize, h - ty);
					float u1 = sprite.getU((float) tw / tileSize);
					float v1 = sprite.getV((float) th / tileSize);
					buf.addVertex(mat, x + tx,      y + ty + th, 0).setUv(sprite.getU0(), v1);
					buf.addVertex(mat, x + tx + tw, y + ty + th, 0).setUv(u1,             v1);
					buf.addVertex(mat, x + tx + tw, y + ty,      0).setUv(u1,             sprite.getV0());
					buf.addVertex(mat, x + tx,      y + ty,      0).setUv(sprite.getU0(), sprite.getV0());
				}
			}
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			BufferUploader.drawWithShader(buf.buildOrThrow());
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			RenderSystem.disableBlend();
		} catch (Exception e) {
			// Fallback: plná barva fluidu
			int fa = ((int)(alpha * 0xDD)) << 24;
			fillRoundRect(gfx, x, y, w, h, CORNER_R - BORDER_W, fa | (color & 0x00FFFFFF));
		}
	}

	// ── Hit detection ─────────────────────────────────────────────────────
	private int getHoveredIdx(int mx, int my) {
		for (int i = 0; i < tankIndices.size(); i++) {
			int x = cardX(i), y = cardY(i);
			if (mx >= x && mx < x + CARD_W && my >= y && my < y + CARD_H) return i;
		}
		return -1;
	}

	// ── Input ─────────────────────────────────────────────────────────────
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button != 0) return super.mouseClicked(mx, my, button);
		int idx = getHoveredIdx((int) mx, (int) my);
		if (idx >= 0) {
			selectedIdx = idx;
			PacketDistributor.sendToServer(
					new ForgeSelectFluidPacket(ctrlPos, tankIndices.get(idx)));
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

	// ── Helpers ───────────────────────────────────────────────────────────
	private static int getFluidColor(FluidStack fs) {
		try { return IClientFluidTypeExtensions.of(fs.getFluid()).getTintColor(fs); }
		catch (Exception e) { return 0xFFFF6600; }
	}

	private static String formatMb(int mb) {
		if (mb >= 1_000_000) return String.format("%.2fkB", mb / 1000f);
		if (mb >= 1_000)     return String.format("%.1f B", mb / 1000f);
		return mb + " mB";
	}

	private static String formatMbShort(int mb) {
		if (mb >= 1_000_000) return String.format("%.0fkB", mb / 1000f);
		if (mb >= 1_000)     return String.format("%.1fB",  mb / 1000f);
		return mb + "m";
	}

	@Override public boolean isPauseScreen() { return false; }
}