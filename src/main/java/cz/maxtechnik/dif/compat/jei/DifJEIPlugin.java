package cz.maxtechnik.dif.compat.jei;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.ForgeMaterialRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
@JeiPlugin
public class DifJEIPlugin implements IModPlugin {
	private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "jei_plugin");

	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return PLUGIN_ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		registration.addRecipeCategories(
				new BlastSmelteryJEI.Category(guiHelper),
				new CokeOvenJEI.Category(guiHelper),
				new DistillationJEI.Category(guiHelper),
				new ForgeMeltingJEI.Category(guiHelper),
				new ForgeMixingJEI.Category(guiHelper),
				new FryingJEI.Category(guiHelper)
		);
	}

	@Override
	public void registerRecipes(@NotNull IRecipeRegistration registration) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		// Blast Smeltery
		registration.addRecipes(BlastSmelteryJEI.TYPE, mc.level.getRecipeManager()
				.getAllRecipesFor(DifModRecipes.BLAST_SMELTERY_TYPE.get())
				.stream()
				.map(RecipeHolder::value)
				.toList());

		// Coke Oven
		registration.addRecipes(CokeOvenJEI.TYPE, mc.level.getRecipeManager()
				.getAllRecipesFor(DifModRecipes.COKE_OVEN_TYPE.get())
				.stream()
				.map(RecipeHolder::value)
				.toList());

		// Distillation
		registration.addRecipes(DistillationJEI.TYPE, mc.level.getRecipeManager()
				.getAllRecipesFor(DifModRecipes.DISTILLATION_TYPE.get())
				.stream()
				.map(RecipeHolder::value)
				.toList());

		// Forge Melting
		List<ForgeMaterialRecipe> rawMeltingRecipes = mc.level.getRecipeManager()
				.getAllRecipesFor(DifModRecipes.FORGE_MATERIAL_TYPE.get())
				.stream()
				.map(RecipeHolder::value)
				.toList();
		List<ForgeMeltingJEI.ForgeMeltingRecipeWrapper> meltingRecipes = new java.util.ArrayList<>();
		for (ForgeMaterialRecipe recipe : rawMeltingRecipes) {
			for (ForgeMaterialRecipe.MaterialConversion conv : recipe.conversions()) {
				if (conv.partType().isPresent() || conv.partMaterial().isPresent()) {
					continue;
				}
				meltingRecipes.add(new ForgeMeltingJEI.ForgeMeltingRecipeWrapper(recipe, conv));
			}
		}
		registration.addRecipes(ForgeMeltingJEI.TYPE, meltingRecipes);

		// Forge Mixing
		registration.addRecipes(ForgeMixingJEI.TYPE, mc.level.getRecipeManager()
				.getAllRecipesFor(DifModRecipes.FORGE_FLUID_MIXING_TYPE.get())
				.stream()
				.map(RecipeHolder::value)
				.toList());

		// Frying
		registration.addRecipes(FryingJEI.TYPE, mc.level.getRecipeManager()
				.getAllRecipesFor(DifModRecipes.FRYING_TYPE.get())
				.stream()
				.map(RecipeHolder::value)
				.toList());
	}

	@Override
	public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.BLAST_SMELTERY_CONTROLLER.get()), BlastSmelteryJEI.TYPE);
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.BLAST_SMELTERY.get()), BlastSmelteryJEI.TYPE);

		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.COKE_OVEN_CONTROLLER.get()), CokeOvenJEI.TYPE);
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.COKE_OVEN.get()), CokeOvenJEI.TYPE);

		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.DISTILLATION_TANK.get()), DistillationJEI.TYPE);

		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.FORGE_FURNACE_CONTROLLER.get()), ForgeMeltingJEI.TYPE);
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.FORGE_BRICK.get()), ForgeMeltingJEI.TYPE);

		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.FORGE_FURNACE_CONTROLLER.get()), ForgeMixingJEI.TYPE);
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.FORGE_BRICK.get()), ForgeMixingJEI.TYPE);

		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.FRYING_TABLE.get()), FryingJEI.TYPE);
	}
}
