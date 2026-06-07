package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.ForgeControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Forge Glass — sklo vrstva Forge pece.
 *
 * Tento blok nemá vlastní BlockEntity — veškerá logika je v controlleru.
 * Vizuální renderování kapaliny uvnitř řešíš vlastním rendererem
 * (ForgeFluidRenderer) který čte data z ForgeControllerBlockEntity.
 *
 * Chování při zničení:
 *   Při onRemove controller detekuje chybějící sklo a přejde do
 *   jednoho ze dvou stavů:
 *     a) Vrstva nad hladinou kapaliny → jen sníží kapacitu, nezamkne se
 *     b) Vrstva na úrovni nebo pod hladinou → controller se uzamkne (LOCKED state)
 *
 * Chování při položení:
 *   Controller při příštím validačním ticku detekuje novou vrstvu
 *   a automaticky rozšíří kapacitu (pokud uzavírá chybějící blok při LOCKED).
 */
public class ForgeGlassBlock extends Block {

    public ForgeGlassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void onRemove(
            BlockState blockState,
            @NotNull Level level,
            @NotNull BlockPos pos,
            BlockState newState,
            boolean isMoving
    ) {
        if (!blockState.is(newState.getBlock()) && !level.isClientSide) {
            // Najdi controller pod tímto sklem (může být několik vrstev níže)
            notifyNearbyController(level, pos);
        }
        super.onRemove(blockState, level, pos, newState, isMoving);
    }

    @Override
    public void onPlace(
            @NotNull BlockState blockState,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull BlockState oldState,
            boolean isMoving
    ) {
        super.onPlace(blockState, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            // Nově položené sklo může uzavřít díru → upozorni controller
            notifyNearbyController(level, pos);
        }
    }

    /**
     * Hledá ForgeControllerBlockEntity pod touto pozicí (max 18 bloků dolů =
     * MAX_GLASS_LAYERS + controller vrstva + burner vrstva).
     * Když najde, nastaví forceValidation = true.
     */
    private void notifyNearbyController(Level level, BlockPos glassPos) {
        // Hledá controller v 3x3 sloupci pod tímto sklem
        int searchDepth = cz.maxtechnik.dif.util.ForgeMultiblockHelper.MAX_GLASS_LAYERS + 2;
        for (int dy = 1; dy <= searchDepth; dy++) {
            int y = glassPos.getY() - dy;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos check = new BlockPos(glassPos.getX() + dx, y, glassPos.getZ() + dz);
                    if (level.getBlockEntity(check) instanceof ForgeControllerBlockEntity ctrl) {
                        ctrl.forceValidation = true;
                        return;
                    }
                }
            }
        }
    }
}