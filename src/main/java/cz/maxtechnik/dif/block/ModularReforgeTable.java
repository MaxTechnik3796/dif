package cz.maxtechnik.dif.block;

import com.mojang.serialization.MapCodec;
import cz.maxtechnik.dif.block.entity.ModularReforgeTableBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
public class ModularReforgeTable extends BaseEntityBlock implements SimpleWaterloggedBlock{
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
	public static final MapCodec<ModularReforgeTable> CODEC=simpleCodec(ModularReforgeTable::new);
	@Override
	protected @NotNull MapCodec<? extends BaseEntityBlock> codec(){
		return CODEC;
	}
	public ModularReforgeTable(BlockBehaviour.Properties properties){
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED,false).setValue(FACING,Direction.NORTH));
	}
	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState state){
		return RenderShape.MODEL;
	}
	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new ModularReforgeTableBlockEntity(pos,state);
	}
	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState state,@NotNull BlockEntityType<T> type){
		return level.isClientSide?null:createTickerHelper(type,DifModBlockEntities.MODULAR_REFORGE_TABLE.get(),(world,pos,blockState,be)->be.serverTick());
	}
	@Override
	protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack heldItem,@NotNull BlockState blockState,@NotNull Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		if(level.isClientSide) return ItemInteractionResult.sidedSuccess(true);
		BlockEntity be=level.getBlockEntity(pos);
		if(!(be instanceof ModularReforgeTableBlockEntity table)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if(!heldItem.isEmpty()&&table.tryInsertItem(heldItem,player,hand)) return ItemInteractionResult.sidedSuccess(false);
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}
	@Override
	protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState blockState,@NotNull Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull BlockHitResult hit){
		if(level.isClientSide) return InteractionResult.SUCCESS;
		BlockEntity be=level.getBlockEntity(pos);
		if(!(be instanceof ModularReforgeTableBlockEntity table)) return InteractionResult.PASS;
		return table.tryExtractItem(player)?InteractionResult.SUCCESS:InteractionResult.PASS;
	}
	@Override
	protected void onRemove(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState newState,boolean movedByPiston){
		if(!state.is(newState.getBlock())){
			if(level.getBlockEntity(pos) instanceof ModularReforgeTableBlockEntity table)
				table.dropAllItems(level,pos);
		}
		super.onRemove(state,level,pos,newState,movedByPiston);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		boolean flag=context.getLevel().getFluidState(context.getClickedPos()).getType().equals(Fluids.WATER);
		return this.defaultBlockState().setValue(WATERLOGGED,flag).setValue(FACING,context.getHorizontalDirection().getClockWise());
	}
	@Override
	public @NotNull FluidState getFluidState(BlockState blockState){
		return blockState.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(blockState);
	}
	@Override
	public @NotNull BlockState updateShape(BlockState blockState,@NotNull Direction facing,@NotNull BlockState facingState,@NotNull LevelAccessor world,@NotNull BlockPos currentPos,@NotNull BlockPos facingPos){
		if(blockState.getValue(WATERLOGGED))
			world.scheduleTick(currentPos,Fluids.WATER,Fluids.WATER.getTickDelay(world));
		return super.updateShape(blockState,facing,facingState,world,currentPos,facingPos);
	}
	@Override
	public float getShadeBrightness(@NotNull BlockState blockState,@NotNull BlockGetter blockGetter,@NotNull BlockPos pos){
		return 1F;
	}
	@Override
	public int getLightBlock(@NotNull BlockState blockState,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return Shapes.empty();
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(WATERLOGGED,FACING);
	}
	@Override
	public @NotNull BlockState rotate(BlockState blockState,Rotation rotation){
		return blockState.setValue(FACING,rotation.rotate(blockState.getValue(FACING)));
	}
	@Override
	public @NotNull BlockState mirror(BlockState state,Mirror mirror){
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}
}