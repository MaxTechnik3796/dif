package cz.maxtechnik.dif.gui.screen;

import cz.maxtechnik.dif.block.entity.ForgeControllerBlockEntity;
import cz.maxtechnik.dif.network.ForgeSelectFluidPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
@SuppressWarnings("deprecation")
public class ForgeRadialScreen extends Screen{
	private static final int COLS=8;
	private static final int CARD_W=40;
	private static final int CARD_H=40;
	private static final int CARD_GAP=4;
	private static final int BORDER_W=2;
	private final ForgeControllerBlockEntity be;
	private final BlockPos ctrlPos;
	private final List<Integer> tankIndices=new ArrayList<>();
	private final List<Integer> fluidColors=new ArrayList<>();
	private int selectedIdx=-1;
	private long openTime;
	public ForgeRadialScreen(ForgeControllerBlockEntity be){
		super(Component.empty());
		this.be=be;
		this.ctrlPos=be.getBlockPos();
		for(int i=0;i<ForgeControllerBlockEntity.FLUID_TANK_COUNT;i++){
			if(!be.fluidTanks[i].isEmpty()){
				tankIndices.add(i);
				fluidColors.add(getFluidColor(be.fluidTanks[i].getFluid()));
			}
		}
		int pref=be.getPreferredOutputTank();
		for(int i=0;i<tankIndices.size();i++){
			if(tankIndices.get(i)==pref){
				selectedIdx=i;
				break;
			}
		}
	}
	@Override
	protected void init(){
		super.init();
		openTime=System.currentTimeMillis();
	}
	// ── Grid helpers ──────────────────────────────────────────────────────
	private int cols(){
		return Math.clamp(tankIndices.size(),1,COLS);
	}
	private int gridX(){
		return width/2-(cols()*(CARD_W+CARD_GAP)-CARD_GAP)/2;
	}
	private int gridY(){
		int rows=(int)Math.ceil((double)tankIndices.size()/cols());
		return height/2-(rows*(CARD_H+CARD_GAP)-CARD_GAP)/2-12;
	}
	private int cardX(int i){
		return gridX()+(i%cols())*(CARD_W+CARD_GAP);
	}
	private int cardY(int i){
		return gridY()+(i/cols())*(CARD_H+CARD_GAP);
	}
	private int gridHeight(){
		int rows=(int)Math.ceil((double)tankIndices.size()/cols());
		return rows*(CARD_H+CARD_GAP)-CARD_GAP;
	}
	// ── Render ────────────────────────────────────────────────────────────
	@Override
	public void renderBackground(@NotNull GuiGraphics gfx,int mx,int my,float partial){
		// Bez bluru, bez ztmavení
	}
	@Override
	public void render(@NotNull GuiGraphics gfx,int mx,int my,float partial){
		int total=tankIndices.size();
		if(total==0){
			gfx.drawCenteredString(font,"§7No fluids in forge",width/2,height/2-4,0xFFCCCCCC);
			super.render(gfx,mx,my,partial);
			return;
		}
		long elapsed=System.currentTimeMillis()-openTime;
		float pulse=(float)(Math.sin(elapsed/350.0)*0.18+0.82);
		int hoveredIdx=getHoveredIdx(mx,my);
		for(int i=0;i<total;i++)
			drawCard(gfx,i,hoveredIdx==i,selectedIdx==i,pulse);
		if(hoveredIdx>=0){
			gfx.pose().pushPose();
			gfx.pose().translate(0,0,400);
			drawTooltip(gfx,hoveredIdx,mx,my);
			gfx.pose().popPose();
		}
		int labelY=gridY()+gridHeight()+10;
		if(selectedIdx>=0){
			FluidStack sf=be.fluidTanks[tankIndices.get(selectedIdx)].getFluid();
			gfx.drawCenteredString(font,
					"§6Selected: §f"+sf.getHoverName().getString(),
					width/2,labelY,0xFFFFFFFF);
		}
		gfx.drawCenteredString(font,"§8click to select  ·  esc to cancel",
				width/2,labelY+12,0xFF555555);
		super.render(gfx,mx,my,partial);
	}
	// ── Card ──────────────────────────────────────────────────────────────
	private void drawCard(GuiGraphics gfx,int idx,
	                      boolean hovered,boolean selected,
	                      float pulse){
		int x=cardX(idx);
		int y=cardY(idx);
		int fc=fluidColors.get(idx);
		FluidStack fs=be.fluidTanks[tankIndices.get(idx)].getFluid();
		// Pozadí
		gfx.fill(x,y,x+CARD_W,y+CARD_H,0xCC111111);
		// Fluid textura přes blit
		try{
			var ext=net.neoforged.neoforge.client.extensions.common
					.IClientFluidTypeExtensions.of(fs.getFluid());
			net.minecraft.resources.ResourceLocation texLoc=ext.getStillTexture(fs);
			net.minecraft.client.renderer.texture.TextureAtlasSprite sprite=
					net.minecraft.client.Minecraft.getInstance()
							.getTextureAtlas(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)
							.apply(texLoc);
			int sx=x+BORDER_W;
			int sy=y+BORDER_W;
			int sw=CARD_W-BORDER_W*2;
			int sh=CARD_H-BORDER_W*2-12;
			int tint=ext.getTintColor(fs);
			float r=((tint>>16)&0xFF)/255f;
			float g=((tint>>8)&0xFF)/255f;
			float b=(tint&0xFF)/255f;
			float a=((tint>>24)&0xFF)/255f;
			gfx.setColor(r,g,b,a);
			gfx.blit(sx,sy,0,sw,sh,sprite);
			gfx.setColor(1f,1f,1f,1f);
		}catch(Exception e){
			gfx.fill(x+BORDER_W,y+BORDER_W,
					x+CARD_W-BORDER_W,y+CARD_H-BORDER_W-12,
					0xFF000000|fc);
		}
		// Tmavý pruh dole
		gfx.fill(x,y+CARD_H-14,x+CARD_W,y+CARD_H,0xFF000000);
		// Množství
		gfx.drawCenteredString(font,"§f"+formatMbShort(fs.getAmount()),
				x+CARD_W/2,y+CARD_H-11,0xFFFFFFFF);
		// Border
		drawBorder(gfx,x,y,CARD_W,CARD_H,BORDER_W,0xFF555555);
		if(selected){
			drawBorder(gfx,x-1,y-1,CARD_W+2,CARD_H+2,BORDER_W,0xFFD4891A);
			int glowA=(int)(pulse*180);
			drawBorder(gfx,x+1,y+1,CARD_W-2,CARD_H-2,1,(glowA<<24)|0xD4891A);
		}
		if(hovered&&!selected)
			drawBorder(gfx,x-1,y-1,CARD_W+2,CARD_H+2,BORDER_W,0x99FFFFFF);
	}
	// ── Simple border (4 fill calls) ──────────────────────────────────────
	private void drawBorder(GuiGraphics gfx,int x,int y,int w,int h,int t,int color){
		gfx.fill(x,y,x+w,y+t,color); // top
		gfx.fill(x,y+h-t,x+w,y+h,color); // bottom
		gfx.fill(x,y+t,x+t,y+h-t,color); // left
		gfx.fill(x+w-t,y+t,x+w,y+h-t,color); // right
	}
	// ── Tooltip ───────────────────────────────────────────────────────────
	private void drawTooltip(GuiGraphics gfx,int idx,int mx,int my){
		FluidStack fs=be.fluidTanks[tankIndices.get(idx)].getFluid();
		String name=fs.getHoverName().getString();
		String amount=formatMb(fs.getAmount());
		int tw=Math.max(font.width(name),font.width(amount))+16;
		int th=24;
		int tx=mx+10;
		int ty=my-th-4;
		if(tx+tw>width-4) tx=mx-tw-10;
		if(ty<4) ty=my+10;
		gfx.fill(tx,ty,tx+tw,ty+th,0xFF0D0D0D);
		gfx.fill(tx,ty,tx+tw,ty+1,0xFFD4891A);
		gfx.fill(tx,ty+th-1,tx+tw,ty+th,0x66D4891A);
		gfx.drawString(font,name,tx+8,ty+3,0xFFEEEEEE,false);
		gfx.drawString(font,amount,tx+8,ty+13,0xFF999999,false);
	}
	// ── Hit detection ─────────────────────────────────────────────────────
	private int getHoveredIdx(int mx,int my){
		for(int i=0;i<tankIndices.size();i++){
			int x=cardX(i), y=cardY(i);
			if(mx>=x&&mx<x+CARD_W&&my>=y&&my<y+CARD_H) return i;
		}
		return -1;
	}
	// ── Input ─────────────────────────────────────────────────────────────
	@Override
	public boolean mouseClicked(double mx,double my,int button){
		if(button!=0) return super.mouseClicked(mx,my,button);
		int idx=getHoveredIdx((int)mx,(int)my);
		if(idx>=0){
			selectedIdx=idx;
			PacketDistributor.sendToServer(
					new ForgeSelectFluidPacket(ctrlPos,tankIndices.get(idx)));
			onClose();
			return true;
		}
		return super.mouseClicked(mx,my,button);
	}
	@Override
	public boolean keyPressed(int key,int scan,int mods){
		if(key==256){
			onClose();
			return true;
		}
		return super.keyPressed(key,scan,mods);
	}
	// ── Helpers ───────────────────────────────────────────────────────────
	private static int getFluidColor(FluidStack fs){
		try{
			var ext=net.neoforged.neoforge.client.extensions.common
					.IClientFluidTypeExtensions.of(fs.getFluid());
			int tint=ext.getTintColor(fs);
			if(tint==-1) return 0x3F76E4; // voda fallback
			return tint&0x00FFFFFF;
		}catch(Exception e){
			return 0xFF6600;
		}
	}
	private static String formatMb(int mb){
		if(mb>=1_000_000) return String.format("%.2fkB",mb/1000f);
		if(mb>=1_000) return String.format("%.1f B",mb/1000f);
		return mb+" mB";
	}
	private static String formatMbShort(int mb){
		if(mb>=1_000_000) return String.format("%.0fkB",mb/1000f);
		if(mb>=1_000) return String.format("%.1fB",mb/1000f);
		return mb+"m";
	}
	@Override
	public boolean isPauseScreen(){
		return false;
	}
}