package cz.maxtechnik.dif.fluid.block;

import cz.maxtechnik.dif.init.fluid.DifModFluids;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.PushReaction;
public class SunflowerOilBlock extends LiquidBlock{
	public SunflowerOilBlock(){
		super(DifModFluids.SUNFLOWER_OIL,Properties.of().strength(100f).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable());
	}
}
