package cz.maxtechnik.dif.compat.jei;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.FryingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
@SuppressWarnings("removal")
@JeiPlugin
public class FryingTableJeiPlugin implements IModPlugin{
	public static final ResourceLocation PLUGIN_ID=
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"jei_plugin");
	public static final RecipeType<FryingRecipe> FRYING_TYPE=
			RecipeType.create(DifMod.MODID,"frying",FryingRecipe.class);
	@Override
	public @NotNull ResourceLocation getPluginUid(){
		return PLUGIN_ID;
	}
	@Override
	public void registerCategories(IRecipeCategoryRegistration registration){
		registration.addRecipeCategories(
				new FryingCategory(registration.getJeiHelpers().getGuiHelper()));
	}
	@Override
	public void registerRecipes(@NotNull IRecipeRegistration registration){
		Minecraft mc=Minecraft.getInstance();
		if(mc.level==null) return;
		List<FryingRecipe> recipes=mc.level.getRecipeManager()
				.getAllRecipesFor(DifModRecipes.FRYING_TYPE.get())
				.stream()
				.map(RecipeHolder::value)
				.toList();
		registration.addRecipes(FRYING_TYPE,recipes);
	}
	public static class FryingCategory implements IRecipeCategory<FryingRecipe>{
		private final IDrawable background;
		private final IDrawable icon;
		public FryingCategory(IGuiHelper guiHelper){
			this.background=guiHelper.createBlankDrawable(82,44);
			this.icon=guiHelper.createDrawableItemStack(
					new ItemStack(DifModBlocks.FRYING_TABLE.get()));
		}
		@Override
		public @NotNull RecipeType<FryingRecipe> getRecipeType(){
			return FRYING_TYPE;
		}
		@Override
		public @NotNull Component getTitle(){
			return Component.translatable("jei.dif.frying_table");
		}
		@Override
		public @NotNull IDrawable getBackground(){
			return background;
		}
		@Override
		public @NotNull IDrawable getIcon(){
			return icon;
		}
		@Override
		public void setRecipe(@NotNull IRecipeLayoutBuilder builder,
		                      @NotNull FryingRecipe recipe,
		                      @NotNull IFocusGroup focuses){
			builder.addInputSlot(1,1)
					.setStandardSlotBackground()
					.addIngredients(recipe.getIngredient());
			builder.addOutputSlot(61,9)
					.setOutputSlotBackground()
					.addItemStack(recipe.getResultItem(
							Objects.requireNonNull(Minecraft.getInstance().level).registryAccess()));
		}
		@Override
		public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder,
		                               @NotNull FryingRecipe recipe,
		                               @NotNull IFocusGroup focuses){
			int cookTime=recipe.getProcessingTime();
			if(cookTime<=0){
				cookTime=400;
			}
			builder.addAnimatedRecipeArrow(cookTime)
					.setPosition(26,7);
			builder.addAnimatedRecipeFlame(300)
					.setPosition(1,20);
			int cookTimeSeconds=cookTime/20;
			Component timeString=Component.translatable("gui.jei.category.smelting.time.seconds",cookTimeSeconds);
			builder.addText(timeString,82-20,10)
					.setPosition(0,0,82,44,HorizontalAlignment.RIGHT,VerticalAlignment.BOTTOM)
					.setTextAlignment(HorizontalAlignment.RIGHT)
					.setTextAlignment(VerticalAlignment.BOTTOM)
					.setColor(0xFF808080);
		}
	}
}