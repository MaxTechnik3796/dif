package cz.maxtechnik.dif.block;

import com.simibubi.create.content.kinetics.press.MechanicalPressBlock;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import cz.maxtechnik.dif.block.entity.BrassMechanicalPressBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BrassMechanicalPressBlock extends MechanicalPressBlock {
    public BrassMechanicalPressBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<MechanicalPressBlockEntity> getBlockEntityClass() {
        return (Class<MechanicalPressBlockEntity>) (Class<?>) BrassMechanicalPressBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalPressBlockEntity> getBlockEntityType() {
        return DifModBlockEntities.BRASS_MECHANICAL_PRESS.get();
    }
}
