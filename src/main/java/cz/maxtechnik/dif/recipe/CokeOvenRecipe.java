package cz.maxtechnik.dif.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
/**
 * Recept pro Coke Oven:
 *   - 1 item vstup (ingredient s počtem)
 *   - 1 item výstup
 *   - 1 fluid výstup (volitelný — amount 0 = žádný fluid)
 *   - doba zpracování v tickách (default 900 = 45 s)
 */
public record CokeOvenRecipe(
		Ingredient ingredient,
		int ingredientCount,
		ItemStack result,
		FluidStack fluidOutput,
		int processingTime
) implements Recipe<SingleRecipeInput>{
	public static final int DEFAULT_TIME=900;
	public static final MapCodec<CokeOvenRecipe> CODEC=RecordCodecBuilder.mapCodec(inst->inst.group(
			Ingredient.CODEC.fieldOf("ingredient").forGetter(CokeOvenRecipe::ingredient),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("ingredient_count",1).forGetter(CokeOvenRecipe::ingredientCount),
			ItemStack.STRICT_CODEC.fieldOf("result").forGetter(CokeOvenRecipe::result),
			FluidStack.CODEC.optionalFieldOf("fluid_output",FluidStack.EMPTY).forGetter(CokeOvenRecipe::fluidOutput),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("processing_time",DEFAULT_TIME).forGetter(CokeOvenRecipe::processingTime)
	).apply(inst,CokeOvenRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf,CokeOvenRecipe> STREAM_CODEC=StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC,CokeOvenRecipe::ingredient,
			ByteBufCodecs.INT,CokeOvenRecipe::ingredientCount,
			ItemStack.STREAM_CODEC,CokeOvenRecipe::result,
			FluidStack.STREAM_CODEC,CokeOvenRecipe::fluidOutput,
			ByteBufCodecs.INT,CokeOvenRecipe::processingTime,
			CokeOvenRecipe::new
	);
	/** Zda ItemStack odpovídá vstupu receptu (kontrola item + počet). */
	public boolean matches(ItemStack stack){
		return !stack.isEmpty()
				&&stack.getCount()>=ingredientCount
				&&ingredient.test(stack);
	}
	public boolean hasFluidOutput(){
		return !fluidOutput.isEmpty();
	}
	// ── Recipe<SingleRecipeInput> (vanilla rozhraní — recept hledáme ručně) ──
	@Override
	public boolean matches(@NotNull SingleRecipeInput input,@NotNull Level level){
		return matches(input.item());
	}
	@Override
	public @NotNull ItemStack assemble(@NotNull SingleRecipeInput input,HolderLookup.@NotNull Provider provider){
		return result.copy();
	}
	@Override
	public boolean canCraftInDimensions(int w,int h){
		return true;
	}
	@Override
	public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider){
		return result.copy();
	}
	@Override
	public @NotNull RecipeSerializer<?> getSerializer(){
		return DifModRecipes.COKE_OVEN_SERIALIZER.get();
	}
	@Override
	public @NotNull RecipeType<?> getType(){
		return DifModRecipes.COKE_OVEN_TYPE.get();
	}
	public static class Serializer implements RecipeSerializer<CokeOvenRecipe>{
		@Override
		public @NotNull MapCodec<CokeOvenRecipe> codec(){
			return CODEC;
		}
		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf,CokeOvenRecipe> streamCodec(){
			return STREAM_CODEC;
		}
	}
}