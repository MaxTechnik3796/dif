package cz.maxtechnik.dif.gui.screen;

import cz.maxtechnik.dif.init.procedures.RocketControlProcedure;
import cz.maxtechnik.dif.gui.menu.Rocketg00Menu;
import cz.maxtechnik.dif.network.Rocketg00ButtonMessage;
import cz.maxtechnik.dif.DifMod;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.NotNull;
public class Rocketg00Screen extends AbstractContainerScreen<Rocketg00Menu>{
	private final int x, y, z;
	private final Player entity;
	private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/screens/rocketg_00.png");
	private static final ResourceLocation PLANETS_TEX=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/screens/planets.png");
	private static final ResourceLocation ARROWS_TEX=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/screens/arrows.png");
	public Rocketg00Screen(Rocketg00Menu container,Inventory inventory,Component text){
		super(container,inventory,text);
		this.x=container.x;
		this.y=container.y;
		this.z=container.z;
		this.entity=container.entity;
		this.imageWidth=238;
		this.imageHeight=222;
	}
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
		guiGraphics.blit(TEXTURE,this.leftPos,this.topPos,0,0,this.imageWidth,this.imageHeight,this.imageWidth,this.imageHeight);
		RenderSystem.disableBlend();
	}
	@Override
	public void init(){
		super.init();
		LevelAccessor world=entity.level();
		// 1. Tlačítka planet
		for(int i=0;i<4;i++){
			final int buttonId=i;
			int btnX=this.leftPos+16+(i*49);
			this.addRenderableWidget(new ImageButton(
					btnX,this.topPos+20,44,73,
					getTextureUV(world,x,y,z,0,i),
					getTextureUV(world,x,y,z,1,i),
					292,PLANETS_TEX,176,584, // Opraveno V-offset na 73 pro správný hover
					e->sendButtonPacket(buttonId)
			));
		}
		// 2. Šipky
		this.addRenderableWidget(new ImageButton(this.leftPos+8,this.topPos+43,5,20,0,0,20,ARROWS_TEX,10,40,e->sendButtonPacket(4)));
		this.addRenderableWidget(new ImageButton(this.leftPos+210,this.topPos+43,5,20,5,0,20,ARROWS_TEX,10,40,e->sendButtonPacket(5)));
	}
	private void sendButtonPacket(int id){
		DifMod.PACKET_HANDLER.sendToServer(new Rocketg00ButtonMessage(id,x,y,z));
	}
	public static int getTextureUV(LevelAccessor world,double x,double y,double z,int mode,int slot){
		int scroll=RocketControlProcedure.getNBT(world,x,y,z,"scroll");
		int index=scroll+slot;
		if(mode==0) return (index%4)*44;
		if(mode==1) return Math.min(index/4,3)*73;
		return 0;
	}
	@Override
	protected void renderLabels(@NotNull GuiGraphics guiGraphics,int mouseX,int mouseY){
	}
}