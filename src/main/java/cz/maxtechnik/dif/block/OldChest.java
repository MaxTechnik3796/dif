package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.OldChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class OldChest extends Block implements EntityBlock{
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty CONNECTED=BooleanProperty.create("connected");
	public static final EnumProperty<OldChestType>TYPE=EnumProperty.create("type",OldChestType.class);
	public OldChest(){
		super(Properties.of().strength(3F,3F).sound(SoundType.WOOD));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(CONNECTED,false).setValue(TYPE,OldChestType.SINGLE));
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 15;
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING,TYPE,CONNECTED);
	}
	@Override
	public BlockState getStateForPlacement(@NotNull BlockPlaceContext context){
		return this.defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());
	}
	public @NotNull BlockState rotate(BlockState state,Rotation rot){
		return state.setValue(FACING,rot.rotate(state.getValue(FACING)));
	}
	public @NotNull BlockState mirror(BlockState state,Mirror mirrorIn){
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new OldChestBlockEntity(pos,state);
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
			if(blockEntity instanceof OldChestBlockEntity validBlockEntity){
				Containers.dropContents(world,pos,validBlockEntity);
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
		if(tileentity instanceof OldChestBlockEntity validBlockEntity)
			return AbstractContainerMenu.getRedstoneSignalFromContainer(validBlockEntity);
		else
			return 0;
	}
	public enum OldChestType implements net.minecraft.util.StringRepresentable{
		SINGLE("single"),LEFT("left"),RIGHT("right");
		private final String name;
		OldChestType(String name){this.name=name;}
		public @NotNull String getSerializedName(){return this.name;}
	}
	@Override
	public MenuProvider getMenuProvider(@NotNull BlockState state,Level worldIn,@NotNull BlockPos pos){
		BlockEntity blockEntity=worldIn.getBlockEntity(pos);
		return blockEntity instanceof MenuProvider menuProvider?menuProvider:null;
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState blockstate,@NotNull Level world,@NotNull BlockPos pos,@NotNull Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		if(world.isClientSide())return InteractionResult.SUCCESS;
		if(player instanceof ServerPlayer serverPlayer){
			BlockEntity blockEntity=world.getBlockEntity(pos);
			if(blockEntity instanceof MenuProvider menuProvider){
				NetworkHooks.openScreen(serverPlayer,menuProvider,pos);
			}
		}
		return InteractionResult.CONSUME;
	}
	@Override
	public void onPlace(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,@NotNull BlockState oldState,boolean moving){
		super.onPlace(blockState,world,pos,oldState,moving);
		if(!oldState.getBlock().equals(blockState.getBlock()))
			blockStateCheck(world,pos,blockState);
	}
	@Override
	public void neighborChanged(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,@NotNull Block neighborBlock,@NotNull BlockPos fromPos,boolean moving){
		super.neighborChanged(blockState,world,pos,neighborBlock,fromPos,moving);
		blockStateCheck(world,pos,blockState);
	}
	public void blockStateCheck(Level world,BlockPos pos,BlockState blockState){
		BlockPos left=pos.relative(blockState.getValue(FACING).getClockWise());
		BlockPos right=pos.relative(blockState.getValue(FACING).getCounterClockWise());
		BlockPos front=pos.relative(blockState.getValue(FACING));
		BlockPos back=pos.relative(blockState.getValue(FACING).getOpposite());
		if(world.getBlockState(left).getBlock().equals(blockState.getBlock())){
			if(!world.getBlockState(left).getValue(TYPE).equals(OldChestType.LEFT)&&!world.getBlockState(left).getValue(CONNECTED))
				world.setBlock(left,world.getBlockState(left).setValue(TYPE,OldChestType.LEFT).setValue(CONNECTED,true),3);
			if(!blockState.getValue(TYPE).equals(OldChestType.RIGHT)&&!blockState.getValue(CONNECTED)){
				world.setBlock(pos,blockState.setValue(TYPE,OldChestType.RIGHT).setValue(CONNECTED,true),3);
				blockState=world.getBlockState(pos);
			}
		}
		if(world.getBlockState(right).getBlock().equals(blockState.getBlock())){
			if(!world.getBlockState(right).getValue(TYPE).equals(OldChestType.RIGHT)&&!world.getBlockState(right).getValue(CONNECTED))
				world.setBlock(right,world.getBlockState(right).setValue(TYPE,OldChestType.RIGHT).setValue(CONNECTED,true),3);
			if(!blockState.getValue(TYPE).equals(OldChestType.LEFT)&&!blockState.getValue(CONNECTED)){
				world.setBlock(pos,blockState.setValue(TYPE,OldChestType.LEFT).setValue(CONNECTED,true),3);
				blockState=world.getBlockState(pos);
			}
		}
		if(!world.getBlockState(left).getBlock().equals(blockState.getBlock())&&blockState.getValue(TYPE).equals(OldChestType.RIGHT)){
			world.setBlock(pos,world.getBlockState(pos).setValue(TYPE,OldChestType.SINGLE),3);
			blockState=world.getBlockState(pos);
		}
		if(!world.getBlockState(right).getBlock().equals(blockState.getBlock())&&blockState.getValue(TYPE).equals(OldChestType.LEFT)){
			world.setBlock(pos,world.getBlockState(pos).setValue(TYPE,OldChestType.SINGLE),3);
			blockState=world.getBlockState(pos);
		}
		if(blockState.getValue(TYPE).equals(OldChestType.SINGLE)&&blockState.getValue(CONNECTED)
				&&!world.getBlockState(front).getBlock().equals(blockState.getBlock())
				&&!world.getBlockState(left).getBlock().equals(blockState.getBlock())
				&&!world.getBlockState(back).getBlock().equals(blockState.getBlock())
				&&!world.getBlockState(right).getBlock().equals(blockState.getBlock())){
			world.setBlock(pos,blockState.setValue(CONNECTED,false),3);
		}
	}
}