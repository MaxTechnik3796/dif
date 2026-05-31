package cz.maxtechnik.dif.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import cz.maxtechnik.dif.block.entity.BlastSmelteryBlockEntity;
import cz.maxtechnik.dif.block.entity.BlastSmelteryControllerBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class BlastSmelteryController extends Block implements EntityBlock, IWrenchable {

    public static final BooleanProperty  ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty  FORMED = BooleanProperty.create("formed");
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public BlastSmelteryController(Properties properties) {
        super(properties.lightLevel(bs -> bs.getValue(ACTIVE) ? 12 : 0));
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false)
                .setValue(FORMED, false));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState blockState) {
        return DifModBlockEntities.BLAST_SMELTERY_CONTROLLER.get().create(pos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level, @NotNull BlockState blockState, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return BlastSmelteryControllerBlockEntity.ticker(type);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE, FORMED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void onRemove(BlockState blockState, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!blockState.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BlastSmelteryControllerBlockEntity be) {
            // Drop inventář
            var inv = be.getInventory();
            for (int i = 0; i < inv.getSlots(); i++)
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), inv.getStackInSlot(i));
            // Uvolni cihličky
            if (blockState.getValue(FORMED))
                releaseBricks(level, pos, blockState.getValue(FACING).getOpposite());
            // Fluidy se záměrně ztrácejí (nemají item formu mimo kbelík)
        }
        super.onRemove(blockState, level, pos, newState, isMoving);
    }

    private static void releaseBricks(Level level, BlockPos ctrlPos, Direction intoStructure) {
        cz.maxtechnik.dif.util.MultiblockHelper.forEachBrick(ctrlPos, intoStructure, mp -> {
            if (level.getBlockEntity(mp) instanceof BlastSmelteryBlockEntity brick)
                brick.setControllerPos(null);
            return true;
        });
    }

    @Override
    public InteractionResult onWrenched(BlockState blockState, UseOnContext ctx) {
        if (ctx.getLevel().isClientSide || blockState.getValue(FORMED)) return InteractionResult.PASS;
        return InteractionResult.CONSUME;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(
            @NotNull ItemStack heldItem, @NotNull BlockState blockState, @NotNull Level level,
            @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand,
            @NotNull BlockHitResult hit) {
        if (blockState.getValue(FORMED)
                && level.getBlockEntity(pos) instanceof BlastSmelteryControllerBlockEntity be
                && be.handleInteraction(player, hand)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(
            @NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos pos,
            @NotNull Player player, @NotNull BlockHitResult hit) {
        if (blockState.getValue(FORMED)
                && level.getBlockEntity(pos) instanceof BlastSmelteryControllerBlockEntity be
                && be.handleInteraction(player, InteractionHand.MAIN_HAND)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}