package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.CokeOvenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;

/**
 * Controller blok Coke Ovenu — přední stěna (středová pozice).
 *
 * BLOCKSTATE:
 *   FACING  = ve kterou stranu kouká přední stěna
 *   FORMED  = je pec správně sestavena?
 *
 * INTERAKCE:
 *   Pravý klik = pokus o sestavení / rozložení pece.
 *   Při sestavení: validátor projde 3×3×3, nastaví formed=true.
 *   Při rozložení: formed=false, progress resetován.
 *
 * TICKER:
 *   Pouze server-side, volá CokeOvenBlockEntity.serverTick().
 */
public class CokeOvenControllerBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty   FORMED = BooleanProperty.create("formed");

    public static final MapCodec<CokeOvenControllerBlock> CODEC = simpleCodec(properties -> new CokeOvenControllerBlock());

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public CokeOvenControllerBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(3.5f, 8.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noOcclusion());
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(FORMED, false);
    }

    // ── Interakce: pravý klik → sestavit / rozložit ──────────────────────────
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (!(level.getBlockEntity(pos) instanceof CokeOvenBlockEntity be)) return InteractionResult.PASS;

        if (state.getValue(FORMED)) {
            // Rozlož pec
            deform(level, pos, state, be);
            player.sendSystemMessage(Component.translatable("dif.coke_oven.deformed"));
        } else {
            // Pokus o sestavení
            if (CokeOvenMultiblockValidator.validate(level, pos)) {
                form(level, pos, state, be);
                player.sendSystemMessage(Component.translatable("dif.coke_oven.formed"));
            } else {
                player.sendSystemMessage(Component.translatable("dif.coke_oven.invalid_structure"));
            }
        }
        return InteractionResult.CONSUME;
    }

    /** Sestaví pec — nastaví FORMED=true na controlleru. */
    private void form(Level level, BlockPos pos, BlockState state, CokeOvenBlockEntity be) {
        level.setBlock(pos, state.setValue(FORMED, true), 3);
        be.setFormed(true);
    }

    /** Rozloží pec — nastaví FORMED=false. */
    private void deform(Level level, BlockPos pos, BlockState state, CokeOvenBlockEntity be) {
        level.setBlock(pos, state.setValue(FORMED, false), 3);
        be.setFormed(false);
    }

    // ── Pokud soused bloku se změní → zkontroluj strukturu ───────────────────
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide || !state.getValue(FORMED)) return;

        // Pokud je pec sestavena a někdo odebral blok → validuj
        if (!(level.getBlockEntity(pos) instanceof CokeOvenBlockEntity be)) return;
        if (!CokeOvenMultiblockValidator.validate(level, pos)) {
            deform(level, pos, state, be);
        }
    }

    // ── GUI (otevři inventář pokud je pec sestavena) ─────────────────────────
    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        if (!state.getValue(FORMED)) return null;
        return (MenuProvider) level.getBlockEntity(pos);
    }

    // ── BlockEntity ───────────────────────────────────────────────────────────
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CokeOvenBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, DifModBlockEntities.COKE_OVEN.get(),
                CokeOvenBlockEntity::serverTick);
    }

    // ── Při odebrání bloku: deaktivuj pec ────────────────────────────────────
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && !state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof CokeOvenBlockEntity be) {
                be.setFormed(false);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}