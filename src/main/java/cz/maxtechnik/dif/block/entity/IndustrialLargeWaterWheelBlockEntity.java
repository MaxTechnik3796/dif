package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class IndustrialLargeWaterWheelBlockEntity extends WaterWheelBlockEntity {

    // WaterWheelVisualizer NEEXISTUJE v Create 0.5.1.h — odstraněno.
    // WaterWheelBlockEntity konstruktor bere (BlockEntityType, BlockPos, BlockState).

    public IndustrialLargeWaterWheelBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state) {
        super(type, pos, state);
    }

    // calculateStressApplied() — vrátíme 0, tento blok generuje, nevyužívá stress.
    @Override
    public float calculateStressApplied() {
        return 0f;
    }

    // calculateAddedStressCapacity() — 4x více než standardní LargeWaterWheel.
    @Override
    public float calculateAddedStressCapacity() {
        return super.calculateAddedStressCapacity() * 4.0f;
    }
}