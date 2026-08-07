package cz.maxtechnik.dif.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;

import static cz.maxtechnik.dif.block.entity.FryingTableBlockEntity.INPUT_SLOT;
public class FryingRecipe implements Recipe<SingleRecipeInput>{
	private final Ingredient input;
	private final SizedFluidIngredient fluidIngredient;
	private final ItemStack output;
	private final int processingTime;

	public FryingRecipe(Ingredient input, SizedFluidIngredient fluidIngredient, ItemStack output, int processingTime){
		this.input=input;
		this.fluidIngredient=fluidIngredient;
		this.output=output;
		this.processingTime=processingTime;
	}
	public Ingredient getIngredient(){
		return input;
	}

	public SizedFluidIngredient getFluidIngredient(){
		return fluidIngredient;
	}

	public int getProcessingTime(){
		return processingTime;
	}
	public int getOilAmount(){
		return fluidIngredient.amount();
	}

	public boolean matchesFluid(FluidStack tankFluid){
		return fluidIngredient.test(tankFluid) && tankFluid.getAmount() >= fluidIngredient.amount();
	}

	@Override
	public boolean matches(@NotNull SingleRecipeInput recipeInput,@NotNull Level level){
		return input.test(recipeInput.getItem(INPUT_SLOT));
	}
	@Override
	public @NotNull ItemStack assemble(@NotNull SingleRecipeInput recipeInput,HolderLookup.@NotNull Provider provider){
		return output.copy();
	}
	@Override
	public boolean canCraftInDimensions(int width,int height){
		return true;
	}
	@Override
	public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider){
		return output;
	}
	@Override
	public @NotNull RecipeSerializer<?> getSerializer(){
		return DifModRecipes.FRYING_SERIALIZER.get();
	}
	@Override
	public @NotNull RecipeType<?> getType(){
		return DifModRecipes.FRYING_TYPE.get();
	}
	public static class Type implements RecipeType<FryingRecipe>{
	}
	public static class Serializer implements RecipeSerializer<FryingRecipe>{
		public static final MapCodec<FryingRecipe> CODEC=RecordCodecBuilder.mapCodec(instance->
				instance.group(
						Ingredient.CODEC.fieldOf("ingredient").forGetter(r->r.input),
						SizedFluidIngredient.FLAT_CODEC.fieldOf("fluid_ingredient").forGetter(r->r.fluidIngredient),
						ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r->r.output),
						Codec.INT.optionalFieldOf("processingTime",200).forGetter(r->r.processingTime)
				).apply(instance,FryingRecipe::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf,FryingRecipe> STREAM_CODEC=
				StreamCodec.composite(
						Ingredient.CONTENTS_STREAM_CODEC,r->r.input, SizedFluidIngredient.STREAM_CODEC,r->r.fluidIngredient,
						ItemStack.STREAM_CODEC,r->r.output,
						net.minecraft.network.codec.ByteBufCodecs.INT,r->r.processingTime,
						FryingRecipe::new
				);
		@Override
		public @NotNull MapCodec<FryingRecipe> codec(){
			return CODEC;
		}
		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf,FryingRecipe> streamCodec(){
			return STREAM_CODEC;
		}
	}
}