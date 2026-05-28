package cz.maxtechnik.dif.block.barrel;

import cz.maxtechnik.dif.block.Engine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class EngineExtender extends Block{
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty TOP=BooleanProperty.create("top");
	public EngineExtender(Properties properties){
		super(properties.noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(TOP,false));
	}
	@Override
	public int getLightBlock(@NotNull BlockState blockState,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING,TOP);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		Level level=context.getLevel();
		Direction newFacing=context.getClickedFace();
		if(newFacing.equals(Direction.UP)){
			if(level.getBlockState(context.getClickedPos().below()).getBlock() instanceof Engine) newFacing=level.getBlockState(context.getClickedPos().below()).getValue(FACING);
			else newFacing=context.getHorizontalDirection();
			return this.defaultBlockState().setValue(FACING,newFacing).setValue(TOP,true);
		}else{
			if(newFacing.equals(Direction.DOWN)) newFacing=context.getHorizontalDirection().getOpposite();
			return this.defaultBlockState().setValue(FACING,newFacing).setValue(TOP,false);
		}
	}
	public @NotNull BlockState rotate(BlockState blockState,Rotation rotation){
		return blockState.setValue(FACING,rotation.rotate(blockState.getValue(FACING)));
	}
	public @NotNull BlockState mirror(BlockState blockState,Mirror mirror){
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}
}
