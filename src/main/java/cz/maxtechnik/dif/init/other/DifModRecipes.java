package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.recipe.DistillationRecipe;
import cz.maxtechnik.dif.recipe.FryingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
public class DifModRecipes{
	public static final DeferredRegister<RecipeSerializer<?>> REGISTRY=DeferredRegister.create(Registries.RECIPE_SERIALIZER,DifMod.MODID);
	public static final DeferredRegister<RecipeType<?>> TYPE_REGISTRY=DeferredRegister.create(Registries.RECIPE_TYPE,DifMod.MODID);
	public static final net.neoforged.neoforge.registries.DeferredHolder<RecipeSerializer<?>,FryingRecipe.Serializer> FRYING_SERIALIZER=REGISTRY.register("frying",FryingRecipe.Serializer::new);
	public static final net.neoforged.neoforge.registries.DeferredHolder<RecipeType<?>,RecipeType<FryingRecipe>> FRYING_TYPE=TYPE_REGISTRY.register("frying",()->FryingRecipe.Type.INSTANCE);
	public static final DeferredHolder<RecipeSerializer<?>,DistillationRecipe.Serializer> DISTILLATION_SERIALIZER=
			REGISTRY.register("distillation",DistillationRecipe.Serializer::new);
	public static final DeferredHolder<RecipeType<?>,RecipeType<DistillationRecipe>> DISTILLATION_TYPE=TYPE_REGISTRY.register("distillation",()->new RecipeType<>(){
		@Override
		public String toString(){
			return DifMod.MODID+":distillation";
		}
	});
}