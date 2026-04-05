package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.MonitorBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.util.MonitorState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
@SuppressWarnings("deprecation")
public class MonitorBlock extends BaseEntityBlock{
	public static final EnumProperty<MonitorState> STATE=EnumProperty.create("state",MonitorState.class);
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public MonitorBlock(Properties properties){
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(STATE,MonitorState.NO_SIGNAL).setValue(FACING,Direction.NORTH));
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state,Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		if(level.getBlockEntity(pos) instanceof MonitorBlockEntity monitor){
			if(player.getMainHandItem().getItem().equals(DifModItems.CAMERA_LINK.get())) return InteractionResult.PASS;
			return monitor.useMonitor(player);
		}
		return InteractionResult.PASS;
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(STATE,FACING);
	}
	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return this.defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());
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
		return new MonitorBlockEntity(pos,state);
	}
	@Override
	public void onRemove(BlockState state,@NotNull Level level,@NotNull BlockPos pos,BlockState newState,boolean isMoving){
		if(!state.is(newState.getBlock())){
			super.onRemove(state,level,pos,newState,isMoving);
		}
	}
}