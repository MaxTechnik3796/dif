package cz.maxtechnik.dif.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
public class QuarryFrame extends Block{
	public QuarryFrame(){
		super(Properties.of().strength(1F, 1F).noLootTable().sound(SoundType.METAL));
	}
}
