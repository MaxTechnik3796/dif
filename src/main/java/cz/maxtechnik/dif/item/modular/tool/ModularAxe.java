package cz.maxtechnik.dif.item.modular.tool;

import cz.maxtechnik.dif.item.modular.ModularBase;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
public class ModularAxe extends ModularBase{
	public ModularAxe(){
		super(5,4,0,6.0F,-3.2F,"Wood",new Properties().stacksTo(1).fireResistant());
	}
	@Override
	protected TagKey<Block> getMineableTag(){
		return BlockTags.MINEABLE_WITH_AXE;
	}
}