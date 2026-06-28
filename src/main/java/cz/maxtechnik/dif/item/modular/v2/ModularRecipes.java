package cz.maxtechnik.dif.item.modular.v2;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.dif.init.basic.DifModItems;
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
public class ModularRecipes extends SmithingExtraRecipe{
	private int efficiencyItems(ItemStack itemStack){
		return switch(ModularTool.getModifierLevel(itemStack,ModularModifier.EFFICIENCY)){
			case 0 -> 10;
			case 1 -> 20;
			case 2 -> 30;
			case 3 -> 40;
			case 4 -> 50;
			case 5 -> 64;
			default -> 0;
		};
	}
	public ModularRecipes(Ingredient template,Ingredient base,Ingredient addition,ItemStack result){
		// Zavoláme rodičovský konstruktor z Recipexu.
		// Pro šablonu a zbraň chceme 1 kus, pro modifikační materiál (addition) vyžadujeme celých 64 kusů!
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
			if(template.getItem().equals(DifModItems.MODULAR_TEMPLATE_EFFICIENCY.get())&&addition.getCount()>=efficiencyItems(base))
				return ModularTool.getModifierLevel(base,ModularModifier.EFFICIENCY)<ModularModifier.EFFICIENCY.getMaxLvl();
		}
		return false;
	}
	@Override
	public @NotNull ItemStack assemble(SmithingRecipeInput container,@NotNull HolderLookup.Provider provider){
		ItemStack template=container.getItem(0).copy();
		ItemStack base=container.getItem(1).copy();
		if(template.getItem().equals(DifModItems.MODULAR_TEMPLATE_EFFICIENCY.get()))
			ModularTool.upgradeModifier(provider,base,ModularModifier.EFFICIENCY);
		return base;
	}
	@Override
	public int getAdditionConsumeCount(SmithingRecipeInput container){
		ItemStack template=container.getItem(0).copy();
		ItemStack base=container.getItem(1).copy();
		if(template.getItem().equals(DifModItems.MODULAR_TEMPLATE_EFFICIENCY.get())) return efficiencyItems(base);
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