package cz.maxtechnik.dif.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class FluidHatch extends Block implements SimpleWaterloggedBlock{
	public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public FluidHatch(){
		super(Properties.of()
				.sound(SoundType.NETHERITE_BLOCK)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.isRedstoneConductor((bs,br,bp)->false));
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING,Direction.NORTH)
				.setValue(WATERLOGGED,false));
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState state,@NotNull BlockGetter world,
											  @NotNull BlockPos pos,@NotNull CollisionContext context){
		return Shapes.empty(); // Render přes model JSON (doporučuji zkopírovat z Create Item Hatch)
	}
	@Override
	public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){
		Direction facing=state.getValue(FACING);
		// Base shape pro SOUTH (klapka vyčnívá do pozitivního Z) – přesně podle Create
		VoxelShape baseSouth=Shapes.or(
				Block.box(1,0,0,15,16,2),     // základní deska (2 pixely tlustá)
				Block.box(2,2,0,14,13,3.8),   // první schod dovnitř
				Block.box(2,4,0,14,11,5.8),   // druhý schod
				Block.box(2,6,0,14,9,7.8)     // třetí schod (nejužší)
		);
		// Rotace podle facing (klapka vyčnívá opačným směrem než facing bloku)
		return switch(facing){
			case NORTH -> rotateHorizontal(baseSouth,2); // 180°
			case EAST -> rotateHorizontal(baseSouth,1); // 90° CW
			case WEST -> rotateHorizontal(baseSouth,3); // 90° CCW
			default -> baseSouth;
		};
	}
	// Pomocná metoda pro horizontální rotaci shape (90° kroky)
	private static VoxelShape rotateHorizontal(VoxelShape shape,int times){
		VoxelShape[] buffer=new VoxelShape[]{shape,Shapes.empty()};
		for(int i=0;i<times;i++){
			buffer[0].forAllBoxes((minX,minY,minZ,maxX,maxY,maxZ)->
					buffer[1]=Shapes.or(buffer[1],Block.box(16-maxZ,minY,minX,16-minZ,maxY,maxX)));
			buffer[0]=buffer[1];
			buffer[1]=Shapes.empty();
		}
		return buffer[0];
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING,WATERLOGGED);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		boolean waterlogged=context.getLevel().getFluidState(context.getClickedPos()).getType()==Fluids.WATER;
		return this.defaultBlockState()
				.setValue(FACING,context.getHorizontalDirection())
				.setValue(WATERLOGGED,waterlogged);
	}
	@Override
	public @NotNull BlockState rotate(BlockState state,Rotation rot){
		return state.setValue(FACING,rot.rotate(state.getValue(FACING)));
	}
	@Override
	public @NotNull BlockState mirror(BlockState state,Mirror mirror){
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}
	@Override
	public @NotNull FluidState getFluidState(BlockState state){
		return state.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(state);
	}
	@Override
	public @NotNull BlockState updateShape(BlockState state,@NotNull Direction facing,
										   @NotNull BlockState facingState,@NotNull LevelAccessor world,
										   @NotNull BlockPos currentPos,@NotNull BlockPos facingPos){
		if(state.getValue(WATERLOGGED)){
			world.scheduleTick(currentPos,Fluids.WATER,Fluids.WATER.getTickDelay(world));
		}
		return super.updateShape(state,facing,facingState,world,currentPos,facingPos);
	}
}