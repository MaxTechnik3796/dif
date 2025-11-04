package cz.maxtechnik.dif.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import cz.maxtechnik.dif.world.inventory.SuperBoxGuiMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class SuperBoxGuiScreen extends AbstractContainerScreen<SuperBoxGuiMenu>{

    public SuperBoxGuiScreen(SuperBoxGuiMenu container,Inventory inventory,Component text){
		super(container,inventory,text);
        this.imageWidth=424;
		this.imageHeight=236;
	}

	private static final ResourceLocation texture=ResourceLocation.fromNamespaceAndPath("dif","textures/screens/super_box.png");

	@Override
	public void render(@NotNull GuiGraphics guiGraphics,int mouseX,int mouseY,float partialTicks){
		this.renderBackground(guiGraphics);
		super.render(guiGraphics,mouseX,mouseY,partialTicks);
		this.renderTooltip(guiGraphics,mouseX,mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics,float partialTicks,int gx,int gy){
		RenderSystem.setShaderColor(1,1,1,1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		guiGraphics.blit(texture,this.leftPos,this.topPos,0,0,this.imageWidth,this.imageHeight,this.imageWidth,this.imageHeight);
		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key,int b,int c){
		if(key==256){
            assert this.minecraft!=null;
            assert this.minecraft.player!=null;
            this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key,b,c);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics,int mouseX,int mouseY){
		guiGraphics.drawString(this.font,Component.translatable("gui.dif.inventory"),130,144,-12632257,false);
		guiGraphics.drawString(this.font,Component.translatable("gui.dif.super_box"),4,6,-12632257,false);
	}

	@Override
	public void init() {
		super.init();
	}
}
