package cz.maxtechnik.dif.item.modular.tool;

import cz.maxtechnik.dif.item.modular.ModularBase;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
public class ModularPickaxe extends ModularBase{
	public ModularPickaxe(){
		super(5,4F,0,1.0F,-2.8F,"Wood",new Properties().stacksTo(1));
	}
	@Override
	protected TagKey<Block> getMineableTag(){
		return BlockTags.MINEABLE_WITH_PICKAXE;
	}
}