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
	private static final ResourceLocation FALLBACK_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/dispenser.png");

	public QuarryScreen(QuarryMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// Vykreslení záložního pozadí (standardní 176x166 s inv hráčem)
		guiGraphics.blit(FALLBACK_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

		// Překrytí horní části, abychom zamaskovali sloty dispensru
		guiGraphics.fill(this.leftPos + 7, this.topPos + 15, this.leftPos + this.imageWidth - 7, this.topPos + 80, 0xFFC6C6C6);

		// Vykreslení 3 slotů vlevo nad sebou
		for (int i = 0; i < 3; i++) {
			int sx = this.leftPos + 7;
			int sy = this.topPos + 16 + i * 18;
			guiGraphics.fill(sx, sy, sx + 18, sy + 18, 0xFF373737); // stín
			guiGraphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B); // vnitřek
		}

		// Černá tabulka (od slotů doprava: x = 30, y = 17, width = 138, height = 54)
		guiGraphics.fill(this.leftPos + 30, this.topPos + 17, this.leftPos + 168, this.topPos + 71, 0xFF101010);

		RenderSystem.disableBlend();
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, this.title, 8, 5, 0x404040, false);
		guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 94, 0x404040, false);

		int state = this.menu.getStateOrdinal();
		int statusMode = this.menu.getStatusMode();
		String stateMsg;
		int stateColor;

		if (state == 0) { // NO_ENERGY
			stateMsg = "Out of Energy!";
			stateColor = 0xFF5555; // Red
		} else if (state == 4) { // DONE
			stateMsg = "Finished";
			stateColor = 0xFFFF55; // Yellow
		} else if (statusMode == 1) {
            stateMsg = "Missing Engine!"; 
            stateColor = 0xFF5555; // Red
        } else if (statusMode == 2) {
            stateMsg = "Missing Drill Head!";
            stateColor = 0xFF5555; // Red
        } else if (statusMode == 3) {
            stateMsg = "Low Drill Power!";
            stateColor = 0xFF5555; // Red
        } else {
            stateMsg = "Active";
            stateColor = 0x55FF55; // Lime
		}

		int textX = 34;
		guiGraphics.drawString(this.font, stateMsg, textX, 21, stateColor, false);

		// Dimensions
		int areaX = this.menu.getAreaX();
		int areaZ = this.menu.getAreaZ();
		guiGraphics.drawString(this.font, "Area: " + areaX + " x " + areaZ, textX, 33, 0xFFFFFF, false);

		// Stats
		int speedVal = this.menu.getSpeed(); 
		int in = this.menu.getFEInput();
		int out = this.menu.getFEOutput(); 

		if (stateMsg.equals("Active") || stateMsg.equals("Out of Energy!")) {
			guiGraphics.drawString(this.font, "Power: " + speedVal + "%", textX, 45, 0xFFFFFF, false);
			guiGraphics.drawString(this.font, "In " + in + " FE", textX, 57, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, "Out " + out + " FE", 100, 57, 0xFFFFFF, false);
		}



	}
}
