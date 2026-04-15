package cz.maxtechnik.dif.gui.screen;

import cz.maxtechnik.dif.gui.menu.MegaBackpackMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
public class MegaBackpackScreen extends AbstractContainerScreen<MegaBackpackMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

    public MegaBackpackScreen(MegaBackpackMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        // Rozměry celého okna (musí být větší než 176x166)
        this.imageWidth = 250; 
        this.imageHeight = 240;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        // Tlačítko Předchozí strana
        this.addRenderableWidget(Button.builder(Component.literal("<"), (btn) -> {
            // Tady pošleš paket na server pro změnu stránky
        }).bounds(leftPos + 5, topPos + 5, 20, 20).build());

        // Tlačítko Další strana
        this.addRenderableWidget(Button.builder(Component.literal(">"), (btn) -> {
            // Tady pošleš paket na server pro změnu stránky
        }).bounds(leftPos + imageWidth - 25, topPos + 5, 20, 20).build());
    }

    @Override
    public void render(@NotNull GuiGraphics graphics,int mouseX,int mouseY,float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        // Vykreslení hlavního šedého pozadí (můžeš použít vanilla texturu a roztáhnout ji)
        // Horní část (sloty batohu)
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        
        // Pokud chceš vanillu, musíš kreslit sloty v cyklu nebo mít vlastní 256x256 texturu
        // Tady kreslíme sloty dynamicky (bílé čtverečky z textury truhly)
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 13; j++) {
                graphics.blit(TEXTURE, leftPos + 7 + j * 18, topPos + 17 + i * 18, 7, 17, 18, 18);
            }
        }
    }
}