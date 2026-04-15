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
import org.jetbrains.annotations.NotNull;
public class MegaBackpackScreen extends AbstractContainerScreen<MegaBackpackMenu>{
	private static final ResourceLocation SLOT=ResourceLocation.fromNamespaceAndPath("minecraft","textures/gui/container/generic_54.png");
	private static final ResourceLocation BACKGROUND00=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/screens/mega_backpack_00.png");
	private static final ResourceLocation BACKGROUND01=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/screens/mega_backpack_01.png");
	private Button btnPrev;
	private Button btnNext;
	public MegaBackpackScreen(MegaBackpackMenu menu,Inventory playerInv,Component title){
		super(menu,playerInv,title);
		// Rozměry celého okna (musí být větší než 176x166)
		this.imageWidth=480;
		this.imageHeight=250;
		this.inventoryLabelY=160;
	}
	@Override
	protected void init(){
		super.init();
		this.btnPrev=this.addRenderableWidget(Button.builder(Component.literal("<"),(btn)->DifMod.PACKET_HANDLER.sendToServer(new MegaBackpackPagePacket(-1))).bounds(leftPos+116,topPos+146,20,20).build());
		this.btnNext=this.addRenderableWidget(Button.builder(Component.literal(">"),(btn)->DifMod.PACKET_HANDLER.sendToServer(new MegaBackpackPagePacket(1))).bounds(leftPos+146,topPos+146,20,20).build());
	}
	@Override
	public void render(@NotNull GuiGraphics graphics,int mouseX,int mouseY,float partialTicks){
		this.renderBackground(graphics);
		super.render(graphics,mouseX,mouseY,partialTicks);
		this.renderTooltip(graphics,mouseX,mouseY);
	}
	@Override
	protected void renderBg(GuiGraphics graphics,float partialTicks,int mouseX,int mouseY){
		// Vykreslení hlavního šedého pozadí (můžeš použít vanilla texturu a roztáhnout ji)
		// Horní část (sloty batohu)
		graphics.blit(BACKGROUND00,leftPos,topPos,0,0,256,256);
		graphics.blit(BACKGROUND01,leftPos+256,topPos,0,0,256,256);
		int playerX=leftPos+47;  // X pozice středu postavy
		int playerY=topPos+142; // Y pozice nohou postavy
		int size=30;              // Velikost (vanilla používá cca 30)
		// Tato metoda se postará o všechno: 3D model, rotaci za myší i osvětlení
		assert this.minecraft!=null;
		assert this.minecraft.player!=null;
		InventoryScreen.renderEntityInInventoryFollowsMouse(
				graphics,
				playerX,
				playerY,
				size,
				(float)(playerX-mouseX), // Sledování myši na ose X
				(float)(playerY-50-mouseY), // Sledování myši na ose Y
				this.minecraft.player // Entita, která se má vykreslit
		);
		// Pokud chceš vanillu, musíš kreslit sloty v cyklu nebo mít vlastní 256x256 texturu
		// Tady kreslíme sloty dynamicky (bílé čtverečky z textury truhly)
		for(int i=0;i<13;i++){
			for(int j=0;j<17;j++){
				graphics.blit(SLOT,leftPos+170+j*18,topPos+12+i*18,7,17,18,18);
			}
		}
	}
	@Override
	protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY){
		// Vykreslí název kontejneru (Mega Backpack) na standardní pozici
		graphics.drawString(this.font,this.title,this.titleLabelX,this.titleLabelY,4210752,false);
		// Vykreslí titulek inventáře hráče (u spodních slotů)
		graphics.drawString(this.font,this.playerInventoryTitle,this.inventoryLabelX-3,this.inventoryLabelY,4210752,false);
		// --- Zobrazení aktuální stránky ---
		// Stránky indexujeme od 0, takže pro lidi zobrazíme +1
		String pageText="Page: "+(this.menu.getCurrentPage()+1)+"/16";
		// Výpočet pozice: zhruba doprostřed mezi tlačítka < a >
		// 4210752 je barva klasického tmavě šedého textu v Minecraftu
		int posX=this.menu.getCurrentPage()+1>9?110:116;
		graphics.drawString(this.font,pageText,posX,136,4210752,false);
	}
	@Override
	protected void containerTick(){
		super.containerTick();
		// Získáme aktuální stránku z Menu
		int page=this.menu.getCurrentPage();
		// Logika pro zneaktivnění
		if(this.btnPrev!=null){
			this.btnPrev.active=(page>0); // Aktivní jen když nejsme na nule
		}
		if(this.btnNext!=null){
			this.btnNext.active=(page<15); // Aktivní jen když nejsme na patnáctce
		}
	}
}