package cz.maxtechnik.dif.init.basic;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.DifModCommonConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
public class DifModTabs{
	public static final DeferredRegister<CreativeModeTab>REGISTER=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,DifMod.MODID);
	public static final RegistryObject<CreativeModeTab>MAIN=REGISTER.register("main",()->CreativeModeTab.builder().title(Component.translatable("creative_tab.dif.main")).icon(()->new ItemStack(DifModItems.THE_DIFFERENTIAL.get())).displayItems(((parameters,tabData)->{
		ItemStack PORTAL_GUN=new ItemStack(DifModItems.PORTAL_GUN.get());PORTAL_GUN.getOrCreateTag().putInt("ammo",DifModCommonConfig.portalGunMaxAmmo);

		tabData.accept(DifModItems.THE_DIFFERENTIAL.get());
		tabData.accept(DifModItems.BAN_HAMMER.get());
		tabData.accept(DifModItems.EVENT_BUS.get());
		tabData.accept(DifModItems.HOSPITAL_HANDLE.get());
		tabData.accept(DifModItems.WASHING_MACHINE.get());
		tabData.accept(DifModItems.BURNING_GENERATOR.get());
		tabData.accept(DifModItems.FLUID_HATCH.get());
		tabData.accept(DifModItems.ANDESITE_LATTICE.get());
		tabData.accept(DifModItems.ANDESITE_WINDOW.get());

		tabData.accept(DifModItems.SUPER_BOX.get());
		tabData.accept(DifModItems.SINGULARITATOR.get());
		tabData.accept(DifModItems.SOLANA_BLOCK.get());
		tabData.accept(DifModItems.BITCOIN_BLOCK.get());
		tabData.accept(DifModItems.SOLAR_PANEL_00.get());
		tabData.accept(DifModItems.SOLAR_PANEL_01.get());
		tabData.accept(DifModItems.SOLAR_PANEL_02.get());
		tabData.accept(DifModItems.SOLAR_PANEL_03.get());
		tabData.accept(DifModItems.SOLAR_PANEL_04.get());

		tabData.accept(DifModItems.MASTICKA.get());
		tabData.accept(DifModItems.ITEM_5261.get());
		tabData.accept(DifModItems.CPU_SINGULARITY.get());
		tabData.accept(DifModItems.HEAVY_PLATE.get());
		tabData.accept(DifModItems.SPRING.get());
		tabData.accept(DifModItems.MITHRIL.get());
		tabData.accept(DifModItems.MITHRIL_PLATE.get());
		tabData.accept(DifModItems.BLUESTONE.get());
		tabData.accept(DifModItems.BLUE_PLATE.get());

		tabData.accept(DifModItems.COIN_00.get());
		tabData.accept(DifModItems.COIN_01.get());
		tabData.accept(DifModItems.COIN_02.get());
		tabData.accept(DifModItems.COIN_03.get());
		tabData.accept(DifModItems.RAM.get());
		tabData.accept(DifModItems.EXPLOSIVE_RAM.get());
		tabData.accept(DifModItems.ROTTEN_BELT.get());
		tabData.accept(DifModItems.ROTTEN_APPLE.get());
		tabData.accept(DifModItems.QUESTION_MARK.get());

		tabData.accept(DifModItems.LASER_HOOKAH.get());
		tabData.accept(PORTAL_GUN);


		tabData.accept(DifModItems.INCOMPLETE_CPU_SINGULARITY.get());
		tabData.accept(DifModItems.INCOMPLETE_MITHRIL_PLATE.get());


	})).build());
	public static void addCreative(BuildCreativeModeTabContentsEvent event){
		if(event.getTabKey().equals(CreativeModeTabs.BUILDING_BLOCKS)){
			event.getEntries().putAfter(new ItemStack(Items.SMOOTH_STONE),new ItemStack(DifModItems.SMOOTH_STONE_DOUBLE_SLAB.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.accept(DifModItems.DEEPSLATED_ARROW.get());
			event.accept(DifModItems.STONED_ARROW.get());
			event.accept(DifModItems.WOODED_ARROW.get());
		}else if(event.getTabKey().equals(CreativeModeTabs.NATURAL_BLOCKS)){
			event.getEntries().putAfter(new ItemStack(Items.SNOW),new ItemStack(DifModItems.MATY_BLOCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(Items.CRYING_OBSIDIAN),new ItemStack(DifModItems.CINDER_FLOUR_BLOCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(Items.AMETHYST_CLUSTER),new ItemStack(DifModItems.ENERGY_BLOCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.ENERGY_BLOCK.get()),new ItemStack(DifModItems.BUDDING_ENERGY.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.BUDDING_ENERGY.get()),new ItemStack(DifModItems.SMALL_ENERGY_BUD.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.SMALL_ENERGY_BUD.get()),new ItemStack(DifModItems.MEDIUM_ENERGY_BUD.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.MEDIUM_ENERGY_BUD.get()),new ItemStack(DifModItems.LARGE_ENERGY_BUD.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.LARGE_ENERGY_BUD.get()),new ItemStack(DifModItems.ENERGY_CLUSTER.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(Items.WARPED_FUNGUS),new ItemStack(DifModItems.SUGAR_MUSHROOM.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(Items.SUGAR_CANE),new ItemStack(DifModItems.MATA_PLANT.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.MATA_PLANT.get()),new ItemStack(DifModItems.CANOLA_PLANT.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.CANOLA_PLANT.get()),new ItemStack(DifModItems.CANOLA_SEEDS.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(Items.BEDROCK),new ItemStack(DifModItems.PEDROCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}else if(event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)){
			event.getEntries().putAfter(new ItemStack(Items.CHEST),new ItemStack(DifModItems.OLD_CHEST.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(Items.BARREL),new ItemStack(DifModItems.ANDESITE_BARREL.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.ANDESITE_BARREL.get()),new ItemStack(DifModItems.COPPER_BARREL.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.COPPER_BARREL.get()),new ItemStack(DifModItems.BRASS_BARREL.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}else if(event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)){
			event.getEntries().putAfter(new ItemStack(Items.NETHERITE_HOE),new ItemStack(DifModItems.ELECTRUM_DESTROYER.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(Items.LAVA_BUCKET),new ItemStack(DifModItems.BEER_BUCKET.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.BEER_BUCKET.get()),new ItemStack(DifModItems.XP_BUCKET.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.accept(DifModItems.CREMEKA.get());
			event.accept(DifModItems.MATY_CREATE.get());
			event.accept(DifModItems.MAYONNAISE.get());
			event.accept(DifModItems.REDSTONE.get());
			event.accept(DifModItems.MATY_PADA_STREAM.get());
			event.accept(DifModItems.FURT_TA_STEJNA_HRA.get());
		}else if(event.getTabKey().equals(CreativeModeTabs.COMBAT)){
			event.getEntries().putAfter(new ItemStack(Items.NETHERITE_SWORD),new ItemStack(DifModItems.WOODEN_KATANA.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.WOODEN_KATANA.get()),new ItemStack(DifModItems.STONE_KATANA.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.STONE_KATANA.get()),new ItemStack(DifModItems.IRON_KATANA.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.IRON_KATANA.get()),new ItemStack(DifModItems.GOLDEN_KATANA.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.GOLDEN_KATANA.get()),new ItemStack(DifModItems.DIAMOND_KATANA.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.DIAMOND_KATANA.get()),new ItemStack(DifModItems.NETHERITE_KATANA.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.NETHERITE_KATANA.get()),new ItemStack(DifModItems.WOODEN_BATTLE_AXE.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.WOODEN_BATTLE_AXE.get()),new ItemStack(DifModItems.STONE_BATTLE_AXE.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.STONE_BATTLE_AXE.get()),new ItemStack(DifModItems.IRON_BATTLE_AXE.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.IRON_BATTLE_AXE.get()),new ItemStack(DifModItems.GOLDEN_BATTLE_AXE.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.GOLDEN_BATTLE_AXE.get()),new ItemStack(DifModItems.DIAMOND_BATTLE_AXE.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.DIAMOND_BATTLE_AXE.get()),new ItemStack(DifModItems.NETHERITE_BATTLE_AXE.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}else if(event.getTabKey().equals(CreativeModeTabs.FOOD_AND_DRINKS)){
			event.getEntries().putAfter(new ItemStack(Items.RABBIT_STEW),new ItemStack(DifModItems.CREATE_CAN.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.CREATE_CAN.get()),new ItemStack(DifModItems.CREATE_BOWL.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.CREATE_BOWL.get()),new ItemStack(DifModItems.SUPER_HEATED_CREATE_BOWL.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(Items.SWEET_BERRIES),new ItemStack(DifModItems.MATA.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.MATA.get()),new ItemStack(DifModItems.CHERRY.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(Items.MILK_BUCKET),new ItemStack(DifModItems.BEER.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.BEER.get()),new ItemStack(DifModItems.MATY_DRINK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.MATY_DRINK.get()),new ItemStack(DifModItems.FERNET.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.FERNET.get()),new ItemStack(DifModItems.WINE.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.WINE.get()),new ItemStack(DifModItems.CHERRY_BOTTLE.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.getEntries().putAfter(new ItemStack(DifModItems.CHERRY.get()),new ItemStack(DifModItems.NETHER_WART_BOTTLE.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			event.accept(DifModItems.BOTTLE_OF_MOLOTOVUV_KOKTEJL.get());
			event.accept(DifModItems.BOTTLE_OF_URANOVEJ_KOKTEJL.get());
		}else if(event.getTabKey().equals(CreativeModeTabs.INGREDIENTS)){
			event.getEntries().putAfter(new ItemStack(Items.AMETHYST_SHARD),new ItemStack(DifModItems.ENERGY_SHARD.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}else if(event.getTabKey().equals(CreativeModeTabs.OP_BLOCKS)){
			event.accept(DifModItems.EXAMPLE_ITEM.get());
			event.accept(DifModItems.EXAMPLE_BLOCK.get());
			event.accept(DifModItems.XP_STORAGE.get());
			event.accept(DifModItems.END_PORTAL.get());
			event.accept(DifModItems.END_GATEWAY.get());
			event.accept(DifModItems.NETHER_PORTAL.get());
			event.accept(DifModItems.WATER.get());
			event.accept(DifModItems.LAVA.get());
			event.accept(DifModItems.FIRE.get());
		}
	}
}
