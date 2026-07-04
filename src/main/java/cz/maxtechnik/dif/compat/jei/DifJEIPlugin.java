package cz.maxtechnik.dif.compat.jei;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.CokeOvenRecipe;
import cz.maxtechnik.dif.recipe.FryingRecipe;
import cz.maxtechnik.dif.recipe.BlastSmelteryRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class DifJEIPlugin implements IModPlugin {
	public static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "jei_plugin");
	public static final RecipeType<FryingRecipe> FRYING_TYPE = RecipeType.create(DifMod.MODID, "frying", FryingRecipe.class);
	public static final RecipeType<CokeOvenRecipe> COKE_OVEN_TYPE = RecipeType.create(DifMod.MODID, "coke_oven", CokeOvenRecipe.class);
	public static final RecipeType<BlastSmelteryRecipe> BLAST_SMELTERY_TYPE = RecipeType.create(DifMod.MODID, "blast_smeltery", BlastSmelteryRecipe.class);

	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return PLUGIN_ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new FryingJEI(registration.getJeiHelpers().getGuiHelper()));
		registration.addRecipeCategories(new CokeOvenJEI(registration.getJeiHelpers().getGuiHelper()));
		registration.addRecipeCategories(new BlastSmelteryJEI(registration.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(@NotNull IRecipeRegistration registration) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		List<FryingRecipe> fryingRecipes = mc.level.getRecipeManager()
				.getAllRecipesFor(DifModRecipes.FRYING_TYPE.get())
				.stream()
				.map(RecipeHolder::value)
				.toList();
		registration.addRecipes(FRYING_TYPE, fryingRecipes);

		List<CokeOvenRecipe> cokeRecipes = mc.level.getRecipeManager()
				.getAllRecipesFor(DifModRecipes.COKE_OVEN_TYPE.get())
				.stream()
				.map(RecipeHolder::value)
				.toList();
		registration.addRecipes(COKE_OVEN_TYPE, cokeRecipes);

		List<BlastSmelteryRecipe> blastRecipes = mc.level.getRecipeManager()
				.getAllRecipesFor(DifModRecipes.BLAST_SMELTERY_TYPE.get())
				.stream()
				.map(RecipeHolder::value)
				.toList();
		registration.addRecipes(BLAST_SMELTERY_TYPE, blastRecipes);
	}

	@Override
	public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.FRYING_TABLE.get()), FRYING_TYPE);
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.COKE_OVEN_CONTROLLER.get()), COKE_OVEN_TYPE);
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.COKE_OVEN.get()), COKE_OVEN_TYPE);
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.BLAST_SMELTERY_CONTROLLER.get()), BLAST_SMELTERY_TYPE);
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.BLAST_SMELTERY.get()), BLAST_SMELTERY_TYPE);
	}
}
