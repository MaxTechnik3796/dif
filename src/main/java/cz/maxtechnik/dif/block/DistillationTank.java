package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.DistillationControllerBlockEntity;
import cz.maxtechnik.dif.block.entity.DistillationTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class DistillationTank extends Block implements EntityBlock{
	public DistillationTank(){
		super(Properties.of().strength(5F,6F).sound(SoundType.METAL).requiresCorrectToolForDrops());
	}
	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return new DistillationTankBlockEntity(pos,blockState);
	}
	@Override
	public void onRemove(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState newState,boolean moving){
		super.onRemove(state,level,pos,newState,moving);
	}
	@Override
	public boolean hasAnalogOutputSignal(@NotNull BlockState blockState){
		return true;
	}
	@Override
	public int getAnalogOutputSignal(@NotNull BlockState blockState,@NotNull Level level,@NotNull BlockPos pos){
		if(level.getBlockEntity(pos) instanceof DistillationControllerBlockEntity be)
			return Math.round((float)be.tank.getFluidAmount()/be.tank.getCapacity()*15);
		return 0;
	}
}