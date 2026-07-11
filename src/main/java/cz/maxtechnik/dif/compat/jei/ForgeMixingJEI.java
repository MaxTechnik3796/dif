package cz.maxtechnik.dif.compat.jei;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.recipe.ForgeFluidMixingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ForgeMixingJEI {
	public static final RecipeType<ForgeFluidMixingRecipe> TYPE=RecipeType.create(DifMod.MODID,"forge_mixing",ForgeFluidMixingRecipe.class);

	public static class Category extends JeiCompact.Category<ForgeFluidMixingRecipe>{
		public Category(IGuiHelper guiHelper){
			super(guiHelper,98,56,new ItemStack(DifModBlocks.FORGE_FURNACE_CONTROLLER.get()),TYPE,"jei.dif.forge_mixing");
		}
		@Override
		public void setRecipe(@NotNull IRecipeLayoutBuilder builder,@NotNull ForgeFluidMixingRecipe recipe,@NotNull IFocusGroup focuses){
			builder.addSlot(RecipeIngredientRole.INPUT,1,19)
					.setStandardSlotBackground()
					.setFluidRenderer(recipe.inputFluidA().getAmount(),false,16,16)
					.addIngredient(NeoForgeTypes.FLUID_STACK,recipe.inputFluidA());
			builder.addSlot(RecipeIngredientRole.INPUT,19,19)
					.setStandardSlotBackground()
					.setFluidRenderer(recipe.inputFluidB().getAmount(),false,16,16)
					.addIngredient(NeoForgeTypes.FLUID_STACK,recipe.inputFluidB());
			builder.addSlot(RecipeIngredientRole.OUTPUT,77,19)
					.setStandardSlotBackground()
					.setFluidRenderer(recipe.resultFluid().getAmount(),false,16,16)
					.addIngredient(NeoForgeTypes.FLUID_STACK,recipe.resultFluid());
		}
		@Override
		public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder,@NotNull ForgeFluidMixingRecipe recipe,@NotNull IFocusGroup focuses){
			builder.addAnimatedRecipeArrow(100).setPosition(42,19);
			if(recipe.minHeatTier()>0){
				net.minecraft.network.chat.Component heatString=net.minecraft.network.chat.Component.literal("Heat: Tier "+recipe.minHeatTier());
				builder.addText(heatString,96,10)
						.setPosition(0,2,98,56,HorizontalAlignment.RIGHT,VerticalAlignment.TOP)
						.setTextAlignment(HorizontalAlignment.RIGHT)
						.setTextAlignment(VerticalAlignment.TOP)
						.setColor(0xFFFF5555);
			}
		}
	}
}