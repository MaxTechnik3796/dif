package cz.maxtechnik.dif.compat.jei;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.recipe.BlastSmelteryRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.constants.VanillaTypes;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlastSmelteryJEI {
	public static final RecipeType<BlastSmelteryRecipe> TYPE=RecipeType.create(DifMod.MODID,"blast_smeltery",BlastSmelteryRecipe.class);

	public static class Category extends JeiCompact.Category<BlastSmelteryRecipe>{
		public Category(IGuiHelper guiHelper){
			super(guiHelper,116,56,new ItemStack(DifModBlocks.BLAST_SMELTERY_CONTROLLER.get()),TYPE,"jei.dif.blast_smeltery");
		}
		@Override
		public void setRecipe(@NotNull IRecipeLayoutBuilder builder,@NotNull BlastSmelteryRecipe recipe,@NotNull IFocusGroup focuses){
			if(recipe.hasItemInput()){
				List<ItemStack> inputs=java.util.Arrays.stream(recipe.itemIngredient().getItems())
						.map(stack->{
							ItemStack copy=stack.copy();
							copy.setCount(recipe.itemIngredientCount());
							return copy;
						}).toList();
				builder.addSlot(RecipeIngredientRole.INPUT,1,19)
						.setStandardSlotBackground()
						.addIngredients(VanillaTypes.ITEM_STACK,inputs);
			}
			if(recipe.hasFluidInput()){
				builder.addSlot(RecipeIngredientRole.INPUT,19,19)
						.setStandardSlotBackground()
						.setFluidRenderer(recipe.fluidInput().getAmount(),false,16,16)
						.addIngredient(NeoForgeTypes.FLUID_STACK,recipe.fluidInput());
			}
			if(recipe.hasItemOutput()){
				builder.addSlot(RecipeIngredientRole.OUTPUT,77,19)
						.setStandardSlotBackground()
						.addItemStack(recipe.itemResult());
			}
			if(recipe.hasFluidOutput()){
				builder.addSlot(RecipeIngredientRole.OUTPUT,95,19)
						.setStandardSlotBackground()
						.setFluidRenderer(recipe.fluidOutput().getAmount(),false,16,16)
						.addIngredient(NeoForgeTypes.FLUID_STACK,recipe.fluidOutput());
			}
		}
		@Override
		public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder,@NotNull BlastSmelteryRecipe recipe,@NotNull IFocusGroup focuses){
			int cookTime=recipe.processingTime();
			if(cookTime<=0) cookTime=600;
			builder.addAnimatedRecipeArrow(cookTime).setPosition(42,19);
			int cookTimeSeconds=cookTime/20;
			net.minecraft.network.chat.Component timeString=net.minecraft.network.chat.Component.translatable("gui.jei.category.smelting.time.seconds",cookTimeSeconds);
			builder.addText(timeString,114,10)
					.setPosition(0,2,116,56,HorizontalAlignment.RIGHT,VerticalAlignment.BOTTOM)
					.setTextAlignment(HorizontalAlignment.RIGHT)
					.setTextAlignment(VerticalAlignment.BOTTOM)
					.setColor(0xFF808080);
		}
	}
}
