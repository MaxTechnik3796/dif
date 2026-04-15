package cz.maxtechnik.dif.gui.screen;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.gui.menu.MegaBackpackMenu;
import cz.maxtechnik.dif.network.BackpackPagePacket;
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
		this.addRenderableWidget(Button.builder(Component.literal(">"), (btn) -> {
			DifMod.PACKET_HANDLER.sendToServer(new BackpackPagePacket(1));
		}).bounds(leftPos + imageWidth - 25, topPos + 5, 20, 20).build());

        // Tlačítko Další strana
		this.addRenderableWidget(Button.builder(Component.literal("<"), (btn) -> {
			DifMod.PACKET_HANDLER.sendToServer(new BackpackPagePacket(-1));
		}).bounds(leftPos + 5, topPos + 5, 20, 20).build());
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
	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		// Vykreslí název kontejneru (Mega Backpack) na standardní pozici
		graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);

		// Vykreslí titulek inventáře hráče (u spodních slotů)
		graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX + 36, this.inventoryLabelY, 4210752, false);

		// --- Zobrazení aktuální stránky ---
		// Stránky indexujeme od 0, takže pro lidi zobrazíme +1
		String pageText = "Strana: " + (this.menu.getCurrentPage() + 1) + " / 16";

		// Výpočet pozice: zhruba doprostřed mezi tlačítka < a >
		// 4210752 je barva klasického tmavě šedého textu v Minecraftu
		int xPos = (this.imageWidth / 2) - (this.font.width(pageText) / 2);
		graphics.drawString(this.font, pageText, xPos, 10, 4210752, false);
	}
}