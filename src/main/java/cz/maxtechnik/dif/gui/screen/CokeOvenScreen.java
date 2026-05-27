package cz.maxtechnik.dif.gui.screen;

import cz.maxtechnik.dif.gui.menu.CokeOvenMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Coke Oven GUI Screen.
 *
 * LAYOUT (176×166):
 *   [56,35]  slot — vstup (uhlí)
 *   [80,35]  progress šipka (24×17)
 *   [116,35] slot — výstup (coke)
 *   [152,17] fluid bar (16×52)
 *
 * Pozadí a textury přidáš sám — zatím placeholder barvami.
 */
public class CokeOvenScreen extends AbstractContainerScreen<CokeOvenMenu> {

    public CokeOvenScreen(CokeOvenMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth  = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = (width - imageWidth) / 2, y = (height - imageHeight) / 2;

        // Placeholder pozadí
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF_C6C6C6);
        g.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFF_8B8B8B);

        // Progress šipka
        int aw = menu.getProgressBarWidth();
        if (aw > 0) g.fill(x+80, y+35, x+80+aw, y+52, 0xFF_FF8C00);
        g.renderOutline(x+80, y+35, 24, 17, 0xFF_555555);

        // Fluid bar
        int fh = menu.getFluidBarHeight();
        g.fill(x+152, y+17, x+168, y+69, 0xFF_404040);
        if (fh > 0) g.fill(x+152, y+69-fh, x+168, y+69, 0xFF_8B6914);
        g.renderOutline(x+151, y+16, 18, 54, 0xFF_555555);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        renderTooltip(g, mx, my);

        int x = (width - imageWidth) / 2, y = (height - imageHeight) / 2;

        // Fluid tooltip
        if (mx >= x+152 && mx < x+168 && my >= y+17 && my < y+69)
            g.renderTooltip(font, Component.literal(
                    menu.getFluidAmount() + " / " + menu.getFluidCapacity() + " mB"), mx, my);

        // Progress tooltip
        if (mx >= x+80 && mx < x+104 && my >= y+35 && my < y+52) {
            int t = menu.getFluidCapacity(); // reuse method — použij správný
            g.renderTooltip(font, Component.literal(
                    menu.getProgressBarWidth() * 100 / 24 + "%"), mx, my);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}