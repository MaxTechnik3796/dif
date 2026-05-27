package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class CokeOven extends Block implements EntityBlock{
	public CokeOven(BlockBehaviour.Properties properties){
		super(properties);
	}
	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return DifModBlockEntities.COKE_OVEN.get().create(pos,blockState);
	}

	@Override
	public void onRemove(BlockState state, @NotNull net.minecraft.world.level.Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (level.getBlockEntity(pos) instanceof cz.maxtechnik.dif.block.entity.CokeOvenBlockEntity brick) {
				BlockPos controllerPos = brick.getControllerPos();
				if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof cz.maxtechnik.dif.block.entity.CokeOvenControllerBlockEntity ctrl) {
					ctrl.forceValidation = true;
				}
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}
}