package cz.maxtechnik.dif.gui.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cz.maxtechnik.dif.gui.menu.BurningGeneratorMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

import static cz.maxtechnik.dif.DifMod.mouseIn;
public class BurningGeneratorScreen extends AbstractContainerScreen<BurningGeneratorMenu>{
	public BurningGeneratorScreen(BurningGeneratorMenu container,Inventory inventory,Component text){
		super(container,inventory,text);
		this.imageWidth=176;
		this.imageHeight=166;
	}
	private static final ResourceLocation GUI_TEXTURE=ResourceLocation.fromNamespaceAndPath("dif","textures/screens/burning_generator.png");
	private static final ResourceLocation WIDGETS_TEXTURE=ResourceLocation.fromNamespaceAndPath("dif","textures/screens/widgets.png");
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
		guiGraphics.blit(GUI_TEXTURE,this.leftPos,this.topPos,0,0,this.imageWidth,this.imageHeight,this.imageWidth,this.imageHeight);
		int sizeWidgetsX=128;
		int sizeWidgetsY=128;
		int burnTime=this.menu.getBurnTime();
		int maxBurnTime=this.menu.getMaxBurnTime();
		if(burnTime>0&&maxBurnTime>0){
			int scaledFlameHeight=(int)(14*(burnTime/(double)maxBurnTime));
			int flameSourceX=32;
			int flameSourceY=14-scaledFlameHeight;//0+
			int flameX=this.leftPos+79;
			int flameY=this.topPos+16+(14-scaledFlameHeight);
			guiGraphics.blit(WIDGETS_TEXTURE,flameX,flameY,flameSourceX,flameSourceY,14,scaledFlameHeight,sizeWidgetsX,sizeWidgetsY);
		}
		int energyStored=this.menu.getEnergyStored();
		int maxEnergy=this.menu.getMaxEnergyStored();
		int energyBarWidth=14;
		int energyBarTotalHeight=52;
		int scaledEnergyHeight=(int)(((double)energyStored/maxEnergy)*energyBarTotalHeight);
		int sourceEnergyBarX=100;
		int sourceEnergyBarY=energyBarTotalHeight-scaledEnergyHeight;//0+
		int screenEnergyBarX=this.leftPos+9;
		int screenEnergyBarY=this.topPos+17+(energyBarTotalHeight-scaledEnergyHeight);
		guiGraphics.blit(WIDGETS_TEXTURE,screenEnergyBarX,screenEnergyBarY,sourceEnergyBarX,sourceEnergyBarY,energyBarWidth,scaledEnergyHeight,sizeWidgetsX,sizeWidgetsY);
		if(this.menu.getLit()==1){
			int sourcePowerX=60;
			int sourcePowerY=0;
			int screenPowerX=this.leftPos+59;
			int screenPowerY=this.topPos+36;
			int sizePowerX=7;
			int sizePowerY=13;
			guiGraphics.blit(WIDGETS_TEXTURE,screenPowerX,screenPowerY,sourcePowerX,sourcePowerY,sizePowerX,sizePowerY,sizeWidgetsX,sizeWidgetsY);
		}
		if(this.menu.getFuel()==0&&this.menu.getEmpty()==0){
			int sourceWarnX=0;
			int sourceWarnY=32;
			int screenWarnX=this.leftPos+75;
			int screenWarnY=this.topPos+31;
			int sizeWarnX=24;
			int sizeWarnY=24;
			guiGraphics.blit(WIDGETS_TEXTURE,screenWarnX,screenWarnY,sourceWarnX,sourceWarnY,sizeWarnX,sizeWarnY,sizeWidgetsX,sizeWidgetsY);
		}
		RenderSystem.disableBlend();
	}
	@Override
	public boolean keyPressed(int key,int b,int c){
		if(key==256){
			assert Objects.requireNonNull(this.minecraft).player!=null;
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key,b,c);
	}
	@Override
	protected void renderLabels(GuiGraphics guiGraphics,int mouseX,int mouseY){
		guiGraphics.drawString(this.font,Component.translatable("gui.dif.burning_generator"),8,6,-12632257,false);
		guiGraphics.drawString(this.font,Component.translatable("gui.dif.inventory"),8,73,-12632257,false);
		if(this.menu.getFuel()==0&&this.menu.getEmpty()==0){
			guiGraphics.drawString(this.font,Component.translatable("gui.dif.invalid_fuel0"),100,34,-8905443,false);
			guiGraphics.drawString(this.font,Component.translatable("gui.dif.invalid_fuel1"),100,44,-8905443,false);
		}
	}
	@Override
	public void renderTooltip(@NotNull GuiGraphics guiGraphics,int mouseX,int mouseY){
		super.renderTooltip(guiGraphics,mouseX,mouseY);
		int flameX=this.leftPos+79;
		int flameY=this.topPos+16;
		int flameWidth=14;
		int flameHeight=14;
		if(mouseIn(mouseX,mouseY,flameX,flameY,flameWidth,flameHeight)){
			int burnTime=this.menu.getBurnTime();
			List<Component> tooltip;
			if(burnTime>0||this.menu.getFuel()>0){
				tooltip=new ArrayList<>();
				tooltip.add(Component.literal("Burning for: "+burnTime/20+" seconds"));
				if(this.menu.getLit()==0)
					tooltip.add(Component.literal("(Paused)").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.GRAY)).withItalic(true)));
			}else{
				tooltip=new ArrayList<>();
				tooltip.add(Component.literal("Not burning"));
			}
			guiGraphics.renderComponentTooltip(this.font,tooltip,mouseX,mouseY);
		}
		int energyBarX=this.leftPos+9;
		int energyBarY=this.topPos+17;
		int energyBarWidth=14;
		int energyBarHeight=52;
		if(mouseIn(mouseX,mouseY,energyBarX,energyBarY,energyBarWidth,energyBarHeight)){
			List<Component> tooltip=new ArrayList<>();
			tooltip.add(Component.literal(this.menu.getEnergyStored()+" / "+this.menu.getMaxEnergyStored()+" FE"));
			guiGraphics.renderComponentTooltip(this.font,tooltip,mouseX,mouseY);
		}
		int powerX=this.leftPos+54;
		int powerY=this.topPos+34;
		int powerWidth=18;
		int powerHeight=18;
		if(mouseIn(mouseX,mouseY,powerX,powerY,powerWidth,powerHeight)){
			List<Component> tooltip=new ArrayList<>();
			if(this.menu.getLit()==1){
				tooltip.add(Component.literal("Status: ON"));
			}else{
				tooltip.add(Component.literal("Status: OFF"));
			}
			guiGraphics.renderComponentTooltip(this.font,tooltip,mouseX,mouseY);
		}
	}
	@Override
	public void init(){
		super.init();
	}
}
