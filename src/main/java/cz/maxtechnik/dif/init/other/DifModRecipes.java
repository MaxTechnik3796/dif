package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.modular.v2.ModularRecipes;
import cz.maxtechnik.dif.recipe.CokeOvenRecipe;
import cz.maxtechnik.dif.recipe.DistillationRecipe;
import cz.maxtechnik.dif.recipe.FryingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
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
	public static final net.neoforged.neoforge.registries.DeferredHolder<RecipeSerializer<?>,RecipeSerializer<?>> MODULAR_REPAIR_SERIALIZER=REGISTRY.register("modular_recipe", ModularRecipes.Serializer::new);
	public static final DeferredHolder<RecipeType<?>, RecipeType<CokeOvenRecipe>> COKE_OVEN_TYPE = TYPE_REGISTRY.register("coke_oven", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath("dif", "coke_oven")));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CokeOvenRecipe>> COKE_OVEN_SERIALIZER = REGISTRY.register("coke_oven", CokeOvenRecipe.Serializer::new);
	public static final DeferredHolder<RecipeType<?>, RecipeType<cz.maxtechnik.dif.recipe.BlastSmelteryRecipe>> BLAST_SMELTERY_TYPE = TYPE_REGISTRY.register("blast_smeltery", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath("dif", "blast_smeltery")));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<cz.maxtechnik.dif.recipe.BlastSmelteryRecipe>> BLAST_SMELTERY_SERIALIZER = REGISTRY.register("blast_smeltery", cz.maxtechnik.dif.recipe.BlastSmelteryRecipe.Serializer::new);
	public static final DeferredHolder<RecipeType<?>,RecipeType<cz.maxtechnik.dif.recipe.ForgeMaterialRecipe>> FORGE_MATERIAL_TYPE=TYPE_REGISTRY.register("forge_material",()->RecipeType.simple(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("dif","forge_material")));
	public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<cz.maxtechnik.dif.recipe.ForgeMaterialRecipe>> FORGE_MATERIAL_SERIALIZER=REGISTRY.register("forge_material",cz.maxtechnik.dif.recipe.ForgeMaterialRecipe.Serializer::new);
	public static final DeferredHolder<RecipeType<?>, RecipeType<cz.maxtechnik.dif.recipe.ForgeFluidMixingRecipe>> FORGE_FLUID_MIXING_TYPE = TYPE_REGISTRY.register("forge_fluid_mixing", () -> RecipeType.simple(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("dif", "forge_fluid_mixing")));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<cz.maxtechnik.dif.recipe.ForgeFluidMixingRecipe>> FORGE_FLUID_MIXING_SERIALIZER = REGISTRY.register("forge_fluid_mixing", cz.maxtechnik.dif.recipe.ForgeFluidMixingRecipe.Serializer::new);
}