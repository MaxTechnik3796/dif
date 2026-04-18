package cz.maxtechnik.dif.block;

import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlock;
import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;
import cz.maxtechnik.dif.block.entity.IndustrialLargeWaterWheelBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class IndustrialLargeWaterWheelBlock extends LargeWaterWheelBlock {

    public IndustrialLargeWaterWheelBlock() {
        super(BlockBehaviour.Properties.of()
                .sound(SoundType.METAL)
                .strength(1.5F, 6.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        );
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntityType<? extends LargeWaterWheelBlockEntity> getBlockEntityType() {
        return DifModBlockEntities.INDUSTRIAL_LARGE_WATER_WHEEL.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<LargeWaterWheelBlockEntity> getBlockEntityClass() {
        return (Class<LargeWaterWheelBlockEntity>) (Class<?>) IndustrialLargeWaterWheelBlockEntity.class;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IndustrialLargeWaterWheelBlockEntity(pos, state);
    }
}