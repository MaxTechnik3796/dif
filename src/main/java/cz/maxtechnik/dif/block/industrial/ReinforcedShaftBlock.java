package cz.maxtechnik.dif.block.industrial;

import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import cz.maxtechnik.dif.block.industrial.entity.ReinforcedShaftBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ReinforcedShaftBlock extends ShaftBlock {

    public ReinforcedShaftBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends ReinforcedShaftBlockEntity> getBlockEntityType() {
        return DifModBlockEntities.REINFORCED_SHAFT.get();
    }

    // ==========================================================================
    //  Interaction
    // ==========================================================================

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof ReinforcedShaftBlockEntity be) {
                // Auto-detected role
                String role = be.isTransmitter() ? "Vysílač (Auto)" : "Přijímač (Auto)";
                String pairInfo = be.partnerPos != null
                        ? "Spárováno @ §a" + be.partnerPos.toShortString()
                        : "§chledá partnera...";
                float speed = be.getSpeed();
                player.displayClientMessage(
                        Component.literal("§6[DIF]§r [§b" + be.frequency + "§r] §e"
                                + role + "§r | " + pairInfo
                                + " §r| §a" + String.format("%.0f", speed) + " RPM"), true);
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    // ==========================================================================
    //  Destruction detection
    // ==========================================================================

    /**
     * Called when the block state changes. When the block is actually removed
     * (newState is a different block) we notify the BlockEntity BEFORE super removes it,
     * so it can tell the partner shaft it's gone.
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // Block is being DESTROYED – not a state-only update
            if (level.getBlockEntity(pos) instanceof ReinforcedShaftBlockEntity be) {
                be.onDestroyed();
            }
        }
        // Let Create + vanilla handle drops, BE removal, etc.
        super.onRemove(state, level, pos, newState, isMoving);
    }
}