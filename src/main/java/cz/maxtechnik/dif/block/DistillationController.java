package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.DistillationControllerBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
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
	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new DistillationControllerBlockEntity(pos,state);
	}
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState state,@NotNull BlockEntityType<T> type){
		if(level.isClientSide) return null;
		return type==DifModBlockEntities.DISTILLATION_CONTROLLER.get()?(lvl,pos,st,be)->DistillationControllerBlockEntity.serverTick(lvl,pos,(DistillationControllerBlockEntity)be):null;
	}
	@Override
	public boolean hasAnalogOutputSignal(@NotNull BlockState state){
		return true;
	}
	@Override
	public int getAnalogOutputSignal(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos){
		if(level.getBlockEntity(pos) instanceof DistillationControllerBlockEntity be){
			int amount=be.tank.getFluidAmount();
			int cap=be.tank.getCapacity();
			return cap==0?0:Math.round((float)amount/cap*15F);
		}
		return 0;
	}
}