package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.DistillationControllerBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class DistillationController extends Block implements EntityBlock{
	public DistillationController(){
		super(BlockBehaviour.Properties.of().strength(5F,6F).sound(SoundType.METAL).requiresCorrectToolForDrops());
	}
	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return new DistillationControllerBlockEntity(pos,blockState);
	}
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState state,@NotNull BlockEntityType<T> type){
		if(level.isClientSide) return null;
		if(type.equals(DifModBlockEntities.DISTILLATION_CONTROLLER.get()))
			return (lvl,pos,st,be)->DistillationControllerBlockEntity.serverTick(lvl,pos,st,(DistillationControllerBlockEntity)be);
		return null;
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
	public int getAnalogOutputSignal(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos){
		if(level.getBlockEntity(pos) instanceof DistillationControllerBlockEntity be)
			return Math.round((float)be.tank.getFluidAmount()/be.tank.getCapacity()*15);
		return 0;
	}
}