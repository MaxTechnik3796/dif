package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.FryingTableBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
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
	public static final BooleanProperty HEATED=BooleanProperty.create("heated");
	public static final BooleanProperty TRAY=BooleanProperty.create("tray");
	public static final TagKey<Block> TAG_HEAT_SOURCES=BlockTags.create(ResourceLocation.fromNamespaceAndPath("farmersdelight","heat_sources"));
	public static final TagKey<Block> TAG_TRAY=BlockTags.create(ResourceLocation.fromNamespaceAndPath("farmersdelight","tray_heat_sources"));
	public FryingTable(){
		super(Properties.of().strength(0.5F,6F).sound(SoundType.LANTERN).noOcclusion().isRedstoneConductor((bs,br,bp)->false));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(WATERLOGGED,false).setValue(OIL,false).setValue(HEATED,false).setValue(TRAY,false));
	}
	public static boolean isHeatSource(Level level,BlockPos pos){
		BlockState below=level.getBlockState(pos.below());
		return below.is(TAG_HEAT_SOURCES);
	}
	public static boolean isTray(Level level,BlockPos pos){
		BlockState below=level.getBlockState(pos.below());
		return below.is(TAG_TRAY);
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
	public @NotNull VoxelShape getShape(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return box(0,0,0,16,4,16);
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING,WATERLOGGED,OIL,HEATED,TRAY);
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
	@Override
	public @NotNull BlockState rotate(BlockState blockState,Rotation rotation){
		return blockState.setValue(FACING,rotation.rotate(blockState.getValue(FACING)));
	}
	@Override
	public @NotNull BlockState mirror(BlockState blockState,Mirror mirror){
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
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
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return new FryingTableBlockEntity(pos,blockState);
	}
	@Override
	public MenuProvider getMenuProvider(@NotNull BlockState blockState,Level worldIn,@NotNull BlockPos pos){
		BlockEntity te=worldIn.getBlockEntity(pos);
		return te instanceof MenuProvider mp?mp:null;
	}
	@Override
	public boolean triggerEvent(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,int eventID,int eventParam){
		super.triggerEvent(blockState,world,pos,eventID,eventParam);
		BlockEntity be=world.getBlockEntity(pos);
		return be!=null&&be.triggerEvent(eventID,eventParam);
	}
	@Override
	public void onRemove(BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,BlockState newState,boolean isMoving){
		if(blockState.getBlock()!=newState.getBlock()){
			BlockEntity be=world.getBlockEntity(pos);
			if(be instanceof FryingTableBlockEntity fbe){
				Containers.dropContents(world,pos,fbe);
				world.updateNeighbourForOutputSignal(pos,this);
			}
			super.onRemove(blockState,world,pos,newState,isMoving);
		}
	}
	@Override
	public boolean hasAnalogOutputSignal(@NotNull BlockState blockState){
		return true;
	}
	@Override
	public int getAnalogOutputSignal(@NotNull BlockState blockState,Level world,@NotNull BlockPos pos){
		BlockEntity blockEntity=world.getBlockEntity(pos);
		return blockEntity instanceof FryingTableBlockEntity be?AbstractContainerMenu.getRedstoneSignalFromContainer(be):0;
	}
	@Override
	public void tick(@NotNull BlockState blockstate,@NotNull ServerLevel world,@NotNull BlockPos pos,@NotNull RandomSource random){
		super.tick(blockstate,world,pos,random);
	}
	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState blockState,@NotNull BlockEntityType<T> type){
		return level.isClientSide?createClientTicker(type,DifModBlockEntities.FRYING_TABLE.get()):createServerTicker(type,DifModBlockEntities.FRYING_TABLE.get());
	}
	@Nullable
	protected static <T extends BlockEntity> BlockEntityTicker<T> createServerTicker(BlockEntityType<T> type,BlockEntityType<? extends FryingTableBlockEntity> expected){
		return type.equals(expected)?(level,pos,blockState,be)->FryingTableBlockEntity.serverTick(level,pos,blockState,(FryingTableBlockEntity)be):null;
	}
	protected static <T extends BlockEntity> BlockEntityTicker<T> createClientTicker(BlockEntityType<T> type,BlockEntityType<? extends FryingTableBlockEntity> expected){
		return type.equals(expected)?(level,pos,blockState,be)->FryingTableBlockEntity.clientTick(level,pos):null;
	}
	@Override
	public @NotNull InteractionResult useWithoutItem(@NotNull BlockState blockstate,@NotNull Level world,@NotNull BlockPos pos,@NotNull Player player,@NotNull BlockHitResult hit){
		if(world.isClientSide) return InteractionResult.SUCCESS;
		BlockEntity blockEntity=world.getBlockEntity(pos);
		if(!(blockEntity instanceof FryingTableBlockEntity be)) return InteractionResult.PASS;
		ItemStack handItem=player.getMainHandItem();
		if(handItem.getItem().equals(Items.ICE)){
			world.explode(null,pos.getX(),pos.getY(),pos.getZ(),10,Level.ExplosionInteraction.BLOCK);
			return InteractionResult.SUCCESS;
		}
		if(handItem.is(DifModItems.SUNFLOWER_OIL_BUCKET.get())){
			int accepted=be.fluidTank.fill(new FluidStack(DifModFluids.SUNFLOWER_OIL.get(),1000),IFluidHandler.FluidAction.EXECUTE);
			if(accepted>0){
				if(!player.getAbilities().instabuild)
					player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(net.minecraft.world.item.Items.BUCKET));
				world.playSound(null,pos,SoundEvents.BUCKET_EMPTY,SoundSource.BLOCKS,1F,1F);
				be.setChanged();
				return InteractionResult.SUCCESS;
			}
		}
		if(handItem.is(net.minecraft.world.item.Items.BUCKET)){
			FluidStack drained=be.fluidTank.drain(1000,IFluidHandler.FluidAction.SIMULATE);
			if(drained.getAmount()==1000){
				be.fluidTank.drain(1000,IFluidHandler.FluidAction.EXECUTE);
				ItemStack oilBucket=new ItemStack(DifModItems.SUNFLOWER_OIL_BUCKET.get());
				if(!player.getAbilities().instabuild){
					handItem.shrink(1);
					if(handItem.isEmpty())
						player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,oilBucket);
					else if(!player.getInventory().add(oilBucket)) player.drop(oilBucket,false);
				}
				world.playSound(null,pos,SoundEvents.BUCKET_FILL,SoundSource.BLOCKS,1F,1F);
				be.setChanged();
				return InteractionResult.SUCCESS;
			}
		}
		ItemStack inputStack=be.getItem(INPUT_SLOT);
		ItemStack outputStack=be.getItem(OUTPUT_SLOT);
		if(!handItem.isEmpty()){
			if(inputStack.isEmpty()){
				be.setItem(INPUT_SLOT,handItem.copy());
				handItem.setCount(0);
				be.setChanged();
				world.playSound(null,pos,SoundEvents.LANTERN_PLACE,SoundSource.BLOCKS,1F,1F);
				if(blockstate.getValue(OIL)&&blockstate.getValue(HEATED))
					world.playSound(null,pos,ModSounds.BLOCK_SKILLET_ADD_FOOD.get(),SoundSource.BLOCKS,1F,1F);
				return InteractionResult.SUCCESS;
			}
		}else{
			if(!outputStack.isEmpty()){
				player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,outputStack.copy());
				be.setItem(OUTPUT_SLOT,ItemStack.EMPTY);
				be.setChanged();
				world.playSound(null,pos,SoundEvents.ITEM_PICKUP,SoundSource.BLOCKS,1F,1F);
				return InteractionResult.SUCCESS;
			}else if(!inputStack.isEmpty()){
				player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,inputStack.copy());
				be.setItem(INPUT_SLOT,ItemStack.EMPTY);
				be.setChanged();
				world.playSound(null,pos,SoundEvents.ITEM_PICKUP,SoundSource.BLOCKS,1F,1F);
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}
}