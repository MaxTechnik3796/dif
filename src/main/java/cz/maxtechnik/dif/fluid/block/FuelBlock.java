package cz.maxtechnik.dif.fluid.block;

import cz.maxtechnik.dif.init.fluid.DifModFluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.LiquidBlock;

public class FuelBlock extends LiquidBlock {
	public FuelBlock() {
		super(DifModFluids.FUEL, BlockBehaviour.Properties.of().mapColor(MapColor.WATER).strength(100f).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable());
	}
}
