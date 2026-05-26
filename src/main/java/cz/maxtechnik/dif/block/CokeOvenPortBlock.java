package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.CokeOvenPortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;

/**
 * Port blok — umístěn na stěně pece, deleguje capability na controller.
 * Každý port ví:
 *   - Svoji pozici ve světě
 *   - Pozici controlleru (uložena v NBT)
 *   - Směr (FACING) — ze které strany capability přijímá
 * Capability jsou registrovány v DifMod pro tento BlockEntityType
 * a delegují na CokeOvenBlockEntity controlleru.
 * Port se NEdá ručně umístit — vzniká automaticky při form() v controlleru.
 * (TODO: přidat form() logiku pro auto-placement portů)
 */
public class CokeOvenPortBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public static final MapCodec<CokeOvenPortBlock> CODEC = simpleCodec(properties -> new CokeOvenPortBlock());

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public CokeOvenPortBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(3.5f, 8.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state) {
        return new CokeOvenPortBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> type) {
        return null; // Port sám o sobě nic nedělá
    }
}