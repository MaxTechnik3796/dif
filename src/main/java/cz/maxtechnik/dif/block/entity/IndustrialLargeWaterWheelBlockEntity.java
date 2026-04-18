package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

@SuppressWarnings("deprecation")
public class IndustrialLargeWaterWheelBlockEntity extends LargeWaterWheelBlockEntity {

    public IndustrialLargeWaterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(DifModBlockEntities.INDUSTRIAL_LARGE_WATER_WHEEL.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
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
            this.material = DifModBlocks.INDUSTRIAL_LARGE_WATER_WHEEL.get().defaultBlockState();
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
            this.material = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), compound.getCompound("Material"));
        }
        // Zajistíme valid material po načtení
        initializeMaterial();
    }

    @Override
    public float calculateStressApplied() {
        return 0f;
    }

    @Override
    public float calculateAddedStressCapacity() {
        return 512f;
    }
}