package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
@SuppressWarnings("deprecation")
public class IndustrialLargeWaterWheelBlockEntity extends LargeWaterWheelBlockEntity {

    public IndustrialLargeWaterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(cz.maxtechnik.dif.init.other.DifModBlockEntities.INDUSTRIAL_LARGE_WATER_WHEEL.get(), pos, state);
    }

    @Override
    public void addBehaviours(java.util.List<com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        initializeMaterial();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        initializeMaterial();
    }

    private void initializeMaterial() {
        if (this.material == null || this.material.isAir()) {
            this.material = cz.maxtechnik.dif.init.basic.DifModBlocks.INDUSTRIAL_LARGE_WATER_WHEEL.get().defaultBlockState();
        }
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        if (material != null) {
            compound.put("Material", NbtUtils.writeBlockState(material));
        }
    }

    @Override
    public void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        if (compound.contains("Material")) {
            // Použijeme asLookup() na BuiltInRegistries, což funguje v 1.20.1
            this.material = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), compound.getCompound("Material"));
        }
    }

    @Override
    public float calculateStressApplied() {
        return 0f;
    }

    @Override
    public float calculateAddedStressCapacity() {
        return 2048f;
    }
}