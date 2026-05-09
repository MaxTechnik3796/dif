package cz.maxtechnik.dif.block.barrel;

import cz.maxtechnik.dif.block.entity.barrel.BrassBarrelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
public class BrassBarrel extends Block implements EntityBlock{
	public static final DirectionProperty FACING=BlockStateProperties.FACING;
	public static final BooleanProperty OPEN=BlockStateProperties.OPEN;
	public BrassBarrel(){
		super(Properties.of().sound(SoundType.WOOD).strength(2.5F).isRedstoneConductor((bs,br,bp)->false));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(OPEN,false));
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 15;
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING,OPEN);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return this.defaultBlockState().setValue(FACING,context.getNearestLookingDirection().getOpposite()).setValue(OPEN,false);
	}
	public @NotNull BlockState rotate(BlockState state,Rotation rot){
		return state.setValue(FACING,rot.rotate(state.getValue(FACING)));
	}
	public @NotNull BlockState mirror(BlockState state,Mirror mirrorIn){
		return state.setValue(FACING,mirrorIn.mirror(state.getValue(FACING)));
	}
	@Override
	protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull BlockHitResult hitResult){
		if(level.isClientSide){
			return InteractionResult.SUCCESS;
		}
		if(player instanceof ServerPlayer serverPlayer){
			BlockEntity be=level.getBlockEntity(pos);
			if(be instanceof MenuProvider menuProvider){
				serverPlayer.openMenu(menuProvider,pos);
			}
		}
		return InteractionResult.CONSUME;
	}
	@Override
	public MenuProvider getMenuProvider(@NotNull BlockState state,Level worldIn,@NotNull BlockPos pos){
		BlockEntity tileEntity=worldIn.getBlockEntity(pos);
		return tileEntity instanceof MenuProvider menuProvider?menuProvider:null;
	}
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new BrassBarrelBlockEntity(pos,state);
	}
	@Override
	public boolean triggerEvent(@NotNull BlockState state,@NotNull Level world,@NotNull BlockPos pos,int eventID,int eventParam){
		super.triggerEvent(state,world,pos,eventID,eventParam);
		BlockEntity blockEntity=world.getBlockEntity(pos);
		return blockEntity!=null&&blockEntity.triggerEvent(eventID,eventParam);
	}
	@Override
	public void onRemove(BlockState state,@NotNull Level world,@NotNull BlockPos pos,BlockState newState,boolean isMoving){
		if(state.getBlock()!=newState.getBlock()){
			BlockEntity blockEntity=world.getBlockEntity(pos);
			if(blockEntity instanceof BrassBarrelBlockEntity be){
				Containers.dropContents(world,pos,be);
				world.updateNeighbourForOutputSignal(pos,this);
			}
			super.onRemove(state,world,pos,newState,isMoving);
		}
	}
	@Override
	public boolean hasAnalogOutputSignal(@NotNull BlockState state){
		return true;
	}
	@Override
	public int getAnalogOutputSignal(@NotNull BlockState blockState,Level world,@NotNull BlockPos pos){
		BlockEntity tileentity=world.getBlockEntity(pos);
		if(tileentity instanceof BrassBarrelBlockEntity be)
			return AbstractContainerMenu.getRedstoneSignalFromContainer(be);
		else
			return 0;
	}
}