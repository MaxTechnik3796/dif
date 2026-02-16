package cz.maxtechnik.dif.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.DifModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
@Mod.EventBusSubscriber(modid=DifMod.MODID,bus=Mod.EventBusSubscriber.Bus.FORGE)
public class ComposterFurnaceInjector{
	@SubscribeEvent
	public static void addComposterItems(FMLCommonSetupEvent event){
		ComposterBlock.COMPOSTABLES.put(Blocks.BAMBOO.asItem(),0.4F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.MATA.get(),0.9F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.MATA_PLANT.get(),0.8F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.MATY_BLOCK.get(),1F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.CANOLA_PLANT.get(),1F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.CANOLA_SEEDS.get(),1F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.CHERRY.get(),0.6F);
	}
	@SubscribeEvent
	public static void furnaceFuelBurnTimeEvent(FurnaceFuelBurnTimeEvent event) {
		ItemStack itemstack=event.getItemStack();
		if(itemstack.getItem().equals(Items.PAPER))event.setBurnTime(5);
	}
}
