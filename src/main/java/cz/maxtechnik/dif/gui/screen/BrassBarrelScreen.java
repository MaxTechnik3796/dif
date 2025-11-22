package cz.maxtechnik.dif.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import cz.maxtechnik.dif.gui.menu.BrassBarrelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
public class BrassBarrelScreen extends AbstractContainerScreen<BrassBarrelMenu>{
	// Textura pro 6 řádků (54 slotů)
	private static final ResourceLocation texture=ResourceLocation.fromNamespaceAndPath("minecraft","textures/gui/container/generic_54.png");
	public BrassBarrelScreen(BrassBarrelMenu container,Inventory inventory,Component text){
		super(container,inventory,text);
		this.imageWidth=176;
		this.imageHeight=222;
		// Vypočítá správnou Y pozici pro popisek inventáře hráče
		// (this.imageHeight - 94) = 222 - 94 = 128
		// Tvůj starý kód měl this.imageHeight - 96 = 126 (o 2 pixely špatně)
		this.inventoryLabelY=this.imageHeight-94;
	}
	@Override
	public void render(@NotNull GuiGraphics guiGraphics,int mouseX,int mouseY,float partialTicks){
		this.renderBackground(guiGraphics); // Vykreslí pozadí (ztmavení světa)
		super.render(guiGraphics,mouseX,mouseY,partialTicks); // Vykreslí sloty a itemy
		this.renderTooltip(guiGraphics,mouseX,mouseY); // Vykreslí tooltipy itemů
	}
	@Override
	protected void renderBg(GuiGraphics guiGraphics,float partialTicks,int gx,int gy){
		RenderSystem.setShaderColor(1.0F,1.0F,1.0F,1.0F);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		int x=this.leftPos;
		int y=this.topPos;
		// OPRAVA ZDE: Použita 6-argumentová metoda blit
		// Toto vykreslí část textury od (0,0) o velikosti (imageWidth, imageHeight)
		// na pozici (x, y) na obrazovce.
		guiGraphics.blit(texture,x,y,0,0,this.imageWidth,this.imageHeight);
		RenderSystem.disableBlend();
	}
	@Override
	protected void renderLabels(GuiGraphics guiGraphics,int mouseX,int mouseY){
		// OPRAVA ZDE: Používáme this.title a this.playerInventoryTitle pro správné texty
		// a this.titleLabelY / this.inventoryLabelY pro správné pozice.
		// Vykreslí název kontejneru (např. "Brass Barrel")
		guiGraphics.drawString(this.font,this.title,this.titleLabelX,this.titleLabelY,4210752,false);
		// Vykreslí název hráčova inventáře (např. "Inventory")
		guiGraphics.drawString(this.font,this.playerInventoryTitle,this.inventoryLabelX,this.inventoryLabelY,4210752,false);
	}
}