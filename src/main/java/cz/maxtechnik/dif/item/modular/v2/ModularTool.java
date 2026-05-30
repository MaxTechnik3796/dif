package cz.maxtechnik.dif.item.modular.v2;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
public class ModularTool extends DiggerItem{
	public ModularTool(){
		super(new Tier(){
			@Override
			public int getUses(){
				return 5;
			}
			@Override
			public float getSpeed(){
				return 5;
			}
			@Override
			public float getAttackDamageBonus(){
				return 2;
			}
			@Override
			public @NotNull TagKey<Block> getIncorrectBlocksForDrops(){
				return BlockTags.MINEABLE_WITH_PICKAXE;
			}
			@Override
			public int getEnchantmentValue(){
				return 0;
			}
			@Override
			public @NotNull Ingredient getRepairIngredient(){
				return Ingredient.EMPTY;
			}
		},BlockTags.MINEABLE_WITH_PICKAXE,new Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
	}
}