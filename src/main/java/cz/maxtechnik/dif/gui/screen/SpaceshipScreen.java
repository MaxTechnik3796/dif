package cz.maxtechnik.dif.gui.screen;

import cz.maxtechnik.dif.init.events.SpaceshipControl;
import cz.maxtechnik.dif.gui.menu.SpaceshipMenu;
import cz.maxtechnik.dif.network.SpaceshipScreenButtonMessage;
import cz.maxtechnik.dif.DifMod;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.NotNull;

public class SpaceshipScreen extends AbstractContainerScreen<SpaceshipMenu>{
	private final int x,y,z;
	private final Player entity;
	private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/screens/rocketg_00.png");
	private static final ResourceLocation PLANETS_TEX=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/screens/planets.png");
	private static final ResourceLocation ARROWS_TEX=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/screens/arrows.png");

	public SpaceshipScreen(SpaceshipMenu container,Inventory inventory,Component text){
		super(container,inventory,text);
		this.x=container.x;
		this.y=container.y;
		this.z=container.z;
		this.entity=container.entity;
		this.imageWidth=238;
		this.imageHeight=222;
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
		g.blit(TEXTURE,this.leftPos,this.topPos,0,0,this.imageWidth,this.imageHeight,this.imageWidth,this.imageHeight);
		RenderSystem.disableBlend();
	}

	@Override
	public void init(){
		super.init();
		LevelAccessor world=entity.level();

		// 4 tlačítka planet – použijeme vanilla Button s vlastním renderem přes GuiGraphics
		for(int i=0;i<4;i++){
			final int buttonId=i;
            int btnX=this.leftPos+16+(i*49);
			int btnY=this.topPos+20;
			int u=getTextureUV(world,x,y,z,0, i);
			int v=getTextureUV(world,x,y,z,1, i);
			// Vlastní button který renderuje z textury atlas
			this.addRenderableWidget(new PlanetButton(btnX,btnY,44,73,u,v,PLANETS_TEX,176,584,btn->sendButtonPacket(buttonId)));
		}

		// Šipka doleva
		this.addRenderableWidget(new PlanetButton(this.leftPos+8,this.topPos+43,5,20,0,0,ARROWS_TEX,10,40,btn->sendButtonPacket(4)));
		// Šipka doprava
		this.addRenderableWidget(new PlanetButton(this.leftPos+210,this.topPos+43,5,20,5,0,ARROWS_TEX,10,40,btn->sendButtonPacket(5)));
	}

	private void sendButtonPacket(int id){
		net.neoforged.neoforge.network.PacketDistributor.sendToServer(new SpaceshipScreenButtonMessage(id,x,y,z));
	}

	public static int getTextureUV(LevelAccessor world,double x,double y,double z,int mode,int slot){
		int scroll=SpaceshipControl.getNBT(world,x,y,z,"scroll");
		int index=scroll+slot;
		if(mode==0) return (index%4)*44;
		if(mode==1) return Math.min(index/4,3)*73;
		return 0;
	}

	@Override
	protected void renderLabels(@NotNull GuiGraphics g,int mouseX,int mouseY){}

	// Jednoduchý button který renderuje část textury
	private static class PlanetButton extends Button{
		private final ResourceLocation tex;
		private final int u,v,texW,texH;

		public PlanetButton(int x,int y,int w,int h,int u,int v,ResourceLocation tex,int texW,int texH,OnPress onPress){
			super(x,y,w,h,Component.empty(),onPress,DEFAULT_NARRATION);
			this.u=u; this.v=v; this.tex=tex; this.texW=texW; this.texH=texH;
		}

		@Override
		public void renderWidget(@NotNull GuiGraphics g,int mouseX,int mouseY,float pt){
			g.blit(tex,this.getX(),this.getY(),u,v,this.width,this.height,texW,texH);
		}
	}
}