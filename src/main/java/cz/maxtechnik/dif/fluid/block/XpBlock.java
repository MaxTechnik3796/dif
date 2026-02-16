package cz.maxtechnik.dif.fluid.block;

import cz.maxtechnik.dif.init.fluid.DifModFluids;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
public class XpBlock extends LiquidBlock{
	public XpBlock(){
		super(DifModFluids.XP,Properties.of().mapColor(MapColor.WATER).strength(100F).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable().lightLevel(s->15));
	}
}
