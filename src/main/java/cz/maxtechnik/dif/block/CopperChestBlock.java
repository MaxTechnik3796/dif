package cz.maxtechnik.dif.block;

import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class CopperChestBlock extends ChestBlock {
    public CopperChestBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(2.5F), () -> net.minecraft.world.level.block.entity.BlockEntityType.CHEST);
    }
}
