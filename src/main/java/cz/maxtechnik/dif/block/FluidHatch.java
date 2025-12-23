package cz.maxtechnik.dif.block;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import cz.maxtechnik.dif.init.fluid.DifModFluids;

import java.util.concurrent.atomic.AtomicInteger;
@SuppressWarnings("deprecation")
public class FluidHatch extends Block implements SimpleWaterloggedBlock{
	public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty XP=BooleanProperty.create("xp");
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public FluidHatch(){
		super(Properties.of().sound(SoundType.NETHERITE_BLOCK).strength(3F,6F).requiresCorrectToolForDrops().noOcclusion().isRedstoneConductor((bs,br,bp)->false));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(WATERLOGGED,false).setValue(XP,false));
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
	public @NotNull VoxelShape getShape(BlockState state,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return switch(state.getValue(FACING)){
			case NORTH -> box(1,0,0,15,16,6);
			case EAST -> box(10,0,1,16,16,15);
			case WEST -> box(0,0,1,6,16,15);
			default -> box(1,0,10,15,16,16);
		};
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING,WATERLOGGED,XP);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		if(context.getClickedFace().getAxis().equals(Direction.Axis.Y))return null;
		boolean waterlogged=context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER);
		return this.defaultBlockState().setValue(FACING,context.getClickedFace().getOpposite()).setValue(WATERLOGGED,waterlogged);
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
	public void attack(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,@NotNull Player player){
		super.attack(blockState,world,pos,player);
		if(player instanceof ServerPlayer serverPlayer){
			if(DifMod.playerGameModeIsCreativeCategory(serverPlayer)||!player.getMainHandItem().isEmpty())return;
			if(!blockState.getValue(XP)) return;
			if(player.isShiftKeyDown()){
				// left-click + shift: extract +30 levels (or as much as tank has)
				handleXpExtraction(world,pos,blockState,player,30);
			}else{
				// left-click: extract +1 level (but if fractional only to next whole level first)
				handleXpExtraction(world,pos,blockState,player,1);
			}
		}
	}

		// XP / level conversion helpers (Minecraft formulas)
		private static int totalXpForLevel(int level){
			if(level<=0) return 0;
			if(level<=16) return level*level + 6*level;
			if(level<=31) return (int)(2.5*level*level - 40.5*level + 360);
			return (int)(4.5*level*level - 162.5*level + 2220);
		}

    private void handleXpInsertion(Level world, BlockPos targetPos, BlockState blockState, Player player, boolean insertAll){
			BlockEntity blockEntity=world.getBlockEntity(targetPos);
			if(blockEntity==null) return;
			blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER,blockState.getValue(FACING)).ifPresent(capability->{
				int playerTotal=getPlayerTotalXp(player);
				int toInsert;
				if(insertAll){
					toInsert=playerTotal;
				}else{
					int nextLevel=player.experienceLevel+1;
					int need=totalXpForLevel(nextLevel)-playerTotal; // xp needed to reach next whole level
					if(need<0) need=0;
					toInsert=need;
				}
				if(toInsert<=0) return;
				FluidStack sim=new FluidStack(DifModFluids.XP.get(),toInsert);
				int accepted=capability.fill(sim,IFluidHandler.FluidAction.SIMULATE);
				if(accepted>0){
					int filled=capability.fill(new FluidStack(DifModFluids.XP.get(),accepted),IFluidHandler.FluidAction.EXECUTE);
					if(filled>0){
						if(player instanceof ServerPlayer serverPlayer){
							if(!DifMod.playerGameModeIsCreativeCategory(serverPlayer)){
								player.giveExperiencePoints(-filled);
							}
						}
					}
				}
			});
		}

		private void handleXpExtraction(Level world, BlockPos hatchPos, BlockState blockState, Player player, int levelsRequested){
			BlockPos targetPos=hatchPos.relative(blockState.getValue(FACING));
			BlockEntity blockEntity=world.getBlockEntity(targetPos);
			if(blockEntity==null) return;
			blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER,blockState.getValue(FACING)).ifPresent(capability->{
				int currentTotal=getPlayerTotalXp(player);
				int desiredXp;
				int targetLevel;
				if(levelsRequested<=1){
					// give enough to reach next whole level
					targetLevel=player.experienceLevel+1;
				}else{
					// shift-left: add N levels (e.g., 30) relative to current level
					targetLevel=player.experienceLevel + levelsRequested;
				}
				desiredXp = totalXpForLevel(targetLevel) - currentTotal;
				if(desiredXp<=0){
					// edge-case rounding: if player level still below target, request at least 1 xp
					if(player.experienceLevel < targetLevel) desiredXp = 1; else return;
				}
				FluidStack want=new FluidStack(DifModFluids.XP.get(),desiredXp);
				FluidStack drainedSim=capability.drain(want,IFluidHandler.FluidAction.SIMULATE);
				int available= drainedSim.getAmount();
				if(available<=0) return;
				FluidStack drained=capability.drain(new FluidStack(DifModFluids.XP.get(),available),IFluidHandler.FluidAction.EXECUTE);
				int actual= drained.getAmount();
				if(actual<=0) return;
				// give to player the actual drained amount (1 mB = 1 xp point)
				if(player instanceof ServerPlayer serverPlayer){
					if(!DifMod.playerGameModeIsCreativeCategory(serverPlayer)){
						player.giveExperiencePoints(actual);
					}
				}
			});
		}

		private static int getPlayerTotalXp(Player player){
			return player.totalExperience;
		}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,@NotNull Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		super.use(blockState,world,pos,player,hand,hit);
		if(world.isClientSide())return InteractionResult.SUCCESS;
		if(player.getItemInHand(hand).is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge","tools/wrench")))){
			world.setBlock(pos,blockState.setValue(XP,!blockState.getValue(XP)),3);
			AllSoundEvents.WRENCH_ROTATE.playOnServer(world,pos,1.0F,Create.RANDOM.nextFloat() * 0.5F + 0.5F);
			return InteractionResult.SUCCESS;
		}
		pos=pos.relative(blockState.getValue(FACING));
		BlockEntity blockEntity=world.getBlockEntity(pos);
		if(blockEntity!=null){
			if(blockState.getValue(XP)&&player.getItemInHand(hand).isEmpty()){
				AtomicInteger retval0=new AtomicInteger(0);
				blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER,blockState.getValue(FACING)).ifPresent(capability->retval0.set(capability.getTanks()));
				if(retval0.get()>0){
                    // right-click + shift: insert all levels (or as much as tank accepts)
                    // right-click: insert fractional progress (round down to whole level)
                    handleXpInsertion(world,pos,blockState,player, player.isShiftKeyDown());
				}
			}else{
				AtomicInteger retval0=new AtomicInteger(0);
				blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER,blockState.getValue(FACING)).ifPresent(capability->retval0.set(capability.getTanks()));
				if(retval0.get()>0){
					if(player.getItemInHand(hand).getItem() instanceof BucketItem bucket){
						AtomicInteger retval1=new AtomicInteger(0);
						blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER,blockState.getValue(FACING)).ifPresent(capability->retval1.set(capability.fill(new FluidStack(bucket.getFluid(),1000),IFluidHandler.FluidAction.SIMULATE)));
						if(retval1.get()>=1000){
							if(player instanceof ServerPlayer serverPlayer){
								if(!DifMod.playerGameModeIsCreativeCategory(serverPlayer))
									player.getItemInHand(hand).shrink(1);
								ItemHandlerHelper.giveItemToPlayer(player,new ItemStack(Items.BUCKET));
							}
							blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER,blockState.getValue(FACING)).ifPresent(capability->capability.fill(new FluidStack(bucket.getFluid(),1000),IFluidHandler.FluidAction.EXECUTE));
						}
					}
				}
			}
		}
		return InteractionResult.SUCCESS;
	}
}