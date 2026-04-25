package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.QuarryBlockEntity;
import cz.maxtechnik.dif.block.entity.QuarryLandmarkBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class Quarry extends BaseEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public Quarry(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.MODEL; }

    @Nullable @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new QuarryBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    private static boolean hasUnbreakableInFrameArea(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        int range = QuarryBlockEntity.DEFAULT_RANGE;
        BlockPos center = pos.relative(facing.getOpposite(), range + 1);
        int yBase = pos.getY();
        int yTop  = yBase + 3;

        for (int y = yBase; y <= yTop; y++)
            for (int x = center.getX() - range; x <= center.getX() + range; x++)
                for (int z = center.getZ() - range; z <= center.getZ() + range; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState bs = level.getBlockState(p);
                    if (!bs.isAir() && bs.getDestroySpeed(level, p) < 0) return true;
                }
        return false;
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                        @NotNull BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (level.isClientSide) return;

        if (hasUnbreakableInFrameArea(level, pos, state)) {
            level.removeBlock(pos, false);
            Block.popResource(level, pos, new net.minecraft.world.item.ItemStack(this));
            return;
        }

        // Hledej formed landmarky v okolí a aplikuj jejich oblast
        tryApplyNearbyLandmarks(level, pos);
    }

    /**
     * Po položení quarry prohledá okolí (±MAX_SEARCH bloků) na formed landmarky.
     * Pokud najde skupinu se sformovanou oblastí, aplikuje ji na quarry
     * a landmarky dropnou a zmizí.
     */
    private static void tryApplyNearbyLandmarks(Level level, BlockPos quarryPos) {
        int search = QuarryBlockEntity.MAX_AREA_SIDE + 4;
        int baseY  = quarryPos.getY();

        for (int dx = -search; dx <= search; dx++) {
            for (int dz = -search; dz <= search; dz++) {
                for (int dy = -8; dy <= 8; dy++) {
                    BlockPos p = new BlockPos(quarryPos.getX() + dx, baseY + dy, quarryPos.getZ() + dz);
                    if (!level.getBlockState(p).is(DifModBlocks.QUARRY_LANDMARK.get())) continue;
                    if (!(level.getBlockEntity(p) instanceof QuarryLandmarkBlockEntity lbe)) continue;
                    if (!lbe.isFormed()) continue;

                    // Najden formed landmark – aplikuj oblast na quarry a zruš landmarky
                    lbe.applyToQuarry(level, quarryPos);
                    return;
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                         BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof QuarryBlockEntity q)
            q.onQuarryRemoved();
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, 
                                          @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof QuarryBlockEntity qbe) {
                NetworkHooks.openScreen((ServerPlayer) player, qbe, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return createTickerHelper(type, DifModBlockEntities.QUARRY.get(), QuarryBlockEntity::tick);
    }
}