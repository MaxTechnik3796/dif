package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
public class ElectrumDestroyer extends PickaxeItem{
	public ElectrumDestroyer(){
		super(new Tier(){
			public int getUses(){
				return 3060;
			}
			public float getSpeed(){
				return 10f;
			}
			public float getAttackDamageBonus(){
				return 5f;
			}
			public int getLevel(){
				return 4;
			}
			public int getEnchantmentValue(){
				return 14;
			}
			public @NotNull Ingredient getRepairIngredient(){
				return Ingredient.of(new ItemStack(DifModItems.MITHRIL_PLATE.get()));
			}
		},1,-2.8f,new Properties());
	}
}
