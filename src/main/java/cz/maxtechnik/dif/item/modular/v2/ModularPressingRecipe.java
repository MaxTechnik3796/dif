package cz.maxtechnik.dif.item.modular.v2;

import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.other.DifModComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
/**
 * Custom pressing recipe for filled casting molds.
 * Pressing any filled modular mold (cast_mold=true) in the Create Mechanical Press
 * outputs the finished part (cast_mold=false) and returns the empty casting mold.
 * Extends PressingRecipe so its getType() returns create:pressing, meaning the
 * Mechanical Press will find and use this recipe automatically without any mixin.
 */
public class ModularPressingRecipe extends PressingRecipe{
	/** Stores the matched input between matches() and rollResults() calls on the same thread. */
	private static final ThreadLocal<ItemStack> CURRENT_INPUT=new ThreadLocal<>();
	public ModularPressingRecipe(ProcessingRecipeParams params){
		super(params);
	}
	@Override
	public boolean matches(@NotNull SingleRecipeInput inv,@NotNull Level worldIn){
		if(inv.isEmpty()) return false;
		ItemStack stack=inv.getItem(0);
		if(ModularPart.isModularPart(stack)&&ModularPart.isCast(stack)){
			CURRENT_INPUT.set(stack.copy());
			return true;
		}
		return false;
	}
	@Override
	public @NotNull List<ItemStack> rollResults(
			@NotNull List<ProcessingOutput> rollableResults,
			@NotNull RandomSource randomSource){
		ItemStack input=CURRENT_INPUT.get();
		CURRENT_INPUT.remove();
		if(input==null||input.isEmpty()){
			return super.rollResults(rollableResults,randomSource);
		}
		ModularParts partType=ModularPart.getPart(input);
		ModularMaterial material=ModularPart.getMaterial(input);
		// 1. Finished part without the mold marker
		ItemStack finishedPart=new ItemStack(DifModItems.MODULAR_PART.get());
		finishedPart.set(DifModComponents.MODULAR_PART_PROPERTIES.get(),
				new ModularPartProperties(partType.getName(),material.getName(),false));
		// 2. Return the empty casting mold
		ItemStack emptyMold=new ItemStack(partType.getCastingMold().get());
		List<ItemStack> results=new ArrayList<>();
		results.add(finishedPart);
		results.add(emptyMold);
		return results;
	}
}
