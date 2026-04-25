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
@SuppressWarnings("deprecation")
public class AluminumProfile extends Block implements SimpleWaterloggedBlock{
	public static final EnumProperty<Direction.Axis> AXIS=BlockStateProperties.AXIS;
	public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
	public AluminumProfile(){
		super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(5F,6F).requiresCorrectToolForDrops().noOcclusion().isRedstoneConductor((bs,br,bp)->false));
		this.registerDefaultState(this.stateDefinition.any().setValue(AXIS,Direction.Axis.Y).setValue(WATERLOGGED,false));
	}
	@Override
	public boolean skipRendering(@NotNull BlockState state,BlockState adjacentBlockState,@NotNull Direction side){
		return adjacentBlockState.getBlock()==this||super.skipRendering(state,adjacentBlockState,side);
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
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
	public @NotNull BlockState rotate(@NotNull BlockState state,@NotNull Rotation rot){
		if(rot==Rotation.CLOCKWISE_90||rot==Rotation.COUNTERCLOCKWISE_90){
			if(state.getValue(AXIS)==Direction.Axis.X){
				return state.setValue(AXIS,Direction.Axis.Z);
			}else if(state.getValue(AXIS)==Direction.Axis.Z){
				return state.setValue(AXIS,Direction.Axis.X);
			}
		}
		return state;
	}
	@Override
	public @NotNull FluidState getFluidState(BlockState state){
		return state.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(state);
	}
	@Override
	public @NotNull BlockState updateShape(BlockState state,@NotNull Direction facing,@NotNull BlockState facingState,@NotNull LevelAccessor world,@NotNull BlockPos currentPos,@NotNull BlockPos facingPos){
		if(state.getValue(WATERLOGGED)){
			world.scheduleTick(currentPos,Fluids.WATER,Fluids.WATER.getTickDelay(world));
		}
		return super.updateShape(state,facing,facingState,world,currentPos,facingPos);
	}
}
