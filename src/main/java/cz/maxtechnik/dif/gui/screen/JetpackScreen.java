package cz.maxtechnik.dif.gui.screen;

import cz.maxtechnik.dif.config.DifModClientConfig;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.item.armor.Jetpack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
@EventBusSubscriber(Dist.CLIENT)
public class JetpackScreen{
	@SubscribeEvent(priority=EventPriority.NORMAL)
	public static void eventHandler(RenderGuiEvent.Pre event){
		Player player=Minecraft.getInstance().player;
		if(player==null) return;
		ItemStack jetpack=player.getInventory().armor.get(2);
		if(jetpack.getItem().equals(DifModItems.JETPACK.get())){
			int width=event.getGuiGraphics().guiWidth();
			int height=event.getGuiGraphics().guiHeight();
			Font font=Minecraft.getInstance().font;
			GuiGraphics gui=event.getGuiGraphics();
			int mode;
			if(Jetpack.Chestplate.isOff(jetpack)) mode=0;
			else mode=Jetpack.Chestplate.isHovering(jetpack)?2:1;
			Component modeText=switch(mode){
				case 0 -> Component.translatable("info.dif.jetpack.off");
				case 1 -> Component.translatable("info.dif.jetpack.thrust");
				default -> Component.translatable("info.dif.jetpack.hover");
			};
			ChatFormatting color=switch(mode){
				case 0 -> ChatFormatting.GRAY;
				case 1 -> ChatFormatting.AQUA;
				default -> ChatFormatting.GREEN;
			};
			int fuel=Jetpack.Chestplate.getThrust(jetpack);
			int max=Jetpack.Chestplate.getMax();
			int percent=max>0?(fuel*100)/max:0;
			Component text=Component.translatable("info.dif.jetpack.info_panel",modeText,fuel,percent).withStyle(color);
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
