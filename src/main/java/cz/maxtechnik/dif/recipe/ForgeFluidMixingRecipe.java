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
 * Recept pro míchání kapalin v Forge peci.
 * Pokud jsou v peci obě vstupní kapaliny, každý tick se odebere
 * inputAmountPerTick mB z každé a přidá outputAmountPerTick mB výstupu.
 *
 * JSON příklad:
 * {
 *   "type": "dif:forge_fluid_mixing",
 *   "input_fluid_a": { "id": "minecraft:water",     "amount": 1 },
 *   "input_fluid_b": { "id": "minecraft:lava",      "amount": 1 },
 *   "result_fluid":  { "id": "create:honey",        "amount": 1 },
 *   "min_heat_tier": 1
 * }
 *
 * Množství jsou mB per tick. Pokud chceš pomalejší míchání, použij nižší čísla.
 */
public record ForgeFluidMixingRecipe(
        FluidStack inputFluidA,
        FluidStack inputFluidB,
        FluidStack resultFluid,
        int minHeatTier
) implements Recipe<SingleRecipeInput> {

    // ── Codec ─────────────────────────────────────────────────────────────────

    public static final MapCodec<ForgeFluidMixingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            FluidStack.CODEC.fieldOf("input_fluid_a").forGetter(ForgeFluidMixingRecipe::inputFluidA),
            FluidStack.CODEC.fieldOf("input_fluid_b").forGetter(ForgeFluidMixingRecipe::inputFluidB),
            FluidStack.CODEC.fieldOf("result_fluid").forGetter(ForgeFluidMixingRecipe::resultFluid),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("min_heat_tier", 0).forGetter(ForgeFluidMixingRecipe::minHeatTier)
    ).apply(i, ForgeFluidMixingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ForgeFluidMixingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    FluidStack.STREAM_CODEC, ForgeFluidMixingRecipe::inputFluidA,
                    FluidStack.STREAM_CODEC, ForgeFluidMixingRecipe::inputFluidB,
                    FluidStack.STREAM_CODEC, ForgeFluidMixingRecipe::resultFluid,
                    ByteBufCodecs.INT,        ForgeFluidMixingRecipe::minHeatTier,
                    ForgeFluidMixingRecipe::new);

    // ── API ───────────────────────────────────────────────────────────────────

    /**
     * Vrátí true pokud pec obsahuje dostatek obou vstupních kapalin
     * a heat je dostatečný.
     *
     * @param availableA  kolik mB kapaliny A je v peci
     * @param availableB  kolik mB kapaliny B je v peci
     * @param heatPoints  aktuální heat pece
     */
    public boolean canMix(int availableA, int availableB, int heatPoints) {
        return availableA >= inputFluidA.getAmount()
                && availableB >= inputFluidB.getAmount()
                && heatPoints >= cz.maxtechnik.dif.util.ForgeMultiblockHelper.minHeatForTier(minHeatTier);
    }

    /** Fluid A který recept konzumuje. */
    public net.minecraft.world.level.material.Fluid fluidA() { return inputFluidA.getFluid(); }

    /** Fluid B který recept konzumuje. */
    public net.minecraft.world.level.material.Fluid fluidB() { return inputFluidB.getFluid(); }

    /** Kolik mB A se odebere za 1 tick míchání. */
    public int consumeA() { return inputFluidA.getAmount(); }

    /** Kolik mB B se odebere za 1 tick míchání. */
    public int consumeB() { return inputFluidB.getAmount(); }

    /** Kolik mB výstupu se přidá za 1 tick míchání. */
    public int produceAmount() { return resultFluid.getAmount(); }

    /** FluidStack výstupu (kopie) pro přidání do tanku. */
    public FluidStack makeOutput() { return resultFluid.copy(); }

    // ── Recipe boilerplate (fluid recept, item input nedává smysl) ────────────

    @Override public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) { return false; }
    @Override public @NotNull ItemStack assemble(@NotNull SingleRecipeInput i, HolderLookup.@NotNull Provider p) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w, int h) { return false; }
    @Override public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider p) { return ItemStack.EMPTY; }
    @Override public @NotNull RecipeSerializer<?> getSerializer() { return DifModRecipes.FORGE_FLUID_MIXING_SERIALIZER.get(); }
    @Override public @NotNull RecipeType<?> getType() { return DifModRecipes.FORGE_FLUID_MIXING_TYPE.get(); }

    // ── Serializer ────────────────────────────────────────────────────────────

    public static class Serializer implements RecipeSerializer<ForgeFluidMixingRecipe> {
        @Override public @NotNull MapCodec<ForgeFluidMixingRecipe> codec() { return CODEC; }
        @Override public @NotNull StreamCodec<RegistryFriendlyByteBuf, ForgeFluidMixingRecipe> streamCodec() { return STREAM_CODEC; }
    }
}