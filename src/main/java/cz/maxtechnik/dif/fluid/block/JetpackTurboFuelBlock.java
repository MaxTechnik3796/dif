package cz.maxtechnik.dif.fluid.block;

import cz.maxtechnik.dif.init.fluid.DifModFluids;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.PushReaction;
public class JetpackTurboFuelBlock extends LiquidBlock{
	public JetpackTurboFuelBlock(){
		super(DifModFluids.JETPACK_TURBO_FUEL,Properties.of().strength(100F).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable());
	}
}
