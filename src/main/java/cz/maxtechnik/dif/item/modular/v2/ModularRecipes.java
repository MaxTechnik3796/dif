package cz.maxtechnik.dif.item.modular.v2;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
public class ModularRecipes implements SmithingRecipe{
	final Ingredient template;
	final Ingredient base;
	final Ingredient addition;
	final ItemStack result;
	public ModularRecipes(Ingredient template,Ingredient base,Ingredient addition,ItemStack result){
		this.template=template;
		this.base=base;
		this.addition=addition;
		this.result=result;
	}
	@Override
	public boolean matches(SmithingRecipeInput container,@NotNull Level world){
		return false;
	}
	@Override
	public @NotNull ItemStack assemble(SmithingRecipeInput container,@NotNull HolderLookup.Provider provider){
		ItemStack template=container.getItem(0).copy();
		return template;
	}
	@Override
	public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider provider){
		return this.result;
	}
	public boolean isTemplateIngredient(@NotNull ItemStack itemStack){
		return this.template.test(itemStack);
	}
	public boolean isBaseIngredient(@NotNull ItemStack itemStack){
		return this.base.test(itemStack);
	}
	public boolean isAdditionIngredient(@NotNull ItemStack itemStack){
		return this.addition.test(itemStack);
	}
	public boolean isIncomplete(){
		return this.base.isEmpty();
	}
	@Override
	public @NotNull RecipeSerializer<?> getSerializer(){
		return DifModRecipes.MODULAR_REPAIR_SERIALIZER.get();
	}
	public static class Serializer implements RecipeSerializer<ModularRecipes>{
		public static final MapCodec<ModularRecipes> CODEC=RecordCodecBuilder.mapCodec(inst->inst.group(
				Ingredient.CODEC.fieldOf("template").forGetter(r->r.template),
				Ingredient.CODEC.fieldOf("base").forGetter(r->r.base),
				Ingredient.CODEC.fieldOf("addition").forGetter(r->r.addition),
				ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r->r.result)
		).apply(inst,ModularRecipes::new));
		public static final StreamCodec<RegistryFriendlyByteBuf,ModularRecipes> STREAM_CODEC=StreamCodec.of(
				(buf,r)->{
					Ingredient.CONTENTS_STREAM_CODEC.encode(buf,r.template);
					Ingredient.CONTENTS_STREAM_CODEC.encode(buf,r.base);
					Ingredient.CONTENTS_STREAM_CODEC.encode(buf,r.addition);
					ItemStack.STREAM_CODEC.encode(buf,r.result);
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