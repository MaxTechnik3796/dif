package cz.maxtechnik.dif.init.other;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.item.modular.v2.ModularMaterial;
import cz.maxtechnik.dif.item.modular.v2.ModularParts;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import static cz.maxtechnik.dif.item.modular.v2.ModularMaterial.*;
import static cz.maxtechnik.dif.item.modular.v2.ModularParts.*;
import static cz.maxtechnik.mtrecipex.MTRecipexModRegistry.addCustom;
public class DifModRecipex{
	/**
	 * Centrální seznam všech part typů a jejich hodnota v ingotech.
	 * 1 = binding/handle (144 mb), 2 = head (288 mb).
	 * Přidáš nový part? Stačí ho přidat sem – casting i melting se přizpůsobí automaticky.
	 */
	private static final Object[][] PARTS={
		{HANDLE,       1},
		{BINDING,      1},
		{SWORD_BINDING,1},
		{AXE_HEAD,     2},
		{PICKAXE_HEAD, 2},
		{SHOVEL_HEAD,  2},
		{SWORD_HEAD,   2},
		{HOE_HEAD,     2},
		{BATTLE_AXE_HEAD,  2},
		{KATANA_HEAD,      2},
		{TIMBER_AXE_HEAD,  2},
		{HAMMER_HEAD,      2},
		{EXCAVATOR_HEAD,   2},
	};

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
		for(Object[] entry:PARTS){
			ModularParts part=(ModularParts)entry[0];
			int ingotValue=(int)entry[1];
			addCustom(sMaterial+"_"+part.getName(),addCasting(part,material,ingotValue));
		}
		if(!material.getLiquid().isEmpty()&&material.getMinHeatTier()>0){
			addCustom(sMaterial+"_parts_melting",addForgeMelting(material));
		}
	}
	private static JsonObject addCasting(ModularParts partType,ModularMaterial material,int count){
		JsonObject recipeJson=new JsonObject();
		recipeJson.addProperty("type","create:filling");
		JsonArray ingredientsArray=new JsonArray();
		JsonObject itemObj=new JsonObject();
		itemObj.addProperty("item",BuiltInRegistries.ITEM.getKey(partType.getCastingMold().get().asItem()).toString());
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
		for(Object[] entry:PARTS){
			ModularParts part=(ModularParts)entry[0];
			int ingotValue=(int)entry[1];
			JsonObject conv=new JsonObject();
			JsonObject ingredient=new JsonObject();
			ingredient.addProperty("type","neoforge:components");
			ingredient.addProperty("item",BuiltInRegistries.ITEM.getKey(DifModItems.MODULAR_PART.get()).toString());
			JsonObject comObj=new JsonObject();
			JsonObject mpp=new JsonObject();
			mpp.addProperty("part_type",part.getName());
			mpp.addProperty("material",material.getName());
			mpp.addProperty("cast_mold",false);
			comObj.add("dif:modular_part_properties",mpp);
			ingredient.add("components",comObj);
			conv.add("ingredient",ingredient);
			conv.addProperty("ingot_value",(float)ingotValue);
			conv.addProperty("processing_time_multiplier",1.0f);
			conversions.add(conv);
		}
		recipeJson.add("conversions",conversions);
		JsonObject fluidObj=new JsonObject();
		fluidObj.addProperty("id","dif:molten_"+material.getName()+"_fluid");
		fluidObj.addProperty("amount",144);
		recipeJson.add("result_fluid",fluidObj);
		return recipeJson;
	}
}