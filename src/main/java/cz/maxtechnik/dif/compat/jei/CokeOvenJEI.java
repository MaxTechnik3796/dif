package cz.maxtechnik.dif.compat.jei;

import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.recipe.CokeOvenRecipe;
import cz.maxtechnik.dif.block.CokeOvenController;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.neoforge.NeoForgeTypes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CokeOvenJEI implements IRecipeCategory<CokeOvenRecipe> {
	private final IDrawable background;
	private final IDrawable icon;

	public CokeOvenJEI(IGuiHelper guiHelper) {
		this.background = guiHelper.createBlankDrawable(180, 96);
		this.icon = guiHelper.createDrawableItemStack(new ItemStack(DifModBlocks.COKE_OVEN_CONTROLLER.get()));
	}

	@Override
	public @NotNull RecipeType<CokeOvenRecipe> getRecipeType() {
		return DifJEIPlugin.COKE_OVEN_TYPE;
	}

	@Override
	public @NotNull Component getTitle() {
		return Component.translatable("jei.dif.coke_oven");
	}

	@Override
	public @NotNull IDrawable getBackground() {
		return background;
	}

	@Override
	public @NotNull IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CokeOvenRecipe recipe, @NotNull IFocusGroup focuses) {
		List<ItemStack> inputs = java.util.Arrays.stream(recipe.ingredient().getItems())
			.map(stack -> {
				ItemStack copy = stack.copy();
				copy.setCount(recipe.ingredientCount());
				return copy;
			}).toList();
		builder.addSlot(RecipeIngredientRole.INPUT, 75, 39)
			.setStandardSlotBackground()
			.addIngredients(VanillaTypes.ITEM_STACK, inputs);

		builder.addSlot(RecipeIngredientRole.OUTPUT, 135, 39)
			.setStandardSlotBackground()
			.addItemStack(recipe.result());

		if (!recipe.fluidOutput().isEmpty()) {
			builder.addSlot(RecipeIngredientRole.OUTPUT, 155, 39)
				.setStandardSlotBackground()
				.setFluidRenderer(recipe.fluidOutput().getAmount(), false, 16, 16)
				.addIngredient(NeoForgeTypes.FLUID_STACK, recipe.fluidOutput());
		}
	}

	@Override
	public void draw(@NotNull CokeOvenRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
		com.mojang.blaze3d.vertex.PoseStack matrixStack = graphics.pose();
		matrixStack.pushPose();

		matrixStack.translate(38, 52, 200);
		
		dev.engine_room.flywheel.lib.transform.TransformStack.of(matrixStack)
			.rotateXDegrees(-15f)
			.rotateYDegrees(-75f);
		
		int scale = 16;
		
		com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
		
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					// Render only outer blocks on top, front, and right faces
					if (x != 1 && y != 1 && z != 1) continue;
					
					BlockState state = (x == 1 && y == 0 && z == 0)
						? DifModBlocks.COKE_OVEN_CONTROLLER.get().defaultBlockState()
							.setValue(CokeOvenController.FACING, Direction.EAST)
							.setValue(CokeOvenController.FORMED, true)
							.setValue(CokeOvenController.ACTIVE, true)
						: DifModBlocks.COKE_OVEN.get().defaultBlockState();
						
					matrixStack.pushPose();
					matrixStack.translate(x * scale, -y * scale, z * scale);
					
					AnimatedKinetics.defaultBlockElement(state)
						.scale(scale)
						.render(graphics);
						
					matrixStack.popPose();
				}
			}
		}
		
		matrixStack.popPose();
	}

	@Override
	public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, @NotNull CokeOvenRecipe recipe, @NotNull IFocusGroup focuses) {
		int cookTime = recipe.processingTime();
		if (cookTime <= 0) cookTime = 900;
		builder.addAnimatedRecipeArrow(cookTime).setPosition(102, 39);
		int cookTimeSeconds = cookTime / 20;
		Component timeString = Component.translatable("gui.jei.category.smelting.time.seconds", cookTimeSeconds);
		builder.addText(timeString, 177 - 20, 10)
				.setPosition(0, 2, 177, 96, HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM)
				.setTextAlignment(HorizontalAlignment.RIGHT)
				.setTextAlignment(VerticalAlignment.BOTTOM)
				.setColor(0xFF808080);
	}
}
