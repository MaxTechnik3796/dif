package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.QuarryBlockEntity;
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

    @Nullable @Override public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new QuarryBlockEntity(pos, state);
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    /** Zkontroluje pouze oblast kde budou stát frame bloky (ne celou hloubku šachty).
     *  Vrátí true pokud tam je blok s destroySpeed < 0 (bedrock, barrier...). */
    private static boolean hasUnbreakableInFrameArea(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        BlockPos center = pos.relative(facing.getOpposite(), QuarryBlockEntity.RANGE + 1);
        int range = QuarryBlockEntity.RANGE;
        int yBase = pos.getY();
        int yTop  = yBase + 3; // FRAME_HEIGHT = 3

        for (int y = yBase; y <= yTop; y++) {
            for (int x = center.getX() - range; x <= center.getX() + range; x++) {
                for (int z = center.getZ() - range; z <= center.getZ() + range; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState bs = level.getBlockState(p);
                    if (!bs.isAir() && bs.getDestroySpeed(level, p) < 0) return true;
                }
            }
        }
        return false;
    }

    @Override public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                  @NotNull BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (!level.isClientSide && hasUnbreakableInFrameArea(level, pos, state)) {
            level.removeBlock(pos, false);
            Block.popResource(level, pos, new net.minecraft.world.item.ItemStack(this));
        }
    }

    @Override public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof QuarryBlockEntity q)
            q.onQuarryRemoved();
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return createTickerHelper(type, DifModBlockEntities.QUARRY.get(), QuarryBlockEntity::tick);
    }
}