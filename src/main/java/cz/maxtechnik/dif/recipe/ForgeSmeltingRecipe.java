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
 * Recept pro tavení v Forge peci:
 *   - 1 item vstup (ingredient)
 *   - minimální heat tier (0-3)
 *   - 1 fluid výstup (roztavený kov)
 *   - základní čas zpracování v tickách
 */
public record ForgeSmeltingRecipe(
        Ingredient ingredient,
        int minHeatTier,
        FluidStack outputFluid,
        int baseTime
) implements Recipe<SingleRecipeInput> {

    public static final MapCodec<ForgeSmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(ForgeSmeltingRecipe::ingredient),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("min_heat_tier", 0).forGetter(ForgeSmeltingRecipe::minHeatTier),
            FluidStack.CODEC.fieldOf("result_fluid").forGetter(ForgeSmeltingRecipe::outputFluid),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("processing_time", 80).forGetter(ForgeSmeltingRecipe::baseTime)
    ).apply(inst, ForgeSmeltingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ForgeSmeltingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, ForgeSmeltingRecipe::ingredient,
            ByteBufCodecs.INT, ForgeSmeltingRecipe::minHeatTier,
            FluidStack.STREAM_CODEC, ForgeSmeltingRecipe::outputFluid,
            ByteBufCodecs.INT, ForgeSmeltingRecipe::baseTime,
            ForgeSmeltingRecipe::new
    );

    /**
     * Ověří zda daný input item odpovídá tomuto receptu
     * a zda je k dispozici dostatečný heat tier.
     *
     * @param input      item ve vstupním slotu
     * @param heatPoints aktuální heat body controlleru
     */
    public boolean matches(ItemStack input, int heatPoints) {
        if (input.isEmpty()) return false;
        if (!ingredient.test(input)) return false;
        int requiredPoints = cz.maxtechnik.dif.util.ForgeMultiblockHelper.minHeatForTier(minHeatTier);
        return heatPoints >= requiredPoints;
    }

    // ── Recipe<SingleRecipeInput> (vanilla rozhraní — recept hledáme ručně) ──

    @Override
    public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SingleRecipeInput input, HolderLookup.@NotNull Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return DifModRecipes.FORGE_SMELTING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return DifModRecipes.FORGE_SMELTING_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<ForgeSmeltingRecipe> {
        @Override
        public @NotNull MapCodec<ForgeSmeltingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ForgeSmeltingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}