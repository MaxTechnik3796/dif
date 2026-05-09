package cz.maxtechnik.dif.gui.screen;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.gui.menu.MegaBackpackMenu;
import cz.maxtechnik.dif.network.MegaBackpackPagePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
public class MegaBackpackScreen extends AbstractContainerScreen<MegaBackpackMenu>{
	private static final ResourceLocation SLOT=
			ResourceLocation.parse("minecraft:textures/gui/container/generic_54.png");
	private static final ResourceLocation BACKGROUND00=
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/screens/mega_backpack_00.png");
	private static final ResourceLocation BACKGROUND01=
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/screens/mega_backpack_01.png");
	private Button btnPrev;
	private Button btnNext;
	public MegaBackpackScreen(MegaBackpackMenu menu,Inventory playerInv,Component title){
		super(menu,playerInv,title);
		this.imageWidth=480;
		this.imageHeight=250;
		this.inventoryLabelY=160;
	}
	@Override
	protected void init(){
		super.init();
		this.btnPrev=this.addRenderableWidget(
				Button.builder(Component.literal("<"),
								btn->PacketDistributor.sendToServer(new MegaBackpackPagePacket(-1)))
						.bounds(leftPos+116,topPos+146,20,20)
						.build()
		);
		this.btnNext=this.addRenderableWidget(
				Button.builder(Component.literal(">"),
								btn->PacketDistributor.sendToServer(new MegaBackpackPagePacket(1)))
						.bounds(leftPos+146,topPos+146,20,20)
						.build()
		);
	}
	@Override
	public void render(@NotNull GuiGraphics graphics,int mouseX,int mouseY,float partialTicks){
		this.renderBackground(graphics,mouseX,mouseY,partialTicks);
		super.render(graphics,mouseX,mouseY,partialTicks);
		this.renderTooltip(graphics,mouseX,mouseY);
	}
	@Override
	protected void renderBg(GuiGraphics graphics,float partialTicks,int mouseX,int mouseY){
		// Pozadí (dvě 256x256 textury vedle sebe)
		graphics.blit(BACKGROUND00,leftPos,topPos,0,0,256,256);
		graphics.blit(BACKGROUND01,leftPos+256,topPos,0,0,256,256);
		// -------------------------------------------------------------------------
		// Vykreslení hráčského modelu
		// V NeoForge 1.21.1 se signatua renderEntityInInventoryFollowsMouse změnila:
		// renderEntityInInventoryFollowsMouse(GuiGraphics, int x1, int y1, int x2, int y2,
		//     int scale, float yOffset, float mouseX, float mouseY, LivingEntity entity)
		// -------------------------------------------------------------------------
		assert this.minecraft!=null;
		assert this.minecraft.player!=null;
		int playerX=leftPos+47;
		int playerY=topPos+142;
		int size=30;
		InventoryScreen.renderEntityInInventoryFollowsMouse(
				graphics,
				playerX-size,      // x1 (levý okraj oblasti)
				playerY-size*2,  // y1 (horní okraj oblasti)
				playerX+size,      // x2 (pravý okraj oblasti)
				playerY,             // y2 (spodní okraj oblasti)
				size,                // scale
				0.0F,                // yOffset
				mouseX,              // mouseX pro sledování pohledu
				mouseY,              // mouseY pro sledování pohledu
				this.minecraft.player
		);
		// Sloty batohu (dynamicky vykreslené z vanilla textury)
		for(int i=0;i<13;i++){
			for(int j=0;j<17;j++){
				graphics.blit(SLOT,leftPos+170+j*18,topPos+12+i*18,7,17,18,18);
			}
		}
	}
	@Override
	protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY){
		// Název kontejneru
		graphics.drawString(this.font,this.title,this.titleLabelX,this.titleLabelY,4210752,false);
		// Titulek inventáře hráče
		graphics.drawString(this.font,this.playerInventoryTitle,this.inventoryLabelX-3,this.inventoryLabelY,4210752,false);
		// Aktuální stránka
		String pageText="Page: "+(this.menu.getCurrentPage()+1)+"/16";
		int posX=this.menu.getCurrentPage()+1>9?110:116;
		graphics.drawString(this.font,pageText,posX,136,4210752,false);
	}
	@Override
	protected void containerTick(){
		super.containerTick();
		int page=this.menu.getCurrentPage();
		if(this.btnPrev!=null) this.btnPrev.active=(page>0);
		if(this.btnNext!=null) this.btnNext.active=(page<15);
	}
}