package cz.maxtechnik.dif.item.tool.modular;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
public class ModularSword extends ModularBase{
	public ModularSword(){
		super(3.0F,-2.4F,new Properties().stacksTo(1));
		this.defaultDurability=11;
		this.defaultEfficiency=4.0F;
	}
	@Override
	protected TagKey<Block> getMineableTag(){
		return BlockTags.SWORD_EFFICIENT ;
	}
}