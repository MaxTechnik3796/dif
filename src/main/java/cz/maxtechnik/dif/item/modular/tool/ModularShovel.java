package cz.maxtechnik.dif.item.modular.tool;

import cz.maxtechnik.dif.item.modular.ModularBase;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
public class ModularShovel extends ModularBase{
	public ModularShovel(){
		super(1.5F,-3.0F,new Properties().stacksTo(1));
		this.defaultDurability=11;
		this.defaultEfficiency=4.0F;
	}
	@Override
	protected TagKey<Block> getMineableTag(){
		return BlockTags.MINEABLE_WITH_SHOVEL;
	}
}