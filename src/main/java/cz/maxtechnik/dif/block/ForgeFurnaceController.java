package cz.maxtechnik.dif.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import cz.maxtechnik.dif.block.entity.ForgeBrickBlockEntity;
import cz.maxtechnik.dif.block.entity.ForgeControllerBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.ForgeMultiblockHelper;
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
 *   FACING  → směr čelní strany
 *   FORMED  → true pokud je struktura platná
 *   ACTIVE  → true pokud právě probíhá tavení
 *   LOCKED  → true pokud bylo poškozeno sklo pod hladinou kapaliny
 *
 * Interakce:
 *   Pravý klik s itemem     → vloží stack do fronty tavení
 *   Pravý klik prázdnou rukou → vyndá 1 item (shift = celý stack)
 *   Pravý klik kbelíkem    → naplní kbelík z tanku
 *   Wrench (Create)        → cykluje preferovanou výstupní kapalinu
 *                            + přesune ji vizuálně na spod render orderu
 */
@SuppressWarnings("deprecation")
public class ForgeFurnaceController extends Block implements EntityBlock, IWrenchable {

    public static final BooleanProperty   FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty   ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty   LOCKED = BooleanProperty.create("locked");
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ForgeFurnaceController(Properties properties) {
        super(properties.lightLevel(bs -> bs.getValue(ACTIVE) ? 10 : 0));
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false)
                .setValue(ACTIVE, false)
                .setValue(LOCKED, false));
    }

    // ── BlockEntity ───────────────────────────────────────────────────────────

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return DifModBlockEntities.FORGE_FURNACE_CONTROLLER.get().create(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return ForgeControllerBlockEntity.ticker(type);
    }

    // ── Block state ───────────────────────────────────────────────────────────

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED, ACTIVE, LOCKED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
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
            for (int i = 0; i < inv.getSlots(); i++)
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), inv.getStackInSlot(i));

            // Uvolni briky
            if (blockState.getValue(FORMED)) releaseBricks(level, be);

            // Fluidy zůstávají v NBT controlleru — obnoví se při novém umístění
        }
        super.onRemove(blockState, level, pos, newState, isMoving);
    }

    private static void releaseBricks(Level level, ForgeControllerBlockEntity be) {
        Direction facing       = level.getBlockState(be.getBlockPos()).getOptionalValue(FACING).orElse(Direction.SOUTH);
        Direction intoStructure = facing.getOpposite();
        ForgeMultiblockHelper.forEachBrick(be.getBlockPos(), intoStructure, brickPos -> {
            if (level.getBlockEntity(brickPos) instanceof ForgeBrickBlockEntity brick)
                brick.setControllerPos(null);
            return true;
        });
    }

    // ── Interakce hráče ───────────────────────────────────────────────────────

    /**
     * Wrench → cykluje preferovanou výstupní kapalinu a posouvá ji
     * na spod render orderu (vizuálně nejníže = první na výstupu).
     * Jiný item → deleguj na handleInteraction (vložení do fronty).
     */
    @Override
    protected @NotNull ItemInteractionResult useItemOn(
            @NotNull ItemStack heldItem, @NotNull BlockState blockState, @NotNull Level level,
            @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand,
            @NotNull BlockHitResult hit) {

        if (!blockState.getValue(FORMED)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(level.getBlockEntity(pos) instanceof ForgeControllerBlockEntity be))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        // Wrench detekce — Create WrenchItem
        boolean isWrench = heldItem.getItem() instanceof com.simibubi.create.content.equipment.wrench.WrenchItem;

        if (isWrench) {
            if (level.isClientSide) {
                net.minecraft.client.Minecraft.getInstance()
                        .setScreen(new cz.maxtechnik.dif.gui.screen.ForgeRadialScreen(be));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // Ostatní itemy → vložení do fronty
        if (be.handleInteraction(player, hand)) return ItemInteractionResult.sidedSuccess(level.isClientSide);
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** Prázdná ruka → vyndej item z fronty. */
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

    /**
     * IWrenchable override — blokujeme výchozí Create wrench chování
     * (otočení bloku) pokud je structure zformována. Místo toho cyklujeme
     * kapaliny přes useItemOn výše.
     */
    @Override
    public InteractionResult onWrenched(BlockState blockState, UseOnContext ctx) {
        // Pokud je zformováno, wrench řešíme v useItemOn — tady vrátíme PASS
        // aby Create nespustil rotaci bloku
        if (blockState.getValue(FORMED)) return InteractionResult.PASS;
        // Nezformováno → normálně nechej Create otočit blok
        return InteractionResult.CONSUME;
    }
}