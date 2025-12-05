package cz.maxtechnik.dif.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import cz.maxtechnik.dif.gui.menu.AndesiteBarrelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
public class AndesiteBarrelScreen extends AbstractContainerScreen<AndesiteBarrelMenu>{
	private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("minecraft","textures/gui/container/generic_54.png");
	public AndesiteBarrelScreen(AndesiteBarrelMenu container,Inventory inventory,Component text){
		super(container,inventory,text);
		// Získáme počet řádků z menu (4)
		int rows=container.getRows();
		this.imageWidth=176;
		// Dynamický výpočet výšky: 114 (základ pro inventář a okraje) + počet řádků * 18
		// Pro 4 řádky to bude: 114 + 4*18 = 186 pixelů
		this.imageHeight=114+rows*18;
		this.inventoryLabelY=this.imageHeight-94;
	}
	@Override
	public void render(@NotNull GuiGraphics guiGraphics,int mouseX,int mouseY,float partialTicks){
		this.renderBackground(guiGraphics);
		super.render(guiGraphics,mouseX,mouseY,partialTicks);
		this.renderTooltip(guiGraphics,mouseX,mouseY);
	}
	@Override
	protected void renderBg(GuiGraphics guiGraphics,float partialTicks,int gx,int gy){
		RenderSystem.setShaderColor(1.0F,1.0F,1.0F,1.0F);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		int x=(this.width-this.imageWidth)/2;
		int y=(this.height-this.imageHeight)/2;
		int rows=this.menu.getRows();
		// 1. Vykreslení horního okraje (Hlavička) - 17 pixelů
		// Použijeme texturu generic_54.png
		guiGraphics.blit(TEXTURE,x,y,0,0,this.imageWidth,17);
		// 2. Vykreslení řádků barelu
		// Opakujeme první řádek slotů z textury pro každý řádek v barelu
		for(int i=0;i<rows;i++){
			// Y pozice v GUI: y + 17 (hlavička) + i*18
			// Y pozice v textuře: 17 (začátek slotů)
			guiGraphics.blit(TEXTURE,x,y+17+(i*18),0,17,this.imageWidth,18);
		}
		// 3. Vykreslení spodní části (Inventář hráče a okraj) - 96 pixelů
		// Ve standardní textuře začíná inventář hráče na Y=126 (pro generic_54 / double chest).
		// Umístíme ho pod poslední řádek barelu.
		guiGraphics.blit(TEXTURE,x,y+17+(rows*18),0,126,this.imageWidth,96);
		RenderSystem.disableBlend();
	}
}