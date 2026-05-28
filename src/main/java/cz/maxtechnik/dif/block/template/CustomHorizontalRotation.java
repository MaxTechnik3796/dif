package cz.maxtechnik.dif.block.template;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class CustomHorizontalRotation extends Block{
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public CustomHorizontalRotation(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH));
	}
	@Override
	public int getLightBlock(@NotNull BlockState blockState,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 15;
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return this.defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());
	}
	public @NotNull BlockState rotate(BlockState blockState,Rotation rotation){
		return blockState.setValue(FACING,rotation.rotate(blockState.getValue(FACING)));
	}
	public @NotNull BlockState mirror(BlockState blockState,Mirror mirror){
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}
}
