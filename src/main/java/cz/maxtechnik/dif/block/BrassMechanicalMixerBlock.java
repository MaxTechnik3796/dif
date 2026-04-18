package cz.maxtechnik.dif.block;

import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlock;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import cz.maxtechnik.dif.block.entity.BrassMechanicalMixerBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BrassMechanicalMixerBlock extends MechanicalMixerBlock {
    public BrassMechanicalMixerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<MechanicalMixerBlockEntity> getBlockEntityClass() {
        return (Class<MechanicalMixerBlockEntity>) (Class<?>) BrassMechanicalMixerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalMixerBlockEntity> getBlockEntityType() {
        return DifModBlockEntities.BRASS_MECHANICAL_MIXER.get();
    }
}
