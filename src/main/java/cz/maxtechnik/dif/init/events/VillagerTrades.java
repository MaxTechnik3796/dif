package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=DifMod.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class VillagerTrades{
	public static ItemStack emerald(int count){
		return new ItemStack(Items.EMERALD,count);
	}
	@SubscribeEvent
	public static void registerTrades(VillagerTradesEvent event){
		/*if(event.getType().equals(VillagerProfession.CARTOGRAPHER)){
			//event.getTrades().get(3).add(new TrialsMapTrade(12,12,5));
		}*/
	}
	@SubscribeEvent
	public static void registerWanderingTrades(WandererTradesEvent event){
		event.getGenericTrades().add(new BasicItemListing(emerald(5),new ItemStack(DifModItems.MATY_CREATE.get(),1),2,0,0F));
		event.getGenericTrades().add(new BasicItemListing(emerald(5),new ItemStack(DifModItems.DOG.get(),1),2,0,0F));
	}
}
