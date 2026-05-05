package cz.maxtechnik.dif.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import cz.maxtechnik.dif.gui.menu.OldChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
public class OldChestScreen extends AbstractContainerScreen<OldChestMenu>{
	private static final ResourceLocation TEXTURE=ResourceLocation.parse("minecraft:textures/gui/container/generic_54.png");
	public OldChestScreen(OldChestMenu container,Inventory inventory,Component text){
		super(container,inventory,text);
		// Získáme počet řádků z menu
		int rows=container.getChestRows();
		this.imageWidth=176;
		// Dynamický výpočet výšky: 114 (základ pro inv a okraje) + počet řádků * 18
		this.imageHeight=114+rows*18;
		this.inventoryLabelY=this.imageHeight-94;
	}
	@Override
	public void render(@NotNull GuiGraphics guiGraphics,int mouseX,int mouseY,float partialTicks){
		this.renderBackground(guiGraphics,mouseX,mouseY,partialTicks);
		super.render(guiGraphics,mouseX,mouseY,partialTicks);
		this.renderTooltip(guiGraphics,mouseX,mouseY);
	}
	@Override
	protected void renderBg(GuiGraphics guiGraphics,float partialTicks,int mouseX,int mouseY){
		RenderSystem.setShaderColor(1.0F,1.0F,1.0F,1.0F);
		int x=(this.width-this.imageWidth)/2;
		int y=(this.height-this.imageHeight)/2;
		int rows=this.menu.getChestRows();
		// 1. Vykreslení horního okraje (Hlavička) - 17 pixelů
		guiGraphics.blit(TEXTURE,x,y,0,0,this.imageWidth,17);
		// 2. Vykreslení řádků beden
		// Protože textura má omezený počet řádků, budeme opakovat první řádek textury pro každý řádek bedny.
		// První řádek slotů v 'generic_54.png' začíná na Y=17 a má výšku 18.
		for(int i=0;i<rows;i++){
			guiGraphics.blit(TEXTURE,x,y+17+(i*18),0,17,this.imageWidth,18);
		}
		// 3. Vykreslení spodní části (Inventář hráče) - 96 pixelů
		// Ve standardní textuře začíná inventář hráče na Y=126 (pro double chest).
		guiGraphics.blit(TEXTURE,x,y+17+(rows*18),0,126,this.imageWidth,96);
	}
}