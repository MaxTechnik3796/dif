package cz.maxtechnik.dif.block;

import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlock;
import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;
import cz.maxtechnik.dif.block.entity.BrassLargeWaterWheelBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BrassLargeWaterWheelBlock extends LargeWaterWheelBlock {

    public BrassLargeWaterWheelBlock() {
        super(BlockBehaviour.Properties.of()
                .sound(SoundType.METAL)
                .strength(1.5F, 6.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        );
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntityType<? extends LargeWaterWheelBlockEntity> getBlockEntityType() {
        return DifModBlockEntities.BRASS_LARGE_WATER_WHEEL.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<LargeWaterWheelBlockEntity> getBlockEntityClass() {
        return (Class<LargeWaterWheelBlockEntity>) (Class<?>) BrassLargeWaterWheelBlockEntity.class;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrassLargeWaterWheelBlockEntity(pos, state);
    }
}