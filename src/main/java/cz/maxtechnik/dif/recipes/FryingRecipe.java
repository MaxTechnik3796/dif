package cz.maxtechnik.dif.recipes;

import cz.maxtechnik.dif.init.other.DifModRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.network.FriendlyByteBuf;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static cz.maxtechnik.dif.block.entity.FryingTableBlockEntity.INPUT_SLOT;
public class FryingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack output;
    private final int processingTime;
    private final int oilAmount;

    public FryingRecipe(ResourceLocation id, Ingredient input, ItemStack output, int processingTime, int oilAmount) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.processingTime = processingTime;
        this.oilAmount = oilAmount;
    }

    @Override
    public boolean matches(Container container,@NotNull Level level) {
        return input.test(container.getItem(INPUT_SLOT));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container container,@NotNull RegistryAccess access) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess access) { return output; }

    @Override
    public @NotNull ResourceLocation getId() { return id; }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() { return DifModRecipes.FRYING_SERIALIZER.get(); }

    @Override
    public @NotNull RecipeType<?> getType() { return Type.INSTANCE; }

    public int getProcessingTime() { return processingTime; }
    public int getOilAmount() { return oilAmount; }

    public static class Type implements RecipeType<FryingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "frying";
    }

    public static class Serializer implements RecipeSerializer<FryingRecipe> {
        @Override
        public @NotNull FryingRecipe fromJson(@NotNull ResourceLocation id,@NotNull JsonObject json) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int processingTime = GsonHelper.getAsInt(json, "processingTime", 200);
            int oilAmount = GsonHelper.getAsInt(json, "oilAmount", 100);
            return new FryingRecipe(id, input, output, processingTime, oilAmount);
        }

        @Override
        public @Nullable FryingRecipe fromNetwork(@NotNull ResourceLocation id,@NotNull FriendlyByteBuf buf) {
            Ingredient input = Ingredient.fromNetwork(buf);
            ItemStack output = buf.readItem();
            int processingTime = buf.readInt();
            int oilAmount = buf.readInt();
            return new FryingRecipe(id, input, output, processingTime, oilAmount);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buf,FryingRecipe recipe) {
            recipe.input.toNetwork(buf);
            buf.writeItem(recipe.output);
            buf.writeInt(recipe.processingTime);
            buf.writeInt(recipe.oilAmount);
        }
    }
}