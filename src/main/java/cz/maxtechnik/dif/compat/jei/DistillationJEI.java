package cz.maxtechnik.dif.compat.jei;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.recipe.DistillationRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class DistillationJEI {
	public static final RecipeType<DistillationRecipe> TYPE=RecipeType.create(DifMod.MODID,"distillation",DistillationRecipe.class);

	public static class Category extends JeiCompact.Category<DistillationRecipe>{
		public Category(IGuiHelper guiHelper){
			super(guiHelper,162,56,new ItemStack(DifModBlocks.DISTILLATION_TANK.get()),TYPE,"jei.dif.distillation");
		}
		@Override
		public void setRecipe(@NotNull IRecipeLayoutBuilder builder,@NotNull DistillationRecipe recipe,@NotNull IFocusGroup focuses){
			FluidStack[] matchingFluids=recipe.input().getFluids();
			builder.addSlot(RecipeIngredientRole.INPUT,1,19)
					.setStandardSlotBackground()
					.setFluidRenderer(recipe.input().amount(),false,16,16)
					.addIngredients(NeoForgeTypes.FLUID_STACK,java.util.Arrays.asList(matchingFluids));
			int size=recipe.outputs().size();
			for(int i=0;i<size;i++){
				int col=i%5;
				int row=i/5;
				int slotX=61+col*18;
				int slotY=19+row*18-(size>5?9:0);
				builder.addSlot(RecipeIngredientRole.OUTPUT,slotX,slotY)
						.setStandardSlotBackground()
						.setFluidRenderer(recipe.outputs().get(i).getAmount(),false,16,16)
						.addIngredient(NeoForgeTypes.FLUID_STACK,recipe.outputs().get(i));
			}
		}
		@Override
		public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder,@NotNull DistillationRecipe recipe,@NotNull IFocusGroup focuses){
			builder.addAnimatedRecipeArrow(100).setPosition(26,19);
		}
	}
}
