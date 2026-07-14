package cz.maxtechnik.dif.item.modular.v2;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.mtrecipex.recipe.SizedIngredientExtra;
import cz.maxtechnik.mtrecipex.recipe.SmithingExtraRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static cz.maxtechnik.dif.init.basic.DifModItems.*;
import static cz.maxtechnik.dif.item.modular.v2.ModularModifier.*;
import static cz.maxtechnik.dif.item.modular.v2.ModularTool.getModifierLevel;
import static cz.maxtechnik.dif.item.modular.v2.ModularTool.upgradeModifier;
import static net.minecraft.world.item.Items.*;
public class ModularModifierRecipe extends SmithingExtraRecipe{
	private int getItemCount(ItemStack itemStack,ModularModifier modifier){
		return switch(modifier){
			case EFFICIENCY -> switch(getModifierLevel(itemStack,EFFICIENCY)){
				case 0 -> 16;
				case 1 -> 24;
				case 2,5 -> 32;
				case 3 -> 48;
				case 4 -> 64;
				default -> 0;
			};
			case SHARPNESS -> switch(getModifierLevel(itemStack,SHARPNESS)){
				case 0 -> 16;
				case 1 -> 24;
				case 2,5 -> 32;
				case 3 -> 48;
				case 4 -> 64;
				default -> 0;
			};
			case LUCK -> switch(getModifierLevel(itemStack,ModularModifier.LUCK)){
				case 0 -> 32;
				case 1 -> 48;
				case 2 -> 64;
				case 3 -> 16;
				default -> 0;
			};
			case SWEEPING_EDGE -> switch(getModifierLevel(itemStack,ModularModifier.SWEEPING_EDGE)){
				case 0 -> 16;
				case 1 -> 24;
				case 2 -> 32;
				case 3 -> 8;
				default -> 0;
			};
			case REINFORCED -> switch(getModifierLevel(itemStack,ModularModifier.REINFORCED)){
				case 0 -> 2;
				case 1 -> 4;
				case 2 -> 8;
				case 3 -> 24;
				default -> 0;
			};
			case VOLCANIC,MENDING -> 1;
			default -> 0;
		};
	}
	public ModularModifierRecipe(Ingredient template,Ingredient base,Ingredient addition,ItemStack result){
		super(new SizedIngredientExtra(template,1),new SizedIngredientExtra(base,1),new SizedIngredientExtra(addition,64),result);
	}
	@Override
	public boolean isTemplateIngredient(@NotNull ItemStack itemStack){
		return this.template.ingredient().test(itemStack);
	}
	@Override
	public boolean isBaseIngredient(@NotNull ItemStack itemStack){
		return this.base.ingredient().test(itemStack);
	}
	@Override
	public boolean isAdditionIngredient(@NotNull ItemStack itemStack){
		return this.addition.ingredient().test(itemStack);
	}
	private boolean isModifierAllowed(ItemStack base,ModularModifier modifier){
		String typeName=ModularTool.getProps(base).toolType();
		ModularTools tool=ModularTools.byName(typeName);
		return modifier.isAllowedOn(tool);
	}
	private ModularModifier getModifierForTemplate(Item templateItem,Item additionItem){
		if(templateItem.equals(MODULAR_TEMPLATE_NORMAL.get())){
			if(additionItem.equals(REDSTONE)) return EFFICIENCY;
			if(additionItem.equals(LAPIS_LAZULI)) return LUCK;
			if(additionItem.equals(QUARTZ)) return SHARPNESS;
			if(additionItem.equals(DIAMOND)) return REINFORCED;
			if(additionItem.equals(IRON_INGOT)) return SWEEPING_EDGE;
			if(additionItem.equals(SILKY_STONE.get())) return SILK_TOUCH;
			if(additionItem.equals(LAVA_BUCKET)) return VOLCANIC;
		}else if(templateItem.equals(MODULAR_TEMPLATE_HYPER.get())){
			if(additionItem.equals(REDSTONE_BLOCK)) return EFFICIENCY;
			if(additionItem.equals(LAPIS_BLOCK)) return LUCK;
			if(additionItem.equals(QUARTZ_BLOCK)) return SHARPNESS;
			if(additionItem.equals(DIAMOND)) return REINFORCED;
			if(additionItem.equals(IRON_BLOCK)) return SWEEPING_EDGE;
			if(additionItem.equals(Objects.requireNonNull(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:experience_block"))))) return MENDING;
		}
		return null;
	}
	@Override
	public boolean matches(@NotNull SmithingRecipeInput container,@NotNull Level level){
		ItemStack template=container.getItem(0).copy();
		ItemStack base=container.getItem(1).copy();
		ItemStack addition=container.getItem(2).copy();
		if(this.template.ingredient().test(template)&&this.base.ingredient().test(base)&&this.addition.ingredient().test(addition)){
			ModularModifier modifier=getModifierForTemplate(template.getItem(),addition.getItem());
			if(modifier==null) return false;
			if(!isModifierAllowed(base,modifier)) return false;
			if(addition.getCount()<getItemCount(base,modifier)) return false;
			if(modifier==LUCK&&getModifierLevel(base,SILK_TOUCH)>0) return false;
			if(modifier==SILK_TOUCH&&getModifierLevel(base,LUCK)>0) return false;
			int currentLvl=getModifierLevel(base,modifier);
			int maxLvl=modifier.getMaxLvl();
			if(template.getItem().equals(MODULAR_TEMPLATE_NORMAL.get())) return currentLvl<maxLvl;
			if(template.getItem().equals(MODULAR_TEMPLATE_HYPER.get())){
				if(modifier==MENDING) return currentLvl==0;
				return currentLvl==maxLvl;
			}
		}
		return false;
	}
	@Override
	public @NotNull ItemStack assemble(SmithingRecipeInput container,@NotNull HolderLookup.Provider provider){
		ItemStack base=container.getItem(1).copy();
		ItemStack template=container.getItem(0).copy();
		ItemStack addition=container.getItem(2).copy();
		ModularModifier modifier=getModifierForTemplate(template.getItem(),addition.getItem());
		if(modifier!=null) upgradeModifier(provider,base,modifier);
		return base;
	}
	@Override
	public int getAdditionConsumeCount(SmithingRecipeInput container){
		ItemStack base=container.getItem(1).copy();
		ItemStack template=container.getItem(0).copy();
		ItemStack addition=container.getItem(2).copy();
		ModularModifier modifier=getModifierForTemplate(template.getItem(),addition.getItem());
		if(modifier!=null) return getItemCount(base,modifier);
		return this.addition.count();
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
		return DifModRecipes.MODULAR_MODIFIER_SERIALIZER.get();
	}
	public static class Serializer implements RecipeSerializer<ModularModifierRecipe>{
		public static final MapCodec<ModularModifierRecipe> CODEC=RecordCodecBuilder.mapCodec(inst->inst.group(
				Ingredient.CODEC.fieldOf("template").forGetter(ModularModifierRecipe::getJsonTemplate),
				Ingredient.CODEC.fieldOf("base").forGetter(ModularModifierRecipe::getJsonBase),
				Ingredient.CODEC.fieldOf("addition").forGetter(ModularModifierRecipe::getJsonAddition),
				ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ModularModifierRecipe::getJsonResult)).apply(inst,ModularModifierRecipe::new));
		public static final StreamCodec<RegistryFriendlyByteBuf,ModularModifierRecipe> STREAM_CODEC=StreamCodec.of((buf,r)->{
			Ingredient.CONTENTS_STREAM_CODEC.encode(buf,r.getJsonTemplate());
			Ingredient.CONTENTS_STREAM_CODEC.encode(buf,r.getJsonBase());
			Ingredient.CONTENTS_STREAM_CODEC.encode(buf,r.getJsonAddition());
			ItemStack.STREAM_CODEC.encode(buf,r.getJsonResult());
		},buf->{
			Ingredient template=Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			Ingredient base=Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			Ingredient addition=Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			ItemStack result=ItemStack.STREAM_CODEC.decode(buf);
			return new ModularModifierRecipe(template,base,addition,result);
		});
		@Override
		public @NotNull MapCodec<ModularModifierRecipe> codec(){
			return CODEC;
		}
		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf,ModularModifierRecipe> streamCodec(){
			return STREAM_CODEC;
		}
	}
}