package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.BlastSmelteryBlockEntity;
import cz.maxtechnik.dif.block.entity.BlastSmelteryControllerBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlastSmeltery extends Block implements EntityBlock {

    public BlastSmeltery(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState blockState) {
        return DifModBlockEntities.BLAST_SMELTERY.get().create(pos, blockState);
    }

    @Override
    public void onRemove(BlockState blockState, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!blockState.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BlastSmelteryBlockEntity brick) {
            BlockPos ctrlPos = brick.getControllerPos();
            if (ctrlPos != null && level.getBlockEntity(ctrlPos) instanceof BlastSmelteryControllerBlockEntity ctrl)
                ctrl.forceValidation = true;
        }
        super.onRemove(blockState, level, pos, newState, isMoving);
    }
}
