package cz.maxtechnik.dif.item.modular.tool;

import cz.maxtechnik.dif.item.modular.ModularBase;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
public class ModularSword extends ModularBase{
	public ModularSword(){
		super(5,4,0,3.0F,-2.4F,"Wood",new Properties().stacksTo(1).fireResistant());
	}
	@Override
	protected TagKey<Block> getMineableTag(){
		return BlockTags.SWORD_EFFICIENT ;
	}
}