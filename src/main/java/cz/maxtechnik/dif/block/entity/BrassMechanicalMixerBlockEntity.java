package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BrassMechanicalMixerBlockEntity extends MechanicalMixerBlockEntity {
    public BrassMechanicalMixerBlockEntity(BlockPos pos, BlockState state) {
        super(DifModBlockEntities.BRASS_MECHANICAL_MIXER.get(), pos, state);
    }

    @Override
    public float calculateStressApplied() {
        float impact = 4.0f * 4; // Vanilla is 4.0f
        this.lastStressApplied = impact;
        return impact;
    }

    @Override
    protected void applyBasinRecipe() {
        if (currentRecipe == null) return;
        for (int i = 0; i < 4; i++) {
            if (!matchBasinRecipe(currentRecipe)) break;
            super.applyBasinRecipe();
        }
    }
}
