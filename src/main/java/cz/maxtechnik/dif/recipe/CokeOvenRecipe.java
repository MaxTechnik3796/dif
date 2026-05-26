package cz.maxtechnik.dif.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
 *   - doba zpracování v tickách (default 900 = 45s)
 *
 * Příklad JSON:
 * {
 *   "type": "dif:coke_oven",
 *   "ingredient": { "item": "minecraft:coal" },
 *   "ingredient_count": 1,
 *   "result": { "id": "dif:coke_coal", "count": 1 },
 *   "fluid_output": { "fluid": "dif:creosote_oil", "amount": 250 },
 *   "processing_time": 900
 * }
 */
public record CokeOvenRecipe(
        Ingredient ingredient,
        int ingredientCount,
        ItemStack result,
        FluidStack fluidOutput,
        int processingTime
) implements Recipe<SingleRecipeInput> {

    public static final int DEFAULT_TIME = 900;

    public static final MapCodec<CokeOvenRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(CokeOvenRecipe::ingredient),
            net.minecraft.util.ExtraCodecs.POSITIVE_INT.optionalFieldOf("ingredient_count", 1).forGetter(CokeOvenRecipe::ingredientCount),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(CokeOvenRecipe::result),
            FluidStack.CODEC.optionalFieldOf("fluid_output", FluidStack.EMPTY).forGetter(CokeOvenRecipe::fluidOutput),
            net.minecraft.util.ExtraCodecs.POSITIVE_INT.optionalFieldOf("processing_time", DEFAULT_TIME).forGetter(CokeOvenRecipe::processingTime)
    ).apply(inst, CokeOvenRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CokeOvenRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CokeOvenRecipe::ingredient,
            ByteBufCodecs.INT, CokeOvenRecipe::ingredientCount,
            ItemStack.STREAM_CODEC, CokeOvenRecipe::result,
            FluidStack.STREAM_CODEC, CokeOvenRecipe::fluidOutput,
            ByteBufCodecs.INT, CokeOvenRecipe::processingTime,
            CokeOvenRecipe::new
    );

    /** Zkontroluje jestli daný ItemStack odpovídá vstupu receptu. */
    public boolean matches(ItemStack stack) {
        return ingredient.test(stack) && stack.getCount() >= ingredientCount;
    }

    /** Má tento recept fluid výstup? */
    public boolean hasFluidOutput() {
        return !fluidOutput.isEmpty();
    }

    // ── Povinné Recipe metody (hledáme recept ručně, ne přes vanilla systém) ──

    @Override
    public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) {
        return matches(input.item());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SingleRecipeInput input, HolderLookup.@NotNull Provider provider) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) { return true; }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
        return result.copy();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return DifModRecipes.COKE_OVEN_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return DifModRecipes.COKE_OVEN_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<CokeOvenRecipe> {
        @Override
        public @NotNull MapCodec<CokeOvenRecipe> codec() { return CODEC; }
        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, CokeOvenRecipe> streamCodec() { return STREAM_CODEC; }
    }
}