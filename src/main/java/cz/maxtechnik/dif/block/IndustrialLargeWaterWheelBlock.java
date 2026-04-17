package cz.maxtechnik.dif.block;

import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlock;
import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.block.entity.IndustrialLargeWaterWheelBlockEntity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;

public class IndustrialLargeWaterWheelBlock extends LargeWaterWheelBlock {

    public IndustrialLargeWaterWheelBlock() {
        super(BlockBehaviour.Properties.of()
                .sound(SoundType.METAL)
                .strength(1.5F, 6.0F)
                .requiresCorrectToolForDrops()
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
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        return null;
    }

    @Override
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable net.minecraft.world.entity.Entity entity) {
        return 0.6f;
    }
}