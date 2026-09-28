package cz.maxtechnik.dif.gui.screen;

import cz.maxtechnik.dif.config.DifModClientConfig;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.item.armor.Jetpack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
@EventBusSubscriber({Dist.CLIENT})
public class JetpackScreen{
	@SubscribeEvent(priority=EventPriority.NORMAL)
	public static void eventHandler(RenderGuiEvent.Pre event){
		Player player=Minecraft.getInstance().player;
		if(player==null) return;
		if(player.getInventory().armor.get(2).getItem().equals(DifModItems.JETPACK.get())){
			int width=event.getGuiGraphics().guiWidth();
			int height=event.getGuiGraphics().guiHeight();
			Font font=Minecraft.getInstance().font;
			GuiGraphics gui=event.getGuiGraphics();
			int thrust=Jetpack.Chestplate.getThrust(player.getInventory().armor.get(2));
			int max=Jetpack.Chestplate.getMax();
			String text="Tento text bude pak tamto ale zatim ne XDDD";
			int x=2;
			int y=2;
			switch(DifModClientConfig.JETPACK_OVERLAYER_POS.get()){
				case BOTTOM_LEFT -> y=height-12;
				case TOP_RIGHT -> x=width-font.width(text)-2;
				case BOTTOM_RIGHT -> {
					x=width-font.width(text)-2;
					y=height-12;
				}
				default -> {
				}
			}
			gui.drawString(font,text,x,y,0xFFFFFF);
		}
	}
}
