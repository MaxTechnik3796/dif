package cz.maxtechnik.dif.compat.jei;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.FryingRecipe;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
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
import java.util.Objects;
@JeiPlugin
public class FryingJEI extends JeiCompact.Plugin{
	public static final ResourceLocation PLUGIN_ID=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"frying_jei");
	public static final RecipeType<FryingRecipe> TYPE=RecipeType.create(DifMod.MODID,"frying",FryingRecipe.class);
	public FryingJEI(){
		super("frying_jei");
	}
	@Override
	public void registerCategories(IRecipeCategoryRegistration registration){
		registration.addRecipeCategories(new Category(registration.getJeiHelpers().getGuiHelper()));
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
		registration.addRecipes(TYPE,recipes);
	}
	@Override
	public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration){
		registration.addRecipeCatalyst(new ItemStack(DifModBlocks.FRYING_TABLE.get()),TYPE);
	}
	public static class Category extends JeiCompact.Category<FryingRecipe>{
		public Category(IGuiHelper guiHelper){
			super(guiHelper,82,56,new ItemStack(DifModBlocks.FRYING_TABLE.get()),TYPE,"jei.dif.frying_table");
		}
		@Override
		public void setRecipe(@NotNull IRecipeLayoutBuilder builder,@NotNull FryingRecipe recipe,@NotNull IFocusGroup focuses){
			builder.addInputSlot(1,1).setStandardSlotBackground().addIngredients(recipe.getIngredient());
			builder.addOutputSlot(61,19).setOutputSlotBackground().addItemStack(recipe.getResultItem(Objects.requireNonNull(Minecraft.getInstance().level).registryAccess()));
			if(recipe.getOilAmount()>0){
				builder.addSlot(RecipeIngredientRole.INPUT,1,38)
						.setStandardSlotBackground()
						.setFluidRenderer(recipe.getOilAmount(),false,16,16)
						.addIngredient(NeoForgeTypes.FLUID_STACK,new FluidStack(DifModFluids.SUNFLOWER_OIL.get(),recipe.getOilAmount()));
			}
		}
		@Override
		public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder,@NotNull FryingRecipe recipe,@NotNull IFocusGroup focuses){
			int cookTime=recipe.getProcessingTime();
			if(cookTime<=0) cookTime=400;
			builder.addAnimatedRecipeArrow(cookTime).setPosition(26,19);
			builder.addAnimatedRecipeFlame(300).setPosition(1,20);
			int cookTimeSeconds=cookTime/20;
			Component timeString=Component.translatable("gui.jei.category.smelting.time.seconds",cookTimeSeconds);
			builder.addText(timeString,82-20,10)
					.setPosition(0,2,82,54,HorizontalAlignment.RIGHT,VerticalAlignment.BOTTOM)
					.setTextAlignment(HorizontalAlignment.RIGHT)
					.setTextAlignment(VerticalAlignment.BOTTOM)
					.setColor(0xFF808080);
		}
	}
}
