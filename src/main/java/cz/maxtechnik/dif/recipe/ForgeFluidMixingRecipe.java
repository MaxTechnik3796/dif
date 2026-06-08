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
 * (consumeA * batches) mB z A a (consumeB * batches) mB z B,
 * kde batches = min(availableA/consumeA, availableB/consumeB, maxPerTick).
 * JSON příklad:
 * {
 *   "type": "dif:forge_fluid_mixing",
 *   "input_fluid_a": { "id": "minecraft:water", "amount": 1 },
 *   "input_fluid_b": { "id": "minecraft:lava",  "amount": 1 },
 *   "result_fluid":  { "id": "create:honey",    "amount": 1 },
 *   "max_per_tick":  20,
 *   "min_heat_tier": 1
 * }
 * max_per_tick: kolik dávek se zpracuje za 1 tick (výchozí 1).
 *   Příklad: amount=1, max_per_tick=20 → max 20 mB za tick.
 *   Bucket (1000 mB) pak trvá min. 50 ticků = 2.5s.
 */
public record ForgeFluidMixingRecipe(
        FluidStack inputFluidA,
        FluidStack inputFluidB,
        FluidStack resultFluid,
        int minHeatTier,
        int maxPerTick
) implements Recipe<SingleRecipeInput> {

    // ── Codec ─────────────────────────────────────────────────────────────────

    public static final MapCodec<ForgeFluidMixingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            FluidStack.CODEC.fieldOf("input_fluid_a").forGetter(ForgeFluidMixingRecipe::inputFluidA),
            FluidStack.CODEC.fieldOf("input_fluid_b").forGetter(ForgeFluidMixingRecipe::inputFluidB),
            FluidStack.CODEC.fieldOf("result_fluid").forGetter(ForgeFluidMixingRecipe::resultFluid),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("min_heat_tier", 0).forGetter(ForgeFluidMixingRecipe::minHeatTier),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_per_tick", 1).forGetter(ForgeFluidMixingRecipe::maxPerTick)
    ).apply(i, ForgeFluidMixingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ForgeFluidMixingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    FluidStack.STREAM_CODEC, ForgeFluidMixingRecipe::inputFluidA,
                    FluidStack.STREAM_CODEC, ForgeFluidMixingRecipe::inputFluidB,
                    FluidStack.STREAM_CODEC, ForgeFluidMixingRecipe::resultFluid,
                    ByteBufCodecs.INT,        ForgeFluidMixingRecipe::minHeatTier,
                    ByteBufCodecs.INT,        ForgeFluidMixingRecipe::maxPerTick,
                    ForgeFluidMixingRecipe::new);

    // ── API ───────────────────────────────────────────────────────────────────

    /**
     * Vrátí true pokud pec obsahuje alespoň 1 dávku obou vstupních kapalin
     * a heat je dostatečný.
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

    /** Kolik mB A se odebere za 1 dávku. */
    public int consumeA() { return inputFluidA.getAmount(); }

    /** Kolik mB B se odebere za 1 dávku. */
    public int consumeB() { return inputFluidB.getAmount(); }

    /**
     * Spočítá kolik dávek se zpracuje tento tick.
     * Omezeno dostupným množstvím obou kapalin a maxPerTick.
     */
    public int calcBatches(int availableA, int availableB) {
        return Math.min(
                Math.min(availableA / consumeA(), availableB / consumeB()),
                maxPerTick
        );
    }

    /** FluidStack výstupu pro daný počet dávek. */
    public FluidStack makeOutput(int batches) {
        return new FluidStack(resultFluid.getFluid(), resultFluid.getAmount() * batches);
    }

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