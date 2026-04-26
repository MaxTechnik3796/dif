package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.CameraBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
@SuppressWarnings("deprecation")
public class Camera extends BaseEntityBlock{
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public Camera(Properties properties){
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH));
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING);
	}
	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return this.defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());
	}
	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState blockState){
		return RenderShape.MODEL;
	}
	public @NotNull BlockState rotate(BlockState state,Rotation rot){
		return state.setValue(FACING,rot.rotate(state.getValue(FACING)));
	}
	public @NotNull BlockState mirror(BlockState state,Mirror mirrorIn){
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}
	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new CameraBlockEntity(pos,state);
	}
	@Override
	public void onRemove(BlockState state,@NotNull Level level,@NotNull BlockPos pos,BlockState newState,boolean isMoving){
		if(!state.is(newState.getBlock())){
			super.onRemove(state,level,pos,newState,isMoving);
		}
	}
}