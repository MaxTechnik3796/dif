package cz.maxtechnik.dif.init.auto_loader;

import cz.maxtechnik.dif.init.DifModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class DifModTrades{
	@SubscribeEvent
	public static void registerWanderingTrades(WandererTradesEvent event){
		event.getGenericTrades().add(new BasicItemListing(new ItemStack(Items.EMERALD,5),new ItemStack(DifModItems.CLAIRDELUNE.get(),1),3,0,0F));
		event.getGenericTrades().add(new BasicItemListing(new ItemStack(Items.EMERALD,5),new ItemStack(DifModItems.CREMEKA.get(),1),3,0,0F));
		event.getGenericTrades().add(new BasicItemListing(new ItemStack(Items.EMERALD,5),new ItemStack(DifModItems.FURT_TA_STEJNA_HRA.get(),1),3,0,0F));
		event.getGenericTrades().add(new BasicItemListing(new ItemStack(Items.EMERALD,5),new ItemStack(DifModItems.MATY_CREATE.get(),1),3,0,0F));
		event.getGenericTrades().add(new BasicItemListing(new ItemStack(Items.EMERALD,5),new ItemStack(DifModItems.MATY_PADA_STREAM.get(),1),3,0,0F));
		event.getGenericTrades().add(new BasicItemListing(new ItemStack(Items.EMERALD,5),new ItemStack(DifModItems.MAYONNAISE.get(),1),3,0,0F));
		event.getGenericTrades().add(new BasicItemListing(new ItemStack(Items.EMERALD,5),new ItemStack(DifModItems.REDSTONE.get(),1),3,0,0F));
	}
}
