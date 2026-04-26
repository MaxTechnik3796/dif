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
import static cz.maxtechnik.dif.init.basic.DifModItems.*;
import static cz.maxtechnik.dif.item.modular.ModularBase.*;
public class DifModTabs{
	public static final DeferredRegister<CreativeModeTab>REGISTER=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,DifMod.MODID);
	public static final RegistryObject<CreativeModeTab>MAIN=REGISTER.register("main",()->CreativeModeTab.builder().title(Component.translatable("creative_tab.dif.main")).icon(()->new ItemStack(THE_DIFFERENTIAL.get())).displayItems(((parameters,tabData)->{
		tabData.accept(THE_DIFFERENTIAL.get());
		tabData.accept(MEGA_TORCH.get());
		tabData.accept(BAN_HAMMER.get());
		tabData.accept(PORTAL_GUN.get());
		tabData.accept(CHUNK_LOADER_1X1.get());
		tabData.accept(CHUNK_LOADER_3X3.get());
		tabData.accept(EVENT_BUS.get());
		tabData.accept(VENT.get());
		tabData.accept(BURNING_GENERATOR.get());
		tabData.accept(ANDESITE_LATTICE.get());
		tabData.accept(ANDESITE_WINDOW.get());
		tabData.accept(SUPER_BOX.get());
		tabData.accept(SINGULARITATOR.get());
		tabData.accept(CAMERA_MONITOR.get());
		tabData.accept(CAMERA.get());
		tabData.accept(CAMERA_LINK.get());
		tabData.accept(SOLAR_PANEL_00.get());
		tabData.accept(SOLAR_PANEL_01.get());
		tabData.accept(SOLAR_PANEL_02.get());
		tabData.accept(SOLAR_PANEL_03.get());
		tabData.accept(SOLAR_PANEL_04.get());
		tabData.accept(SOLAR_PANEL_00_W.get());
		tabData.accept(SOLAR_PANEL_01_W.get());
		tabData.accept(SOLAR_PANEL_02_W.get());
		tabData.accept(SOLAR_PANEL_03_W.get());
		tabData.accept(SOLAR_PANEL_04_W.get());
		tabData.accept(CPU_SINGULARITY.get());
		tabData.accept(HEAVY_PLATE.get());
		tabData.accept(MITHRIL.get());
		tabData.accept(MITHRIL_PLATE.get());
		tabData.accept(BLUESTONE.get());
		tabData.accept(BLUE_PLATE.get());

		tabData.accept(QUESTION_MARK.get());


		tabData.accept(INCOMPLETE_CPU_SINGULARITY.get());
		tabData.accept(INCOMPLETE_MITHRIL_PLATE.get());
		tabData.accept(SOLAR_PANEL_INC.get());

		tabData.accept(INCOMPLETE_UNIVERSAL.get());

		tabData.accept(QUARRY.get());
		tabData.accept(QUARRY_FRAME.get());
		tabData.accept(QUARRY_LANDMARK.get());

		tabData.accept(QUARRY_DRILL_IRON.get());
		tabData.accept(QUARRY_DRILL_DIAMOND.get());
		tabData.accept(QUARRY_ENGINE_IRON.get());
		tabData.accept(QUARRY_ENGINE_GOLD.get());
		tabData.accept(QUARRY_ENGINE_DIAMOND.get());
		tabData.accept(QUARRY_LIQUID_REMOVER.get());



	})).build());
	public static final RegistryObject<CreativeModeTab>SPACE=REGISTER.register("space",()->CreativeModeTab.builder().withTabsBefore(DifModTabs.MAIN.getKey()).title(Component.translatable("creative_tab.dif.space")).icon(()->new ItemStack(SPACESHIP.get())).displayItems(((parameters,tabData)->{
		tabData.accept(SPACESHIP.get());
		tabData.accept(SPACE_ENGINE.get());
		tabData.accept(SPACE_SCAFFOLDING.get());

		tabData.accept(ROCKET_FUEL.get());
		tabData.accept(EMPTY_ROCKET_FUEL.get());

		tabData.accept(AURORA_CASING.get());
		tabData.accept(AURORA_INGOT.get());

		tabData.accept(SPACE_CASING.get());
		tabData.accept(SPACE_CASING_METAL.get());
		tabData.accept(SPACE_CASING_REINFORCED.get());
		tabData.accept(SPACE_DOOR.get());
		tabData.accept(SPACE_CORRIDOR.get());
		tabData.accept(SPACE_CRATE.get());
		tabData.accept(SOLAR_PANEL_BLOCK.get());

		tabData.accept(SPACE_SUIT_HELMET.get());
		tabData.accept(SPACE_SUIT_CHESTPLATE.get());
		tabData.accept(SPACE_SUIT_LEGGINGS.get());
		tabData.accept(SPACE_SUIT_BOOTS.get());
		tabData.accept(CARBON_SUIT_HELMET.get());
		tabData.accept(CARBON_SUIT_CHESTPLATE.get());
		tabData.accept(CARBON_SUIT_LEGGINGS.get());
		tabData.accept(CARBON_SUIT_BOOTS.get());

		tabData.accept(MOON_STONE.get());
		tabData.accept(MARS_STONE.get());

		tabData.accept(JETPACK.get());
		tabData.accept(JETPACK_FUEL.get());
		tabData.accept(JETPACK_TURBO_FUEL.get());
		tabData.accept(JETPACK_CANISTER.get());

		tabData.accept(ELECTRO_RUNNERS.get());
	})).build());
	public static final RegistryObject<CreativeModeTab>MODULAR_TOOLS=REGISTER.register("modular_tools",()->CreativeModeTab.builder().withTabsBefore(DifModTabs.SPACE.getKey()).title(Component.translatable("creative_tab.dif.modular_tools")).icon(()->newToolFromMaterials(MODULAR_PICKAXE.get(),"Diamond","Gold","Obsidian")).displayItems(((parameters,tabData)->{
		tabData.accept(Items.SMITHING_TABLE);

		tabData.accept(MODULAR_PICKAXE.get());
		tabData.accept(MODULAR_AXE.get());
		tabData.accept(MODULAR_SHOVEL.get());
		tabData.accept(MODULAR_SWORD.get());

		tabData.accept(MODULAR_PART_PICKAXE_HEAD.get());
		tabData.accept(MODULAR_PART_AXE_HEAD.get());
		tabData.accept(MODULAR_PART_SHOVEL_HEAD.get());
		tabData.accept(MODULAR_PART_SWORD_HEAD.get());

		tabData.accept(MODULAR_PART_BINDING.get());
		tabData.accept(MODULAR_PART_SWORD_BINDING.get());

		tabData.accept(MODULAR_PART_HANDLE.get());





		tabData.accept(newPartFromMaterial(MODULAR_PART_PICKAXE_HEAD.get(),"Wood"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_PICKAXE_HEAD.get(),"Stone"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_PICKAXE_HEAD.get(),"Copper"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_PICKAXE_HEAD.get(),"Iron"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_PICKAXE_HEAD.get(),"Gold"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_PICKAXE_HEAD.get(),"Diamond"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_PICKAXE_HEAD.get(),"Obsidian"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_PICKAXE_HEAD.get(),"Netherite"));

		tabData.accept(newPartFromMaterial(MODULAR_PART_AXE_HEAD.get(),"Wood"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_AXE_HEAD.get(),"Stone"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_AXE_HEAD.get(),"Copper"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_AXE_HEAD.get(),"Iron"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_AXE_HEAD.get(),"Gold"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_AXE_HEAD.get(),"Diamond"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_AXE_HEAD.get(),"Obsidian"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_AXE_HEAD.get(),"Netherite"));

		tabData.accept(newPartFromMaterial(MODULAR_PART_SHOVEL_HEAD.get(),"Wood"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SHOVEL_HEAD.get(),"Stone"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SHOVEL_HEAD.get(),"Copper"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SHOVEL_HEAD.get(),"Iron"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SHOVEL_HEAD.get(),"Gold"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SHOVEL_HEAD.get(),"Diamond"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SHOVEL_HEAD.get(),"Obsidian"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SHOVEL_HEAD.get(),"Netherite"));

		tabData.accept(newPartFromMaterial(MODULAR_PART_SWORD_HEAD.get(),"Wood"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SWORD_HEAD.get(),"Stone"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SWORD_HEAD.get(),"Copper"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SWORD_HEAD.get(),"Iron"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SWORD_HEAD.get(),"Gold"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SWORD_HEAD.get(),"Diamond"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SWORD_HEAD.get(),"Obsidian"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SWORD_HEAD.get(),"Netherite"));

		tabData.accept(newPartFromMaterial(MODULAR_PART_BINDING.get(),"Wood"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_BINDING.get(),"Stone"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_BINDING.get(),"Copper"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_BINDING.get(),"Iron"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_BINDING.get(),"Gold"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_BINDING.get(),"Diamond"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_BINDING.get(),"Obsidian"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_BINDING.get(),"Netherite"));

		tabData.accept(newPartFromMaterial(MODULAR_PART_SWORD_BINDING.get(),"Wood"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SWORD_BINDING.get(),"Stone"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SWORD_BINDING.get(),"Copper"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SWORD_BINDING.get(),"Iron"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SWORD_BINDING.get(),"Gold"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SWORD_BINDING.get(),"Diamond"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SWORD_BINDING.get(),"Obsidian"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_SWORD_BINDING.get(),"Netherite"));

		tabData.accept(newPartFromMaterial(MODULAR_PART_HANDLE.get(),"Wood"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_HANDLE.get(),"Stone"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_HANDLE.get(),"Copper"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_HANDLE.get(),"Iron"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_HANDLE.get(),"Gold"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_HANDLE.get(),"Diamond"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_HANDLE.get(),"Obsidian"));
		tabData.accept(newPartFromMaterial(MODULAR_PART_HANDLE.get(),"Netherite"));


		tabData.accept(newSingleMaterialPreFab(MODULAR_PICKAXE.get(),"Wood"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_PICKAXE.get(),"Stone"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_PICKAXE.get(),"Copper"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_PICKAXE.get(),"Iron"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_PICKAXE.get(),"Gold"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_PICKAXE.get(),"Diamond"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_PICKAXE.get(),"Obsidian"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_PICKAXE.get(),"Netherite"));

		tabData.accept(newSingleMaterialPreFab(MODULAR_AXE.get(),"Wood"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_AXE.get(),"Stone"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_AXE.get(),"Copper"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_AXE.get(),"Iron"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_AXE.get(),"Gold"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_AXE.get(),"Diamond"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_AXE.get(),"Obsidian"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_AXE.get(),"Netherite"));

		tabData.accept(newSingleMaterialPreFab(MODULAR_SHOVEL.get(),"Wood"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_SHOVEL.get(),"Stone"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_SHOVEL.get(),"Copper"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_SHOVEL.get(),"Iron"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_SHOVEL.get(),"Gold"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_SHOVEL.get(),"Diamond"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_SHOVEL.get(),"Obsidian"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_SHOVEL.get(),"Netherite"));

		tabData.accept(newSingleMaterialPreFab(MODULAR_SWORD.get(),"Wood"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_SWORD.get(),"Stone"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_SWORD.get(),"Copper"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_SWORD.get(),"Iron"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_SWORD.get(),"Gold"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_SWORD.get(),"Diamond"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_SWORD.get(),"Obsidian"));
		tabData.accept(newSingleMaterialPreFab(MODULAR_SWORD.get(),"Netherite"));
	})).build());


	public static void addCreative(BuildCreativeModeTabContentsEvent tabData){
		if(tabData.getTabKey().equals(CreativeModeTabs.BUILDING_BLOCKS)){
			tabData.getEntries().putAfter(new ItemStack(Items.SMOOTH_STONE),new ItemStack(SMOOTH_STONE_DOUBLE_SLAB.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.getEntries().putAfter(new ItemStack(Items.IRON_BARS),new ItemStack(IRON_BARS_BLOCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.accept(DEEPSLATED_ARROW.get());
			tabData.accept(STONED_ARROW.get());
			tabData.accept(WOODED_ARROW.get());
			tabData.accept(GLITCH_BLOCK.get());


		}else if(tabData.getTabKey().equals(CreativeModeTabs.NATURAL_BLOCKS)){
			tabData.getEntries().putAfter(new ItemStack(Items.BEDROCK),new ItemStack(PEDROCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.getEntries().putAfter(new ItemStack(Items.SNOW),new ItemStack(MATY_BLOCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.getEntries().putAfter(new ItemStack(Items.CRYING_OBSIDIAN),new ItemStack(CINDER_FLOUR_BLOCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.getEntries().putAfter(new ItemStack(Items.WARPED_FUNGUS),new ItemStack(SUGAR_MUSHROOM.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			addItemStacksBehind(tabData,new ItemStack(Items.AMETHYST_CLUSTER),
					new ItemStack[]{
							new ItemStack(ENERGY_BLOCK.get()),
							new ItemStack(BUDDING_ENERGY.get()),
							new ItemStack(SMALL_ENERGY_BUD.get()),
							new ItemStack(MEDIUM_ENERGY_BUD.get()),
							new ItemStack(LARGE_ENERGY_BUD.get()),
							new ItemStack(ENERGY_CLUSTER.get())
					});
			addItemStacksBehind(tabData,new ItemStack(Items.SUGAR_CANE),
					new ItemStack[]{
							new ItemStack(MATA_PLANT.get()),
							new ItemStack(CANOLA_PLANT.get()),
							new ItemStack(CANOLA_SEEDS.get())
					});
		}else if(tabData.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)){
			tabData.getEntries().putAfter(new ItemStack(Items.CHEST),new ItemStack(OLD_CHEST.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			addItemStacksBehind(tabData,new ItemStack(Items.BARREL),
					new ItemStack[]{
							new ItemStack(ANDESITE_BARREL.get()),
							new ItemStack(COPPER_BARREL.get()),
							new ItemStack(BRASS_BARREL.get())
					});
		}else if(tabData.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)){
			tabData.getEntries().putBefore(new ItemStack(Items.BUCKET),new ItemStack(ELECTRUM_DESTROYER.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.getEntries().putAfter(new ItemStack(Items.ELYTRA),new ItemStack(PHANTOM_RING.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			addItemStacksBehind(tabData,new ItemStack(Items.STONE),
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
							new ItemStack(JETPACK_TURBO_FUEL_BUCKET.get()),
							new ItemStack(SUNFLOWER_OIL_BUCKET.get()),
							new ItemStack(CRUDE_OIL_BUCKET.get())
					});
			tabData.accept(DOG.get());
		}else if(tabData.getTabKey().equals(CreativeModeTabs.COMBAT)){
			tabData.getEntries().putAfter(new ItemStack(Items.STONE_SWORD),new ItemStack(COPPER_SWORD.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.getEntries().putAfter(new ItemStack(Items.STONE_AXE),new ItemStack(COPPER_AXE.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
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
			tabData.getEntries().putAfter(new ItemStack(Items.POISONOUS_POTATO),new ItemStack(FRIES.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.getEntries().putAfter(new ItemStack(Items.BREAD),new ItemStack(BURNED_TOAST.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			addItemStacksBehind(tabData,new ItemStack(Items.COOKED_CHICKEN),
					new ItemStack[]{
							new ItemStack(BUCKET_OF_CHICKEN.get()),
							new ItemStack(RIZEK.get())
					});
			addItemStacksBehind(tabData,new ItemStack(Items.SWEET_BERRIES),
					new ItemStack[]{
							new ItemStack(MATA.get()),
							new ItemStack(CHERRY.get()),
							new ItemStack(NETHER_WART_BOTTLE.get())
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
							new ItemStack(MATY_DRINK.get()),
							new ItemStack(FERNET.get()),
							new ItemStack(WINE.get()),
							new ItemStack(CHERRY_BOTTLE.get()),
							new ItemStack(CIDER_BOTTLE.get())
					});
			tabData.accept(BOTTLE_OF_MOLOTOVUV_KOKTEJL.get());
			tabData.accept(BOTTLE_OF_URANOVEJ_KOKTEJL.get());
			tabData.accept(FLAT_DOUGH.get());
		}else if(tabData.getTabKey().equals(CreativeModeTabs.INGREDIENTS)){
			tabData.getEntries().putAfter(new ItemStack(Items.AMETHYST_SHARD),new ItemStack(ENERGY_SHARD.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}else if(tabData.getTabKey().equals(CreativeModeTabs.OP_BLOCKS)){
			tabData.accept(EXAMPLE_ITEM.get());
			tabData.accept(EXAMPLE_BLOCK.get());
			tabData.accept(XP_STORAGE.get());
			tabData.accept(END_PORTAL.get());
			tabData.accept(END_GATEWAY.get());
			tabData.accept(NETHER_PORTAL.get());
			tabData.accept(WATER.get());
			tabData.accept(LAVA.get());
			tabData.accept(FIRE.get());

			tabData.accept(FRYING_TABLE.get());
			tabData.accept(FAST_POWERED_RAIL.get());
			tabData.accept(FAST_RAIL.get());

			tabData.accept(REMOTE_CONTROLLER.get());
			tabData.accept(REMOTE_MINECART.get());
			tabData.accept(REMOTE_MINECART_BLOCK.get());

			tabData.accept(SLEEPING_BAG.get());


		}else if(tabData.getTab().equals(ModCreativeTabs.TAB_FARMERS_DELIGHT.get())){
			tabData.getEntries().putAfter(new ItemStack(ModItems.STRAW_BALE.get()),new ItemStack(TREE_BARK_BLOCK.get()),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}else if(tabData.getTab().equals(AllCreativeModeTabs.BASE_CREATIVE_TAB.get())){
			addItemStacksBehind(tabData,new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("create","item_hatch")))),new ItemStack[]{new ItemStack(FLUID_HATCH.get())});
			addItemStacksBehind(tabData, new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("create","water_wheel")))), new ItemStack[]{new ItemStack(BRASS_WATER_WHEEL.get())});
			addItemStacksBehind(tabData, new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("create","large_water_wheel")))), new ItemStack[]{new ItemStack(BRASS_LARGE_WATER_WHEEL.get())});
			addItemStacksBehind(tabData, new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("create","mechanical_press")))), new ItemStack[]{new ItemStack(BRASS_MECHANICAL_PRESS.get())});
			addItemStacksBehind(tabData, new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("create","mechanical_mixer")))), new ItemStack[]{new ItemStack(BRASS_MECHANICAL_MIXER.get())});
		}else if(tabData.getTab().equals(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.get())){
			addItemStacksBehind(tabData,new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("create","framed_glass_trapdoor")))),
					new ItemStack[]{
							new ItemStack(BROKEN_TRACK00.get()),
							new ItemStack(BROKEN_TRACK01.get()),
							new ItemStack(BROKEN_TRACK02.get())
					});
		}
	}
}

