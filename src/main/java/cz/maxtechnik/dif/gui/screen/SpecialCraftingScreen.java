package cz.maxtechnik.dif.gui.screen;

import cz.maxtechnik.dif.gui.menu.SpecialCraftingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
@OnlyIn(Dist.CLIENT)
/**
 Out of service (Unfinished)
 **/
public class SpecialCraftingScreen extends AbstractContainerScreen<SpecialCraftingMenu> implements RecipeUpdateListener{
	public SpecialCraftingScreen(SpecialCraftingMenu menu,Inventory inventory,Component text){
		super(menu,inventory,text);
	}
	private static final ResourceLocation GUI_TEXTURE=ResourceLocation.parse("minecraft:textures/gui/container/crafting_table.png");
	private final RecipeBookComponent recipeBookComponent=new RecipeBookComponent();
	public void render(@NotNull GuiGraphics guiGraphics,int mouseX,int mouseY,float partialTicks){
		this.renderBackground(guiGraphics,mouseX,mouseY,partialTicks);
		this.renderBg(guiGraphics,partialTicks,mouseX,mouseY);
		super.render(guiGraphics,mouseX,mouseY,partialTicks);
		this.renderTooltip(guiGraphics,mouseX,mouseY);
	}
	protected void renderBg(GuiGraphics p_283540_,float p_282132_,int p_283078_,int p_283647_){
		int i=this.leftPos;
		int j=(this.height-this.imageHeight)/2;
		p_283540_.blit(GUI_TEXTURE,i,j,0,0,this.imageWidth,this.imageHeight);
	}
	public void containerTick(){
		super.containerTick();
	}
	public void recipesUpdated(){
	}
	public @NotNull RecipeBookComponent getRecipeBookComponent(){
		return this.recipeBookComponent;
	}
	protected void init(){
		super.init();
		this.titleLabelX=29;
	}
}