package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.QuarryLandmarkBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * QuarryLandmark – blok (model torch) sloužící k definici těžební oblasti quarry.
 *
 * Pravidla:
 *  - Musí být 3 landmarky na stejné Y úrovni v L-tvaru.
 *  - Pravý klik spustí scan + zobrazí zprávu nebo formaci.
 *  - Po přiřazení k quarry se landmarky zničí a droppí item.
 *  - Při zničení po formaci: ostatní LM ztratí formaci, ale nezničí se.
 */
@SuppressWarnings("deprecation")
public class QuarryLandmark extends BaseEntityBlock {

    public QuarryLandmark() {
        super(Properties.of()
                .strength(0.5F, 0.5F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .lightLevel(s -> 14));
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new QuarryLandmarkBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        // Landmark nemá vlastní tick
        return null;
    }

    /**
     * Pravý klik → scan + formace (nebo zpráva o stavu).
     */
    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level,
                                          @NotNull BlockPos pos, @NotNull Player player,
                                          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof QuarryLandmarkBlockEntity lbe) {
            lbe.onRightClick(player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Při zničení → informuj partnery aby ztratili formaci.
     */
    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                         @NotNull BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof QuarryLandmarkBlockEntity lbe) {
                lbe.onRemoved();
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}