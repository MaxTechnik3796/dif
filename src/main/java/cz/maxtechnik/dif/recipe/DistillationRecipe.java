package cz.maxtechnik.dif.recipe;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
/**
 * Distillation recept: 1 vstupní fluid -> N výstupů (každý do svého tanku nad controllerem).
 * Index v outputs odpovídá pos.above(index+1): 0 -> tank těsně nad controllerem, 1 -> druhý, atd.
 * Maximum {@link #MAX_OUTPUTS} výstupů (= 15 tanků nad controllerem).
 */
public record DistillationRecipe(
		SizedFluidIngredient input,
		List<FluidStack> outputs
) implements Recipe<RecipeInput>{
	public static final int MAX_OUTPUTS=15;
	public static final MapCodec<DistillationRecipe> CODEC=RecordCodecBuilder.mapCodec(inst->inst.group(
			SizedFluidIngredient.FLAT_CODEC.fieldOf("input").forGetter(DistillationRecipe::input),
			FluidStack.CODEC.listOf().validate(DistillationRecipe::validateOutputs).fieldOf("outputs").forGetter(DistillationRecipe::outputs)
	).apply(inst,DistillationRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf,DistillationRecipe> STREAM_CODEC=StreamCodec.composite(
			SizedFluidIngredient.STREAM_CODEC,DistillationRecipe::input,
			FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_OUTPUTS)),DistillationRecipe::outputs,
			DistillationRecipe::new
	);
	private static DataResult<List<FluidStack>> validateOutputs(List<FluidStack> list){
		if(list.isEmpty()) return DataResult.error(()->"outputs must not be empty");
		if(list.size()>MAX_OUTPUTS) return DataResult.error(()->"outputs must not exceed "+MAX_OUTPUTS+" entries");
		return DataResult.success(list);
	}
	/**
	 * Kontrola, jestli fluid v tanku odpovídá ingredienci a má dostatečný amount.
	 */
	public boolean matches(FluidStack tankFluid){
		return input.test(tankFluid)&&tankFluid.getAmount()>=input.amount();
	}
	// Vyžadované metody Recipe<RecipeInput> - my recept hledáme manuálně, takže prázdné
	@Override
	public boolean matches(@NotNull RecipeInput input,@NotNull Level level){
		return false;
	}
	@Override
	public @NotNull ItemStack assemble(@NotNull RecipeInput input,HolderLookup.@NotNull Provider provider){
		return ItemStack.EMPTY;
	}
	@Override
	public boolean canCraftInDimensions(int w,int h){
		return true;
	}
	@Override
	public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider){
		return ItemStack.EMPTY;
	}
	@Override
	public @NotNull RecipeSerializer<?> getSerializer(){
		return DifModRecipes.DISTILLATION_SERIALIZER.get();
	}
	@Override
	public @NotNull RecipeType<?> getType(){
		return DifModRecipes.DISTILLATION_TYPE.get();
	}
	public static class Serializer implements RecipeSerializer<DistillationRecipe>{
		@Override
		public @NotNull MapCodec<DistillationRecipe> codec(){
			return CODEC;
		}
		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf,DistillationRecipe> streamCodec(){
			return STREAM_CODEC;
		}
	}
}