package cz.maxtechnik.dif.compat.jei;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModRecipes;
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
				new DistillationJEI.Category(guiHelper),
				new FryingJEI.Category(guiHelper)
		);
	}

	@Override
	public void registerRecipes(@NotNull IRecipeRegistration registration) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		// Distillation
		registration.addRecipes(DistillationJEI.TYPE, mc.level.getRecipeManager()
				.getAllRecipesFor(DifModRecipes.DISTILLATION_TYPE.get())
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
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.DISTILLATION_TANK.get()), DistillationJEI.TYPE);

		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.FRYING_TABLE.get()), FryingJEI.TYPE);
	}
}
