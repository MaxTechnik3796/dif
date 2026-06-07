package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.AbstractMultiblockBrickBlockEntity;
import cz.maxtechnik.dif.block.entity.ForgeBrickBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Forge Brick — jeden z 8 okolních bloků v controller vrstvě Forge pece.
 *
 * Dědí AbstractMultiblockBrick, takže:
 * - Při položení automaticky maže referenci na controller (ochrana před kontrapcemi)
 * - Při odebrání upozorní controller aby převalidoval strukturu
 *
 * Integrita briků se sleduje v ForgeBrickBlockEntity.
 * Ke každému briku lze připojit Hopper nebo Neob Pipe z venku —
 * controller to detekuje v ForgeControllerBlockEntity.detectAttachments().
 */
public class ForgeBrickBlock extends AbstractMultiblockBrick {

    public ForgeBrickBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState blockState) {
        return DifModBlockEntities.FORGE_BRICK.get().create(pos, blockState);
    }

    @Override
    protected @Nullable AbstractMultiblockBrickBlockEntity getBlockEntityFromPos(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ForgeBrickBlockEntity be ? be : null;
    }
}