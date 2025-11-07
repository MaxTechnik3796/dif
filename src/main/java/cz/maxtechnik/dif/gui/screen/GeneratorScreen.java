package cz.maxtechnik.dif.gui.screen;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;

import java.util.HashMap;
import java.util.Objects;

import cz.maxtechnik.dif.gui.menu.GeneratorMenu;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
	private final static HashMap<String, Object> guistate = GeneratorMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;

	public GeneratorScreen(GeneratorMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath("dif","textures/screens/generator.png");
	private static final ResourceLocation WIDGETS_TEXTURE = ResourceLocation.fromNamespaceAndPath("dif","textures/screens/widgets.png");

	@Override
	public void render(@NotNull GuiGraphics guiGraphics,int mouseX,int mouseY,float partialTicks) {
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		guiGraphics.blit(GUI_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
		int burnTime = this.menu.getBurnTime();
		int maxBurnTime = this.menu.getCurrentFuelItemBurnTime();
		if (burnTime > 0 && maxBurnTime > 0){
			int scaledFlameHeight=(int)(14*(burnTime/(double)maxBurnTime));
			int flameSourceY=32+(14-scaledFlameHeight);

			int flameX=this.leftPos+51;
			int flameY=this.topPos+23+(14-scaledFlameHeight);

			guiGraphics.blit(WIDGETS_TEXTURE,flameX,flameY,32,flameSourceY,14,scaledFlameHeight,128,128);
		}





		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			assert Objects.requireNonNull(this.minecraft).player!=null;
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font,Component.translatable("gui.dif.generator"),8,6,-12829636,false);
		guiGraphics.drawString(this.font,Component.translatable("gui.dif.inventory"),8,73,-12829636,false);
	}


	@Override
	public void init() {
		super.init();
	}
}
