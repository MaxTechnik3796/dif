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

import static cz.maxtechnik.dif.init.basic.DifModItems.MODULAR_TOOL;
import static cz.maxtechnik.dif.init.other.DifModComponents.MODULAR_TOOL_PROPERTIES;
import static cz.maxtechnik.dif.item.modular.v2.ModularPart.*;
import static cz.maxtechnik.dif.item.modular.v2.ModularPartType.*;
import static cz.maxtechnik.dif.item.modular.v2.ModularTier.*;
import static cz.maxtechnik.dif.item.modular.v2.ModularTools.getToolFromParts;
public class ModularAssemblyRecipe implements SmithingRecipe{
	final Ingredient template;
	final Ingredient base;
	final Ingredient addition;
	final ItemStack result;
	public ModularAssemblyRecipe(Ingredient template,Ingredient base,Ingredient addition,ItemStack result){
		this.template=template;
		this.base=base;
		this.addition=addition;
		this.result=result;
	}
	@Override
	public boolean matches(@NotNull SmithingRecipeInput container,@NotNull Level level){
		ItemStack binding=container.getItem(0).copy();
		ItemStack head=container.getItem(1).copy();
		ItemStack handle=container.getItem(2).copy();
		if(this.template.test(binding)&&this.base.test(head)&&this.addition.test(handle))
			return getToolFromParts(getPart(head),getPart(binding),getPart(handle))!=null;
		return false;
	}
	@Override
	public @NotNull ItemStack assemble(SmithingRecipeInput container,@NotNull HolderLookup.Provider provider){
		ItemStack binding=container.getItem(0).copy();
		ItemStack head=container.getItem(1).copy();
		ItemStack handle=container.getItem(2).copy();
		ItemStack tool=new ItemStack(MODULAR_TOOL.get());
		ModularTools toolType=getToolFromParts(getPart(head),getPart(binding),getPart(handle));
		if(toolType==null) return head;
		tool.set(MODULAR_TOOL_PROPERTIES.get(),new ModularToolProperties(toolType.getName(),getMaterial(head).getName(),getMaterial(binding).getName(),getMaterial(handle).getName(),calculateTier(head,binding,handle).getName(),ModularReforge.NONE.getName()));
		return tool;
	}
	private ModularTier calculateTier(ItemStack head,ItemStack binding,ItemStack handle){
		ModularTier h=getTier(head),b=getTier(binding),ha=getTier(handle);
		if(countOf(MYTHIC,h,b,ha)>1) return MYTHIC;
		if(countOf(LEGENDARY,h,b,ha)>1) return LEGENDARY;
		if(countOf(EPIC,h,b,ha)>1) return EPIC;
		if(countOf(RARE,h,b,ha)>1) return RARE;
		return COMMON;
	}
	private static int countOf(ModularTier tier,ModularTier a,ModularTier b,ModularTier c){
		return (a==tier?1:0)+(b==tier?1:0)+(c==tier?1:0);
	}
	@Override
	public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider provider){
		return this.result;
	}
	@Override
	public boolean isTemplateIngredient(@NotNull ItemStack itemStack){
		return this.template.test(itemStack)&&isBinding(getPart(itemStack));
	}
	@Override
	public boolean isBaseIngredient(@NotNull ItemStack itemStack){
		return this.base.test(itemStack)&&isHead(getPart(itemStack));
	}
	@Override
	public boolean isAdditionIngredient(@NotNull ItemStack itemStack){
		return this.addition.test(itemStack)&&isHandle(getPart(itemStack));
	}
	public boolean isIncomplete(){
		return this.template.isEmpty()||this.base.isEmpty()||this.addition.isEmpty();
	}
	@Override
	public @NotNull RecipeSerializer<?> getSerializer(){
		return DifModRecipes.MODULAR_ASSEMBLY_SERIALIZER.get();
	}
	public static class Serializer implements RecipeSerializer<ModularAssemblyRecipe>{
		public static final MapCodec<ModularAssemblyRecipe> CODEC=RecordCodecBuilder.mapCodec(inst->inst.group(Ingredient.CODEC.fieldOf("template").forGetter(r->r.template),Ingredient.CODEC.fieldOf("base").forGetter(r->r.base),Ingredient.CODEC.fieldOf("addition").forGetter(r->r.addition),ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r->r.result)).apply(inst,ModularAssemblyRecipe::new));
		public static final StreamCodec<RegistryFriendlyByteBuf,ModularAssemblyRecipe> STREAM_CODEC=StreamCodec.of((buf,r)->{
			Ingredient.CONTENTS_STREAM_CODEC.encode(buf,r.template);
			Ingredient.CONTENTS_STREAM_CODEC.encode(buf,r.base);
			Ingredient.CONTENTS_STREAM_CODEC.encode(buf,r.addition);
			ItemStack.STREAM_CODEC.encode(buf,r.result);
		},buf->{
			Ingredient template=Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			Ingredient base=Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			Ingredient addition=Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			ItemStack result=ItemStack.STREAM_CODEC.decode(buf);
			return new ModularAssemblyRecipe(template,base,addition,result);
		});
		@Override
		public @NotNull MapCodec<ModularAssemblyRecipe> codec(){
			return CODEC;
		}
		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf,ModularAssemblyRecipe> streamCodec(){
			return STREAM_CODEC;
		}
	}
}