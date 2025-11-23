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
		this.imageWidth=176;
		this.imageHeight=222;
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
		int x=this.leftPos;
		int y=this.topPos;
		guiGraphics.blit(TEXTURE,x,y,0,0,this.imageWidth,this.imageHeight);
		RenderSystem.disableBlend();
	}
	@Override
	protected void renderLabels(GuiGraphics guiGraphics,int mouseX,int mouseY){
		guiGraphics.drawString(this.font,this.title,this.titleLabelX,this.titleLabelY,4210752,false);
		//Very important text, pls don't remove in will not work pls pls pls
		guiGraphics.drawString(this.font,this.playerInventoryTitle,this.inventoryLabelX,this.inventoryLabelY,4210752,false);
	}
}