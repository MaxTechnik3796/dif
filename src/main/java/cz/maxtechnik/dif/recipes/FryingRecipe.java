package cz.maxtechnik.dif.recipes;

import cz.maxtechnik.dif.init.other.DifModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static cz.maxtechnik.dif.block.entity.FryingTableBlockEntity.INPUT_SLOT;

public class FryingRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient input;
    private final ItemStack output;
    private final int processingTime;
    private final int oilAmount;

    public FryingRecipe(Ingredient input, ItemStack output, int processingTime, int oilAmount) {
        this.input = input;
        this.output = output;
        this.processingTime = processingTime;
        this.oilAmount = oilAmount;
    }

    @Override
    public boolean matches(@NotNull SingleRecipeInput recipeInput, @NotNull Level level) {
        return input.test(recipeInput.getItem(INPUT_SLOT));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SingleRecipeInput recipeInput, HolderLookup.@NotNull Provider provider) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
        return output;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return DifModRecipes.FRYING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return DifModRecipes.FRYING_TYPE.get();
    }

    public int getProcessingTime() { return processingTime; }
    public int getOilAmount() { return oilAmount; }
    public Ingredient getInput() { return input; }

    // -------------------------------------------------------------------------
    // Recipe Type
    // -------------------------------------------------------------------------
    public static class Type implements RecipeType<FryingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "frying";
    }

    // -------------------------------------------------------------------------
    // Serializer – v 1.21.1 NeoForge: MapCodec + StreamCodec, žádný fromJson/fromNetwork
    // -------------------------------------------------------------------------
    public static class Serializer implements RecipeSerializer<FryingRecipe> {

        public static final MapCodec<FryingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.input),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.output),
                        Codec.INT.optionalFieldOf("processingTime", 200).forGetter(r -> r.processingTime),
                        Codec.INT.optionalFieldOf("oilAmount", 100).forGetter(r -> r.oilAmount)
                ).apply(instance, FryingRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, FryingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, r -> r.input,
                        ItemStack.STREAM_CODEC, r -> r.output,
                        net.minecraft.network.codec.ByteBufCodecs.INT, r -> r.processingTime,
                        net.minecraft.network.codec.ByteBufCodecs.INT, r -> r.oilAmount,
                        FryingRecipe::new
                );

        @Override
        public @NotNull MapCodec<FryingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, FryingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}