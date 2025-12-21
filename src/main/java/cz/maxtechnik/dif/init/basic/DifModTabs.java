package cz.maxtechnik.dif.init.basic;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.DifModConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class DifModTabs{
    public static final DeferredRegister<CreativeModeTab>REGISTER=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,DifMod.MODID);
    public static final RegistryObject<CreativeModeTab>MAIN=REGISTER.register("main",()->CreativeModeTab.builder().title(Component.translatable("creative_tab.dif.main")).icon(()->new ItemStack(DifModItems.THE_DIFFERENTIAL.get())).displayItems(((parameters,tabData)->{
		tabData.accept(DifModItems.THE_DIFFERENTIAL.get());
		tabData.accept(DifModItems.PEDROCK.get());
		tabData.accept(DifModItems.CINDER_FLOUR_BLOCK.get());
		tabData.accept(DifModItems.DEEPSLATED_ARROW.get());
		tabData.accept(DifModItems.STONED_ARROW.get());
		tabData.accept(DifModItems.WOODED_ARROW.get());
		tabData.accept(DifModItems.ANDESITE_LATTICE.get());
		tabData.accept(DifModItems.ANDESITE_WINDOW.get());
		tabData.accept(DifModItems.BAN_HAMMER.get());
		tabData.accept(DifModItems.CPU_SINGULARITY.get());
		tabData.accept(DifModItems.MITHRIL.get());
		tabData.accept(DifModItems.MITHRIL_PLATE.get());
		tabData.accept(DifModItems.HEAVY_PLATE.get());
		tabData.accept(DifModItems.TRESNOVICE.get());
		tabData.accept(DifModItems.CHERRY.get());
        tabData.accept(DifModItems.FERNET.get());
		tabData.accept(DifModItems.BEER.get());
		tabData.accept(DifModItems.CREATE_CAN.get());
		tabData.accept(DifModItems.CREATE_BOWL.get());
		tabData.accept(DifModItems.SUPER_HEATED_CREATE_BOWL.get());
		tabData.accept(DifModItems.MATY_DRINK.get());
		tabData.accept(DifModItems.MATA.get());
		tabData.accept(DifModItems.MATA_PLANT.get());
		tabData.accept(DifModItems.MATY_BLOCK.get());
		tabData.accept(DifModItems.CANOLA_PLANT.get());
		tabData.accept(DifModItems.CANOLA_SEEDS.get());
		tabData.accept(DifModItems.BOTTLE_OF_MOLOTOVUV_KOKTEJL.get());
		tabData.accept(DifModItems.BOTTLE_OF_URANOVEJ_KOKTEJL.get());
		tabData.accept(DifModItems.SOLAR_PANEL_00.get());
		tabData.accept(DifModItems.SOLAR_PANEL_01.get());
		tabData.accept(DifModItems.SOLAR_PANEL_02.get());
		tabData.accept(DifModItems.SOLAR_PANEL_03.get());
		tabData.accept(DifModItems.SOLAR_PANEL_04.get());
		tabData.accept(DifModItems.COIN_00.get());
		tabData.accept(DifModItems.COIN_01.get());
		tabData.accept(DifModItems.COIN_02.get());
		tabData.accept(DifModItems.COIN_03.get());
		tabData.accept(DifModItems.ENERGY_BLOCK.get());
		tabData.accept(DifModItems.BUDDING_ENERGY.get());
		tabData.accept(DifModItems.ENERGY_CLUSTER.get());
		tabData.accept(DifModItems.LARGE_ENERGY_BUD.get());
		tabData.accept(DifModItems.MEDIUM_ENERGY_BUD.get());
		tabData.accept(DifModItems.SMALL_ENERGY_BUD.get());
		tabData.accept(DifModItems.ENERGY_SHARD.get());
		tabData.accept(DifModItems.ANDESITE_BARREL.get());
		tabData.accept(DifModItems.COPPER_BARREL.get());
		tabData.accept(DifModItems.BRASS_BARREL.get());
    })).build());
    public static final RegistryObject<CreativeModeTab>MUSIC=REGISTER.register("music",()->CreativeModeTab.builder().title(Component.translatable("creative_tab.dif.music")).icon(()->new ItemStack(DifModItems.REDSTONE.get())).withTabsBefore(MAIN.getKey()).displayItems(((parameters,tabData)->{
		tabData.accept(DifModItems.CREMEKA.get());
		tabData.accept(DifModItems.MATY_CREATE.get());
		tabData.accept(DifModItems.MAYONNAISE.get());
		tabData.accept(DifModItems.REDSTONE.get());
		tabData.accept(DifModItems.MATY_PADA_STREAM.get());
		tabData.accept(DifModItems.FURT_TA_STEJNA_HRA.get());
    })).build());
    public static final RegistryObject<CreativeModeTab>RANDOM=REGISTER.register("random",()->CreativeModeTab.builder().title(Component.translatable("creative_tab.dif.random")).icon(()->new ItemStack(DifModItems.QUESTION_MARK.get())).withTabsBefore(MUSIC.getKey()).displayItems(((parameters,tabData)->{
		tabData.accept(DifModItems.QUESTION_MARK.get());
		tabData.accept(DifModItems.EVENT_BUS.get());
		tabData.accept(DifModItems.WASHING_MACHINE.get());
		tabData.accept(DifModItems.BURNING_GENERATOR.get());
		tabData.accept(DifModItems.HOSPITAL_HANDLE.get());
		tabData.accept(DifModItems.SINGULARITATOR.get());
		tabData.accept(DifModItems.SOLANA_BLOCK.get());
		tabData.accept(DifModItems.BITCOIN_BLOCK.get());
		tabData.accept(DifModItems.SUPER_BOX.get());
		tabData.accept(DifModItems.OLD_CHEST.get());

		tabData.accept(DifModItems.MASTICKA.get());
		tabData.accept(DifModItems.BLUESTONE.get());
		tabData.accept(DifModItems.BLUE_PLATE.get());
		tabData.accept(DifModItems.SPRING.get());

		tabData.accept(DifModItems.ITEM_5261.get());
        tabData.accept(DifModItems.ROTTEN_BELT.get());
        tabData.accept(DifModItems.ROTTEN_APPLE.get());

		tabData.accept(DifModItems.RAM.get());
		tabData.accept(DifModItems.EXPLOSIVE_RAM.get());

		tabData.accept(DifModItems.LASER_HOOKAH.get());
		ItemStack portalGun=new ItemStack(DifModItems.PORTAL_GUN.get());
		portalGun.getOrCreateTag().putInt("ammo",DifModConfig.portalGunMaxAmmo);
		tabData.accept(portalGun);

        tabData.accept(DifModItems.INCOMPLETE_CPU_SINGULARITY.get());
		tabData.accept(DifModItems.INCOMPLETE_MITHRIL_PLATE.get());
    })).build());
	public static void addCreative(BuildCreativeModeTabContentsEvent event){
		if(event.getTabKey().equals(CreativeModeTabs.COMBAT)){
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
		}else if(event.getTabKey().equals(CreativeModeTabs.OP_BLOCKS)){
			event.accept(DifModItems.EXAMPLE_ITEM.get());
			event.accept(DifModItems.EXAMPLE_BLOCK.get());
			event.accept(DifModItems.END_PORTAL.get());
			event.accept(DifModItems.END_GATEWAY.get());
			event.accept(DifModItems.NETHER_PORTAL.get());
			event.accept(DifModItems.WATER.get());
			event.accept(DifModItems.LAVA.get());
			event.accept(DifModItems.FIRE.get());
		}else if(event.getTabKey().equals(CreativeModeTabs.BUILDING_BLOCKS)){
			event.getEntries().putAfter(new ItemStack(Items.SMOOTH_STONE),new ItemStack(DifModItems.SMOOTH_STONE_DOUBLE_SLAB.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}
	}
}
