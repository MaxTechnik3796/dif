package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.DistillationTankBlockEntity;
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

public class DistillationTankBlock extends Block implements EntityBlock {

    public DistillationTankBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(3f, 6f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new DistillationTankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,
                                                                   @NotNull BlockState state,
                                                                   @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        if (type == DifModBlockEntities.DISTILLATION_TANK.get())
            return (lvl, pos, st, be) -> DistillationTankBlockEntity.serverTick(lvl, pos, st, (DistillationTankBlockEntity) be);
        return null;
    }

    // Invaliduj cache pokud se změní blok nad nebo pod
    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean moving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, moving);
        if (!level.isClientSide) {
            boolean isVerticalNeighbor = neighborPos.equals(pos.above()) || neighborPos.equals(pos.below());
            if (isVerticalNeighbor && level.getBlockEntity(pos) instanceof DistillationTankBlockEntity be) {
                be.invalidateCache();
            }
        }
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                         BlockState newState, boolean moving) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            // Invaliduj blok pod (pokud byl controller, teď přišel o svůj výstupní tank)
            if (level.getBlockEntity(pos.below()) instanceof DistillationTankBlockEntity be)
                be.invalidateCache();
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) { return true; }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof DistillationTankBlockEntity be) {
            float fill = (float) be.tank.getFluidAmount() / be.tank.getCapacity();
            return Math.round(fill * 15);
        }
        return 0;
    }
}