package cz.maxtechnik.dif.init.other;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.item.modular.v2.ModularMaterial;
import cz.maxtechnik.dif.item.modular.v2.ModularParts;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import static cz.maxtechnik.dif.item.modular.v2.ModularMaterial.*;
import static cz.maxtechnik.dif.item.modular.v2.ModularParts.*;
import static cz.maxtechnik.mtrecipex.MTRecipexModRegistry.addCustom;
public class DifModRecipex{
	public static void onCommonSetup(final FMLCommonSetupEvent event){
		event.enqueueWork(()->{
			addAllTypes(IRON);
			addAllTypes(COPPER);
			addAllTypes(GOLD);
			addAllTypes(STEEL);
			addAllTypes(OBSIDIAN);
			addAllTypes(ZINC);
			addAllTypes(BRASS);
			addAllTypes(NICKEL);
			addAllTypes(MITHRIL);
		});
	}
	private static void addAllTypes(ModularMaterial material){
		String sMaterial=material.getName();
		addCustom(sMaterial+"_handle",addCasting(HANDLE,material,1));
		addCustom(sMaterial+"_binding",addCasting(BINDING,material,1));
		addCustom(sMaterial+"_axe_head",addCasting(AXE_HEAD,material,2));
		addCustom(sMaterial+"_pickaxe_head",addCasting(PICKAXE_HEAD,material,2));
		addCustom(sMaterial+"_shovel_head",addCasting(SHOVEL_HEAD,material,2));
		addCustom(sMaterial+"_sword_head",addCasting(SWORD_HEAD,material,2));
		addCustom(sMaterial+"_sword_binding",addCasting(SWORD_BINDING,material,1));
		addCustom(sMaterial+"_hoe_head",addCasting(HOE_HEAD,material,2));
		addCustom(sMaterial+"_battle_axe_head",addCasting(BATTLE_AXE_HEAD,material,2));
		addCustom(sMaterial+"_katana_head",addCasting(KATANA_HEAD,material,2));
		addCustom(sMaterial+"_timber_axe_head",addCasting(TIMBER_AXE_HEAD,material,2));
		addCustom(sMaterial+"_hammer_head",addCasting(HAMMER_HEAD,material,2));
		addCustom(sMaterial+"_excavator_head",addCasting(EXCAVATOR_HEAD,material,2));
		if(!material.getLiquid().isEmpty()&&material.getMinHeatTier()>0)
			addCustom(sMaterial+"_parts_melting",addForgeMelting(material));
	}
	private static JsonObject addCasting(ModularParts partType,ModularMaterial material,int count){
		Item casing_mold=partType.getCastingMold().get();
		JsonObject recipeJson=new JsonObject();
		recipeJson.addProperty("type","create:filling");
		JsonArray ingredientsArray=new JsonArray();
		JsonObject itemObj=new JsonObject();
		itemObj.addProperty("item",BuiltInRegistries.ITEM.getKey(casing_mold.asItem()).toString());
		ingredientsArray.add(itemObj);
		JsonObject fluidObj=new JsonObject();
		fluidObj.addProperty("type","neoforge:tag");
		fluidObj.addProperty("tag",material.getLiquid());
		fluidObj.addProperty("amount",count*144);
		ingredientsArray.add(fluidObj);
		JsonArray resultsArray=new JsonArray();
		JsonObject resObj=new JsonObject();
		resObj.addProperty("id",BuiltInRegistries.ITEM.getKey(DifModItems.MODULAR_PART.get()).toString());
		JsonObject comObj=new JsonObject();
		JsonObject mpp=new JsonObject();
		mpp.addProperty("part_type",partType.getName());
		mpp.addProperty("material",material.getName());
		mpp.addProperty("cast_mold",true);
		comObj.add("dif:modular_part_properties",mpp);
		resObj.add("components",comObj);
		resultsArray.add(resObj);
		recipeJson.add("ingredients",ingredientsArray);
		recipeJson.add("results",resultsArray);
		return recipeJson;
	}
	private static JsonObject addForgeMelting(ModularMaterial material){
		JsonObject recipeJson=new JsonObject();
		recipeJson.addProperty("type","dif:forge_material");
		recipeJson.addProperty("min_heat_tier",material.getMinHeatTier());
		recipeJson.addProperty("processing_time",80);
		JsonArray conversions=new JsonArray();
		addMeltConversion(conversions,material,HANDLE,1);
		addMeltConversion(conversions,material,BINDING,1);
		addMeltConversion(conversions,material,SWORD_BINDING,1);
		addMeltConversion(conversions,material,AXE_HEAD,2);
		addMeltConversion(conversions,material,PICKAXE_HEAD,2);
		addMeltConversion(conversions,material,SHOVEL_HEAD,2);
		addMeltConversion(conversions,material,SWORD_HEAD,2);
		addMeltConversion(conversions,material,HOE_HEAD,2);
		addMeltConversion(conversions,material,BATTLE_AXE_HEAD,2);
		addMeltConversion(conversions,material,KATANA_HEAD,2);
		addMeltConversion(conversions,material,TIMBER_AXE_HEAD,2);
		addMeltConversion(conversions,material,HAMMER_HEAD,2);
		addMeltConversion(conversions,material,EXCAVATOR_HEAD,2);
		recipeJson.add("conversions",conversions);
		JsonObject fluidObj=new JsonObject();
		fluidObj.addProperty("id","dif:molten_"+material.getName()+"_fluid");
		fluidObj.addProperty("amount",144);
		recipeJson.add("result_fluid",fluidObj);
		return recipeJson;
	}
	private static void addMeltConversion(JsonArray conversions,ModularMaterial material,ModularParts partType,int ingotValue){
		JsonObject conv=new JsonObject();
		JsonObject ingredient=new JsonObject();
		ingredient.addProperty("item",BuiltInRegistries.ITEM.getKey(DifModItems.MODULAR_PART.get()).toString());
		conv.add("ingredient",ingredient);
		conv.addProperty("part_type",partType.getName());
		conv.addProperty("part_material",material.getName());
		conv.addProperty("ingot_value",(float)ingotValue);
		conv.addProperty("processing_time_multiplier",1.0f);
		conversions.add(conv);
	}
}