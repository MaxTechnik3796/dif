package cz.maxtechnik.dif.block;

import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlock;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import cz.maxtechnik.dif.block.entity.BrassWaterWheelBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;


public class BrassWaterWheelBlock extends WaterWheelBlock {

    public BrassWaterWheelBlock() {
        super(BlockBehaviour.Properties.of()
                .sound(SoundType.METAL)
                .strength(1.5F, 6.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        );
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntityType<? extends WaterWheelBlockEntity> getBlockEntityType() {
        return DifModBlockEntities.BRASS_WATER_WHEEL.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<WaterWheelBlockEntity> getBlockEntityClass() {
        return (Class<WaterWheelBlockEntity>) (Class<?>) BrassWaterWheelBlockEntity.class;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrassWaterWheelBlockEntity(pos, state);
    }
}
