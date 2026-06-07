package cz.maxtechnik.dif.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import cz.maxtechnik.dif.block.entity.ForgeControllerBlockEntity;
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

/**
 * Forge Furnace Controller — řídicí blok celé pece.
 *
 * Block states:
 *   FACING  → směr čelní strany (pro orientaci GUI a rendering)
 *   FORMED  → true pokud je struktura platná
 *   ACTIVE  → true pokud právě probíhá tavení
 *   LOCKED  → true pokud bylo poškozeno sklo pod hladinou kapaliny
 *
 * LOCKED state blokuje veškeré I/O dokud hráč nenapraví strukturu
 * nebo nevypustí kapalinu pod kritickou hladinu.
 */
@SuppressWarnings("deprecation")
public class ForgeFurnaceController extends Block implements EntityBlock, IWrenchable {

    public static final BooleanProperty   FORMED  = BooleanProperty.create("formed");
    public static final BooleanProperty   ACTIVE  = BooleanProperty.create("active");
    public static final BooleanProperty   LOCKED  = BooleanProperty.create("locked");
    public static final DirectionProperty FACING  = HorizontalDirectionalBlock.FACING;

    public ForgeFurnaceController(Properties properties) {
        // Svítí pokud ACTIVE
        super(properties.lightLevel(bs -> bs.getValue(ACTIVE) ? 10 : 0));
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false)
                .setValue(ACTIVE, false)
                .setValue(LOCKED, false));
    }

    // ── BlockEntity ───────────────────────────────────────────────────────────

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState blockState) {
        return DifModBlockEntities.FORGE_FURNACE_CONTROLLER.get().create(pos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level, @NotNull BlockState blockState, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return ForgeControllerBlockEntity.ticker(type);
    }

    // ── Block state ───────────────────────────────────────────────────────────

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED, ACTIVE, LOCKED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // ── Odebrání ──────────────────────────────────────────────────────────────

    @Override
    public void onRemove(BlockState blockState, @NotNull Level level,
                         @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!blockState.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof ForgeControllerBlockEntity be) {

            // Vyhoď inventář
            var inv = be.getInventory();
            for (int i = 0; i < inv.getSlots(); i++) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                        inv.getStackInSlot(i));
            }

            // Uvolni briky — controller si data uloží do NBT automaticky přes saveAdditional
            if (blockState.getValue(FORMED)) {
                releaseBricks(level, be);
            }

            // Fluidy se záměrně NEZTRÁCEJÍ — controller si je pamatuje v NBT
            // Pokud hráč postaví nový controller na stejné místo, data se obnoví
            // (ForgeControllerBlockEntity.onLoad() zkontroluje NBT marker)
        }
        super.onRemove(blockState, level, pos, newState, isMoving);
    }

    private static void releaseBricks(Level level, ForgeControllerBlockEntity be) {
        net.minecraft.core.Direction facing = level.getBlockState(be.getBlockPos())
                .getOptionalValue(FACING).orElse(net.minecraft.core.Direction.SOUTH);
        net.minecraft.core.Direction intoStructure = facing.getOpposite();
        cz.maxtechnik.dif.util.ForgeMultiblockHelper.forEachBrick(be.getBlockPos(), intoStructure, brickPos -> {
            if (level.getBlockEntity(brickPos) instanceof cz.maxtechnik.dif.block.entity.ForgeBrickBlockEntity brick) {
                brick.setControllerPos(null);
            }
            return true;
        });
    }

    // ── Interakce hráče ───────────────────────────────────────────────────────

    @Override
    protected @NotNull ItemInteractionResult useItemOn(
            @NotNull ItemStack heldItem, @NotNull BlockState blockState, @NotNull Level level,
            @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand,
            @NotNull BlockHitResult hit) {

        if (blockState.getValue(FORMED)
                && level.getBlockEntity(pos) instanceof ForgeControllerBlockEntity be
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
                && level.getBlockEntity(pos) instanceof ForgeControllerBlockEntity be
                && be.handleInteraction(player, InteractionHand.MAIN_HAND)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onWrenched(BlockState blockState, UseOnContext ctx) {
        // Klíčem lze otočit controller pouze pokud není zformován
        if (ctx.getLevel().isClientSide || blockState.getValue(FORMED)) return InteractionResult.PASS;
        return InteractionResult.CONSUME;
    }
}