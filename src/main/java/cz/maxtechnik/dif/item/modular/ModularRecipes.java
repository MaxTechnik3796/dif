package cz.maxtechnik.dif.item.modular;

import com.google.gson.JsonObject;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import static cz.maxtechnik.dif.item.modular.ModularBase.*;
public class ModularRecipes implements SmithingRecipe{
	public static final int REPAIR_AMOUNT=3;
	final Ingredient template;
	final Ingredient base;
	final Ingredient addition;
	final ItemStack result;
	public final ResourceLocation id;
	public ModularRecipes(ResourceLocation id,Ingredient template,Ingredient base,Ingredient addition,ItemStack result){
		this.id=id;
		this.template=template;
		this.base=base;
		this.addition=addition;
		this.result=result;
	}
	@Override
	public boolean matches(Container container,@NotNull Level world){
		ItemStack template=container.getItem(0);
		ItemStack base=container.getItem(1);
		ItemStack addition=container.getItem(2);
		if(base.getItem().equals(Items.AIR))return false;
		assert template.getTag()!=null;
		assert base.getTag()!=null;
		assert addition.getTag()!=null;
		CompoundTag templateTag=template.getTag();
		CompoundTag baseTag=base.getTag();
		CompoundTag additionTag=addition.getTag();
		if(base.getItem() instanceof ModularBase){
			if(isTagged(addition,DifMod.MODID,"modular_tools_materials")){
				return toolRepairCheck(template,base,addition,templateTag,baseTag,additionTag);
			}else if(isTagged(template,DifMod.MODID,"modular_tools_parts")){
				return toolPartReplaceCheck(template,base,addition,templateTag,baseTag,additionTag);
			}else if(isTagged(template,DifMod.MODID,"modular_tools_modifiers")){
				return applyModifiersCheck(template,base,addition,templateTag,baseTag,additionTag);
			}
		}else if(isTagged(template,DifMod.MODID,"modular_tools_parts/handle")&&isTagged(base,DifMod.MODID,"modular_tools_parts/head")&&isTagged(addition,DifMod.MODID,"modular_tools_parts/binding")){
			return newToolCraftCheck(template,base,addition,templateTag,baseTag,additionTag);
		}
		return false;
	}
	@Override
	public @NotNull ItemStack assemble(Container container,@NotNull RegistryAccess registryAccess){
		ItemStack template=container.getItem(0).copy();
		ItemStack base=container.getItem(1).copy();
		ItemStack addition=container.getItem(2).copy();
		CompoundTag templateTag=template.getTag();
		CompoundTag baseTag=base.getTag();
		CompoundTag additionTag=addition.getTag();
		assert templateTag!=null;
		assert baseTag!=null;
		assert additionTag!=null;
		if(base.getItem() instanceof ModularBase){
			if(isTagged(addition,DifMod.MODID,"modular_tools_materials")){
				toolRepair(template,base,addition,templateTag,baseTag,additionTag);
			}else if(isTagged(template,DifMod.MODID,"modular_tools_parts")){
				toolPartReplace(template,base,addition,templateTag,baseTag,additionTag);
			}else if(isTagged(template,DifMod.MODID,"modular_tools_modifiers")){
				applyModifiers(template,base,addition,templateTag,baseTag,additionTag);
			}
		}else if(isTagged(template,DifMod.MODID,"modular_tools_parts/handle")&&isTagged(base,DifMod.MODID,"modular_tools_parts/head")&&isTagged(addition,DifMod.MODID,"modular_tools_parts/binding")){
			return newToolCraft(template,base,addition,templateTag,baseTag,additionTag);
		}
		return base;
	}
	public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess){return this.result;}
	public boolean isTemplateIngredient(@NotNull ItemStack itemStack){return this.template.test(itemStack)||isTagged(itemStack,DifMod.MODID,"modular_tools_parts")||isTagged(itemStack,DifMod.MODID,"modular_tools_modifiers");}
	public boolean isBaseIngredient(@NotNull ItemStack itemStack){return this.base.test(itemStack)||isTagged(itemStack,DifMod.MODID,"modular_tools_parts");}
	public boolean isAdditionIngredient(@NotNull ItemStack itemStack){return this.addition.test(itemStack)||isTagged(itemStack,DifMod.MODID,"modular_tools_parts")||isTagged(itemStack,DifMod.MODID,"modular_tools_materials");}
	@Override
	public @NotNull RecipeSerializer<?> getSerializer(){
		return DifModRecipes.MODULAR_REPAIR_SERIALIZER.get();
	}
	public @NotNull ResourceLocation getId(){
		return this.id;
	}
	public boolean isIncomplete(){
		return this.base.isEmpty();
	}
	public static class Serializer implements RecipeSerializer<ModularRecipes>{
		public @NotNull ModularRecipes fromJson(@NotNull ResourceLocation resourceLocation,@NotNull JsonObject jsonObject){
			Ingredient template=Ingredient.fromJson(GsonHelper.getNonNull(jsonObject,"template"));
			if(template.getItems()[0].getItem().equals(Items.BARRIER)){
				template=Ingredient.of(new ItemStack(Items.AIR));
			}
			Ingredient base=Ingredient.fromJson(GsonHelper.getNonNull(jsonObject,"base"));
			Ingredient addition=Ingredient.fromJson(GsonHelper.getNonNull(jsonObject,"addition"));
			if(addition.getItems()[0].getItem().equals(Items.BARRIER)){
				addition=Ingredient.of(new ItemStack(Items.AIR));
			}
			ItemStack result=ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonObject,"result"));
			return new ModularRecipes(resourceLocation,template,base,addition,result);
		}
		public ModularRecipes fromNetwork(@NotNull ResourceLocation resourceLocation,@NotNull FriendlyByteBuf friendlyByteBuf){
			Ingredient template=Ingredient.fromNetwork(friendlyByteBuf);
			Ingredient base=Ingredient.fromNetwork(friendlyByteBuf);
			Ingredient addition=Ingredient.fromNetwork(friendlyByteBuf);
			ItemStack result=friendlyByteBuf.readItem();
			return new ModularRecipes(resourceLocation,template,base,addition,result);
		}
		public void toNetwork(@NotNull FriendlyByteBuf friendlyByteBuf,ModularRecipes modularRepairRecipe){
			modularRepairRecipe.template.toNetwork(friendlyByteBuf);
			modularRepairRecipe.base.toNetwork(friendlyByteBuf);
			modularRepairRecipe.addition.toNetwork(friendlyByteBuf);
			friendlyByteBuf.writeItem(modularRepairRecipe.result);
		}
	}
}