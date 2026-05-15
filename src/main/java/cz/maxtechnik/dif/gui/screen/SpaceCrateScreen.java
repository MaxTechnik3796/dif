package cz.maxtechnik.dif.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import cz.maxtechnik.dif.gui.menu.SpaceCrateMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
public class SpaceCrateScreen extends AbstractContainerScreen<SpaceCrateMenu>{
	private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("minecraft","textures/gui/container/generic_54.png");
	public SpaceCrateScreen(SpaceCrateMenu container,Inventory inventory,Component text){
		super(container,inventory,text);
		int rows=container.getRows();
		this.imageWidth=176;
		this.imageHeight=114+rows*18;
		this.inventoryLabelY=this.imageHeight-94;
	}
	@Override
	public void render(@NotNull GuiGraphics g,int mouseX,int mouseY,float pt){
		this.renderBackground(g,mouseX,mouseY,pt);
		super.render(g,mouseX,mouseY,pt);
		this.renderTooltip(g,mouseX,mouseY);
	}
	@Override
	protected void renderBg(GuiGraphics g,float pt,int gx,int gy){
		RenderSystem.setShaderColor(1,1,1,1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		int x=(this.width-this.imageWidth)/2;
		int y=(this.height-this.imageHeight)/2;
		int rows=this.menu.getRows();
		g.blit(TEXTURE,x,y,0,0,this.imageWidth,17);
		for(int i=0;i<rows;i++)
			g.blit(TEXTURE,x,y+17+(i*18),0,17,this.imageWidth,18);
		g.blit(TEXTURE,x,y+17+(rows*18),0,126,this.imageWidth,96);
		RenderSystem.disableBlend();
	}
}