package cz.maxtechnik.dif.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
public class AluminumProfile extends Block implements SimpleWaterloggedBlock{
	public static final EnumProperty<Direction.Axis> AXIS=BlockStateProperties.AXIS;
	public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
	public AluminumProfile(){
		super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(5F,6F).requiresCorrectToolForDrops().noOcclusion().isRedstoneConductor((bs,br,bp)->false));
		this.registerDefaultState(this.stateDefinition.any().setValue(AXIS,Direction.Axis.Y).setValue(WATERLOGGED,false));
	}
	@Override
	public boolean skipRendering(@NotNull BlockState blockState,BlockState adjacentBlockState,@NotNull Direction side){
		return adjacentBlockState.getBlock().equals(this)||super.skipRendering(blockState,adjacentBlockState,side);
	}
	@Override
	public int getLightBlock(@NotNull BlockState blockState,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState state,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return Shapes.empty();
	}
	@Override
	public float getShadeBrightness(@NotNull BlockState blockState,@NotNull BlockGetter blockGetter,@NotNull BlockPos pos){
		return 1F;
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(AXIS,WATERLOGGED);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		boolean flag=context.getLevel().getFluidState(context.getClickedPos()).getType()==Fluids.WATER;
		return this.defaultBlockState().setValue(AXIS,context.getClickedFace().getAxis()).setValue(WATERLOGGED,flag);
	}
	@Override
	public @NotNull BlockState rotate(@NotNull BlockState blockState,@NotNull Rotation rotation){
		if(rotation.equals(Rotation.CLOCKWISE_90)||rotation.equals(Rotation.COUNTERCLOCKWISE_90)){
			if(blockState.getValue(AXIS).equals(Direction.Axis.X)){
				return blockState.setValue(AXIS,Direction.Axis.Z);
			}else if(blockState.getValue(AXIS).equals(Direction.Axis.Z)){
				return blockState.setValue(AXIS,Direction.Axis.X);
			}
		}
		return blockState;
	}
	@Override
	public @NotNull FluidState getFluidState(BlockState blockState){
		return blockState.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(blockState);
	}
	@Override
	public @NotNull BlockState updateShape(BlockState blockState,@NotNull Direction facing,@NotNull BlockState facingState,@NotNull LevelAccessor world,@NotNull BlockPos currentPos,@NotNull BlockPos facingPos){
		if(blockState.getValue(WATERLOGGED)){
			world.scheduleTick(currentPos,Fluids.WATER,Fluids.WATER.getTickDelay(world));
		}
		return super.updateShape(blockState,facing,facingState,world,currentPos,facingPos);
	}
}
