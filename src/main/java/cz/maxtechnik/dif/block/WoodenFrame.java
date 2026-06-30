package cz.maxtechnik.dif.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
public class WoodenFrame extends Block{
	public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
	public WoodenFrame(Properties properties){
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED,false));
	}
	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return Shapes.empty();
	}
	@Override
	public @NotNull VoxelShape getShape(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return Shapes.or(
				box(0,14,0,2,16,16),
				box(14,14,0,16,16,16),
				box(2,14,0,14,16,2),
				box(2,14,14,14,16,16),
				box(0,2,0,2,14,2),
				box(14,2,0,16,14,2),
				box(0,2,14,2,14,16),
				box(14,2,14,16,14,16),
				box(0,0,0,2,2,16),
				box(14,0,0,16,2,16),
				box(2,0,0,14,2,2),
				box(2,0,14,14,2,16)
		);
	}
	@Override
	public boolean skipRendering(@NotNull BlockState blockState,BlockState adjacentBlockState,@NotNull Direction side){
		return adjacentBlockState.getBlock()==this||super.skipRendering(blockState,adjacentBlockState,side);
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
	public int getFlammability(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull Direction face){
		return 20;
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(WATERLOGGED);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		boolean flag=context.getLevel().getFluidState(context.getClickedPos()).getType().equals(Fluids.WATER);
		return this.defaultBlockState().setValue(WATERLOGGED,flag);
	}
	@Override
	public @NotNull FluidState getFluidState(BlockState blockState){
		return blockState.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(blockState);
	}
	@Override
	public float getShadeBrightness(@NotNull BlockState blockState,@NotNull BlockGetter blockGetter,@NotNull BlockPos pos){
		return 1F;
	}
	@Override
	public @NotNull BlockState updateShape(BlockState blockState,@NotNull Direction facing,@NotNull BlockState facingState,@NotNull LevelAccessor world,@NotNull BlockPos currentPos,@NotNull BlockPos facingPos){
		if(blockState.getValue(WATERLOGGED))
			world.scheduleTick(currentPos,Fluids.WATER,Fluids.WATER.getTickDelay(world));
		return super.updateShape(blockState,facing,facingState,world,currentPos,facingPos);
	}
}
