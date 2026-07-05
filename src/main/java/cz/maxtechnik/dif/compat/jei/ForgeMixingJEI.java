package cz.maxtechnik.dif.compat.jei;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.ForgeFluidMixingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class ForgeMixingJEI implements IModPlugin {
	public static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(DifMod.MODID,
			"forge_mixing_jei");
	public static final RecipeType<ForgeFluidMixingRecipe> TYPE = RecipeType.create(DifMod.MODID, "forge_mixing",
			ForgeFluidMixingRecipe.class);

	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return PLUGIN_ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new Category(registration.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(@NotNull IRecipeRegistration registration) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return;
		List<ForgeFluidMixingRecipe> mixingRecipes = mc.level.getRecipeManager()
				.getAllRecipesFor(DifModRecipes.FORGE_FLUID_MIXING_TYPE.get())
				.stream()
				.map(RecipeHolder::value)
				.toList();
		registration.addRecipes(TYPE, mixingRecipes);
	}

	@Override
	public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.FORGE_FURNACE_CONTROLLER.get()), TYPE);
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.FORGE_BRICK.get()), TYPE);
	}

	public static class Category implements IRecipeCategory<ForgeFluidMixingRecipe> {
		private final IDrawable background;
		private final IDrawable icon;

		public Category(IGuiHelper guiHelper) {
			this.background = guiHelper.createBlankDrawable(98, 56);
			this.icon = guiHelper.createDrawableItemStack(new ItemStack(DifModBlocks.FORGE_FURNACE_CONTROLLER.get()));
		}

		@Override
		public @NotNull RecipeType<ForgeFluidMixingRecipe> getRecipeType() {
			return TYPE;
		}

		@Override
		public @NotNull Component getTitle() {
			return Component.translatable("jei.dif.forge_mixing");
		}

		@Override
		public @NotNull IDrawable getBackground() {
			return background;
		}

		@Override
		public @NotNull IDrawable getIcon() {
			return icon;
		}

		@Override
		public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull ForgeFluidMixingRecipe recipe,
				@NotNull IFocusGroup focuses) {
			builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
					.setStandardSlotBackground()
					.setFluidRenderer(recipe.inputFluidA().getAmount(), false, 16, 16)
					.addIngredient(NeoForgeTypes.FLUID_STACK, recipe.inputFluidA());

			builder.addSlot(RecipeIngredientRole.INPUT, 19, 19)
					.setStandardSlotBackground()
					.setFluidRenderer(recipe.inputFluidB().getAmount(), false, 16, 16)
					.addIngredient(NeoForgeTypes.FLUID_STACK, recipe.inputFluidB());

			builder.addSlot(RecipeIngredientRole.OUTPUT, 77, 19)
					.setStandardSlotBackground()
					.setFluidRenderer(recipe.resultFluid().getAmount(), false, 16, 16)
					.addIngredient(NeoForgeTypes.FLUID_STACK, recipe.resultFluid());
		}

		@Override
		public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, @NotNull ForgeFluidMixingRecipe recipe,
				@NotNull IFocusGroup focuses) {
			builder.addAnimatedRecipeArrow(100).setPosition(42, 19);

			if (recipe.minHeatTier() > 0) {
				Component heatString = Component.literal("Heat: Tier " + recipe.minHeatTier());
				builder.addText(heatString, 96, 10)
						.setPosition(0, 2, 98, 56, HorizontalAlignment.RIGHT, VerticalAlignment.TOP)
						.setTextAlignment(HorizontalAlignment.RIGHT)
						.setTextAlignment(VerticalAlignment.TOP)
						.setColor(0xFFFF5555);
			}
		}
	}
}