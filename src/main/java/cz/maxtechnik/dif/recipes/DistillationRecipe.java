package cz.maxtechnik.dif.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DistillationRecipe implements Recipe<CraftingInput> {

    private final FluidStack input;
    // outputs: index 0 = nejblíže controlleru (spodní tank), index N = nejvýše
    // V JSON souboru je to zapsáno od spoda nahoru – první output jde do nejnižšího tanku
    private final List<FluidStack> outputs;
    // processingTime v tickách pro heated, superheated = processingTime / 2
    private final int processingTime;

    public DistillationRecipe(FluidStack input, List<FluidStack> outputs, int processingTime) {
        this.input = input;
        this.outputs = outputs;
        this.processingTime = processingTime;
    }

    public FluidStack getInput() { return input; }
    public List<FluidStack> getOutputs() { return outputs; }
    public int getProcessingTime() { return processingTime; }

    /**
     * Kolik mb vstupu se spotřebuje každých 5 ticků.
     * sequences = processingTime / 5
     * mbPerSequence = input.amount / sequences
     * superheated = 2x více
     */
    public int getMbPerSequence(boolean superheated) {
        float sequences = processingTime / 5f;
        float mb = input.getAmount() / sequences;
        if (superheated) mb *= 2f;
        return Math.max(1, Math.round(mb));
    }

    @Override public boolean matches(@NotNull CraftingInput input, @NotNull Level level) { return false; }
    @Override public @NotNull ItemStack assemble(@NotNull CraftingInput input, HolderLookup.@NotNull Provider provider) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w, int h) { return false; }
    @Override public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) { return ItemStack.EMPTY; }
    @Override public @NotNull RecipeSerializer<?> getSerializer() { return DifModRecipes.DISTILLATION_SERIALIZER.get(); }
    @Override public @NotNull RecipeType<?> getType() { return DifModRecipes.DISTILLATION_TYPE.get(); }

    private static final Codec<FluidStack> FLUID_CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(fs -> BuiltInRegistries.FLUID.getKey(fs.getFluid())),
            Codec.INT.optionalFieldOf("amount", 1000).forGetter(FluidStack::getAmount)
    ).apply(i, (loc, amt) -> new FluidStack(BuiltInRegistries.FLUID.get(loc), amt)));

    private static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> FLUID_STREAM =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, fs -> BuiltInRegistries.FLUID.getKey(fs.getFluid()),
                    ByteBufCodecs.INT, FluidStack::getAmount,
                    (loc, amt) -> new FluidStack(BuiltInRegistries.FLUID.get(loc), amt));

    public static class Type implements RecipeType<DistillationRecipe> {
        public static final Type INSTANCE = new Type();
    }

    public static class Serializer implements RecipeSerializer<DistillationRecipe> {
        public static final MapCodec<DistillationRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                FLUID_CODEC.fieldOf("input").forGetter(DistillationRecipe::getInput),
                FLUID_CODEC.listOf().fieldOf("outputs").forGetter(DistillationRecipe::getOutputs),
                Codec.INT.optionalFieldOf("processingTime", 200).forGetter(DistillationRecipe::getProcessingTime)
        ).apply(i, DistillationRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, DistillationRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        FLUID_STREAM, DistillationRecipe::getInput,
                        FLUID_STREAM.apply(ByteBufCodecs.list()), DistillationRecipe::getOutputs,
                        ByteBufCodecs.INT, DistillationRecipe::getProcessingTime,
                        DistillationRecipe::new);

        @Override public @NotNull MapCodec<DistillationRecipe> codec() { return CODEC; }
        @Override public @NotNull StreamCodec<RegistryFriendlyByteBuf, DistillationRecipe> streamCodec() { return STREAM_CODEC; }
    }
}