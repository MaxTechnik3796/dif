package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.MonitorBlockEntity;
import cz.maxtechnik.dif.util.MonitorState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MonitorBlock extends BaseEntityBlock {
    public static final EnumProperty<MonitorState> STATE = EnumProperty.create("state", MonitorState.class);

    public MonitorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STATE, MonitorState.NO_SIGNAL));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state,Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof MonitorBlockEntity monitor) {
            return monitor.useMonitor(player);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state) {
        return new MonitorBlockEntity(pos, state);
    }
}