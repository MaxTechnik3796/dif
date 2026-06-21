package cz.maxtechnik.dif.init.other;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cz.maxtechnik.dif.item.modular.v2.ModularMaterial;
import cz.maxtechnik.dif.item.modular.v2.ModularParts;
import cz.maxtechnik.mtrecipex.MTRecipexModRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import static cz.maxtechnik.dif.init.basic.DifModItems.CASTING_MOLD_HANDLE;
import static cz.maxtechnik.dif.init.basic.DifModItems.MODULAR_PART;
public class DifModRecipex{
	static String fluidCopper="c:molten_copper";
	public static void onCommonSetup(final FMLCommonSetupEvent event){
		event.enqueueWork(()->{
			MTRecipexModRegistry.addCustom("copper_handle",addCasting(CASTING_MOLD_HANDLE.get(),fluidCopper,ModularParts.HANDLE,ModularMaterial.COPPER));

		});
	}
	private static JsonObject addCasting(Item casing_mold,String fluid,ModularParts partType,ModularMaterial material){
		JsonObject recipeJson=new JsonObject();
		recipeJson.addProperty("type","create:filling");
		JsonArray ingredientsArray=new JsonArray();
		JsonObject itemObj=new JsonObject();
		itemObj.addProperty("item",BuiltInRegistries.ITEM.getKey(casing_mold.asItem()).toString());
		ingredientsArray.add(itemObj);
		JsonObject fluidObj=new JsonObject();
		fluidObj.addProperty("type","neoforge:tag");
		fluidObj.addProperty("tag",fluid);
		fluidObj.addProperty("amount",144);
		ingredientsArray.add(fluidObj);
		JsonArray resultsArray=new JsonArray();
		JsonObject resObj=new JsonObject();
		resObj.addProperty("id",BuiltInRegistries.ITEM.getKey(MODULAR_PART.get()).toString());
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