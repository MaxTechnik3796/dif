package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.modular.ModularRecipes;
import cz.maxtechnik.dif.recipes.FryingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
public class DifModRecipes {
	public static final DeferredRegister<RecipeSerializer<?>>REGISTRY=DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS,DifMod.MODID);
	public static final DeferredRegister<RecipeType<?>> TYPE_REGISTRY = DeferredRegister.create(Registries.RECIPE_TYPE, DifMod.MODID);

	public static final RegistryObject<RecipeSerializer<?>>MODULAR_REPAIR_SERIALIZER=REGISTRY.register("modular_recipe",ModularRecipes.Serializer::new);

	public static final RegistryObject<RecipeSerializer<FryingRecipe>> FRYING_SERIALIZER = REGISTRY.register("frying", FryingRecipe.Serializer::new);
	public static final RegistryObject<RecipeType<FryingRecipe>> FRYING_TYPE = TYPE_REGISTRY.register("frying", () -> FryingRecipe.Type.INSTANCE);
}