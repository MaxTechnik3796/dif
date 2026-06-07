package cz.maxtechnik.dif.recipe;

import com.mojang.serialization.Codec;
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

import java.util.List;

/**
 * Material recipe — ONE JSON per material, covering all item forms.
 *
 * JSON format:
 * <pre>{@code
 * {
 *   "type": "dif:forge_material",
 *   "mb_per_ingot": 144,
 *   "min_heat_tier": 0,
 *   "processing_time": 80,
 *   "conversions": [
 *     { "ingredient": { "item": "minecraft:iron_ingot"  }, "ingot_value": 1.0 },
 *     { "ingredient": { "item": "minecraft:iron_block"  }, "ingot_value": 9.0 },
 *     { "ingredient": { "item": "minecraft:iron_ore"    }, "ingot_value": 2.0 },
 *     { "ingredient": { "item": "minecraft:raw_iron"    }, "ingot_value": 1.5 },
 *     { "ingredient": { "tag":  "minecraft:tools"       }, "ingot_value": 2.0 },
 *     { "ingredient": { "item": "minecraft:iron_pickaxe"}, "ingot_value": 2.0 }
 *   ],
 *   "result_fluid": { "id": "dif:molten_iron", "amount": 144 }
 * }
 * }</pre>
 *
 * The {@code result_fluid} amount is used as the per-ingot base; the actual
 * output for each conversion is {@code result_fluid.amount * ingot_value} mB.
 *
 * The controller looks up this type alongside {@link ForgeSmeltingRecipe}.
 * Use {@link #matchesItem(ItemStack)} and {@link #getOutputFor(ItemStack)} to
 * query results.
 */
public record ForgeMaterialRecipe(
        int mbPerIngot,
        int minHeatTier,
        int baseTime,
        List<MaterialConversion> conversions,
        FluidStack resultFluidPerIngot
) implements Recipe<SingleRecipeInput> {

    // ── Nested record ─────────────────────────────────────────────────────────

    public record MaterialConversion(Ingredient ingredient, float ingotValue) {

        public static final Codec<MaterialConversion> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(MaterialConversion::ingredient),
                Codec.FLOAT.optionalFieldOf("ingot_value", 1.0f).forGetter(MaterialConversion::ingotValue)
        ).apply(inst, MaterialConversion::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MaterialConversion> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, MaterialConversion::ingredient,
                        ByteBufCodecs.FLOAT, MaterialConversion::ingotValue,
                        MaterialConversion::new
                );
    }

    // ── Codec ─────────────────────────────────────────────────────────────────

    public static final MapCodec<ForgeMaterialRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("mb_per_ingot", 144).forGetter(ForgeMaterialRecipe::mbPerIngot),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("min_heat_tier", 0).forGetter(ForgeMaterialRecipe::minHeatTier),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("processing_time", 80).forGetter(ForgeMaterialRecipe::baseTime),
            MaterialConversion.CODEC.listOf().fieldOf("conversions").forGetter(ForgeMaterialRecipe::conversions),
            FluidStack.CODEC.fieldOf("result_fluid").forGetter(ForgeMaterialRecipe::resultFluidPerIngot)
    ).apply(inst, ForgeMaterialRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ForgeMaterialRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, ForgeMaterialRecipe::mbPerIngot,
                    ByteBufCodecs.INT, ForgeMaterialRecipe::minHeatTier,
                    ByteBufCodecs.INT, ForgeMaterialRecipe::baseTime,
                    MaterialConversion.STREAM_CODEC.apply(ByteBufCodecs.list()), ForgeMaterialRecipe::conversions,
                    FluidStack.STREAM_CODEC, ForgeMaterialRecipe::resultFluidPerIngot,
                    ForgeMaterialRecipe::new
            );

    // ── Matching ──────────────────────────────────────────────────────────────

    /** Returns the matching conversion for the given item, or null if none. */
    public MaterialConversion findConversion(ItemStack item) {
        if (item.isEmpty()) return null;
        for (MaterialConversion c : conversions) {
            if (c.ingredient().test(item)) return c;
        }
        return null;
    }

    /** True if any conversion accepts this item AND heat requirement is met. */
    public boolean matchesItem(ItemStack item, int heatPoints) {
        if (findConversion(item) == null) return false;
        int required = cz.maxtechnik.dif.util.ForgeMultiblockHelper.minHeatForTier(minHeatTier);
        return heatPoints >= required;
    }

    /**
     * Returns the FluidStack output for the given item based on its ingot value.
     * Returns FluidStack.EMPTY if item does not match.
     */
    public FluidStack getOutputFor(ItemStack item) {
        MaterialConversion conv = findConversion(item);
        if (conv == null) return FluidStack.EMPTY;
        int amount = Math.round(resultFluidPerIngot.getAmount() * conv.ingotValue());
        if (amount <= 0) return FluidStack.EMPTY;
        return new FluidStack(resultFluidPerIngot.getFluid(), amount);
    }

    // ── Recipe<SingleRecipeInput> boilerplate ─────────────────────────────────

    @Override
    public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) {
        return findConversion(input.item()) != null;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SingleRecipeInput input, HolderLookup.@NotNull Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override public boolean canCraftInDimensions(int w, int h) { return true; }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return DifModRecipes.FORGE_MATERIAL_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return DifModRecipes.FORGE_MATERIAL_TYPE.get();
    }

    // ── Serializer ────────────────────────────────────────────────────────────

    public static class Serializer implements RecipeSerializer<ForgeMaterialRecipe> {
        @Override
        public @NotNull MapCodec<ForgeMaterialRecipe> codec() { return CODEC; }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ForgeMaterialRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
