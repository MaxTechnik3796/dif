package cz.maxtechnik.dif.init.misc;

import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DifModCompostableItems{
	@SubscribeEvent
	public static void addComposterItems(FMLCommonSetupEvent event){
		ComposterBlock.COMPOSTABLES.put(Blocks.BAMBOO.asItem(),0.4F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.MATA.get(),0.9F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.MATA_PLANT.get(),0.88F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.MATY_BLOCK.get(),1F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.CANOLA_PLANT.get(),1F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.CANOLA_SEEDS.get(),1F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.CHERRY.get(),0.65F);
	}
}
