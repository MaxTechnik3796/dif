package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;
@SuppressWarnings("removal")
@EventBusSubscriber(modid = DifMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class VillagerTrades{
	public static ItemStack emerald(int count){
		return new ItemStack(Items.EMERALD,count);
	}
	@SubscribeEvent
	public static void registerTrades(VillagerTradesEvent event){
	}
	@SubscribeEvent
	public static void registerWanderingTrades(WandererTradesEvent event){
		event.getGenericTrades().add(new BasicItemListing(emerald(5),new ItemStack(DifModItems.MATY_CREATE.get(),1),2,0,0F));
		event.getGenericTrades().add(new BasicItemListing(emerald(5),new ItemStack(DifModItems.DOG.get(),1),2,0,0F));
	}
}
