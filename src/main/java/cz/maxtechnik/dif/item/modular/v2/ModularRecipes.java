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
public class ModularRecipes extends SmithingExtraRecipe{
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
				case 0 -> 16;
				case 1 -> 24;
				case 2 -> 32;
				case 3 -> 8;
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
	private boolean isModifierAllowed(ItemStack base, ModularModifier modifier) {
		String type = ModularTool.getProps(base).toolType().toLowerCase(java.util.Locale.ROOT);
		if (type.equals("axe") || type.equals("battle_axe")) {
			return modifier!=ModularModifier.SWEEPING_EDGE&&modifier!=ModularModifier.LUCK;
		} else if (type.equals("sword") || type.equals("katana")) {
			return modifier!=ModularModifier.EFFICIENCY&&modifier!=ModularModifier.SILK_TOUCH;
		} else {
			return modifier!=ModularModifier.SHARPNESS&&modifier!=ModularModifier.LUCK&&modifier!=ModularModifier.SWEEPING_EDGE;
		}
	}

	private ModularModifier getModifierForTemplate(Item templateItem,Item additionItem) {
		if (templateItem.equals(MODULAR_TEMPLATE_NORMAL.get())) {
			if (additionItem.equals(REDSTONE)) return EFFICIENCY;
			if (additionItem.equals(LAPIS_LAZULI)) return LUCK;
			if (additionItem.equals(QUARTZ)) return SHARPNESS;
			if (additionItem.equals(DIAMOND)) return REINFORCED;
			if (additionItem.equals(IRON_INGOT)) return SWEEPING_EDGE;
			if (additionItem.equals(Objects.requireNonNull(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:experience_block"))))) return MENDING;
			if (additionItem.equals(SILKY_STONE.get())) return SILK_TOUCH;
		} else if (templateItem.equals(MODULAR_TEMPLATE_HYPER.get())) {
			if (additionItem.equals(REDSTONE_BLOCK)) return EFFICIENCY;
			if (additionItem.equals(LAPIS_BLOCK)) return LUCK;
			if (additionItem.equals(QUARTZ_BLOCK)) return SHARPNESS;
			if (additionItem.equals(DIAMOND)) return REINFORCED;
			if (additionItem.equals(IRON_BLOCK)) return SWEEPING_EDGE;
		}
		return null;
	}

	@Override
	public boolean matches(@NotNull SmithingRecipeInput container,@NotNull Level level){
		ItemStack template=container.getItem(0).copy();
		ItemStack base=container.getItem(1).copy();
		ItemStack addition=container.getItem(2).copy();
		if(this.template.ingredient().test(template)&&this.base.ingredient().test(base)&&this.addition.ingredient().test(addition)){
			ModularModifier modifier = getModifierForTemplate(template.getItem(), addition.getItem());
			if (modifier == null) return false;

			if (!isModifierAllowed(base, modifier)) return false;
			if (addition.getCount() < getItemCount(base, modifier)) return false;

			if (modifier == LUCK && getModifierLevel(base, SILK_TOUCH) > 0) return false;
			if (modifier == SILK_TOUCH && getModifierLevel(base, LUCK) > 0) return false;

			int currentLvl = getModifierLevel(base, modifier);
			int maxLvl = modifier.getMaxLvl();

			if (template.getItem().equals(MODULAR_TEMPLATE_NORMAL.get())) return currentLvl < maxLvl;
			if (template.getItem().equals(MODULAR_TEMPLATE_HYPER.get())) return currentLvl == maxLvl;
		}
		return false;
	}
	@Override
	public @NotNull ItemStack assemble(SmithingRecipeInput container,@NotNull HolderLookup.Provider provider){
		ItemStack base=container.getItem(1).copy();
		ItemStack addition=container.getItem(2).copy();
		if(addition.getItem().equals(REDSTONE)||addition.getItem().equals(REDSTONE_BLOCK))
			upgradeModifier(provider,base,EFFICIENCY);
		else if(addition.getItem().equals(LAPIS_LAZULI)||addition.getItem().equals(LAPIS_BLOCK))
			upgradeModifier(provider,base,ModularModifier.LUCK);
		else if(addition.getItem().equals(QUARTZ)||addition.getItem().equals(QUARTZ_BLOCK))
			upgradeModifier(provider,base,ModularModifier.SHARPNESS);
		else if(addition.getItem().equals(DIAMOND))
			upgradeModifier(provider,base,ModularModifier.REINFORCED);
		else if(addition.getItem().equals(IRON_INGOT)||addition.getItem().equals(IRON_BLOCK))
			upgradeModifier(provider,base,SWEEPING_EDGE);
		else if(addition.getItem().equals(Objects.requireNonNull(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:experience_block")))))
			upgradeModifier(provider,base,MENDING);
		else if(addition.getItem().equals(SILKY_STONE.get()))
			upgradeModifier(provider,base,SILK_TOUCH);
		return base;
	}
	@Override
	public int getAdditionConsumeCount(SmithingRecipeInput container){
		ItemStack base=container.getItem(1).copy();
		ItemStack addition=container.getItem(2).copy();
		if(addition.getItem().equals(REDSTONE)||addition.getItem().equals(REDSTONE_BLOCK))
			return getItemCount(base,EFFICIENCY);
		else if(addition.getItem().equals(LAPIS_LAZULI)||addition.getItem().equals(LAPIS_BLOCK))
			return getItemCount(base,ModularModifier.LUCK);
		else if(addition.getItem().equals(QUARTZ)||addition.getItem().equals(QUARTZ_BLOCK))
			return getItemCount(base,SHARPNESS);
		else if(addition.getItem().equals(DIAMOND)) return getItemCount(base,REINFORCED);
		else if(addition.getItem().equals(IRON_INGOT)||addition.getItem().equals(IRON_BLOCK))
			return getItemCount(base,SWEEPING_EDGE);
		else if(addition.getItem().equals(Objects.requireNonNull(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:experience_block")))))
			return 1;
		else if(addition.getItem().equals(SILKY_STONE.get())) return 1;
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