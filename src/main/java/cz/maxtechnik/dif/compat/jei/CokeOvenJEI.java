package cz.maxtechnik.dif.compat.jei;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.recipe.CokeOvenRecipe;
import mezz.jei.api.constants.VanillaTypes;
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

import java.util.List;

public class CokeOvenJEI {
	public static final RecipeType<CokeOvenRecipe> TYPE=RecipeType.create(DifMod.MODID,"coke_oven",CokeOvenRecipe.class);

	public static class Category extends JeiCompact.Category<CokeOvenRecipe>{
		public Category(IGuiHelper guiHelper){
			super(guiHelper,98,56,new ItemStack(DifModBlocks.COKE_OVEN_CONTROLLER.get()),TYPE,"jei.dif.coke_oven");
		}
		@Override
		public void setRecipe(@NotNull IRecipeLayoutBuilder builder,@NotNull CokeOvenRecipe recipe,@NotNull IFocusGroup focuses){
			List<ItemStack> inputs=java.util.Arrays.stream(recipe.ingredient().getItems())
					.map(stack->{
						ItemStack copy=stack.copy();
						copy.setCount(recipe.ingredientCount());
						return copy;
					}).toList();
			builder.addSlot(RecipeIngredientRole.INPUT,1,19)
					.setStandardSlotBackground()
					.addIngredients(VanillaTypes.ITEM_STACK,inputs);
			builder.addSlot(RecipeIngredientRole.OUTPUT,61,19)
					.setStandardSlotBackground()
					.addItemStack(recipe.result());
			if(!recipe.fluidOutput().isEmpty()){
				builder.addSlot(RecipeIngredientRole.OUTPUT,79,19)
						.setStandardSlotBackground()
						.setFluidRenderer(recipe.fluidOutput().getAmount(),false,16,16)
						.addIngredient(NeoForgeTypes.FLUID_STACK,recipe.fluidOutput());
			}
		}
		@Override
		public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder,@NotNull CokeOvenRecipe recipe,@NotNull IFocusGroup focuses){
			int cookTime=recipe.processingTime();
			if(cookTime<=0) cookTime=900;
			builder.addAnimatedRecipeArrow(cookTime).setPosition(26,19);
			int cookTimeSeconds=cookTime/20;
			net.minecraft.network.chat.Component timeString=net.minecraft.network.chat.Component.translatable("gui.jei.category.smelting.time.seconds",cookTimeSeconds);
			builder.addText(timeString,96,10)
					.setPosition(0,2,98,56,HorizontalAlignment.RIGHT,VerticalAlignment.BOTTOM)
					.setTextAlignment(HorizontalAlignment.RIGHT)
					.setTextAlignment(VerticalAlignment.BOTTOM)
					.setColor(0xFF808080);
		}
	}
}
