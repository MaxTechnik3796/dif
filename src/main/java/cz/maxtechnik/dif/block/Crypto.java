package cz.maxtechnik.dif.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class Crypto extends Block{
	public Crypto(){
		super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(4f,10f).lightLevel(s->3).requiresCorrectToolForDrops());
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 15;
	}
}
