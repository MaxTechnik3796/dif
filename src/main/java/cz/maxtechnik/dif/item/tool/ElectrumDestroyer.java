package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
public class ElectrumDestroyer extends PickaxeItem{
	private static final Tier ELECTRUM_TIER=new Tier(){
		@Override
		public int getUses(){
			return 3060;
		}
		@Override
		public float getSpeed(){
			return 10f;
		}
		@Override
		public float getAttackDamageBonus(){
			return 5f;
		}
		@Override
		public @NotNull TagKey<Block> getIncorrectBlocksForDrops(){
			return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
		}
		@Override
		public int getEnchantmentValue(){
			return 14;
		}
		@Override
		public @NotNull Ingredient getRepairIngredient(){
			return Ingredient.of(new ItemStack(DifModItems.MITHRIL_PLATE.get()));
		}
	};
	public ElectrumDestroyer(){
		super(ELECTRUM_TIER,new Properties().attributes(PickaxeItem.createAttributes(ELECTRUM_TIER,1,-2.8f)));
	}
}
