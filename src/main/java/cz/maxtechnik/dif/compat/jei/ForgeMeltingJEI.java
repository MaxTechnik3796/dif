package cz.maxtechnik.dif.compat.jei;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.ForgeMaterialRecipe;
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
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class ForgeMeltingJEI implements IModPlugin {
	public static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "forge_melting_jei");
	public static final RecipeType<ForgeMeltingRecipeWrapper> TYPE = RecipeType.create(DifMod.MODID, "forge_melting", ForgeMeltingRecipeWrapper.class);

	public static record ForgeMeltingRecipeWrapper(ForgeMaterialRecipe recipe, ForgeMaterialRecipe.MaterialConversion conversion) {}

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
		List<ForgeMaterialRecipe> rawMeltingRecipes = mc.level.getRecipeManager()
				.getAllRecipesFor(DifModRecipes.FORGE_MATERIAL_TYPE.get())
				.stream()
				.map(RecipeHolder::value)
				.toList();
		List<ForgeMeltingRecipeWrapper> meltingRecipes = new java.util.ArrayList<>();
		for (ForgeMaterialRecipe recipe : rawMeltingRecipes) {
			for (ForgeMaterialRecipe.MaterialConversion conv : recipe.conversions()) {
				if (conv.partType().isPresent() || conv.partMaterial().isPresent()) {
					continue;
				}
				meltingRecipes.add(new ForgeMeltingRecipeWrapper(recipe, conv));
			}
		}
		registration.addRecipes(TYPE, meltingRecipes);
	}

	@Override
	public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.FORGE_FURNACE_CONTROLLER.get()), TYPE);
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.FORGE_BRICK.get()), TYPE);
	}

	public static class Category implements IRecipeCategory<ForgeMeltingRecipeWrapper> {
		private final IDrawable background;
		private final IDrawable icon;

		public Category(IGuiHelper guiHelper) {
			this.background = guiHelper.createBlankDrawable(82, 56);
			this.icon = guiHelper.createDrawableItemStack(new ItemStack(DifModBlocks.FORGE_FURNACE_CONTROLLER.get()));
		}

		@Override
		public @NotNull RecipeType<ForgeMeltingRecipeWrapper> getRecipeType() {
			return TYPE;
		}

		@Override
		public @NotNull Component getTitle() {
			return Component.translatable("jei.dif.forge_melting");
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
		public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull ForgeMeltingRecipeWrapper wrapper, @NotNull IFocusGroup focuses) {
			builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
				.setStandardSlotBackground()
				.addIngredients(wrapper.conversion().ingredient());

			int amount = Math.round(wrapper.recipe().resultFluidPerIngot().getAmount() * wrapper.conversion().ingotValue());
			if (amount <= 0) amount = 1000;
			FluidStack outputFluid = new FluidStack(wrapper.recipe().resultFluidPerIngot().getFluid(), amount);

			builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 19)
				.setStandardSlotBackground()
				.setFluidRenderer(outputFluid.getAmount(), false, 16, 16)
				.addIngredient(NeoForgeTypes.FLUID_STACK, outputFluid);
		}

		@Override
		public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, @NotNull ForgeMeltingRecipeWrapper wrapper, @NotNull IFocusGroup focuses) {
			int cookTime = Math.round(wrapper.recipe().baseTime() * wrapper.conversion().processingTimeMultiplier());
			if (cookTime <= 0) cookTime = 80;
			builder.addAnimatedRecipeArrow(cookTime).setPosition(26, 19);
			int cookTimeSeconds = cookTime / 20;
			Component timeString = Component.translatable("gui.jei.category.smelting.time.seconds", cookTimeSeconds);
			builder.addText(timeString, 80, 10)
					.setPosition(0, 2, 82, 56, HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM)
					.setTextAlignment(HorizontalAlignment.RIGHT)
					.setTextAlignment(VerticalAlignment.BOTTOM)
					.setColor(0xFF808080);

			if (wrapper.recipe().minHeatTier() > 0) {
				Component heatString = Component.literal("Heat: Tier " + wrapper.recipe().minHeatTier());
				builder.addText(heatString, 80, 10)
						.setPosition(0, 2, 82, 56, HorizontalAlignment.RIGHT, VerticalAlignment.TOP)
						.setTextAlignment(HorizontalAlignment.RIGHT)
						.setTextAlignment(VerticalAlignment.TOP)
						.setColor(0xFFFF5555);
			}
		}
	}
}
