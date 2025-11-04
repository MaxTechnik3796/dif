package cz.maxtechnik.dif;

import com.mojang.logging.LogUtils;
import cz.maxtechnik.dif.init.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(DifMod.MODID)
public class DifMod{
	public static final String MODID="dif";
	public static final Logger LOGGER=LogUtils.getLogger();
	public DifMod(){
		IEventBus bus=FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::commonSetup);


		DifModBlocks.REGISTRY.register(bus);
		DifModItems.REGISTRY.register(bus);
		DifModTabs.REGISTER.register(bus);
		DifModSounds.REGISTRY.register(bus);
		DifModMobEffects.REGISTRY.register(bus);
		DifModBlockEntities.REGISTRY.register(bus);
		DifModMenus.REGISTRY.register(bus);

		MinecraftForge.EVENT_BUS.register(this);
		bus.addListener(this::addCreative);


		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,DifModConfig.SPEC);
	}

	private void commonSetup(final FMLCommonSetupEvent event){
		LOGGER.info("HELLO FROM COMMON SETUP");
		//LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

		//if (DifModConfig.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

		//LOGGER.info(DifModConfig.magicNumberIntroduction + DifModConfig.magicNumber);

		//DifModConfig.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
	}

	private void addCreative(BuildCreativeModeTabContentsEvent event) {
		//if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) event.accept(EXAMPLE_BLOCK_ITEM);
	}
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event){
		LOGGER.info("HELLO from server starting");
	}

	@Mod.EventBusSubscriber(modid=MODID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
	public static class ClientModEvents {

		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event){
			LOGGER.info("HELLO FROM CLIENT SETUP");
			LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
		}
	}
	public static boolean rouletteBoolean(int range){
		return 0==Mth.nextInt(RandomSource.create(),0,range);
	}
}
