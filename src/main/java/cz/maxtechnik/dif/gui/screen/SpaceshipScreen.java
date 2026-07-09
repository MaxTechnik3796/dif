package cz.maxtechnik.dif.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.gui.menu.SpaceshipMenu;
import cz.maxtechnik.dif.init.events.SpaceshipControl;
import cz.maxtechnik.dif.network.SpaceshipScreenButtonMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
public class SpaceshipScreen extends AbstractContainerScreen<SpaceshipMenu>{
	private final int x, y, z;
	private final Player entity;
	private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/screens/rocketg_00.png");
	private static final ResourceLocation[] PLANETS_TEX={
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"planets/overworld"),
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"planets/orbit"),
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"planets/moon"),
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"planets/unknown")
	};
	private static final ResourceLocation[] PLANETS_FOCUSED_TEX={
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"planets/overworld_focused"),
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"planets/orbit_focused"),
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"planets/moon_focused"),
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"planets/unknown_focused")
	};
	private static final ResourceLocation[] ARROWS_TEX={
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"arrows/left"),
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"arrows/left_focused"),
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"arrows/right"),
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"arrows/right_focused")
	};
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
			int btnX=this.leftPos+16+(i*49);
			int btnY=this.topPos+20;
			int offset=i+SpaceshipControl.getNBT(world,x,y,z,"scroll");
			if(offset>=PLANETS_TEX.length) offset=PLANETS_TEX.length-1;
			// Vlastní button který renderuje z textury atlas
			int finalI=i;
			this.addRenderableWidget(new SpecialButton(btnX,btnY,44,73,new WidgetSprites(PLANETS_TEX[offset],PLANETS_FOCUSED_TEX[offset]),btn->sendButtonPacket(finalI)));
		}
		// Šipka doleva
		this.addRenderableWidget(new SpecialButton(this.leftPos+8,this.topPos+43,5,20,new WidgetSprites(ARROWS_TEX[0],ARROWS_TEX[1]),btn->{
			SpaceshipControl.arrow(world,x,y,z,4);
			sendButtonPacket(4);
			this.rebuildWidgets();
		}));
		// Šipka doprava
		this.addRenderableWidget(new SpecialButton(this.leftPos+210,this.topPos+43,5,20,new WidgetSprites(ARROWS_TEX[2],ARROWS_TEX[3]),btn->{
			SpaceshipControl.arrow(world,x,y,z,5);
			sendButtonPacket(5);
			this.rebuildWidgets();
		}));
	}
	private void sendButtonPacket(int id){
		PacketDistributor.sendToServer(new SpaceshipScreenButtonMessage(id,x,y,z));
	}
	@Override
	protected void renderLabels(@NotNull GuiGraphics g,int mouseX,int mouseY){
	}
	// Jednoduchý button který renderuje část textury
	private static class SpecialButton extends ImageButton{
		public SpecialButton(int x,int y,int w,int h,WidgetSprites sprites,OnPress onPress){
			super(x,y,w,h,sprites,onPress);
		}
	}
}