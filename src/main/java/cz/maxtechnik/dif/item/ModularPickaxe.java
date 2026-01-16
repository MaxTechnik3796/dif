package cz.maxtechnik.dif.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
public class ModularPickaxe extends PickaxeItem{
	public ModularPickaxe(){
		super(new Tier(){
			@Override
			public int getUses(){
				// Toto je základní hodnota, která se použije, pokud stack nemá NBT
				return 100;
			}
			@Override
			public float getSpeed(){
				return 4F;
			}
			@Override
			public float getAttackDamageBonus(){
				return 2F;
			}
			@Override
			public int getLevel(){
				return 0;
			}
			@Override
			public int getEnchantmentValue(){
				return 0;
			}
			@Override
			public @NotNull Ingredient getRepairIngredient(){
				return Ingredient.EMPTY;
			}
		},1,-3F,new Properties());
	}
	/**
	 * Tato metoda vrací maximální poškození pro konkrétní ItemStack.
	 * Načte hodnotu z NBT tagu "Durability".
	 */
	@Override
	public int getMaxDamage(ItemStack stack){
		// Kontrola, zda stack existuje a má tagy
		if(stack.hasTag()){
			assert stack.getTag()!=null;
			if(stack.getTag().contains("Durability")){
				return stack.getTag().getInt("Durability");
			}
		}
		// Pokud NBT neexistuje, vrátí výchozí hodnotu z Tieru
		return super.getMaxDamage(stack);
	}
}