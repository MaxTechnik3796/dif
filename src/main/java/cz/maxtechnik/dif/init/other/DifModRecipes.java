package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.recipe.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
public class DifModRecipes{
	public static final DeferredRegister<RecipeSerializer<?>> REGISTRY=DeferredRegister.create(Registries.RECIPE_SERIALIZER,DifMod.MODID);
	public static final DeferredRegister<RecipeType<?>> TYPE_REGISTRY=DeferredRegister.create(Registries.RECIPE_TYPE,DifMod.MODID);
	public static final DeferredHolder<RecipeSerializer<?>,FryingRecipe.Serializer> FRYING_SERIALIZER=REGISTRY.register("frying",FryingRecipe.Serializer::new);
	public static final DeferredHolder<RecipeType<?>,RecipeType<FryingRecipe>> FRYING_TYPE=TYPE_REGISTRY.register("frying",()->RecipeType.simple(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"frying")));
	public static final DeferredHolder<RecipeSerializer<?>,DistillationRecipe.Serializer> DISTILLATION_SERIALIZER=REGISTRY.register("distillation",DistillationRecipe.Serializer::new);
	public static final DeferredHolder<RecipeType<?>,RecipeType<DistillationRecipe>> DISTILLATION_TYPE=TYPE_REGISTRY.register("distillation",()->RecipeType.simple(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"distillation")));
	}