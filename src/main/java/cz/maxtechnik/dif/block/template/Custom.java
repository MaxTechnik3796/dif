package cz.maxtechnik.dif.block.template;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
public class Custom extends Block{
	public Custom(SoundType sound,float hardness,float resistance,boolean requiresCorrectToolForDrops){
		super(requiresCorrectToolForDrops?Properties.of().strength(hardness,resistance).sound(sound).requiresCorrectToolForDrops():Properties.of().strength(hardness,resistance).sound(sound));
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 15;
	}
}
