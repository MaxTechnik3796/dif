package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.SpaceCrateBlockEntity;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
public class SpaceCrateBlock extends Block implements EntityBlock{
	public static final DirectionProperty FACING=BlockStateProperties.FACING;
	public static final BooleanProperty OPEN=BlockStateProperties.OPEN;
	public SpaceCrateBlock(BlockBehaviour.Properties properties){
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(OPEN,false));
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING,OPEN);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx){
		return this.defaultBlockState().setValue(FACING,ctx.getNearestLookingDirection().getOpposite()).setValue(OPEN,false);
	}
	@Override
	public @NotNull BlockState rotate(BlockState blockState,Rotation rotation){
		return blockState.setValue(FACING,rotation.rotate(blockState.getValue(FACING)));
	}
	@Override
	public @NotNull BlockState mirror(BlockState blockState,Mirror mirror){
		return blockState.setValue(FACING,mirror.mirror(blockState.getValue(FACING)));
	}
	@Override
	public int getLightBlock(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos){
		return 15;
	}
	@Override
	protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState blockState,@NotNull Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull BlockHitResult hit){
		if(level.isClientSide) return InteractionResult.SUCCESS;
		if(player instanceof ServerPlayer serverPlayer){
			BlockEntity be=level.getBlockEntity(pos);
			if(be instanceof MenuProvider menuProvider) serverPlayer.openMenu(menuProvider,pos);
		}
		return InteractionResult.CONSUME;
	}
	@Override
	public MenuProvider getMenuProvider(@NotNull BlockState blockState,Level world,@NotNull BlockPos pos){
		BlockEntity be=world.getBlockEntity(pos);
		return be instanceof MenuProvider mp?mp:null;
	}
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return new SpaceCrateBlockEntity(pos,blockState);
	}
	@Override
	public boolean triggerEvent(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,int id,int param){
		super.triggerEvent(blockState,world,pos,id,param);
		BlockEntity be=world.getBlockEntity(pos);
		return be!=null&&be.triggerEvent(id,param);
	}
	@Override
	public void onRemove(BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,BlockState newState,boolean moving){
		if(blockState.getBlock()!=newState.getBlock()){
			BlockEntity be=world.getBlockEntity(pos);
			if(be instanceof SpaceCrateBlockEntity crate){
				Containers.dropContents(world,pos,crate);
				world.updateNeighbourForOutputSignal(pos,this);
			}
			super.onRemove(blockState,world,pos,newState,moving);
		}
	}
	@Override
	public boolean hasAnalogOutputSignal(@NotNull BlockState blockState){
		return true;
	}
	@Override
	public int getAnalogOutputSignal(@NotNull BlockState state,Level world,@NotNull BlockPos pos){
		BlockEntity blockEntity=world.getBlockEntity(pos);
		if(blockEntity instanceof SpaceCrateBlockEntity crate)
			return AbstractContainerMenu.getRedstoneSignalFromContainer(crate);
		return 0;
	}
}