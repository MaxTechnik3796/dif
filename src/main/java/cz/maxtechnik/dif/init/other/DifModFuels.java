package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import static cz.maxtechnik.dif.init.basic.DifModItems.*;
import static java.lang.Math.round;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.GAME)
public class DifModFuels{
	@SubscribeEvent
	public static void furnaceFuelBurnTimeEvent(FurnaceFuelBurnTimeEvent e){
		add(e,Items.PAPER,0.4F);
		add(e,COKE,16F);
	}


	private static void add(FurnaceFuelBurnTimeEvent event,DeferredItem<Item> item,float burnItems){
		add(event,item.get(),burnItems*200);
	}
	private static void add(FurnaceFuelBurnTimeEvent event,Item item,float burnItems){
		if(event.getItemStack().getItem().equals(item)) event.setBurnTime(round(burnItems*200));
	}
}
