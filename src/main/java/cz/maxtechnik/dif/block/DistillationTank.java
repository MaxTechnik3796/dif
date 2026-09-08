package cz.maxtechnik.dif.block;

import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import cz.maxtechnik.dif.block.entity.DistillationTankBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public class DistillationTank extends FluidTankBlock{
	public DistillationTank(){
		super(BlockBehaviour.Properties.of().strength(5F,6F).sound(SoundType.METAL).requiresCorrectToolForDrops(),false);
		registerDefaultState(defaultBlockState().setValue(TOP,true)
				.setValue(BOTTOM,true)
				.setValue(SHAPE,Shape.PLAIN));
	}
	@Override
	public InteractionResult onWrenched(BlockState state,UseOnContext context){
		return InteractionResult.PASS;
	}
	@Override
	public BlockEntityType<? extends FluidTankBlockEntity> getBlockEntityType(){
		return DifModBlockEntities.DISTILLATION_TANK.get();
	}
	//Ticker
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState blockState,BlockEntityType<T> type){
		BlockEntityTicker<T> createTicker=super.getTicker(level,blockState,type);
		if(level.isClientSide) return createTicker;
		if(type!=DifModBlockEntities.DISTILLATION_TANK.get()) return createTicker;
		return (lvl,pos,state,be)->{
			if(createTicker!=null) createTicker.tick(lvl,pos,state,be);
			if(be instanceof DistillationTankBlockEntity dbe){
				DistillationTankBlockEntity.serverTick(lvl,dbe);
			}
		};
	}
	@Override
	public @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState,
										   @NotNull net.minecraft.world.level.LevelAccessor level, @NotNull BlockPos currentPos, @NotNull BlockPos neighborPos) {
		if (direction == Direction.DOWN)
			return state.setValue(BOTTOM, !(neighborState.getBlock() instanceof DistillationTank));
		if (direction == Direction.UP)
			return state.setValue(TOP, !(neighborState.getBlock() instanceof DistillationTank));
		return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
	}

	@Override
	public void neighborChanged(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos fromPos, boolean isMoving) {
		super.neighborChanged(blockState, level, pos, neighborBlock, fromPos, isMoving);
		if (!fromPos.equals(pos.above()) && !fromPos.equals(pos.below())) return;
		if (level.getBlockEntity(pos) instanceof DistillationTankBlockEntity dbe) {
			DistillationTankBlockEntity master = dbe.getTowerMaster();
			if (master != null) master.notifyMultiUpdated();
		}
	}

	@Override
	public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
						@NotNull BlockState oldState, boolean isMoving) {
		super.onPlace(state, level, pos, oldState, isMoving);
		if (level.isClientSide || state.getBlock() == oldState.getBlock()) return;
		forceConnectivityUpdateInArea(level, pos);
	}

	@Override
	public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
						 @NotNull BlockState newState, boolean isMoving) {
		super.onRemove(state, level, pos, newState, isMoving);
		if (level.isClientSide || state.getBlock() == newState.getBlock()) return;
		forceConnectivityUpdateInArea(level, pos);
	}

	private void forceConnectivityUpdateInArea(Level level, BlockPos pos) {
		if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
			serverLevel.getServer().tell(new net.minecraft.server.TickTask(
					serverLevel.getServer().getTickCount() + 1,
					() -> rebuildArea(serverLevel, pos)
			));
		}
	}

	private void rebuildArea(Level level, BlockPos pos) {
		java.util.List<DistillationTankBlockEntity> tanks = new java.util.ArrayList<>();
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				BlockEntity be = level.getBlockEntity(pos.offset(x, 0, z));
				if (be instanceof DistillationTankBlockEntity tank) {
					tanks.add(tank);
				}
			}
		}
		for (DistillationTankBlockEntity tank : tanks) {
			com.simibubi.create.api.connectivity.ConnectivityHandler.splitMulti(tank);
		}
		tanks.clear();
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				BlockEntity be = level.getBlockEntity(pos.offset(x, 0, z));
				if (be instanceof DistillationTankBlockEntity tank) {
					tanks.add(tank);
				}
			}
		}
		tanks.sort(Comparator.comparingInt((DistillationTankBlockEntity a) -> a.getBlockPos().getX()).thenComparingInt(a -> a.getBlockPos().getZ()));
		for (DistillationTankBlockEntity tank : tanks) {
			if (tank.isController()) {
				com.simibubi.create.api.connectivity.ConnectivityHandler.formMulti(tank);
			}
		}
		// Sync visuals and data
		for (DistillationTankBlockEntity tank : tanks) {
			BlockPos p = tank.getBlockPos();
			BlockState s = level.getBlockState(p);
			if (s.getBlock() instanceof DistillationTank) {
				boolean hasTankBelow = level.getBlockState(p.below()).getBlock() instanceof DistillationTank;
				boolean hasTankAbove = level.getBlockState(p.above()).getBlock() instanceof DistillationTank;
				BlockState updated = s.setValue(BOTTOM, !hasTankBelow).setValue(TOP, !hasTankAbove);
				if (s != updated) level.setBlock(p, updated, 3);
			}
			tank.sendData();
		}
	}
}