package cz.maxtechnik.dif.block;

import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlock;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import org.jetbrains.annotations.Nullable;

public class IndustrialLargeWaterWheelBlock extends LargeWaterWheelBlock {

    // Create 0.5.1.h — LargeWaterWheelBlock konstruktor bere POUZE Properties.
    // BlockEntityType se poskytuje přes přepsání getBlockEntityType().
    public IndustrialLargeWaterWheelBlock() {
        super(BlockBehaviour.Properties.of()
                .sound(SoundType.METAL)       // zvuky iron blocku
                .strength(1.5F, 6.0F)         // odolnost kamene
                .requiresCorrectToolForDrops()
        );
    }

    // Řekneme Create, které BlockEntity má tento blok používat.
    @Override
    public net.minecraft.world.level.block.entity.BlockEntityType<? extends com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity> getBlockEntityType() {
        return DifModBlockEntities.INDUSTRIAL_LARGE_WATER_WHEEL.get();
    }

    // Zakázat změnu textury dřevem (axe stripping) — vrátíme null = žádný efekt
    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        return null;
    }

    // getFriction správně přijímá BlockGetter (NE LevelReader) v 1.20.1
    @Override
    public float getFriction(BlockState state, BlockGetter level, BlockPos pos, @Nullable net.minecraft.world.entity.Entity entity) {
        return 0.6f;
    }
}