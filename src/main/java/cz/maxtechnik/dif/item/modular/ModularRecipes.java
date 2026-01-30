package cz.maxtechnik.dif.item.modular;

import com.google.gson.JsonObject;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
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
		assert base.getTag()!=null;
		assert template.getTag()!=null;
		assert addition.getTag()!=null;
		CompoundTag tag=base.getTag();
		String toolMaterial=tag.getString("Material");
		if(base.getItem() instanceof ModularBase){
			if(isTagged(addition,DifMod.MODID,"modular_tools_materials")){
				boolean correctMaterial=false;
				switch(toolMaterial){
					case "Wood" -> correctMaterial=isTagged(addition,DifMod.MODID,"modular_tools_materials/wood");
					case "Stone" -> correctMaterial=isTagged(addition,DifMod.MODID,"modular_tools_materials/stone");
					case "Iron" -> correctMaterial=isTagged(addition,DifMod.MODID,"modular_tools_materials/iron");
					case "Gold" -> correctMaterial=isTagged(addition,DifMod.MODID,"modular_tools_materials/gold");
					case "Diamond" -> correctMaterial=isTagged(addition,DifMod.MODID,"modular_tools_materials/diamond");
					case "Netherite" -> correctMaterial=isTagged(addition,DifMod.MODID,"modular_tools_materials/netherite");
				}
				return template.getItem().equals(Items.AIR)&&base.getDamageValue()>0&&correctMaterial;
			}else if(isTagged(template,DifMod.MODID,"modular_tools_parts")){
				if(base.getItem().equals(DifModItems.MODULAR_PICKAXE.get())&&isTagged(template,DifMod.MODID,"modular_tools_parts/pickaxe_parts")){
					return isReplacedPartValid(template,base);
				}
				if(base.getItem().equals(DifModItems.MODULAR_AXE.get())&&isTagged(template,DifMod.MODID,"modular_tools_parts/axe_parts")){
					return isReplacedPartValid(template,base);
				}
				if(base.getItem().equals(DifModItems.MODULAR_SHOVEL.get())&&isTagged(template,DifMod.MODID,"modular_tools_parts/shovel_parts")){
					return isReplacedPartValid(template,base);
				}
				if(base.getItem().equals(DifModItems.MODULAR_SWORD.get())&&isTagged(template,DifMod.MODID,"modular_tools_parts/sword_parts")){
					return isReplacedPartValid(template,base);
				}
			}
		}else if(isTagged(template,DifMod.MODID,"modular_tools_parts/handle")&&isTagged(base,DifMod.MODID,"modular_tools_parts/head")&&isTagged(addition,DifMod.MODID,"modular_tools_parts/binding")){
			if(base.getItem().equals(DifModItems.MODULAR_PART_SWORD_HEAD.get())){
				return addition.getItem().equals(DifModItems.MODULAR_PART_SWORD_BINDING.get());
			}else{
				return addition.getItem().equals(DifModItems.MODULAR_PART_BINDING.get());
			}
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
				int currentDamage=base.getDamageValue();
				int newDamage=Math.max(0,currentDamage-REPAIR_AMOUNT);
				base.setDamageValue(newDamage);
				if(base.hasTag()&&base.getTag()!=null){
					if(base.getTag().getBoolean("Broken")&&newDamage<(base.getMaxDamage()-1)){
						baseTag.putBoolean("Broken",false);
						baseTag.putBoolean("Unbreakable",false);
						baseTag.putInt("CustomModelData",0);
					}
				}
			}else if(isTagged(template,DifMod.MODID,"modular_tools_parts")){
				if(isTagged(template,DifMod.MODID,"modular_tools_parts/head")){
					baseTag.putString("Material",templateTag.getString("HeadMaterial"));
					baseTag.putString("HeadMaterial",templateTag.getString("HeadMaterial"));
					baseTag.putInt("HeadColor",templateTag.getInt("HeadColor"));
					baseTag.putInt("HeadDurability",templateTag.getInt("HeadDurability"));
				}
				if(isTagged(template,DifMod.MODID,"modular_tools_parts/binding")){
					baseTag.putString("BindingMaterial",templateTag.getString("BindingMaterial"));
					baseTag.putInt("BindingColor",templateTag.getInt("BindingColor"));
					baseTag.putInt("BindingDurability",templateTag.getInt("BindingDurability"));
				}
				if(isTagged(template,DifMod.MODID,"modular_tools_parts/handle")){
					baseTag.putString("HandleMaterial",templateTag.getString("HandleMaterial"));
					baseTag.putInt("HandleColor",templateTag.getInt("HandleColor"));
					baseTag.putInt("HandleDurability",templateTag.getInt("HandleDurability"));
				}
				calculateDurability(baseTag);
				DifMod.LOGGER.debug(base.getDisplayName().toString());
				if(!(base.getDamageValue()<baseTag.getInt("Durability"))){
					base.setDamageValue(baseTag.getInt("Durability")-1);
					baseTag.putBoolean("Broken",true);
					baseTag.putBoolean("Unbreakable",true);
					baseTag.putInt("CustomModelData",1);
				}
			}
		}else if(isTagged(template,DifMod.MODID,"modular_tools_parts/handle")&&isTagged(base,DifMod.MODID,"modular_tools_parts/head")&&isTagged(addition,DifMod.MODID,"modular_tools_parts/binding")){
			ItemStack tool=new ItemStack(Items.AIR);
			if(base.getItem().equals(DifModItems.MODULAR_PART_PICKAXE_HEAD.get())){
				tool=new ItemStack(DifModItems.MODULAR_PICKAXE.get(),1);
			}else if(base.getItem().equals(DifModItems.MODULAR_PART_AXE_HEAD.get())){
				tool=new ItemStack(DifModItems.MODULAR_AXE.get(),1);
			}else if(base.getItem().equals(DifModItems.MODULAR_PART_SHOVEL_HEAD.get())){
				tool=new ItemStack(DifModItems.MODULAR_SHOVEL.get(),1);
			}else if(base.getItem().equals(DifModItems.MODULAR_PART_SWORD_HEAD.get())){
				tool=new ItemStack(DifModItems.MODULAR_SWORD.get(),1);
			}

			CompoundTag toolTag=new CompoundTag();
			toolTag.putString("Material",baseTag.getString("HeadMaterial"));
			toolTag.putInt("SpecialDurability",1);
			toolTag.putInt("Durability",baseTag.getInt("HeadDurability")+additionTag.getInt("BindingDurability")+templateTag.getInt("HandleDurability")+1);
			toolTag.putInt("HideFlags",4);

			toolTag.putInt("MiningLevel",0);
			toolTag.putInt("Efficiency",4);
			toolTag.putFloat("AttackDamage",2F);
			toolTag.putFloat("AttackSpeed",-2F);

			toolTag.putString("HeadMaterial",baseTag.getString("HeadMaterial"));
			toolTag.putString("BindingMaterial",additionTag.getString("BindingMaterial"));
			baseTag.putString("HandleMaterial",templateTag.getString("HandleMaterial"));

			toolTag.putInt("HeadDurability",baseTag.getInt("HeadDurability"));
			toolTag.putInt("BindingDurability",additionTag.getInt("BindingDurability"));
			toolTag.putInt("HandleDurability",templateTag.getInt("HandleDurability"));

			toolTag.putInt("HeadColor",baseTag.getInt("HeadColor"));
			toolTag.putInt("BindingColor",additionTag.getInt("BindingColor"));
			toolTag.putInt("HandleColor",templateTag.getInt("HandleColor"));
			tool.setTag(toolTag);
			return tool;
		}
		return base;
	}
	public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess){
		return this.result;
	}
	public boolean isTemplateIngredient(@NotNull ItemStack itemStack){
		return this.template.test(itemStack)||isTagged(itemStack,DifMod.MODID,"modular_tools_parts");
	}
	public boolean isBaseIngredient(@NotNull ItemStack itemStack){
		return this.base.test(itemStack)||isTagged(itemStack,DifMod.MODID,"modular_tools_parts");
	}
	public boolean isAdditionIngredient(@NotNull ItemStack itemStack){
		return  this.addition.test(itemStack)||isTagged(itemStack,DifMod.MODID,"modular_tools_parts")||isTagged(itemStack,DifMod.MODID,"modular_tools_materials");
	}
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