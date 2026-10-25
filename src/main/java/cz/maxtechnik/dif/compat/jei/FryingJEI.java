package cz.maxtechnik.dif.compat.jei;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.recipe.FryingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
public class FryingJEI{
	public static final RecipeType<FryingRecipe> TYPE=RecipeType.create(DifMod.MODID,"frying",FryingRecipe.class);
	public static class Category extends JeiCompact.Category<FryingRecipe>{
		public Category(IGuiHelper guiHelper){
			super(guiHelper,82,56,new ItemStack(DifModBlocks.FRYING_TABLE.get()),TYPE,"jei.dif.frying_table");
		}
		@Override
		public void setRecipe(@NotNull IRecipeLayoutBuilder builder,@NotNull FryingRecipe recipe,@NotNull IFocusGroup focuses){
			builder.addSlot(RecipeIngredientRole.INPUT,1,1)
					.setStandardSlotBackground()
					.addIngredients(recipe.getIngredient());
			builder.addSlot(RecipeIngredientRole.OUTPUT,61,19)
					.setStandardSlotBackground()
					.addItemStack(recipe.getResultItem(Objects.requireNonNull(Minecraft.getInstance().level).registryAccess()));
			if(recipe.getOilAmount()>0){
				builder.addSlot(RecipeIngredientRole.INPUT,1,38)
						.setStandardSlotBackground()
						.setFluidRenderer(recipe.getOilAmount(),false,16,16)
						.addIngredients(NeoForgeTypes.FLUID_STACK,Arrays.asList(recipe.getFluidIngredient().getFluids()));
			}
		}
		@Override
		public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder,@NotNull FryingRecipe recipe,@NotNull IFocusGroup focuses){
			int cookTime=recipe.getProcessingTime()>0?recipe.getProcessingTime():400;
			builder.addAnimatedRecipeArrowWidget(cookTime).setPosition(26,19);
			builder.addAnimatedRecipeFlameWidget(300).setPosition(1,20);
			Component timeString=Component.translatable("gui.jei.category.smelting.time.seconds",cookTime/20);
			builder.addText(timeString,62,10)
					.setPosition(0,2,82,54,HorizontalAlignment.RIGHT,VerticalAlignment.BOTTOM)
					.setTextAlignment(HorizontalAlignment.RIGHT)
					.setTextAlignment(VerticalAlignment.BOTTOM)
					.setColor(0xFF808080);
		}
	}
}

