package cz.maxtechnik.dif.block.space;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
public class SpaceEngine extends Block implements SimpleWaterloggedBlock{
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
	public SpaceEngine(){
		super(BlockBehaviour.Properties.of().sound(SoundType.NETHERITE_BLOCK).strength(4F,1000000F).noOcclusion().isRedstoneConductor((bs,br,bp)->false));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(WATERLOGGED,false));
	}
	@Override
	public boolean propagatesSkylightDown(BlockState blockState,@NotNull BlockGetter reader,@NotNull BlockPos pos){
		return blockState.getFluidState().isEmpty();
	}
	@Override
	public int getLightBlock(@NotNull BlockState blockState,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return Shapes.empty();
	}
	@Override
	public @NotNull VoxelShape getShape(BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return switch(blockState.getValue(FACING)){
			case NORTH -> box(4,-5,4,13,32,13);
			case EAST -> box(3,-5,4,12,32,13);
			case WEST -> box(4,-5,3,13,32,12);
			default -> box(3,-5,3,12,32,12);
		};
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING,WATERLOGGED);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		boolean flag=context.getLevel().getFluidState(context.getClickedPos()).getType()==Fluids.WATER;
		return this.defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED,flag);
	}
	public @NotNull BlockState rotate(BlockState blockState,Rotation rotation){
		return blockState.setValue(FACING,rotation.rotate(blockState.getValue(FACING)));
	}
	public @NotNull BlockState mirror(BlockState blockState,Mirror mirror){
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}
	@Override
	public @NotNull FluidState getFluidState(BlockState blockState){
		return blockState.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(blockState);
	}
	@Override
	public @NotNull BlockState updateShape(BlockState blockState,@NotNull Direction facing,@NotNull BlockState facingState,@NotNull LevelAccessor world,@NotNull BlockPos currentPos,@NotNull BlockPos facingPos){
		if(blockState.getValue(WATERLOGGED))
			world.scheduleTick(currentPos,Fluids.WATER,Fluids.WATER.getTickDelay(world));
		return super.updateShape(blockState,facing,facingState,world,currentPos,facingPos);
	}
}
