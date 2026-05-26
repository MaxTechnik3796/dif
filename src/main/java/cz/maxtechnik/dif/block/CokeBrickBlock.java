package cz.maxtechnik.dif.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Čistě dekorativní blok — tvoří stěny Coke Ovenu.
 * Žádná logika, žádná BlockEntity. Lze použít i na stavbu baráků
 * bez jakéhokoli vedlejšího efektu — aktivace pece probíhá pouze
 * přes CokeOvenControllerBlock.
 */
public class CokeBrickBlock extends Block {

    public CokeBrickBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(3.5f, 8.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }
}