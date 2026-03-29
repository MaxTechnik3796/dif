package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.FryingTableBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.registry.ModSounds;

import static cz.maxtechnik.dif.block.entity.FryingTableBlockEntity.INPUT_SLOT;
import static cz.maxtechnik.dif.block.entity.FryingTableBlockEntity.OUTPUT_SLOT;
@SuppressWarnings("deprecation")
public class FryingTable extends Block implements SimpleWaterloggedBlock, EntityBlock{
	public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty OIL=BooleanProperty.create("oil");
	public FryingTable(){
		super(Properties.of().strength(0.5F,6F).sound(SoundType.LANTERN).requiresCorrectToolForDrops().noOcclusion().isRedstoneConductor((bs,br,bp)->false).lightLevel(state->15));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(WATERLOGGED,false).setValue(OIL,false));
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
	public @NotNull VoxelShape getShape(@NotNull BlockState state,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return box(0,0,0,16,13,16);
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING,WATERLOGGED,OIL);
	}
	@Override
	public float getShadeBrightness(@NotNull BlockState blockState,@NotNull BlockGetter blockGetter,@NotNull BlockPos pos){
		return 1F;
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
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return new FryingTableBlockEntity(pos,blockState);
	}
	@Override
	public MenuProvider getMenuProvider(@NotNull BlockState blockState,Level worldIn,@NotNull BlockPos pos){
		BlockEntity tileEntity=worldIn.getBlockEntity(pos);
		return tileEntity instanceof MenuProvider menuProvider?menuProvider:null;
	}
	@Override
	public boolean triggerEvent(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,int eventID,int eventParam){
		super.triggerEvent(blockState,world,pos,eventID,eventParam);
		BlockEntity blockEntity=world.getBlockEntity(pos);
		return blockEntity!=null&&blockEntity.triggerEvent(eventID,eventParam);
	}
	@Override
	public void onRemove(BlockState state,@NotNull Level world,@NotNull BlockPos pos,BlockState newState,boolean isMoving){
		if(state.getBlock()!=newState.getBlock()){
			BlockEntity blockEntity=world.getBlockEntity(pos);
			if(blockEntity instanceof FryingTableBlockEntity be){
				Containers.dropContents(world,pos,be);
				world.updateNeighbourForOutputSignal(pos,this);
			}
			super.onRemove(state,world,pos,newState,isMoving);
		}
	}
	@Override
	public boolean hasAnalogOutputSignal(@NotNull BlockState blockState){
		return true;
	}
	@Override
	public int getAnalogOutputSignal(@NotNull BlockState blockState,Level world,@NotNull BlockPos pos){
		BlockEntity tileentity=world.getBlockEntity(pos);
		if(tileentity instanceof FryingTableBlockEntity be)
			return AbstractContainerMenu.getRedstoneSignalFromContainer(be);
		else return 0;
	}
	@Override
	public void tick(@NotNull BlockState blockstate,@NotNull ServerLevel world,@NotNull BlockPos pos,@NotNull RandomSource random){
		super.tick(blockstate,world,pos,random);
	}
	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState state,@NotNull BlockEntityType<T> type){
		return level.isClientSide?createClientTicker(type,DifModBlockEntities.FRYING_TABLE.get()):createServerTicker(type,DifModBlockEntities.FRYING_TABLE.get());
	}
	@Nullable
	protected static <T extends BlockEntity> BlockEntityTicker<T> createServerTicker(BlockEntityType<T> type,BlockEntityType<? extends FryingTableBlockEntity> expectedType){
		return type==expectedType?(lvl,pos,state,blockEntity)->FryingTableBlockEntity.serverTick(lvl,pos,state,(FryingTableBlockEntity)blockEntity):null;
	}
	protected static <T extends BlockEntity> BlockEntityTicker<T> createClientTicker(BlockEntityType<T> type,BlockEntityType<? extends FryingTableBlockEntity> expectedType){
		return type==expectedType?(lvl,pos,state,blockEntity)->FryingTableBlockEntity.clientTick(lvl,pos):null;
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState blockstate,@NotNull Level world,@NotNull BlockPos pos,@NotNull Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		if(world.isClientSide) return InteractionResult.SUCCESS;
		BlockEntity blockEntity=world.getBlockEntity(pos);
		if(blockEntity instanceof FryingTableBlockEntity be){
			ItemStack handItem=player.getItemInHand(hand);
			// --- LOGIKA PRO TEKUTINY (OLEJ) ---
			// 1. NALÉVÁNÍ OLEJE (Kýbl s olejem -> Pánev)
			if(handItem.is(DifModItems.SUNFLOWER_OIL_BUCKET.get())){
				// Zkusíme naplnit 1000 mB (1 kýbl)
				int accepted=be.fluidTank.fill(new FluidStack(DifModFluids.SUNFLOWER_OIL.get(),1000),IFluidHandler.FluidAction.EXECUTE);
				if(accepted>0){
					// Pokud hráč není v creativu, vyměníme kýbl s olejem za prázdný
					if(!player.getAbilities().instabuild){
						player.setItemInHand(hand,new ItemStack(net.minecraft.world.item.Items.BUCKET));
					}
					world.playSound(null,pos,SoundEvents.BUCKET_EMPTY,SoundSource.BLOCKS,1F,1F);
					be.setChanged();
					return InteractionResult.SUCCESS;
				}
			}
			// 2. NABÍRÁNÍ OLEJE (Prázdný kýbl -> Pánev)
			if(handItem.is(net.minecraft.world.item.Items.BUCKET)){
				// Zkusíme odebrat 1000 mB
				FluidStack drained=be.fluidTank.drain(1000,IFluidHandler.FluidAction.SIMULATE);
				if(drained.getAmount()==1000){
					// Máme dost oleje, provedeme akci reálně
					be.fluidTank.drain(1000,IFluidHandler.FluidAction.EXECUTE);
					ItemStack oilBucket=new ItemStack(cz.maxtechnik.dif.init.basic.DifModItems.SUNFLOWER_OIL_BUCKET.get());
					if(!player.getAbilities().instabuild){
						handItem.shrink(1);
						if(handItem.isEmpty()){
							player.setItemInHand(hand,oilBucket);
						}else if(!player.getInventory().add(oilBucket)){
							player.drop(oilBucket,false);
						}
					}
					world.playSound(null,pos,SoundEvents.BUCKET_FILL,SoundSource.BLOCKS,1F,1F);
					be.setChanged();
					return InteractionResult.SUCCESS;
				}
			}
			// --- STÁVAJÍCÍ LOGIKA PRO ITEMY ---
			ItemStack inputStack=be.getItem(INPUT_SLOT);
			ItemStack outputStack=be.getItem(OUTPUT_SLOT);
			if(!handItem.isEmpty()){
				if(inputStack.isEmpty()){
					be.setItem(INPUT_SLOT,handItem.copy());
					handItem.setCount(0);
					be.setChanged();
					world.playSound(null,pos,SoundEvents.LANTERN_PLACE,SoundSource.BLOCKS,1F,1F);
					if(blockstate.getValue(OIL))
						world.playSound(null,pos,ModSounds.BLOCK_SKILLET_ADD_FOOD.get(),SoundSource.BLOCKS,1F,1F);
					return InteractionResult.SUCCESS;
				}
			}else{
				if(!outputStack.isEmpty()){
					player.setItemInHand(hand,outputStack.copy());
					be.setItem(OUTPUT_SLOT,ItemStack.EMPTY);
					be.setChanged();
					world.playSound(null,pos,SoundEvents.ITEM_PICKUP,SoundSource.BLOCKS,1F,1F);
					return InteractionResult.SUCCESS;
				}else if(!inputStack.isEmpty()){
					player.setItemInHand(hand,inputStack.copy());
					be.setItem(INPUT_SLOT,ItemStack.EMPTY);
					be.setChanged();
					world.playSound(null,pos,SoundEvents.ITEM_PICKUP,SoundSource.BLOCKS,1F,1F);
					return InteractionResult.SUCCESS;
				}
			}
		}
		return InteractionResult.PASS;
	}
}