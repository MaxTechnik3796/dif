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
 * Recept pro Blast Smeltery:
 *   - 1 fluid vstup  (volitelný — amount 0 = žádný fluid)
 *   - 1 item vstup   (volitelný)
 *   - 1 fluid výstup (volitelný)
 *   - 1 item výstup  (volitelný)
 *   - doba zpracování v tickách (default 600 = 30 s)
 */
public record BlastSmelteryRecipe(
		FluidStack fluidInput,
		Ingredient itemIngredient,
		int itemIngredientCount,
		FluidStack fluidOutput,
		ItemStack itemResult,
		int processingTime
) implements Recipe<SingleRecipeInput>{
	public static final int DEFAULT_TIME=600;
	public static final MapCodec<BlastSmelteryRecipe> CODEC=RecordCodecBuilder.mapCodec(inst->inst.group(
			FluidStack.CODEC.optionalFieldOf("fluid_input",FluidStack.EMPTY).forGetter(BlastSmelteryRecipe::fluidInput),
			Ingredient.CODEC.optionalFieldOf("ingredient",Ingredient.EMPTY).forGetter(BlastSmelteryRecipe::itemIngredient),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("ingredient_count",1).forGetter(BlastSmelteryRecipe::itemIngredientCount),
			FluidStack.CODEC.optionalFieldOf("fluid_output",FluidStack.EMPTY).forGetter(BlastSmelteryRecipe::fluidOutput),
			ItemStack.STRICT_CODEC.optionalFieldOf("result",ItemStack.EMPTY).forGetter(BlastSmelteryRecipe::itemResult),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("processing_time",DEFAULT_TIME).forGetter(BlastSmelteryRecipe::processingTime)
	).apply(inst,BlastSmelteryRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf,BlastSmelteryRecipe> STREAM_CODEC=StreamCodec.composite(
			FluidStack.OPTIONAL_STREAM_CODEC,BlastSmelteryRecipe::fluidInput,
			Ingredient.CONTENTS_STREAM_CODEC,BlastSmelteryRecipe::itemIngredient,
			ByteBufCodecs.INT,BlastSmelteryRecipe::itemIngredientCount,
			FluidStack.OPTIONAL_STREAM_CODEC,BlastSmelteryRecipe::fluidOutput,
			ItemStack.OPTIONAL_STREAM_CODEC,BlastSmelteryRecipe::itemResult,
			ByteBufCodecs.INT,BlastSmelteryRecipe::processingTime,
			BlastSmelteryRecipe::new
	);
	public boolean hasFluidInput(){
		return !fluidInput.isEmpty();
	}
	public boolean hasItemInput(){
		return !itemIngredient.isEmpty();
	}
	public boolean hasFluidOutput(){
		return !fluidOutput.isEmpty();
	}
	public boolean hasItemOutput(){
		return !itemResult.isEmpty();
	}
	/** Zkontroluje zda vstupní item odpovídá receptu. */
	public boolean matchesItem(ItemStack stack){
		if(!hasItemInput()) return true;
		return !stack.isEmpty()&&stack.getCount()>=itemIngredientCount&&itemIngredient.test(stack);
	}
	/** Zkontroluje zda vstupní fluid odpovídá receptu. */
	public boolean matchesFluid(FluidStack stack){
		if(!hasFluidInput()) return true;
		return !stack.isEmpty()
				&&stack.getFluid()==fluidInput.getFluid()
				&&stack.getAmount()>=fluidInput.getAmount();
	}
	/** Kompletní shoda (item + fluid). */
	public boolean matches(ItemStack item,FluidStack fluid){
		return matchesItem(item)&&matchesFluid(fluid);
	}
	// ── Recipe<SingleRecipeInput> (vanilla rozhraní – recept hledáme ručně) ──
	@Override
	public boolean matches(@NotNull SingleRecipeInput input,@NotNull Level level){
		return matchesItem(input.item());
	}
	@Override
	public @NotNull ItemStack assemble(@NotNull SingleRecipeInput input,HolderLookup.@NotNull Provider provider){
		return itemResult.copy();
	}
	@Override
	public boolean canCraftInDimensions(int w,int h){
		return true;
	}
	@Override
	public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider){
		return itemResult.copy();
	}
	@Override
	public @NotNull RecipeSerializer<?> getSerializer(){
		return DifModRecipes.BLAST_SMELTERY_SERIALIZER.get();
	}
	@Override
	public @NotNull RecipeType<?> getType(){
		return DifModRecipes.BLAST_SMELTERY_TYPE.get();
	}
	public static class Serializer implements RecipeSerializer<BlastSmelteryRecipe>{
		@Override
		public @NotNull MapCodec<BlastSmelteryRecipe> codec(){
			return CODEC;
		}
		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf,BlastSmelteryRecipe> streamCodec(){
			return STREAM_CODEC;
		}
	}
}
