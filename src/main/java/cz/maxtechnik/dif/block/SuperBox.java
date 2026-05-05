package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.SuperBoxBlockEntity;
import cz.maxtechnik.dif.gui.menu.SuperBoxMenu;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class SuperBox extends Block implements SimpleWaterloggedBlock, EntityBlock{
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
	public SuperBox(){
		super(Properties.of().sound(SoundType.ANVIL).strength(2.5F,20F).requiresCorrectToolForDrops().noOcclusion().isRedstoneConductor((bs,br,bp)->false).pushReaction(PushReaction.BLOCK));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(WATERLOGGED,false));
	}
	@Override
	public boolean skipRendering(@NotNull BlockState state,BlockState adjacentBlockState,@NotNull Direction side){
		return adjacentBlockState.getBlock()==this||super.skipRendering(state,adjacentBlockState,side);
	}
	@Override
	public boolean propagatesSkylightDown(BlockState state,@NotNull BlockGetter reader,@NotNull BlockPos pos){
		return state.getFluidState().isEmpty();
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
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING,WATERLOGGED);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		boolean flag=context.getLevel().getFluidState(context.getClickedPos()).getType()==Fluids.WATER;
		return this.defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED,flag);
	}
	public @NotNull BlockState rotate(BlockState state,Rotation rot){
		return state.setValue(FACING,rot.rotate(state.getValue(FACING)));
	}
	public @NotNull BlockState mirror(BlockState state,Mirror mirrorIn){
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
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
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState blockstate,@NotNull Level world,@NotNull BlockPos pos,@NotNull Player entity,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		super.use(blockstate,world,pos,entity,hand,hit);
		if(entity instanceof ServerPlayer player){
			NetworkHooks.openScreen(player,new MenuProvider(){
				@Override
				public @NotNull Component getDisplayName(){
					return Component.literal("§e§lSuper Box");
				}
				@Override
				public AbstractContainerMenu createMenu(int id,@NotNull Inventory inventory,@NotNull Player player){
					return new SuperBoxMenu(id,inventory,new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(pos));
				}
			},pos);
		}
		return InteractionResult.SUCCESS;
	}
	@Override
	public MenuProvider getMenuProvider(@NotNull BlockState state,Level worldIn,@NotNull BlockPos pos){
		BlockEntity tileEntity=worldIn.getBlockEntity(pos);
		return tileEntity instanceof MenuProvider menuProvider?menuProvider:null;
	}
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new SuperBoxBlockEntity(pos,state);
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
			if(blockEntity instanceof SuperBoxBlockEntity be){
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
		if(tileentity instanceof SuperBoxBlockEntity be)
			return AbstractContainerMenu.getRedstoneSignalFromContainer(be);
		else
			return 0;
	}
}
