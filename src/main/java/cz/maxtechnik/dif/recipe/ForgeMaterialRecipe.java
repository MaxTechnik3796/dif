package cz.maxtechnik.dif.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.util.ForgeMultiblockHelper;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Recept pro tavení materiálu v Forge peci.
 * Jeden JSON pokrývá všechny formy materiálu (ingot, block, ruda, raw).
 * JSON příklad:
 * {
 *   "type": "dif:forge_material",
 *   "min_heat_tier": 1,
 *   "processing_time": 80,
 *   "conversions": [
 *     { "ingredient": { "item": "minecraft:iron_ingot" }, "ingot_value": 1.0, "processing_time_multiplier": 1.0 },
 *     { "ingredient": { "item": "minecraft:iron_block" }, "ingot_value": 9.0, "processing_time_multiplier": 2.0 },
 *     { "ingredient": { "item": "minecraft:raw_iron"   }, "ingot_value": 1.5, "processing_time_multiplier": 0.8 }
 *   ],
 *   "result_fluid": { "id": "dif:molten_iron", "amount": 144 }
 * }
 * result_fluid.amount = mB za 1 ingot (ingot_value=1.0).
 * Výstup = result_fluid.amount * ingot_value, zaokrouhleno.
 * Čas = base * processing_time_multiplier.
 */
public record ForgeMaterialRecipe(
        int minHeatTier,
        int baseTime,
        List<MaterialConversion> conversions,
        FluidStack resultFluidPerIngot
) implements Recipe<SingleRecipeInput> {

    // ── Nested record ─────────────────────────────────────────────────────────

    public record MaterialConversion(Ingredient ingredient, float ingotValue, float processingTimeMultiplier) {
        public static final Codec<MaterialConversion> CODEC = RecordCodecBuilder.create(i -> i.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(MaterialConversion::ingredient),
                Codec.FLOAT.optionalFieldOf("ingot_value", 1.0f).forGetter(MaterialConversion::ingotValue),
                Codec.FLOAT.optionalFieldOf("processing_time_multiplier", 1.0f).forGetter(MaterialConversion::processingTimeMultiplier)
        ).apply(i, MaterialConversion::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MaterialConversion> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, MaterialConversion::ingredient,
                        ByteBufCodecs.FLOAT, MaterialConversion::ingotValue,
                        ByteBufCodecs.FLOAT, MaterialConversion::processingTimeMultiplier,
                        MaterialConversion::new);
    }

    // ── Codec ─────────────────────────────────────────────────────────────────

    public static final MapCodec<ForgeMaterialRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("min_heat_tier", 0).forGetter(ForgeMaterialRecipe::minHeatTier),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("processing_time", 80).forGetter(ForgeMaterialRecipe::baseTime),
            MaterialConversion.CODEC.listOf().fieldOf("conversions").forGetter(ForgeMaterialRecipe::conversions),
            FluidStack.CODEC.fieldOf("result_fluid").forGetter(ForgeMaterialRecipe::resultFluidPerIngot)
    ).apply(i, ForgeMaterialRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ForgeMaterialRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, ForgeMaterialRecipe::minHeatTier,
                    ByteBufCodecs.INT, ForgeMaterialRecipe::baseTime,
                    MaterialConversion.STREAM_CODEC.apply(ByteBufCodecs.list()), ForgeMaterialRecipe::conversions,
                    FluidStack.STREAM_CODEC, ForgeMaterialRecipe::resultFluidPerIngot,
                    ForgeMaterialRecipe::new);

    // ── API ───────────────────────────────────────────────────────────────────

    /** Najde konverzi pro daný item, nebo null. */
    public @Nullable MaterialConversion findConversion(ItemStack item) {
        if (item.isEmpty()) return null;
        for (var c : conversions) {
            if (c.ingredient().test(item)) return c;
        }
        return null;
    }

    /** Vrátí true pokud item sedí a heat je dostatečný. */
    public boolean matchesItem(ItemStack item, int heatPoints) {
        return findConversion(item) != null
                && heatPoints >= ForgeMultiblockHelper.minHeatForTier(minHeatTier);
    }

    /** Vrátí výstupní fluid pro daný item (amount = base * ingotValue), nebo EMPTY. */
    public FluidStack getOutputFor(ItemStack item) {
        var conv = findConversion(item);
        if (conv == null) return FluidStack.EMPTY;
        int amount = Math.round(resultFluidPerIngot.getAmount() * conv.ingotValue());
        return amount > 0 ? new FluidStack(resultFluidPerIngot.getFluid(), amount) : FluidStack.EMPTY;
    }

    /** Vrátí čas zpracování pro daný item (base * multiplier), nebo baseTime. */
    public int getProcessingTimeFor(ItemStack item) {
        var conv = findConversion(item);
        if (conv == null) return baseTime;
        return Math.round(baseTime * conv.processingTimeMultiplier());
    }

    // ── Recipe boilerplate ────────────────────────────────────────────────────

    @Override public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) {
        return findConversion(input.item()) != null;
    }
    @Override public @NotNull ItemStack assemble(@NotNull SingleRecipeInput i, HolderLookup.@NotNull Provider p) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider p) { return ItemStack.EMPTY; }
    @Override public @NotNull RecipeSerializer<?> getSerializer() { return DifModRecipes.FORGE_MATERIAL_SERIALIZER.get(); }
    @Override public @NotNull RecipeType<?> getType() { return DifModRecipes.FORGE_MATERIAL_TYPE.get(); }

    // ── Serializer ────────────────────────────────────────────────────────────

    public static class Serializer implements RecipeSerializer<ForgeMaterialRecipe> {
        @Override public @NotNull MapCodec<ForgeMaterialRecipe> codec() { return CODEC; }
        @Override public @NotNull StreamCodec<RegistryFriendlyByteBuf, ForgeMaterialRecipe> streamCodec() { return STREAM_CODEC; }
    }
}