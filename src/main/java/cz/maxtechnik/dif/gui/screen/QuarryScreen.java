package cz.maxtechnik.dif.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import cz.maxtechnik.dif.gui.menu.QuarryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class QuarryScreen extends AbstractContainerScreen<QuarryMenu> {

	private static final ResourceLocation FALLBACK_TEXTURE =
			ResourceLocation.parse("minecraft:textures/gui/container/dispenser.png");

	public QuarryScreen(QuarryMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth  = 176;
		this.imageHeight = 166;
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics,mouseX,mouseY,partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics g, float pt, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		g.blit(FALLBACK_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		// Překryj horní část (skryje sloty dispenseru)
		g.fill(this.leftPos + 7, this.topPos + 15, this.leftPos + this.imageWidth - 7, this.topPos + 80, 0xFFC6C6C6);

		// 3 sloty vlevo
		for (int i = 0; i < 3; i++) {
			int sx = this.leftPos + 7;
			int sy = this.topPos + 16 + i * 18;
			g.fill(sx, sy, sx + 18, sy + 18, 0xFF373737);
			g.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
		}

		// Černá info tabulka
		g.fill(this.leftPos + 30, this.topPos + 17, this.leftPos + 168, this.topPos + 71, 0xFF101010);

		RenderSystem.disableBlend();
	}

	@Override
	protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
		g.drawString(this.font, this.title, 8, 5, 0x404040, false);
		g.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 94, 0x404040, false);

		int state      = this.menu.getStateOrdinal();
		int statusMode = this.menu.getStatusMode();

		String stateMsg;
		int    stateColor;

		if (state == 0) {
			stateMsg = "Out of Energy!"; stateColor = 0xFF5555;
		} else if (state == 4) {
			stateMsg = "Finished";       stateColor = 0xFFFF55;
		} else if (statusMode == 1) {
			stateMsg = "Missing Engine!";     stateColor = 0xFF5555;
		} else if (statusMode == 2) {
			stateMsg = "Missing Drill Head!"; stateColor = 0xFF5555;
		} else if (statusMode == 3) {
			stateMsg = "Low Drill Power!";    stateColor = 0xFF5555;
		} else {
			stateMsg = "Active"; stateColor = 0x55FF55;
		}

		int tx = 34;
		g.drawString(this.font, stateMsg, tx, 21, stateColor, false);

		int areaX = this.menu.getAreaX();
		int areaZ = this.menu.getAreaZ();
		g.drawString(this.font, "Area: " + areaX + " x " + areaZ, tx, 33, 0xFFFFFF, false);

		int speedVal = this.menu.getSpeed();
		int feCost   = this.menu.getFECost();

		if (!stateMsg.equals("Finished")) {
			g.drawString(this.font, "Power: " + speedVal + " DP",        tx,  45, 0xFFFFFF, false);
			g.drawString(this.font, "Usage: " + formatValue(feCost) + " FE/t", tx, 57, 0xFFFFFF, false);
		}
	}

	private String formatValue(int value) {
		if (value >= 1000000) {
			return String.format("%.1fM", value / 1000000.0f);
		}
		if (value >= 1000) {
			return String.format("%.1fK", value / 1000.0f);
		}
		return String.valueOf(value);
	}
}