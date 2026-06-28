package cz.maxtechnik.dif.item.modular.v2;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
@EventBusSubscriber
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
	public boolean matches(@NotNull SmithingRecipeInput container,@NotNull Level level){
		ItemStack template=container.getItem(0).copy();
		ItemStack base=container.getItem(1).copy();
		ItemStack addition=container.getItem(2).copy();
		if(this.template.test(template)&&this.base.test(base)&&this.addition.test(addition))
			if(template.getItem().equals(DifModItems.MODULAR_TEMPLATE_EFFICIENCY.get())&&addition.getCount()==64)
				if(ModularTool.getModifierLevel(base,ModularModifier.EFFICIENCY)<ModularModifier.EFFICIENCY.getMaxLvl())
					return true;
		return false;
	}
	@Override
	public @NotNull ItemStack assemble(SmithingRecipeInput container,@NotNull HolderLookup.Provider provider){
		ItemStack template=container.getItem(0).copy();
		ItemStack base=container.getItem(1).copy();
		ItemStack addition=container.getItem(2).copy();
		if(template.getItem().equals(DifModItems.MODULAR_TEMPLATE_EFFICIENCY.get()))
			ModularTool.upgradeModifier(provider,base,ModularModifier.EFFICIENCY);
		return base;
	}
	@SubscribeEvent
	public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
		System.out.println("neco");
		// 1. Zkontrolujeme, zda hráč právě pracoval v kovářském stole (SmithingMenu)
		if (event.getEntity().containerMenu instanceof SmithingMenu) {

			// V kovářském stole nám event.getInventory() vrátí jeho 3 vstupní sloty
			var inputs = event.getInventory();
			ItemStack template = inputs.getItem(0);
			ItemStack addition = inputs.getItem(2);

			// 2. Zkontrolujeme, zda hráč použil tvou šablonu pro efektivitu
			if (template.is(DifModItems.MODULAR_TEMPLATE_EFFICIENCY.get())) {

				// POZOR ZDRADA: Tento event se spustí AŽ POTÉ, co vanila kód stolu vykonal svou práci.
				// To znamená, že vanila už stihla ubrat svůj 1 kus.
				// Pokud jich tam hráč vložil 64, v tuto chvíli jich ve slotu zbývá přesně 63!
				if (addition.getCount() == 63) {
					addition.setCount(0); // Vynulujeme slot -> sežralo to celých 64 kusů
				}
			}
		}
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