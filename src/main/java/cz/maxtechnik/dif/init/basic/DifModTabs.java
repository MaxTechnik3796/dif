package cz.maxtechnik.dif.init.basic;

import com.simibubi.create.AllCreativeModeTabs;
import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.registry.ModCreativeTabs;
import vectorwing.farmersdelight.common.registry.ModItems;
import java.util.Objects;
import static cz.maxtechnik.dif.DifMod.addItemStacksBehind;
import static cz.maxtechnik.dif.item.modular.ModularBase.*;
public class DifModTabs{
	public static final DeferredRegister<CreativeModeTab>REGISTER=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,DifMod.MODID);
	public static final RegistryObject<CreativeModeTab>MAIN=REGISTER.register("main",()->CreativeModeTab.builder().title(Component.translatable("creative_tab.dif.main")).icon(()->new ItemStack(DifModItems.THE_DIFFERENTIAL.get())).displayItems(((parameters,tabData)->{
		tabData.accept(DifModItems.THE_DIFFERENTIAL.get());
		tabData.accept(DifModItems.MEGA_TORCH.get());
		tabData.accept(DifModItems.BAN_HAMMER.get());
		tabData.accept(DifModItems.PORTAL_GUN.get());
		tabData.accept(DifModItems.CHUNK_LOADER_1X1.get());
		tabData.accept(DifModItems.CHUNK_LOADER_3X3.get());
		tabData.accept(DifModItems.EVENT_BUS.get());
		tabData.accept(DifModItems.VENT.get());
		tabData.accept(DifModItems.HOSPITAL_HANDLE.get());
		tabData.accept(DifModItems.WASHING_MACHINE.get());
		tabData.accept(DifModItems.AIR_CONDITIONING.get());
		tabData.accept(DifModItems.BURNING_GENERATOR.get());
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
		tabData.accept(DifModItems.SOLAR_PANEL_00_W.get());
		tabData.accept(DifModItems.SOLAR_PANEL_01_W.get());
		tabData.accept(DifModItems.SOLAR_PANEL_02_W.get());
		tabData.accept(DifModItems.SOLAR_PANEL_03_W.get());
		tabData.accept(DifModItems.SOLAR_PANEL_04_W.get());
		tabData.accept(DifModItems.MASTICKA.get());
		tabData.accept(DifModItems.ITEM_5261.get());
		tabData.accept(DifModItems.CPU_SINGULARITY.get());
		tabData.accept(DifModItems.HEAVY_PLATE.get());
		tabData.accept(DifModItems.SPRING.get());
		tabData.accept(DifModItems.SOLDERING_IRON.get());
		tabData.accept(DifModItems.DRILL.get());
		tabData.accept(DifModItems.SCREWDRIVER.get());
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


		tabData.accept(DifModItems.INCOMPLETE_CPU_SINGULARITY.get());
		tabData.accept(DifModItems.INCOMPLETE_MITHRIL_PLATE.get());
		tabData.accept(DifModItems.SOLAR_PANEL_INC.get());

		tabData.accept(DifModItems.INCOMPLETE_UNIVERSAL.get());
	})).build());
	public static final RegistryObject<CreativeModeTab>SPACE=REGISTER.register("space",()->CreativeModeTab.builder().withTabsBefore(DifModTabs.MAIN.getKey()).title(Component.translatable("creative_tab.dif.space")).icon(()->new ItemStack(DifModItems.SPACESHIP.get())).displayItems(((parameters,tabData)->{
		tabData.accept(DifModItems.SPACESHIP.get());
		tabData.accept(DifModItems.SPACE_ENGINE.get());
		tabData.accept(DifModItems.SPACE_SCAFFOLDING.get());

		tabData.accept(DifModItems.ROCKET_FUEL.get());
		tabData.accept(DifModItems.EMPTY_ROCKET_FUEL.get());

		tabData.accept(DifModItems.AURORA_CASING.get());
		tabData.accept(DifModItems.AURORA_INGOT.get());

		tabData.accept(DifModItems.SPACE_CASING.get());
		tabData.accept(DifModItems.SPACE_CASING_METAL.get());
		tabData.accept(DifModItems.SPACE_CASING_REINFORCED.get());
		tabData.accept(DifModItems.SPACE_DOOR.get());
		tabData.accept(DifModItems.SPACE_CORRIDOR.get());
		tabData.accept(DifModItems.SPACE_CRATE.get());
		tabData.accept(DifModItems.SOLAR_PANEL_BLOCK.get());

		tabData.accept(DifModItems.SPACE_SUIT_HELMET.get());
		tabData.accept(DifModItems.SPACE_SUIT_CHESTPLATE.get());
		tabData.accept(DifModItems.SPACE_SUIT_LEGGINGS.get());
		tabData.accept(DifModItems.SPACE_SUIT_BOOTS.get());
		tabData.accept(DifModItems.CARBON_SUIT_HELMET.get());
		tabData.accept(DifModItems.CARBON_SUIT_CHESTPLATE.get());
		tabData.accept(DifModItems.CARBON_SUIT_LEGGINGS.get());
		tabData.accept(DifModItems.CARBON_SUIT_BOOTS.get());

		tabData.accept(DifModItems.MOON_STONE.get());
		tabData.accept(DifModItems.MARS_STONE.get());

		tabData.accept(DifModItems.JETPACK.get());
		tabData.accept(DifModItems.JETPACK_FUEL.get());
		tabData.accept(DifModItems.JETPACK_TURBO_FUEL.get());
		tabData.accept(DifModItems.JETPACK_CANISTER.get());

		tabData.accept(DifModItems.ELECTRO_RUNNERS.get());
	})).build());
	public static final RegistryObject<CreativeModeTab>MODULAR_TOOLS=REGISTER.register("modular_tools",()->CreativeModeTab.builder().withTabsBefore(DifModTabs.SPACE.getKey()).title(Component.translatable("creative_tab.dif.modular_tools")).icon(()->newToolFromMaterials(DifModItems.MODULAR_PICKAXE.get(),"Diamond","Gold","Obsidian")).displayItems(((parameters,tabData)->{
		tabData.accept(Items.SMITHING_TABLE);

		tabData.accept(DifModItems.MODULAR_PICKAXE.get());
		tabData.accept(DifModItems.MODULAR_AXE.get());
		tabData.accept(DifModItems.MODULAR_SHOVEL.get());
		tabData.accept(DifModItems.MODULAR_SWORD.get());

		tabData.accept(DifModItems.MODULAR_PART_PICKAXE_HEAD.get());
		tabData.accept(DifModItems.MODULAR_PART_AXE_HEAD.get());
		tabData.accept(DifModItems.MODULAR_PART_SHOVEL_HEAD.get());
		tabData.accept(DifModItems.MODULAR_PART_SWORD_HEAD.get());

		tabData.accept(DifModItems.MODULAR_PART_BINDING.get());
		tabData.accept(DifModItems.MODULAR_PART_SWORD_BINDING.get());

		tabData.accept(DifModItems.MODULAR_PART_HANDLE.get());





		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_PICKAXE_HEAD.get(),"Wood"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_PICKAXE_HEAD.get(),"Stone"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_PICKAXE_HEAD.get(),"Copper"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_PICKAXE_HEAD.get(),"Iron"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_PICKAXE_HEAD.get(),"Gold"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_PICKAXE_HEAD.get(),"Diamond"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_PICKAXE_HEAD.get(),"Obsidian"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_PICKAXE_HEAD.get(),"Netherite"));

		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_AXE_HEAD.get(),"Wood"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_AXE_HEAD.get(),"Stone"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_AXE_HEAD.get(),"Copper"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_AXE_HEAD.get(),"Iron"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_AXE_HEAD.get(),"Gold"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_AXE_HEAD.get(),"Diamond"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_AXE_HEAD.get(),"Obsidian"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_AXE_HEAD.get(),"Netherite"));

		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SHOVEL_HEAD.get(),"Wood"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SHOVEL_HEAD.get(),"Stone"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SHOVEL_HEAD.get(),"Copper"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SHOVEL_HEAD.get(),"Iron"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SHOVEL_HEAD.get(),"Gold"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SHOVEL_HEAD.get(),"Diamond"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SHOVEL_HEAD.get(),"Obsidian"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SHOVEL_HEAD.get(),"Netherite"));

		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SWORD_HEAD.get(),"Wood"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SWORD_HEAD.get(),"Stone"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SWORD_HEAD.get(),"Copper"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SWORD_HEAD.get(),"Iron"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SWORD_HEAD.get(),"Gold"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SWORD_HEAD.get(),"Diamond"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SWORD_HEAD.get(),"Obsidian"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SWORD_HEAD.get(),"Netherite"));

		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_BINDING.get(),"Wood"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_BINDING.get(),"Stone"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_BINDING.get(),"Copper"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_BINDING.get(),"Iron"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_BINDING.get(),"Gold"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_BINDING.get(),"Diamond"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_BINDING.get(),"Obsidian"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_BINDING.get(),"Netherite"));

		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SWORD_BINDING.get(),"Wood"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SWORD_BINDING.get(),"Stone"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SWORD_BINDING.get(),"Copper"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SWORD_BINDING.get(),"Iron"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SWORD_BINDING.get(),"Gold"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SWORD_BINDING.get(),"Diamond"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SWORD_BINDING.get(),"Obsidian"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_SWORD_BINDING.get(),"Netherite"));

		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_HANDLE.get(),"Wood"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_HANDLE.get(),"Stone"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_HANDLE.get(),"Copper"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_HANDLE.get(),"Iron"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_HANDLE.get(),"Gold"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_HANDLE.get(),"Diamond"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_HANDLE.get(),"Obsidian"));
		tabData.accept(newPartFromMaterial(DifModItems.MODULAR_PART_HANDLE.get(),"Netherite"));


		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_PICKAXE.get(),"Wood"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_PICKAXE.get(),"Stone"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_PICKAXE.get(),"Copper"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_PICKAXE.get(),"Iron"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_PICKAXE.get(),"Gold"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_PICKAXE.get(),"Diamond"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_PICKAXE.get(),"Obsidian"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_PICKAXE.get(),"Netherite"));

		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_AXE.get(),"Wood"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_AXE.get(),"Stone"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_AXE.get(),"Copper"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_AXE.get(),"Iron"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_AXE.get(),"Gold"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_AXE.get(),"Diamond"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_AXE.get(),"Obsidian"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_AXE.get(),"Netherite"));

		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_SHOVEL.get(),"Wood"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_SHOVEL.get(),"Stone"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_SHOVEL.get(),"Copper"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_SHOVEL.get(),"Iron"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_SHOVEL.get(),"Gold"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_SHOVEL.get(),"Diamond"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_SHOVEL.get(),"Obsidian"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_SHOVEL.get(),"Netherite"));

		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_SWORD.get(),"Wood"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_SWORD.get(),"Stone"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_SWORD.get(),"Copper"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_SWORD.get(),"Iron"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_SWORD.get(),"Gold"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_SWORD.get(),"Diamond"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_SWORD.get(),"Obsidian"));
		tabData.accept(newSingleMaterialPreFab(DifModItems.MODULAR_SWORD.get(),"Netherite"));
	})).build());


	public static void addCreative(BuildCreativeModeTabContentsEvent tabData){
		if(tabData.getTabKey().equals(CreativeModeTabs.BUILDING_BLOCKS)){
			tabData.getEntries().putAfter(new ItemStack(Items.SMOOTH_STONE),new ItemStack(DifModItems.SMOOTH_STONE_DOUBLE_SLAB.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.getEntries().putAfter(new ItemStack(Items.IRON_BARS),new ItemStack(DifModItems.IRON_BARS_BLOCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.accept(DifModItems.DEEPSLATED_ARROW.get());
			tabData.accept(DifModItems.STONED_ARROW.get());
			tabData.accept(DifModItems.WOODED_ARROW.get());
			tabData.accept(DifModItems.GLITCH_BLOCK.get());

			tabData.accept(DifModItems.C1_COBBLESTONE.get());
			tabData.accept(DifModItems.C2_COBBLESTONE.get());
			tabData.accept(DifModItems.C3_COBBLESTONE.get());
			tabData.accept(DifModItems.C4_COBBLESTONE.get());
			tabData.accept(DifModItems.C5_COBBLESTONE.get());
			tabData.accept(DifModItems.C6_COBBLESTONE.get());
			tabData.accept(DifModItems.C7_COBBLESTONE.get());
			tabData.accept(DifModItems.C8_COBBLESTONE.get());
			tabData.accept(DifModItems.C9_COBBLESTONE.get());

			tabData.accept(DifModItems.C1_DIRT.get());
			tabData.accept(DifModItems.C2_DIRT.get());
			tabData.accept(DifModItems.C3_DIRT.get());
			tabData.accept(DifModItems.C4_DIRT.get());
			tabData.accept(DifModItems.C5_DIRT.get());
			tabData.accept(DifModItems.C6_DIRT.get());
			tabData.accept(DifModItems.C7_DIRT.get());
			tabData.accept(DifModItems.C8_DIRT.get());
			tabData.accept(DifModItems.C9_DIRT.get());

			tabData.accept(DifModItems.C1_GRAVEL.get());
			tabData.accept(DifModItems.C2_GRAVEL.get());
			tabData.accept(DifModItems.C3_GRAVEL.get());
			tabData.accept(DifModItems.C4_GRAVEL.get());
			tabData.accept(DifModItems.C5_GRAVEL.get());
			tabData.accept(DifModItems.C6_GRAVEL.get());
			tabData.accept(DifModItems.C7_GRAVEL.get());
			tabData.accept(DifModItems.C8_GRAVEL.get());
			tabData.accept(DifModItems.C9_GRAVEL.get());

			tabData.accept(DifModItems.C1_DEEPSLATE.get());
			tabData.accept(DifModItems.C2_DEEPSLATE.get());
			tabData.accept(DifModItems.C3_DEEPSLATE.get());
			tabData.accept(DifModItems.C4_DEEPSLATE.get());
			tabData.accept(DifModItems.C5_DEEPSLATE.get());
			tabData.accept(DifModItems.C6_DEEPSLATE.get());
			tabData.accept(DifModItems.C7_DEEPSLATE.get());
			tabData.accept(DifModItems.C8_DEEPSLATE.get());
			tabData.accept(DifModItems.C9_DEEPSLATE.get());

			tabData.accept(DifModItems.C1_COBBLED_DEEPSLATE.get());
			tabData.accept(DifModItems.C2_COBBLED_DEEPSLATE.get());
			tabData.accept(DifModItems.C3_COBBLED_DEEPSLATE.get());
			tabData.accept(DifModItems.C4_COBBLED_DEEPSLATE.get());
			tabData.accept(DifModItems.C5_COBBLED_DEEPSLATE.get());
			tabData.accept(DifModItems.C6_COBBLED_DEEPSLATE.get());
			tabData.accept(DifModItems.C7_COBBLED_DEEPSLATE.get());
			tabData.accept(DifModItems.C8_COBBLED_DEEPSLATE.get());
			tabData.accept(DifModItems.C9_COBBLED_DEEPSLATE.get());

		}else if(tabData.getTabKey().equals(CreativeModeTabs.NATURAL_BLOCKS)){
			tabData.getEntries().putAfter(new ItemStack(Items.BEDROCK),new ItemStack(DifModItems.PEDROCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.getEntries().putAfter(new ItemStack(Items.SNOW),new ItemStack(DifModItems.MATY_BLOCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.getEntries().putAfter(new ItemStack(Items.CRYING_OBSIDIAN),new ItemStack(DifModItems.CINDER_FLOUR_BLOCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.getEntries().putAfter(new ItemStack(Items.WARPED_FUNGUS),new ItemStack(DifModItems.SUGAR_MUSHROOM.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.getEntries().putAfter(new ItemStack(Items.CHEST),new ItemStack(DifModItems.OLD_CHEST.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			addItemStacksBehind(tabData,new ItemStack(Items.AMETHYST_CLUSTER),
					new ItemStack[]{
							new ItemStack(DifModItems.ENERGY_BLOCK.get()),
							new ItemStack(DifModItems.BUDDING_ENERGY.get()),
							new ItemStack(DifModItems.SMALL_ENERGY_BUD.get()),
							new ItemStack(DifModItems.MEDIUM_ENERGY_BUD.get()),
							new ItemStack(DifModItems.LARGE_ENERGY_BUD.get()),
							new ItemStack(DifModItems.ENERGY_CLUSTER.get())
					});
			addItemStacksBehind(tabData,new ItemStack(Items.SUGAR_CANE),
					new ItemStack[]{
							new ItemStack(DifModItems.MATA_PLANT.get()),
							new ItemStack(DifModItems.CANOLA_PLANT.get()),
							new ItemStack(DifModItems.CANOLA_SEEDS.get())
					});
		}else if(tabData.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)){
			addItemStacksBehind(tabData,new ItemStack(Items.BARREL),
					new ItemStack[]{
							new ItemStack(DifModItems.ANDESITE_BARREL.get()),
							new ItemStack(DifModItems.COPPER_BARREL.get()),
							new ItemStack(DifModItems.BRASS_BARREL.get())
					});
		}else if(tabData.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)){
			tabData.getEntries().putBefore(new ItemStack(Items.BUCKET),new ItemStack(DifModItems.ELECTRUM_DESTROYER.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			addItemStacksBehind(tabData,new ItemStack(Items.STONE),
					new ItemStack[]{
							new ItemStack(DifModItems.COPPER_SHOVEL.get()),
							new ItemStack(DifModItems.COPPER_PICKAXE.get()),
							new ItemStack(DifModItems.COPPER_AXE.get()),
							new ItemStack(DifModItems.COPPER_HOE.get())
					});
			addItemStacksBehind(tabData,new ItemStack(Items.LAVA_BUCKET),
					new ItemStack[]{
							new ItemStack(DifModItems.BEER_BUCKET.get()),
							new ItemStack(DifModItems.XP_BUCKET.get()),
							new ItemStack(DifModItems.CIDER_BUCKET.get()),
							new ItemStack(DifModItems.FUEL_BUCKET.get()),
							new ItemStack(DifModItems.JETPACK_FUEL_BUCKET.get()),
							new ItemStack(DifModItems.JETPACK_TURBO_FUEL_BUCKET.get()),
							new ItemStack(DifModItems.SUNFLOWER_OIL_BUCKET.get())
					});
			tabData.accept(DifModItems.CREMEKA.get());
			tabData.accept(DifModItems.MATY_CREATE.get());
			tabData.accept(DifModItems.MAYONNAISE.get());
			tabData.accept(DifModItems.REDSTONE.get());
			tabData.accept(DifModItems.MATY_PADA_STREAM.get());
			tabData.accept(DifModItems.FURT_TA_STEJNA_HRA.get());
		}else if(tabData.getTabKey().equals(CreativeModeTabs.COMBAT)){
			tabData.getEntries().putAfter(new ItemStack(Items.STONE_SWORD),new ItemStack(DifModItems.COPPER_SWORD.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.getEntries().putAfter(new ItemStack(Items.STONE_AXE),new ItemStack(DifModItems.COPPER_AXE.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			addItemStacksBehind(tabData,new ItemStack(Items.NETHERITE_SWORD),
					new ItemStack[]{
							new ItemStack(DifModItems.WOODEN_KATANA.get()),
							new ItemStack(DifModItems.STONE_KATANA.get()),
							new ItemStack(DifModItems.COPPER_KATANA.get()),
							new ItemStack(DifModItems.IRON_KATANA.get()),
							new ItemStack(DifModItems.GOLDEN_KATANA.get()),
							new ItemStack(DifModItems.DIAMOND_KATANA.get()),
							new ItemStack(DifModItems.NETHERITE_KATANA.get()),
							new ItemStack(DifModItems.WOODEN_BATTLE_AXE.get()),
							new ItemStack(DifModItems.STONE_BATTLE_AXE.get()),
							new ItemStack(DifModItems.COPPER_BATTLE_AXE.get()),
							new ItemStack(DifModItems.IRON_BATTLE_AXE.get()),
							new ItemStack(DifModItems.GOLDEN_BATTLE_AXE.get()),
							new ItemStack(DifModItems.DIAMOND_BATTLE_AXE.get()),
							new ItemStack(DifModItems.NETHERITE_BATTLE_AXE.get())
					});
			addItemStacksBehind(tabData,new ItemStack(Items.CHAINMAIL_BOOTS),
					new ItemStack[]{
							new ItemStack(DifModItems.COPPER_HELMET.get()),
							new ItemStack(DifModItems.COPPER_CHESTPLATE.get()),
							new ItemStack(DifModItems.COPPER_LEGGINGS.get()),
							new ItemStack(DifModItems.COPPER_BOOTS.get())
					});
		}else if(tabData.getTabKey().equals(CreativeModeTabs.FOOD_AND_DRINKS)){
			tabData.getEntries().putAfter(new ItemStack(Items.POISONOUS_POTATO),new ItemStack(DifModItems.FRIES.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.getEntries().putAfter(new ItemStack(Items.BREAD),new ItemStack(DifModItems.BURNED_TOAST.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			addItemStacksBehind(tabData,new ItemStack(Items.COOKED_CHICKEN),
					new ItemStack[]{
							new ItemStack(DifModItems.BUCKET_OF_CHICKEN.get()),
							new ItemStack(DifModItems.RIZEK.get())
					});
			addItemStacksBehind(tabData,new ItemStack(Items.SWEET_BERRIES),
					new ItemStack[]{
							new ItemStack(DifModItems.MATA.get()),
							new ItemStack(DifModItems.CHERRY.get()),
							new ItemStack(DifModItems.NETHER_WART_BOTTLE.get())
					});
			addItemStacksBehind(tabData,new ItemStack(Items.COOKED_RABBIT),
					new ItemStack[]{
							new ItemStack(DifModItems.HORSE_MEAT.get()),
							new ItemStack(DifModItems.COOKED_HORSE_MEAT.get())
					});
			addItemStacksBehind(tabData,new ItemStack(Items.RABBIT_STEW),
					new ItemStack[]{
							new ItemStack(DifModItems.CREATE_CAN.get()),
							new ItemStack(DifModItems.CREATE_BOWL.get()),
							new ItemStack(DifModItems.SUPER_HEATED_CREATE_BOWL.get())
					});
			addItemStacksBehind(tabData,new ItemStack(Items.MILK_BUCKET),
					new ItemStack[]{
							new ItemStack(DifModItems.BEER.get()),
							new ItemStack(DifModItems.MATY_DRINK.get()),
							new ItemStack(DifModItems.FERNET.get()),
							new ItemStack(DifModItems.WINE.get()),
							new ItemStack(DifModItems.CHERRY_BOTTLE.get()),
							new ItemStack(DifModItems.CIDER_BOTTLE.get())
					});
			tabData.accept(DifModItems.BOTTLE_OF_MOLOTOVUV_KOKTEJL.get());
			tabData.accept(DifModItems.BOTTLE_OF_URANOVEJ_KOKTEJL.get());
		}else if(tabData.getTabKey().equals(CreativeModeTabs.INGREDIENTS)){
			tabData.getEntries().putAfter(new ItemStack(Items.AMETHYST_SHARD),new ItemStack(DifModItems.ENERGY_SHARD.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}else if(tabData.getTabKey().equals(CreativeModeTabs.OP_BLOCKS)){
			tabData.accept(DifModItems.EXAMPLE_ITEM.get());
			tabData.accept(DifModItems.EXAMPLE_BLOCK.get());
			tabData.accept(DifModItems.XP_STORAGE.get());
			tabData.accept(DifModItems.END_PORTAL.get());
			tabData.accept(DifModItems.END_GATEWAY.get());
			tabData.accept(DifModItems.NETHER_PORTAL.get());
			tabData.accept(DifModItems.WATER.get());
			tabData.accept(DifModItems.LAVA.get());
			tabData.accept(DifModItems.FIRE.get());

			tabData.accept(DifModItems.FRYING_TABLE.get());
			tabData.accept(DifModItems.FAST_POWERED_RAIL.get());
			tabData.accept(DifModItems.FAST_RAIL.get());

			tabData.accept(DifModItems.REMOTE_CONTROLLER.get());
			tabData.accept(DifModItems.REMOTE_MINECART_ITEM.get());


		}else if(tabData.getTab().equals(ModCreativeTabs.TAB_FARMERS_DELIGHT.get())){
			tabData.getEntries().putAfter(new ItemStack(ModItems.STRAW_BALE.get()),new ItemStack(DifModItems.TREE_BARK_BLOCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}else if(tabData.getTab().equals(AllCreativeModeTabs.BASE_CREATIVE_TAB.get())){
			tabData.getEntries().putAfter(new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("create","item_hatch")))),new ItemStack(DifModItems.FLUID_HATCH.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}else if(tabData.getTab().equals(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.get())){
			addItemStacksBehind(tabData,new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("create","framed_glass_trapdoor")))),
					new ItemStack[]{
							new ItemStack(DifModItems.BROKEN_TRACK00.get()),
							new ItemStack(DifModItems.BROKEN_TRACK01.get()),
							new ItemStack(DifModItems.BROKEN_TRACK02.get())
					});
		}
	}
}

