package cz.maxtechnik.dif.fluid.block;

import cz.maxtechnik.dif.fluid.DifModFluids;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
public class BeerBlock extends LiquidBlock{
	public BeerBlock(){
		super(DifModFluids.BEER,Properties.of().mapColor(MapColor.WATER).strength(100F).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable());
	}
}
