package cz.maxtechnik.dif.init;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.modular.ModularRecipes;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
public class DifModRecipes {
	public static final DeferredRegister<RecipeSerializer<?>>REGISTRY=DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS,DifMod.MODID);
	public static final RegistryObject<RecipeSerializer<?>>MODULAR_REPAIR_SERIALIZER=REGISTRY.register("modular_recipe",ModularRecipes.Serializer::new);
}