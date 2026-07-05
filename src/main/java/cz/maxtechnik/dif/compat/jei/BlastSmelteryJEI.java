package cz.maxtechnik.dif.compat.jei;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.BlastSmelteryRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
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
public class BlastSmelteryJEI implements IModPlugin {
	public static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "blast_smeltery_jei");
	public static final RecipeType<BlastSmelteryRecipe> TYPE = RecipeType.create(DifMod.MODID, "blast_smeltery", BlastSmelteryRecipe.class);

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
		if (mc.level == null) return;
		List<BlastSmelteryRecipe> recipes = mc.level.getRecipeManager()
				.getAllRecipesFor(DifModRecipes.BLAST_SMELTERY_TYPE.get())
				.stream()
				.map(RecipeHolder::value)
				.toList();
		registration.addRecipes(TYPE, recipes);
	}

	@Override
	public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.BLAST_SMELTERY_CONTROLLER.get()), TYPE);
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.BLAST_SMELTERY.get()), TYPE);
	}

	public static class Category implements IRecipeCategory<BlastSmelteryRecipe> {
		private final IDrawable background;
		private final IDrawable icon;

		public Category(IGuiHelper guiHelper) {
			this.background = guiHelper.createBlankDrawable(116, 56);
			this.icon = guiHelper.createDrawableItemStack(new ItemStack(DifModBlocks.BLAST_SMELTERY_CONTROLLER.get()));
		}

		@Override
		public @NotNull RecipeType<BlastSmelteryRecipe> getRecipeType() {
			return TYPE;
		}

		@Override
		public @NotNull Component getTitle() {
			return Component.translatable("jei.dif.blast_smeltery");
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
		public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull BlastSmelteryRecipe recipe, @NotNull IFocusGroup focuses) {
			if (recipe.hasItemInput()) {
				List<ItemStack> inputs = java.util.Arrays.stream(recipe.itemIngredient().getItems())
					.map(stack -> {
						ItemStack copy = stack.copy();
						copy.setCount(recipe.itemIngredientCount());
						return copy;
					}).toList();
				builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
					.setStandardSlotBackground()
					.addIngredients(VanillaTypes.ITEM_STACK, inputs);
			}

			if (recipe.hasFluidInput()) {
				builder.addSlot(RecipeIngredientRole.INPUT, 19, 19)
					.setStandardSlotBackground()
					.setFluidRenderer(recipe.fluidInput().getAmount(), false, 16, 16)
					.addIngredient(NeoForgeTypes.FLUID_STACK, recipe.fluidInput());
			}

			if (recipe.hasItemOutput()) {
				builder.addSlot(RecipeIngredientRole.OUTPUT, 77, 19)
					.setStandardSlotBackground()
					.addItemStack(recipe.itemResult());
			}

			if (recipe.hasFluidOutput()) {
				builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19)
					.setStandardSlotBackground()
					.setFluidRenderer(recipe.fluidOutput().getAmount(), false, 16, 16)
					.addIngredient(NeoForgeTypes.FLUID_STACK, recipe.fluidOutput());
			}
		}

		@Override
		public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, @NotNull BlastSmelteryRecipe recipe, @NotNull IFocusGroup focuses) {
			int cookTime = recipe.processingTime();
			if (cookTime <= 0) cookTime = 600;
			builder.addAnimatedRecipeArrow(cookTime).setPosition(42, 19);
			int cookTimeSeconds = cookTime / 20;
			Component timeString = Component.translatable("gui.jei.category.smelting.time.seconds", cookTimeSeconds);
			builder.addText(timeString, 114, 10)
					.setPosition(0, 2, 116, 56, HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM)
					.setTextAlignment(HorizontalAlignment.RIGHT)
					.setTextAlignment(VerticalAlignment.BOTTOM)
					.setColor(0xFF808080);
		}
	}
}
