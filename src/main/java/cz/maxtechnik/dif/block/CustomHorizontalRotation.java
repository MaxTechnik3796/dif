package cz.maxtechnik.dif.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.NotNull;

public class CustomHorizontalRotation extends Block{
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public CustomHorizontalRotation(SoundType sound,float hardness,float resistance,boolean requiresCorrectToolForDrops){
        super(requiresCorrectToolForDrops?Properties.of().strength(hardness,resistance).sound(sound).requiresCorrectToolForDrops():Properties.of().strength(hardness,resistance).sound(sound));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    @Override
    public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
        return 15;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>builder){
        builder.add(FACING);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context){
        return this.defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());
    }
    public @NotNull BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING,rot.rotate(state.getValue(FACING)));
    }
    public @NotNull BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }
}
