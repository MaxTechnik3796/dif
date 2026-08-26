package cz.maxtechnik.dif.block;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@EventBusSubscriber(modid=DifMod.MODID)
@SuppressWarnings("deprecation")
public class FluidHatch extends Block implements SimpleWaterloggedBlock{
	public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty XP=BooleanProperty.create("xp");
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	private static final Map<UUID,Long> COOLDOWNS=new HashMap<>();
	private static final long COOLDOWN_MS=50;

	public FluidHatch(){
		super(Properties.of().sound(SoundType.NETHERITE_BLOCK).strength(3F,6F).requiresCorrectToolForDrops().noOcclusion().isRedstoneConductor((bs,br,bp)->false));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(WATERLOGGED,false).setValue(XP,false));
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
		BlockState state=event.getLevel().getBlockState(event.getPos());
		if(state.getBlock() instanceof FluidHatch&&state.getValue(XP)){
			event.setUseBlock(TriState.TRUE);
			if(!(event.getItemStack().getItem() instanceof WrenchItem)) event.setUseItem(TriState.FALSE);
		}
	}

	private boolean isOnCooldown(Player player){
		long now=System.currentTimeMillis();
		if(COOLDOWNS.containsKey(player.getUUID())&&now-COOLDOWNS.get(player.getUUID())<COOLDOWN_MS) return true;
		COOLDOWNS.put(player.getUUID(),now);
		return false;
	}

	@Override
	public void attack(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,@NotNull Player player){
		super.attack(blockState,world,pos,player);
		if(world.isClientSide||!(player instanceof ServerPlayer serverPlayer)||DifMod.playerGameModeIsCreativeCategory(serverPlayer)) return;
		if(!blockState.getValue(XP)||isOnCooldown(player)) return;
		handleXpExtraction(world,pos,blockState,player,player.isShiftKeyDown()?30:1);
	}

	@Override
	protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack heldItem,@NotNull BlockState blockState,
	                                                   @NotNull Level world,@NotNull BlockPos pos,@NotNull Player player,
	                                                   @NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		if(world.isClientSide()) return ItemInteractionResult.SUCCESS;
		if(heldItem.getItem() instanceof WrenchItem){
			world.setBlock(pos,blockState.setValue(XP,!blockState.getValue(XP)),3);
			AllSoundEvents.WRENCH_ROTATE.playOnServer(world,pos,1.0F,Create.RANDOM.nextFloat()*0.5F+0.5F);
			return ItemInteractionResult.SUCCESS;
		}
		if(blockState.getValue(XP)){
			if(isOnCooldown(player)) return ItemInteractionResult.SUCCESS;
			handleXpInsertion(world,pos,blockState,player,player.isShiftKeyDown());
			return ItemInteractionResult.SUCCESS;
		}
		var fluidHandlerItem=FluidUtil.getFluidHandler(heldItem);
		if(fluidHandlerItem.isPresent()){
			FluidStack containedFluid=fluidHandlerItem.get().getFluidInTank(0);
			if(containedFluid.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			IFluidHandler cap=getTargetFluidHandler(world,pos,blockState);
			if(cap==null) return ItemInteractionResult.FAIL;
			FluidStack fluidToFill=new FluidStack(containedFluid.getFluid(),1000);
			if(cap.fill(fluidToFill,IFluidHandler.FluidAction.SIMULATE)>=1000){
				cap.fill(fluidToFill,IFluidHandler.FluidAction.EXECUTE);
				if(!player.getAbilities().instabuild){
					heldItem.shrink(1);
					ItemStack emptyBucket=new ItemStack(Items.BUCKET);
					if(heldItem.isEmpty()) player.setItemInHand(hand,emptyBucket);
					else player.getInventory().placeItemBackInInventory(emptyBucket);
				}
			}
			return ItemInteractionResult.SUCCESS;
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	private IFluidHandler getTargetFluidHandler(Level world,BlockPos pos,BlockState blockState){
		BlockPos targetPos=pos.relative(blockState.getValue(FACING));
		Direction targetFace=blockState.getValue(FACING).getOpposite();
		IFluidHandler cap=world.getCapability(Capabilities.FluidHandler.BLOCK,targetPos,targetFace);
		if(cap==null) cap=world.getCapability(Capabilities.FluidHandler.BLOCK,targetPos,null);
		if(cap==null) cap=world.getCapability(Capabilities.FluidHandler.BLOCK,targetPos,blockState.getValue(FACING));
		return cap;
	}

	private void handleXpInsertion(Level world,BlockPos hatchPos,BlockState blockState,Player player,boolean insertAll){
		IFluidHandler cap=getTargetFluidHandler(world,hatchPos,blockState);
		if(cap==null) return;
		int currentTotal=getActualPlayerXP(player);
		int toInsert=insertAll?currentTotal:(currentTotal-totalXpForLevel(player.experienceLevel)>0?currentTotal-totalXpForLevel(player.experienceLevel):(player.experienceLevel>0?totalXpForLevel(player.experienceLevel)-totalXpForLevel(player.experienceLevel-1):0));
		if(toInsert<=0) return;
		int filled=cap.fill(new FluidStack(DifModFluids.XP.get(),toInsert),IFluidHandler.FluidAction.EXECUTE);
		if(filled>0) player.giveExperiencePoints(-filled);
	}

	private void handleXpExtraction(Level world,BlockPos hatchPos,BlockState blockState,Player player,int levelsRequested){
		IFluidHandler cap=getTargetFluidHandler(world,hatchPos,blockState);
		if(cap==null) return;
		int neededXP=totalXpForLevel(player.experienceLevel+levelsRequested)-getActualPlayerXP(player);
		if(neededXP<=0) return;
		FluidStack drained=cap.drain(new FluidStack(DifModFluids.XP.get(),neededXP),IFluidHandler.FluidAction.EXECUTE);
		if(drained.getAmount()>0) player.giveExperiencePoints(drained.getAmount());
	}

	private static int totalXpForLevel(int level){
		if(level<=0) return 0;
		if(level<=16) return level*level+6*level;
		if(level<=31) return (int)(2.5*level*level-40.5*level+360);
		return (int)(4.5*level*level-162.5*level+2220);
	}

	private static int getActualPlayerXP(Player player){
		int l=player.experienceLevel;
		int cap=l>=30?112+(l-30)*9:(l>=15?37+(l-15)*5:7+l*2);
		return totalXpForLevel(l)+(int)(player.experienceProgress*cap);
	}

	@Override
	public int getLightBlock(@NotNull BlockState blockState,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){ return 0; }
	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){ return Shapes.empty(); }
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
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){ builder.add(FACING,WATERLOGGED,XP); }
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		if(context.getClickedFace().getAxis().equals(Direction.Axis.Y)) return null;
		return this.defaultBlockState().setValue(FACING,context.getClickedFace().getOpposite()).setValue(WATERLOGGED,context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
	}
	@Override
	public @NotNull BlockState rotate(BlockState blockState,Rotation rotation){ return blockState.setValue(FACING,rotation.rotate(blockState.getValue(FACING))); }
	@Override
	public @NotNull BlockState mirror(BlockState blockState,Mirror mirror){ return blockState.rotate(mirror.getRotation(blockState.getValue(FACING))); }
	@Override
	public @NotNull FluidState getFluidState(BlockState blockState){ return blockState.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(blockState); }
	@Override
	public @NotNull BlockState updateShape(BlockState blockState,@NotNull Direction facing,@NotNull BlockState facingState,@NotNull LevelAccessor world,@NotNull BlockPos currentPos,@NotNull BlockPos facingPos){
		if(blockState.getValue(WATERLOGGED)) world.scheduleTick(currentPos,Fluids.WATER,Fluids.WATER.getTickDelay(world));
		return super.updateShape(blockState,facing,facingState,world,currentPos,facingPos);
	}
}