package cz.maxtechnik.dif.init.basic;

import com.simibubi.create.AllCreativeModeTabs;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.other.DifModComponents;
import cz.maxtechnik.dif.item.modular.v2.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.common.registry.ModCreativeTabs;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static cz.maxtechnik.dif.DifMod.addItemStacksBehind;
import static cz.maxtechnik.dif.init.basic.DifModItems.*;
@SuppressWarnings("unused")
public class DifModTabs{
	public static final DeferredRegister<CreativeModeTab>REGISTER=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,DifMod.MODID);
	public static final DeferredHolder<CreativeModeTab,CreativeModeTab> MAIN=REGISTER.register("main",()->CreativeModeTab.builder().title(Component.translatable("creative_tab.dif.main")).icon(()->new ItemStack(THE_DIFFERENTIAL.get())).displayItems(((parameters,tabData)->{
		tabData.accept(THE_DIFFERENTIAL);
		tabData.accept(MEGA_TORCH);
		tabData.accept(PORTAL_GUN);
		tabData.accept(CHUNK_LOADER_1X1);
		tabData.accept(CHUNK_LOADER_3X3);

		tabData.accept(EVENT_BUS);
		tabData.accept(VENT);

		tabData.accept(BURNING_GENERATOR);
		tabData.accept(ANDESITE_LATTICE);
		tabData.accept(ANDESITE_WINDOW);
		tabData.accept(SUPER_BOX);
		tabData.accept(FRYING_TABLE);

		tabData.accept(LAP_TIMER);

		tabData.accept(CAMERA_MONITOR);
		tabData.accept(CAMERA);
		tabData.accept(CAMERA_LINK);
		tabData.accept(SLEEPING_BAG);

		tabData.accept(SOLAR_PANEL_00);
		tabData.accept(SOLAR_PANEL_01);
		tabData.accept(SOLAR_PANEL_02);
		tabData.accept(SOLAR_PANEL_03);
		tabData.accept(SOLAR_PANEL_04);

		tabData.accept(SOLAR_PANEL_00_W);
		tabData.accept(SOLAR_PANEL_01_W);
		tabData.accept(SOLAR_PANEL_02_W);
		tabData.accept(SOLAR_PANEL_03_W);
		tabData.accept(SOLAR_PANEL_04_W);

		tabData.accept(MITHRIL);
		tabData.accept(MITHRIL_PLATE);
		tabData.accept(DEEPSLATE_MITHRIL_ORE);
		tabData.accept(BLUESTONE);
		tabData.accept(BLUE_PLATE);

		tabData.accept(QUARRY);
		tabData.accept(QUARRY_FRAME);
		tabData.accept(QUARRY_LANDMARK);

		tabData.accept(QUARRY_DRILL_IRON);
		tabData.accept(QUARRY_DRILL_DIAMOND);
		tabData.accept(QUARRY_ENGINE_IRON);
		tabData.accept(QUARRY_ENGINE_GOLD);
		tabData.accept(QUARRY_ENGINE_DIAMOND);
	})).build());
	public static final DeferredHolder<CreativeModeTab,CreativeModeTab>INDUSTRIAL=REGISTER.register("industrial",()->CreativeModeTab.builder().withTabsBefore(DifModTabs.MAIN.getKey()).title(Component.translatable("creative_tab.dif.industrials")).icon(()->new ItemStack(ENGINE_EXTENDER_DIESEL.get())).displayItems(((parameters,tabData)->{

		tabData.accept(DISTILLATION_TANK);

		tabData.accept(COKE_OVEN);
		tabData.accept(COKE_OVEN_CONTROLLER);
		tabData.accept(COKE);
		tabData.accept(BLAST_SMELTERY);
		tabData.accept(BLAST_SMELTERY_CONTROLLER);
		tabData.accept(FORGE_BRICK);
		tabData.accept(FORGE_GLASS);
		tabData.accept(FORGE_FURNACE_CONTROLLER);

		tabData.accept(ENGINE_BASE);

		tabData.accept(ENGINE_PORTABLE_DIESEL);
		tabData.accept(ENGINE_PORTABLE_GASOLINE);
		tabData.accept(ENGINE_PORTABLE_LPG);

		tabData.accept(ENGINE_EXTENDER_DIESEL);
		tabData.accept(ENGINE_EXTENDER_GASOLINE);
		tabData.accept(ENGINE_EXTENDER_LPG);
		tabData.accept(ENGINE_EXTENDER_HEAVY_FUEL_OIL);

		tabData.accept(ZINC_CASING);
		tabData.accept(STEEL_CASING);
		tabData.accept(BIG_GIRDER);

		tabData.accept(COPPER_SUPPORT);
		tabData.accept(ZINC_SUPPORT);
		tabData.accept(BRASS_SUPPORT);
		tabData.accept(STEEL_SUPPORT);

		tabData.accept(WOODEN_FRAME);

		tabData.accept(STEEL_INGOT);
		tabData.accept(STEEL_SHEET);
		tabData.accept(NICKEL_SHEET);

		tabData.accept(NANO_GLASS);

	})).build());
	public static final DeferredHolder<CreativeModeTab,CreativeModeTab>SPACE=REGISTER.register("space",()->CreativeModeTab.builder().withTabsBefore(DifModTabs.INDUSTRIAL.getKey()).title(Component.translatable("creative_tab.dif.space")).icon(()->new ItemStack(SPACESHIP.get())).displayItems(((parameters,tabData)->{
		tabData.accept(SPACESHIP);
		tabData.accept(SPACE_ENGINE);
		tabData.accept(SPACE_SCAFFOLDING);

		tabData.accept(ROCKET_FUEL);
		tabData.accept(EMPTY_ROCKET_FUEL);

		tabData.accept(AURORA_CASING);
		tabData.accept(AURORA_INGOT);

		tabData.accept(SPACE_CASING);
		tabData.accept(SPACE_CASING_METAL);
		tabData.accept(SPACE_CASING_REINFORCED);
		tabData.accept(SPACE_DOOR);
		tabData.accept(SPACE_CORRIDOR);
		tabData.accept(SPACE_CRATE);
		tabData.accept(SOLAR_PANEL_BLOCK);

		tabData.accept(SPACE_SUIT_HELMET);
		tabData.accept(SPACE_SUIT_CHESTPLATE);
		tabData.accept(SPACE_SUIT_LEGGINGS);
		tabData.accept(SPACE_SUIT_BOOTS);
		tabData.accept(CARBON_SUIT_HELMET);
		tabData.accept(CARBON_SUIT_CHESTPLATE);
		tabData.accept(CARBON_SUIT_LEGGINGS);
		tabData.accept(CARBON_SUIT_BOOTS);

		tabData.accept(MOON_STONE);
		tabData.accept(MARS_STONE);

		tabData.accept(JETPACK);

		tabData.accept(ELECTRO_RUNNERS);

	})).build());
	public static final DeferredHolder<CreativeModeTab,CreativeModeTab>MODULAR_TOOLS=REGISTER.register("modular_tools",()->CreativeModeTab.builder().withTabsBefore(DifModTabs.SPACE.getKey()).title(Component.translatable("creative_tab.dif.modular_tools")).icon(()->new ItemStack(Items.SMITHING_TABLE)).displayItems(((parameters, tabData)->{
		tabData.accept(Items.SMITHING_TABLE);
		tabData.accept(MODULAR_REFORGE_TABLE);

		tabData.accept(MODULAR_WIKI_BOOK);

		tabData.accept(MODULAR_TEMPLATE_NORMAL);
		tabData.accept(MODULAR_TEMPLATE_HYPER);

		tabData.accept(MODULAR_REFORGE_STONE);
		tabData.accept(SILKY_STONE);

		ItemStack ePart=new ItemStack(MODULAR_PART.get());ePart.set(DifModComponents.MODULAR_PART_PROPERTIES.get(),new ModularPartProperties(ModularParts.HANDLE.getName(),ModularMaterial.IRON.getName(),false));
		tabData.accept(ePart);
		ItemStack fPart=new ItemStack(MODULAR_PART.get());fPart.set(DifModComponents.MODULAR_PART_PROPERTIES.get(),new ModularPartProperties(ModularParts.AXE_HEAD.getName(),ModularMaterial.STEEL.getName(),false));
		tabData.accept(fPart);
		ItemStack gPart=new ItemStack(MODULAR_PART.get());gPart.set(DifModComponents.MODULAR_PART_PROPERTIES.get(),new ModularPartProperties(ModularParts.SWORD_HEAD.getName(),ModularMaterial.STEEL.getName(),true));
		tabData.accept(gPart);
		ItemStack hPart=new ItemStack(MODULAR_PART.get());hPart.set(DifModComponents.MODULAR_PART_PROPERTIES.get(),new ModularPartProperties(ModularParts.HANDLE.getName(),ModularMaterial.COPPER.name(),true));
		tabData.accept(hPart);

		tabData.accept(CASTING_MOLD);
		tabData.accept(CASTING_MOLD_HANDLE);
		tabData.accept(CASTING_MOLD_BINDING);
		tabData.accept(CASTING_MOLD_AXE_HEAD);
		tabData.accept(CASTING_MOLD_PICKAXE_HEAD);
		tabData.accept(CASTING_MOLD_SWORD_HEAD);
		tabData.accept(CASTING_MOLD_SHOVEL_HEAD);
		tabData.accept(CASTING_MOLD_SWORD_BINDING);
		tabData.accept(CASTING_MOLD_HOE_HEAD);
		tabData.accept(CASTING_MOLD_BATTLE_AXE_HEAD);
		tabData.accept(CASTING_MOLD_TIMBER_AXE_HEAD);
		tabData.accept(CASTING_MOLD_HAMMER_HEAD);
		tabData.accept(CASTING_MOLD_EXCAVATOR_HEAD);

		for(ModularMaterial mat : ModularMaterial.values()){
			if(mat == ModularMaterial.NONE) continue;
			for(ModularTools toolType : ModularTools.values()){
				if(toolType == ModularTools.NONE) continue;
				ItemStack tool = new ItemStack(MODULAR_TOOL.get());
				ModularToolProperties props = new ModularToolProperties(
					toolType.getName(),
					mat.getName(),
					mat.getName(),
					mat.getName(),
					ModularReforge.NONE.getName()
				);
				tool.set(DifModComponents.MODULAR_TOOL_PROPERTIES.get(), props);
				tabData.accept(tool);
			}
		}

	})).build());


	public static void addCreative(BuildCreativeModeTabContentsEvent tabData){
		if(tabData.getTabKey().equals(CreativeModeTabs.BUILDING_BLOCKS)){
			tabData.insertAfter(new ItemStack(Items.SMOOTH_STONE),new ItemStack(SMOOTH_STONE_DOUBLE_SLAB.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(new ItemStack(Items.IRON_BARS),new ItemStack(IRON_BARS_BLOCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(new ItemStack(Items.IRON_BLOCK),new ItemStack(NICKEL_BLOCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.accept(DEEPSLATED_ARROW);
			tabData.accept(STONED_ARROW);
			tabData.accept(WOODED_ARROW);
			tabData.accept(GLITCH_BLOCK);


		}else if(tabData.getTabKey().equals(CreativeModeTabs.NATURAL_BLOCKS)){
			tabData.insertAfter(new ItemStack(Items.BEDROCK),new ItemStack(PEDROCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(new ItemStack(Items.CRYING_OBSIDIAN),new ItemStack(CINDER_FLOUR_BLOCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(new ItemStack(Items.RAW_IRON_BLOCK),new ItemStack(RAW_NICKEL_BLOCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			addItemStacksBehind(tabData,new ItemStack(Items.DEEPSLATE_IRON_ORE),
					new ItemStack[]{
							new ItemStack(NICKEL_ORE.get()),
							new ItemStack(DEEPSLATE_NICKEL_ORE.get())
					});
		}else if(tabData.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)){
			tabData.insertAfter(new ItemStack(Items.CHEST),new ItemStack(OLD_CHEST.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			addItemStacksBehind(tabData,new ItemStack(Items.BARREL),
					new ItemStack[]{
							new ItemStack(ANDESITE_BARREL.get()),
							new ItemStack(COPPER_BARREL.get()),
							new ItemStack(BRASS_BARREL.get())
					});
		}else if(tabData.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)){
			tabData.insertBefore(new ItemStack(Items.BUCKET),new ItemStack(ELECTRUM_DESTROYER.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(new ItemStack(Items.ELYTRA),new ItemStack(PHANTOM_RING.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			addItemStacksBehind(tabData,new ItemStack(Items.STONE_HOE),
					new ItemStack[]{
							new ItemStack(COPPER_SHOVEL.get()),
							new ItemStack(COPPER_PICKAXE.get()),
							new ItemStack(COPPER_AXE.get()),
							new ItemStack(COPPER_HOE.get())
					});
			addItemStacksBehind(tabData,new ItemStack(Items.LAVA_BUCKET),
					new ItemStack[]{
							new ItemStack(BEER_BUCKET.get()),
							new ItemStack(XP_BUCKET.get()),
							new ItemStack(CIDER_BUCKET.get()),
							new ItemStack(FUEL_BUCKET.get()),
							new ItemStack(JETPACK_FUEL_BUCKET.get()),
							new ItemStack(SUNFLOWER_OIL_BUCKET.get()),
							new ItemStack(CRUDE_OIL_BUCKET.get()),

							new ItemStack(LPG_BUCKET.get()),
							new ItemStack(GASOLINE_BUCKET.get()),
							new ItemStack(DIESEL_BUCKET.get()),
							new ItemStack(LUBRICATING_OIL_BUCKET.get()),
							new ItemStack(HEAVY_FUEL_OIL_BUCKET.get()),
							new ItemStack(CREOSOTE_OIL_BUCKET.get()),

							new ItemStack(MOLTEN_IRON_BUCKET.get()),
							new ItemStack(MOLTEN_COPPER_BUCKET.get()),
							new ItemStack(MOLTEN_GOLD_BUCKET.get()),
							new ItemStack(MOLTEN_STEEL_BUCKET.get()),
							new ItemStack(MOLTEN_OBSIDIAN_BUCKET.get()),
							new ItemStack(MOLTEN_ZINC_BUCKET.get()),
							new ItemStack(MOLTEN_BRASS_BUCKET.get()),
							new ItemStack(MOLTEN_NICKEL_BUCKET.get()),
							new ItemStack(MOLTEN_MITHRIL_BUCKET.get()),
					});
		}else if(tabData.getTabKey().equals(CreativeModeTabs.COMBAT)){
			tabData.insertAfter(new ItemStack(Items.STONE_SWORD),new ItemStack(COPPER_SWORD.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(new ItemStack(Items.STONE_AXE),new ItemStack(COPPER_AXE.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			addItemStacksBehind(tabData,new ItemStack(Items.NETHERITE_SWORD),
					new ItemStack[]{
							new ItemStack(WOODEN_KATANA.get()),
							new ItemStack(STONE_KATANA.get()),
							new ItemStack(COPPER_KATANA.get()),
							new ItemStack(IRON_KATANA.get()),
							new ItemStack(GOLDEN_KATANA.get()),
							new ItemStack(DIAMOND_KATANA.get()),
							new ItemStack(NETHERITE_KATANA.get()),
							new ItemStack(WOODEN_BATTLE_AXE.get()),
							new ItemStack(STONE_BATTLE_AXE.get()),
							new ItemStack(COPPER_BATTLE_AXE.get()),
							new ItemStack(IRON_BATTLE_AXE.get()),
							new ItemStack(GOLDEN_BATTLE_AXE.get()),
							new ItemStack(DIAMOND_BATTLE_AXE.get()),
							new ItemStack(NETHERITE_BATTLE_AXE.get())
					});
			addItemStacksBehind(tabData,new ItemStack(Items.CHAINMAIL_BOOTS),
					new ItemStack[]{
							new ItemStack(COPPER_HELMET.get()),
							new ItemStack(COPPER_CHESTPLATE.get()),
							new ItemStack(COPPER_LEGGINGS.get()),
							new ItemStack(COPPER_BOOTS.get())
					});
		}else if(tabData.getTabKey().equals(CreativeModeTabs.FOOD_AND_DRINKS)){
			tabData.insertAfter(new ItemStack(Items.POISONOUS_POTATO),new ItemStack(FRIES.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			addItemStacksBehind(tabData,new ItemStack(Items.BREAD),
					new ItemStack[]{
							new ItemStack(BAGUETTE.get()),
							new ItemStack(BURNED_TOAST.get())
					});
			addItemStacksBehind(tabData,new ItemStack(Items.COOKED_CHICKEN),
					new ItemStack[]{
							new ItemStack(BUCKET_OF_CHICKEN.get()),
							new ItemStack(RIZEK.get())
					});
			addItemStacksBehind(tabData,new ItemStack(Items.COOKED_RABBIT),
					new ItemStack[]{
							new ItemStack(HORSE_MEAT.get()),
							new ItemStack(COOKED_HORSE_MEAT.get())
					});
			addItemStacksBehind(tabData,new ItemStack(Items.RABBIT_STEW),
					new ItemStack[]{
							new ItemStack(CREATE_CAN.get()),
							new ItemStack(CREATE_BOWL.get()),
							new ItemStack(SUPER_HEATED_CREATE_BOWL.get())
					});
			addItemStacksBehind(tabData,new ItemStack(Items.MILK_BUCKET),
					new ItemStack[]{   
							new ItemStack(BEER.get()),
							new ItemStack(BERRY_BOTTLE.get()),
							new ItemStack(CIDER_BOTTLE.get())
					});
			tabData.accept(BOTTLE_OF_MOLOTOVUV_KOKTEJL);
			tabData.accept(BOTTLE_OF_URANOVEJ_KOKTEJL);
			tabData.accept(FLAT_DOUGH);
		}else if(tabData.getTabKey().equals(CreativeModeTabs.INGREDIENTS)){
			tabData.insertAfter(new ItemStack(Items.RAW_IRON),new ItemStack(RAW_NICKEL.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(new ItemStack(Items.IRON_NUGGET),new ItemStack(NICKEL_NUGGET.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(new ItemStack(Items.IRON_INGOT),new ItemStack(NICKEL_INGOT.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}else if(tabData.getTabKey().equals(CreativeModeTabs.OP_BLOCKS)){
			tabData.accept(BAN_HAMMER);
			tabData.accept(END_PORTAL);
			tabData.accept(END_GATEWAY);
			tabData.accept(NETHER_PORTAL);
			tabData.accept(WATER);
			tabData.accept(LAVA);
			tabData.accept(FIRE);

			tabData.accept(FAST_POWERED_RAIL);
			tabData.accept(FAST_RAIL);

			tabData.accept(NUKE);
			tabData.accept(NUKE_SAFE);
			tabData.accept(INCOMPLETE_UNIVERSAL);

		}else if(tabData.getTab().equals(ModCreativeTabs.TAB_FARMERS_DELIGHT.get())){
			tabData.insertAfter(new ItemStack(ModItems.STRAW_BALE.get()),new ItemStack(TREE_BARK_BLOCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}else if(tabData.getTab().equals(AllCreativeModeTabs.BASE_CREATIVE_TAB.get())){
			addItemStacksBehind(tabData,new ItemStack(Objects.requireNonNull(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create","item_hatch")))),new ItemStack[]{new ItemStack(FLUID_HATCH.get())});
		}else if(tabData.getTab().equals(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.get())){
			addItemStacksBehind(tabData,new ItemStack(Objects.requireNonNull(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create","framed_glass_trapdoor")))),
					new ItemStack[]{
							new ItemStack(BROKEN_TRACK00.get()),
							new ItemStack(BROKEN_TRACK01.get()),
							new ItemStack(BROKEN_TRACK02.get())
					});
		}
	}
}

