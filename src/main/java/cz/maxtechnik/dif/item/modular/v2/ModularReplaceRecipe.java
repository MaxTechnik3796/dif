package cz.maxtechnik.dif.item.modular.v2;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.dif.init.other.DifModComponents;
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
public class ModularReplaceRecipe implements SmithingRecipe{
	final Ingredient template;
	final Ingredient base;
	final Ingredient addition;
	final ItemStack result;
	public ModularReplaceRecipe(Ingredient template,Ingredient base,Ingredient addition,ItemStack result){
		this.template=template;
		this.base=base;
		this.addition=addition;
		this.result=result;
	}
	@Override
	public boolean matches(@NotNull SmithingRecipeInput container,@NotNull Level level){
		ItemStack part=container.getItem(0).copy();
		ItemStack tool=container.getItem(1).copy();
		if(this.template.test(part)&&this.base.test(tool)){
			ModularTools toolType=ModularTool.getToolType(tool);
			ModularParts localPart=ModularPart.getPart(part);
			if(ModularTools.isHead(toolType,localPart))
				return !ModularTool.getMaterial(ModularPartType.HEAD,tool).equals(ModularPart.getMaterial(part));
			if(ModularTools.isBinding(toolType,localPart))
				return !ModularTool.getMaterial(ModularPartType.BINDING,tool).equals(ModularPart.getMaterial(part));
			if(ModularTools.isHandle(toolType,localPart))
				return !ModularTool.getMaterial(ModularPartType.HANDLE,tool).equals(ModularPart.getMaterial(part));
		}
		return false;
	}
	@Override
	public @NotNull ItemStack assemble(SmithingRecipeInput container,@NotNull HolderLookup.Provider provider){
		ItemStack part=container.getItem(0).copy();
		ItemStack tool=container.getItem(1).copy();
		ModularMaterial head=ModularTool.getMaterial(ModularPartType.HEAD,tool);
		ModularMaterial binding=ModularTool.getMaterial(ModularPartType.HANDLE,tool);
		ModularMaterial handle=ModularTool.getMaterial(ModularPartType.HEAD,tool);
		switch(ModularPartType.getPartType(ModularPart.getPart(part))){
			case HEAD -> head=ModularPart.getMaterial(part);
			case BINDING -> binding=ModularPart.getMaterial(part);
			case HANDLE -> handle=ModularPart.getMaterial(part);
			default -> {
			}
		}
		tool.set(DifModComponents.MODULAR_TOOL_PROPERTIES.get(),new ModularToolProperties(ModularTool.getToolType(tool).getName(),head.getName(),binding.getName(),handle.getName(),ModularTool.getTier(tool).getName(),ModularTool.getReforge(tool).getName()));
		return tool;
	}
	@Override
	public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider provider){
		return this.result;
	}
	@Override
	public boolean isTemplateIngredient(@NotNull ItemStack itemStack){
		return this.template.test(itemStack)&&ModularPart.isModularPart(itemStack);
	}
	@Override
	public boolean isBaseIngredient(@NotNull ItemStack itemStack){
		return this.base.test(itemStack)&&ModularTool.isModularTool(itemStack);
	}
	@Override
	public boolean isAdditionIngredient(@NotNull ItemStack itemStack){
		return false;
	}
	public boolean isIncomplete(){
		return this.template.isEmpty()||this.base.isEmpty();
	}
	@Override
	public @NotNull RecipeSerializer<?> getSerializer(){
		return DifModRecipes.MODULAR_REPLACE_SERIALIZER.get();
	}
	public static class Serializer implements RecipeSerializer<ModularReplaceRecipe>{
		public static final MapCodec<ModularReplaceRecipe> CODEC=RecordCodecBuilder.mapCodec(inst->inst.group(Ingredient.CODEC.fieldOf("template").forGetter(r->r.template),Ingredient.CODEC.fieldOf("base").forGetter(r->r.base),Ingredient.CODEC.fieldOf("addition").forGetter(r->r.addition),ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r->r.result)).apply(inst,ModularReplaceRecipe::new));
		public static final StreamCodec<RegistryFriendlyByteBuf,ModularReplaceRecipe> STREAM_CODEC=StreamCodec.of((buf,r)->{
			Ingredient.CONTENTS_STREAM_CODEC.encode(buf,r.template);
			Ingredient.CONTENTS_STREAM_CODEC.encode(buf,r.base);
			Ingredient.CONTENTS_STREAM_CODEC.encode(buf,r.addition);
			ItemStack.STREAM_CODEC.encode(buf,r.result);
		},buf->{
			Ingredient template=Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			Ingredient base=Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			Ingredient addition=Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			ItemStack result=ItemStack.STREAM_CODEC.decode(buf);
			return new ModularReplaceRecipe(template,base,addition,result);
		});
		@Override
		public @NotNull MapCodec<ModularReplaceRecipe> codec(){
			return CODEC;
		}
		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf,ModularReplaceRecipe> streamCodec(){
			return STREAM_CODEC;
		}
	}
}