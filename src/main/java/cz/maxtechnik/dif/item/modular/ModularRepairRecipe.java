package cz.maxtechnik.dif.item.modular;

import com.google.gson.JsonObject;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
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
public class ModularRepairRecipe implements SmithingRecipe{
	public static final int REPAIR_AMOUNT=3;
	final Ingredient template;
	final Ingredient base;
	final Ingredient addition;
	final ItemStack result;
	public final ResourceLocation id;
	public static boolean isTagged(ItemStack itemStack,String namespace,String path){
		return itemStack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(namespace,path)));
	}
	public ModularRepairRecipe(ResourceLocation id,Ingredient template, Ingredient base, Ingredient addition, ItemStack result){
		this.id=id;
		this.template=template;
		this.base=base;
		this.addition=addition;
		this.result=result;
	}
	@Override
	public boolean matches(Container container,@NotNull Level world){
		ItemStack template=container.getItem(0);
		ItemStack base=container.getItem(1); // Prostřední slot
		ItemStack addition=container.getItem(2); // Pravý slot
		if(!(base.getItem() instanceof ModularBase))return false;
		assert base.getTag()!=null;
		boolean correctMaterial=false;
		String toolMaterial=base.getTag().getString("Material");
		switch(toolMaterial){
			case "Wood"->correctMaterial=isTagged(addition,"minecraft","planks");
			case "Stone"->correctMaterial=isTagged(addition,"forge","stone")||isTagged(addition,"forge","cobblestone");
			case "Iron"->correctMaterial=isTagged(addition,"forge","ingots/iron");
			case "Gold"->correctMaterial=isTagged(addition,"forge","ingots/gold");
			case "Diamond"->correctMaterial=isTagged(addition,"forge","gems/diamond");
			case "Netherite"->correctMaterial=isTagged(addition,"forge","ingots/netherite");
		}
		return template.getItem().equals(Items.AIR)&&base.getDamageValue()>0&&correctMaterial;
	}
	@Override
	public @NotNull ItemStack assemble(Container container,@NotNull RegistryAccess registryAccess){
		ItemStack tool=container.getItem(1).copy();
		if(tool.getItem() instanceof ModularBase){
			int currentDamage=tool.getDamageValue();
			// Opravíme o REPAIR_AMOUNT, ale ne do mínusu
			int newDamage=Math.max(0,currentDamage-REPAIR_AMOUNT);
			tool.setDamageValue(newDamage);
			// Synchronizace s tvou logikou v ModularBase (Broken tag)
			if(tool.hasTag()&&tool.getTag()!=null){
				// Pokud už není na pokraji zničení, zrušíme Broken status
				if(tool.getTag().getBoolean("Broken")&&newDamage<(tool.getMaxDamage()-1)){
					tool.getTag().putBoolean("Broken",false);
					tool.getTag().putBoolean("Unbreakable",false);
					tool.getTag().putInt("CustomModelData",0);
				}
			}
		}
		return tool;
	}
	public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
		return this.result;
	}

	public boolean isTemplateIngredient(@NotNull ItemStack itemStack) {
		return this.template.test(itemStack);
	}
	public boolean isBaseIngredient(@NotNull ItemStack itemStack) {
		return this.base.test(itemStack);
	}
	public boolean isAdditionIngredient(@NotNull ItemStack itemStack) {
		return !isTagged(itemStack,"minecraft","trim_templates");
	}
	@Override
	public @NotNull RecipeSerializer<?> getSerializer(){
		return DifModRecipes.MODULAR_REPAIR_SERIALIZER.get();
	}
	public @NotNull ResourceLocation getId(){
		return this.id;
	}
	public boolean isIncomplete(){
		//return Stream.of(this.template,this.base,this.addition).anyMatch(net.minecraftforge.common.ForgeHooks::hasNoElements);
		return this.base.isEmpty();
	}
	public static class Serializer implements RecipeSerializer<ModularRepairRecipe>{
		public @NotNull ModularRepairRecipe fromJson(@NotNull ResourceLocation resourceLocation,@NotNull JsonObject jsonObject){
			Ingredient template=Ingredient.fromJson(GsonHelper.getNonNull(jsonObject,"template"));
			if(template.getItems()[0].getItem().equals(Items.BARRIER)){
				template=Ingredient.of(new ItemStack(Items.AIR));
			}
			Ingredient base=Ingredient.fromJson(GsonHelper.getNonNull(jsonObject,"base"));
			Ingredient addition=Ingredient.fromJson(GsonHelper.getNonNull(jsonObject,"addition"));
			ItemStack result=ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonObject,"result"));
			return new ModularRepairRecipe(resourceLocation,template,base,addition,result);
		}
		public ModularRepairRecipe fromNetwork(@NotNull ResourceLocation resourceLocation,@NotNull FriendlyByteBuf friendlyByteBuf){
			Ingredient template=Ingredient.fromNetwork(friendlyByteBuf);
			Ingredient base=Ingredient.fromNetwork(friendlyByteBuf);
			Ingredient addition=Ingredient.fromNetwork(friendlyByteBuf);
			ItemStack result=friendlyByteBuf.readItem();
			return new ModularRepairRecipe(resourceLocation,template,base,addition,result);
		}
		public void toNetwork(@NotNull FriendlyByteBuf friendlyByteBuf,ModularRepairRecipe modularRepairRecipe){
			modularRepairRecipe.template.toNetwork(friendlyByteBuf);
			modularRepairRecipe.base.toNetwork(friendlyByteBuf);
			modularRepairRecipe.addition.toNetwork(friendlyByteBuf);
			friendlyByteBuf.writeItem(modularRepairRecipe.result);
		}
	}
}