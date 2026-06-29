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
		addCustom(sMaterial+"_handle",addCasting(ModularParts.HANDLE,material));
		addCustom(sMaterial+"_binding",addCasting(ModularParts.BINDING,material));
		addCustom(sMaterial+"_axe_head",addCasting(ModularParts.AXE_HEAD,material));
		addCustom(sMaterial+"_pickaxe_head",addCasting(ModularParts.PICKAXE_HEAD,material));
		addCustom(sMaterial+"_shovel_head",addCasting(ModularParts.SHOVEL_HEAD,material));
		addCustom(sMaterial+"_sword_head",addCasting(ModularParts.SWORD_HEAD,material));
		addCustom(sMaterial+"_sword_binding",addCasting(ModularParts.SWORD_BINDING,material));
		addCustom(sMaterial+"_hoe_head",addCasting(ModularParts.HOE_HEAD,material));
	}
	private static JsonObject addCasting(ModularParts partType,ModularMaterial material){
		Item casing_mold=partType.getCastingMold();
		JsonObject recipeJson=new JsonObject();
		recipeJson.addProperty("type","create:filling");
		JsonArray ingredientsArray=new JsonArray();
		JsonObject itemObj=new JsonObject();
		itemObj.addProperty("item",BuiltInRegistries.ITEM.getKey(casing_mold.asItem()).toString());
		ingredientsArray.add(itemObj);
		JsonObject fluidObj=new JsonObject();
		fluidObj.addProperty("type","neoforge:tag");
		fluidObj.addProperty("tag",material.getLiquid());
		fluidObj.addProperty("amount",144);
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
}