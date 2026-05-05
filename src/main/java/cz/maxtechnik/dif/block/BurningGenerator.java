package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.BurningGeneratorBlockEntity;
import cz.maxtechnik.dif.gui.menu.BurningGeneratorMenu;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
@SuppressWarnings("deprecation")
public class BurningGenerator extends Block implements SimpleWaterloggedBlock, EntityBlock{
	public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty LIT=BooleanProperty.create("lit");
	public BurningGenerator(){
		super(Properties.of().strength(5F,6F).sound(SoundType.STONE).requiresCorrectToolForDrops().noOcclusion().lightLevel(state->state.getValue(LIT)?5:0));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(WATERLOGGED,false).setValue(LIT,false));
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
		builder.add(FACING,WATERLOGGED,LIT);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		boolean flag=context.getLevel().getFluidState(context.getClickedPos()).getType()==Fluids.WATER;
		return this.defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED,flag);
	}
	@Override
	public @NotNull BlockState rotate(BlockState state,Rotation rot){
		return state.setValue(FACING,rot.rotate(state.getValue(FACING)));
	}
	@Override
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
	public void tick(@NotNull BlockState blockstate,@NotNull ServerLevel world,@NotNull BlockPos pos,@NotNull RandomSource random){
		super.tick(blockstate,world,pos,random);
	}
	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState state,@NotNull BlockEntityType<T> type){
		return level.isClientSide?createClientTicker(type,DifModBlockEntities.BURNING_GENERATOR.get()):createServerTicker(type,DifModBlockEntities.BURNING_GENERATOR.get());
	}
	@Nullable
	protected static <T extends BlockEntity> BlockEntityTicker<T> createServerTicker(BlockEntityType<T> type,BlockEntityType<? extends BurningGeneratorBlockEntity> expectedType){
		return type==expectedType?(lvl,pos,state,blockEntity)->BurningGeneratorBlockEntity.serverTick(lvl,pos,state,(BurningGeneratorBlockEntity)blockEntity):null;
	}
	protected static <T extends BlockEntity> BlockEntityTicker<T> createClientTicker(BlockEntityType<T> type,BlockEntityType<? extends BurningGeneratorBlockEntity> expectedType){
		return type==expectedType?(lvl,pos,state,blockEntity)->BurningGeneratorBlockEntity.clientTick(lvl,pos,state):null;
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState blockstate,@NotNull Level world,@NotNull BlockPos pos,@NotNull Player entity,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		super.use(blockstate,world,pos,entity,hand,hit);
		if(entity instanceof ServerPlayer player){
			NetworkHooks.openScreen(player,new MenuProvider(){
				@Override
				public @NotNull Component getDisplayName(){
					return Component.literal("Generator");
				}
				@Override
				public AbstractContainerMenu createMenu(int id,@NotNull Inventory inventory,@NotNull Player player){
					return new BurningGeneratorMenu(id,inventory,new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(pos));
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
		return new BurningGeneratorBlockEntity(pos,state);
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
			if(blockEntity instanceof BurningGeneratorBlockEntity be){
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
		if(tileentity instanceof BurningGeneratorBlockEntity be)
			return AbstractContainerMenu.getRedstoneSignalFromContainer(be);
		else
			return 0;
	}
}