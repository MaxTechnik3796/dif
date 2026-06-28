package cz.maxtechnik.dif.item.modular.v2;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.mtrecipex.recipe.SizedIngredientExtra;
import cz.maxtechnik.mtrecipex.recipe.SmithingExtraRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static cz.maxtechnik.dif.init.basic.DifModItems.*;
import static cz.maxtechnik.dif.item.modular.v2.ModularModifier.*;
import static cz.maxtechnik.dif.item.modular.v2.ModularTool.getModifierLevel;
import static cz.maxtechnik.dif.item.modular.v2.ModularTool.upgradeModifier;
public class ModularRecipes extends SmithingExtraRecipe{
	private int getItemCount(ItemStack itemStack,ModularModifier modifier){
		return switch(modifier){
			case EFFICIENCY-> switch(getModifierLevel(itemStack,EFFICIENCY)){
				case 0 -> 10;
				case 1 -> 20;
				case 2 -> 30;
				case 3 -> 40;
				case 4 -> 50;
				case 5 -> 64;
				default -> 0;
			};
			case LUCK -> switch(getModifierLevel(itemStack,ModularModifier.LUCK)){
				case 0 -> 10;
				case 1 -> 20;
				case 2 -> 30;
				case 3 -> 40;
				default -> 0;
			};
			case SWEEPING_EDGE -> switch(getModifierLevel(itemStack,ModularModifier.SWEEPING_EDGE)){
				case 0 -> 10;
				case 1 -> 20;
				case 2 -> 30;
				case 3 -> 40;
				default -> 0;
			};
			default -> 0;
		};
	}
	public ModularRecipes(Ingredient template,Ingredient base,Ingredient addition,ItemStack result){
		super(
				new SizedIngredientExtra(template,1),
				new SizedIngredientExtra(base,1),
				new SizedIngredientExtra(addition,64),
				result
		);
	}
	@Override
	public boolean matches(@NotNull SmithingRecipeInput container,@NotNull Level level){
		ItemStack template=container.getItem(0).copy();
		ItemStack base=container.getItem(1).copy();
		ItemStack addition=container.getItem(2).copy();
		if(this.template.ingredient().test(template)&&this.base.ingredient().test(base)&&this.addition.ingredient().test(addition)){
			if(template.getItem().equals(MODULAR_TEMPLATE_EFFICIENCY.get())&&addition.getCount()>=getItemCount(base,EFFICIENCY))
				return getModifierLevel(base,EFFICIENCY)<EFFICIENCY.getMaxLvl();
			if(template.getItem().equals(MODULAR_TEMPLATE_LUCK.get())&&addition.getCount()>=getItemCount(base,ModularModifier.LUCK))
				return getModifierLevel(base,LUCK)<LUCK.getMaxLvl();
			if(template.getItem().equals(MODULAR_TEMPLATE_SWEEPING_EDGE.get())&&addition.getCount()>=getItemCount(base,ModularModifier.SWEEPING_EDGE))
				return getModifierLevel(base,SWEEPING_EDGE)<SWEEPING_EDGE.getMaxLvl();
		}
		return false;
	}
	@Override
	public @NotNull ItemStack assemble(SmithingRecipeInput container,@NotNull HolderLookup.Provider provider){
		ItemStack template=container.getItem(0).copy();
		ItemStack base=container.getItem(1).copy();
		if(template.getItem().equals(MODULAR_TEMPLATE_EFFICIENCY.get()))
			upgradeModifier(provider,base,EFFICIENCY);
		if(template.getItem().equals(MODULAR_TEMPLATE_LUCK.get()))
			upgradeModifier(provider,base,ModularModifier.LUCK);
		if(template.getItem().equals(MODULAR_TEMPLATE_SWEEPING_EDGE.get()))
			upgradeModifier(provider,base,SWEEPING_EDGE);
		return base;
	}
	@Override
	public int getAdditionConsumeCount(SmithingRecipeInput container){
		ItemStack template=container.getItem(0).copy();
		ItemStack base=container.getItem(1).copy();
		if(template.getItem().equals(MODULAR_TEMPLATE_EFFICIENCY.get())) return getItemCount(base,EFFICIENCY);
		if(template.getItem().equals(MODULAR_TEMPLATE_LUCK.get())) return getItemCount(base,ModularModifier.LUCK);
		if(template.getItem().equals(MODULAR_TEMPLATE_SWEEPING_EDGE.get())) return getItemCount(base,SWEEPING_EDGE);
		else return this.addition.count();
	}
	public Ingredient getJsonTemplate(){
		return this.template.ingredient();
	}
	public Ingredient getJsonBase(){
		return this.base.ingredient();
	}
	public Ingredient getJsonAddition(){
		return this.addition.ingredient();
	}
	public ItemStack getJsonResult(){
		return this.result;
	}
	@Override
	public @NotNull RecipeSerializer<?> getSerializer(){
		return DifModRecipes.MODULAR_REPAIR_SERIALIZER.get();
	}
	public static class Serializer implements RecipeSerializer<ModularRecipes>{
		public static final MapCodec<ModularRecipes> CODEC=RecordCodecBuilder.mapCodec(inst->inst.group(
				Ingredient.CODEC.fieldOf("template").forGetter(ModularRecipes::getJsonTemplate),
				Ingredient.CODEC.fieldOf("base").forGetter(ModularRecipes::getJsonBase),
				Ingredient.CODEC.fieldOf("addition").forGetter(ModularRecipes::getJsonAddition),
				ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ModularRecipes::getJsonResult)
		).apply(inst,ModularRecipes::new));
		public static final StreamCodec<RegistryFriendlyByteBuf,ModularRecipes> STREAM_CODEC=StreamCodec.of(
				(buf,r)->{
					Ingredient.CONTENTS_STREAM_CODEC.encode(buf,r.getJsonTemplate());
					Ingredient.CONTENTS_STREAM_CODEC.encode(buf,r.getJsonBase());
					Ingredient.CONTENTS_STREAM_CODEC.encode(buf,r.getJsonAddition());
					ItemStack.STREAM_CODEC.encode(buf,r.getJsonResult());
				},
				buf->{
					Ingredient template=Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
					Ingredient base=Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
					Ingredient addition=Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
					ItemStack result=ItemStack.STREAM_CODEC.decode(buf);
					return new ModularRecipes(template,base,addition,result);
				}
		);
		@Override
		public @NotNull MapCodec<ModularRecipes> codec(){
			return CODEC;
		}
		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf,ModularRecipes> streamCodec(){
			return STREAM_CODEC;
		}
	}
}