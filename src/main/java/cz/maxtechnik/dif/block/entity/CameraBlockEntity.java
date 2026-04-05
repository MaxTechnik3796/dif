package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.other.DifModBlockEntities; // Předpokládané umístění registru
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CameraBlockEntity extends BlockEntity {
    
    public CameraBlockEntity(BlockPos pos, BlockState state) {
        super(DifModBlockEntities.CAMERA.get(), pos, state);
    }

    // Do budoucna zde můžeš přidat třeba spotřebu energie 
    // nebo metodu pro rotaci čočky (pokud bys chtěl otáčecí kamery)
}