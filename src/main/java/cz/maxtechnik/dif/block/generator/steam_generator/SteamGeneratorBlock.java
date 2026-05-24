package cz.maxtechnik.dif.block.generator.steam_generator;

import cz.maxtechnik.dif.block.generator.AbstractFluidGeneratorBlock;
import cz.maxtechnik.dif.block.generator.AbstractFluidGeneratorBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Steam Generator blok.
 *
 * Veškerá logika (hřídel, redstone, kýbl) je v {@link AbstractFluidGeneratorBlock}.
 * Tato třída jen přiřadí správný block entity type a definici.
 */
public class SteamGeneratorBlock extends AbstractFluidGeneratorBlock {

    public SteamGeneratorBlock(Properties properties) {
        super(properties, SteamGeneratorDefinition.INSTANCE);
    }

    @Override
    public Class<AbstractFluidGeneratorBlockEntity> getBlockEntityClass() {
        return AbstractFluidGeneratorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AbstractFluidGeneratorBlockEntity> getBlockEntityType() {
        return DifModBlockEntities.STEAM_GENERATOR.get();
    }
}
