package cz.maxtechnik.dif.fluid.block;

import cz.maxtechnik.dif.init.fluid.DifModFluids;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.PushReaction;
public class JetpackFuelBlock extends LiquidBlock{
	public JetpackFuelBlock(){
		super(DifModFluids.JETPACK_FUEL,Properties.of().strength(100f).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable());
	}
}
