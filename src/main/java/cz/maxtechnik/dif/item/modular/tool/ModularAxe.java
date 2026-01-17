package cz.maxtechnik.dif.item.modular.tool;

import cz.maxtechnik.dif.item.modular.ModularBase;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
public class ModularAxe extends ModularBase{
	public ModularAxe(){
		super(6.0F,-3.2F,new Properties().stacksTo(1));
		this.defaultDurability=11;
		this.defaultEfficiency=4.0F;
	}
	@Override
	protected TagKey<Block> getMineableTag(){
		return BlockTags.MINEABLE_WITH_AXE;
	}
}