package cz.maxtechnik.dif.block;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class Drain extends Block implements SimpleWaterloggedBlock{
	public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;

	public Drain(){
		super(Properties.of()
				.sound(SoundType.NETHERITE_BLOCK)
				.strength(3F,6F)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.isRedstoneConductor((bs,br,bp)->false));
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING,Direction.NORTH)
				.setValue(WATERLOGGED,false));
	}

	@Override
	protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack heldItem,@NotNull BlockState blockState,
	                                                   @NotNull Level world,@NotNull BlockPos pos,@NotNull Player player,
	                                                   @NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		if(world.isClientSide()) return ItemInteractionResult.SUCCESS;
		BlockPos targetPos=pos.relative(blockState.getValue(FACING));

		// Wrench logika — rotace
		if(heldItem.getItem() instanceof WrenchItem){
			world.setBlock(pos,blockState.setValue(FACING,blockState.getValue(FACING).getClockWise()),3);
			AllSoundEvents.WRENCH_ROTATE.playOnServer(world,pos,1.0F,Create.RANDOM.nextFloat()*0.5F+0.5F);
			return ItemInteractionResult.SUCCESS;
		}

		// Zjistíme capability tanku za drainem
		IFluidHandler cap=world.getCapability(Capabilities.FluidHandler.BLOCK,targetPos,blockState.getValue(FACING).getOpposite());
		if(cap==null) cap=world.getCapability(Capabilities.FluidHandler.BLOCK,targetPos,blockState.getValue(FACING));
		if(cap==null) cap=world.getCapability(Capabilities.FluidHandler.BLOCK,targetPos,null);
		if(cap==null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		// 1. Kliknutí kbelíkem
		if(heldItem.is(Items.BUCKET)){
			FluidStack drainedSim=cap.drain(1000,IFluidHandler.FluidAction.SIMULATE);
			if(drainedSim.isEmpty()||drainedSim.getAmount()<1000) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			Item bucketItem=drainedSim.getFluid().getBucket();
			if(bucketItem == Items.AIR) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

			FluidStack drained=cap.drain(1000,IFluidHandler.FluidAction.EXECUTE);
			if(drained.getAmount()>=1000){
				SoundEvent sound=drained.getFluid().getFluidType().getSound(drained,SoundActions.BUCKET_FILL);
				if(sound==null) sound=SoundEvents.BUCKET_FILL;
				world.playSound(null,pos,sound,SoundSource.BLOCKS,1.0F,1.0F);

				if(!player.getAbilities().instabuild){
					heldItem.shrink(1);
					ItemStack filledBucket=new ItemStack(bucketItem);
					if(heldItem.isEmpty()) player.setItemInHand(hand,filledBucket);
					else player.getInventory().placeItemBackInInventory(filledBucket);
				}
				return ItemInteractionResult.SUCCESS;
			}
		}else{
			// 2. Kliknutí jinou fluidní nádobou
			var fluidHandlerItem=FluidUtil.getFluidHandler(heldItem);
			if(fluidHandlerItem.isPresent()){
				var result=FluidUtil.tryFillContainerAndStow(heldItem,cap,new PlayerInvWrapper(player.getInventory()),1000,player,true);
				if(result.isSuccess()){
					return ItemInteractionResult.SUCCESS;
				}
			}
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
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
	public @NotNull VoxelShape getShape(BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return switch(blockState.getValue(FACING)){
			case NORTH -> box(1,0,0,15,16,6);
			case EAST -> box(10,0,1,16,16,15);
			case WEST -> box(0,0,1,6,16,15);
			default -> box(1,0,10,15,16,16);
		};
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING,WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		if(context.getClickedFace().getAxis().equals(Direction.Axis.Y)) return null;
		return this.defaultBlockState()
				.setValue(FACING,context.getClickedFace().getOpposite())
				.setValue(WATERLOGGED,context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
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
	public @NotNull BlockState updateShape(BlockState blockState,@NotNull Direction facing,@NotNull BlockState facingState,
	                                       @NotNull LevelAccessor world,@NotNull BlockPos currentPos,@NotNull BlockPos facingPos){
		if(blockState.getValue(WATERLOGGED))
			world.scheduleTick(currentPos,Fluids.WATER,Fluids.WATER.getTickDelay(world));
		return super.updateShape(blockState,facing,facingState,world,currentPos,facingPos);
	}
}
