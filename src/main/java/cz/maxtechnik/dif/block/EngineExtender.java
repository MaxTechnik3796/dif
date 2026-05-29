package cz.maxtechnik.dif.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class EngineExtender extends Block implements SimpleWaterloggedBlock{
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty TOP=BooleanProperty.create("top");
	public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
	public EngineExtender(Properties properties){
		super(properties.noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(TOP,false).setValue(WATERLOGGED,false));
	}
	@Override
	public boolean propagatesSkylightDown(BlockState state,@NotNull BlockGetter reader,@NotNull BlockPos pos){
		return state.getFluidState().isEmpty();
	}
	@Override
	public int getLightBlock(@NotNull BlockState blockState,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING,TOP,WATERLOGGED);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		Level level=context.getLevel();
		Direction newFacing=context.getClickedFace();
		boolean water=context.getLevel().getFluidState(context.getClickedPos()).getType().equals(Fluids.WATER);
		if(newFacing.equals(Direction.UP)){
			if(level.getBlockState(context.getClickedPos().below()).getBlock() instanceof Engine) newFacing=level.getBlockState(context.getClickedPos().below()).getValue(FACING);
			else newFacing=context.getHorizontalDirection();
			return this.defaultBlockState().setValue(FACING,newFacing).setValue(TOP,true).setValue(WATERLOGGED,water);
		}else{
			if(newFacing.equals(Direction.DOWN)) newFacing=context.getHorizontalDirection().getOpposite();
			return this.defaultBlockState().setValue(FACING,newFacing).setValue(TOP,false).setValue(WATERLOGGED,water);
		}
	}
	public @NotNull BlockState rotate(BlockState blockState,Rotation rotation){
		return blockState.setValue(FACING,rotation.rotate(blockState.getValue(FACING)));
	}
	public @NotNull BlockState mirror(BlockState blockState,Mirror mirror){
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}
	@Override
	public float getShadeBrightness(@NotNull BlockState blockState,@NotNull BlockGetter blockGetter,@NotNull BlockPos pos){
		return 1F;
	}
	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return Shapes.empty();
	}
	@Override
	public @NotNull FluidState getFluidState(BlockState blockState){
		return blockState.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(blockState);
	}
	@Override
	public @NotNull BlockState updateShape(BlockState blockState,@NotNull Direction facing,@NotNull BlockState facingState,@NotNull LevelAccessor world,@NotNull BlockPos currentPos,@NotNull BlockPos facingPos){
		if(blockState.getValue(WATERLOGGED)) world.scheduleTick(currentPos,Fluids.WATER,Fluids.WATER.getTickDelay(world));
		return super.updateShape(blockState,facing,facingState,world,currentPos,facingPos);
	}
}
