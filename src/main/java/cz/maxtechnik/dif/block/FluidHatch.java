package cz.maxtechnik.dif.block;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class FluidHatch extends Block implements SimpleWaterloggedBlock {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty XP = BooleanProperty.create("xp");
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	// Cooldown mapa pro prevenci spamu
	private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
	private static final long COOLDOWN_MS = 50; // 1 ticků = 50ms
	public FluidHatch() {
		super(Properties.of().sound(SoundType.NETHERITE_BLOCK).strength(3F, 6F).requiresCorrectToolForDrops().noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false).setValue(XP, false));
	}
	//Cooldown Helper
	private boolean isOnCooldown(Player player) {
		long now = System.currentTimeMillis();
		if (COOLDOWNS.containsKey(player.getUUID())) {
			if (now - COOLDOWNS.get(player.getUUID()) < COOLDOWN_MS) return true;
		}
		COOLDOWNS.put(player.getUUID(), now);
		return false;
	}
	//Interakce
	@Override
	public void attack(@NotNull BlockState blockState, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player) {
		super.attack(blockState, world, pos, player);
		if (world.isClientSide || !(player instanceof ServerPlayer serverPlayer)) return;
		if (DifMod.playerGameModeIsCreativeCategory(serverPlayer) || !player.getMainHandItem().isEmpty()) return;
		if (!blockState.getValue(XP)) return;
		if (isOnCooldown(player)) return;
		handleXpExtraction(world, pos, blockState, player, player.isShiftKeyDown() ? 30 : 1);
	}
	//Interakce
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState blockState, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		if (world.isClientSide()) return InteractionResult.SUCCESS;
		ItemStack heldItem = player.getItemInHand(hand);
		//Wrench logika
		if (heldItem.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "tools/wrench")))) {
			world.setBlock(pos, blockState.setValue(XP, !blockState.getValue(XP)), 3);
			AllSoundEvents.WRENCH_ROTATE.playOnServer(world, pos, 1.0F, Create.RANDOM.nextFloat() * 0.5F + 0.5F);
			return InteractionResult.SUCCESS;
		}
		BlockPos targetPos = pos.relative(blockState.getValue(FACING));
		BlockEntity blockEntity = world.getBlockEntity(targetPos);
		if (blockEntity == null) return InteractionResult.FAIL;
		//XP Logika
		if (blockState.getValue(XP) && heldItem.isEmpty()) {
			if (isOnCooldown(player)) return InteractionResult.SUCCESS;
			handleXpInsertion(world, targetPos, blockState, player, player.isShiftKeyDown());
			return InteractionResult.SUCCESS;
		}
		//Kbelík Logika
		else if (heldItem.getItem() instanceof BucketItem bucket) {
			blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, blockState.getValue(FACING)).ifPresent(capability -> {
				FluidStack fluidToFill = new FluidStack(bucket.getFluid(), 1000);
				int simulated = capability.fill(fluidToFill, IFluidHandler.FluidAction.SIMULATE);
				if (simulated >= 1000) {
					capability.fill(fluidToFill, IFluidHandler.FluidAction.EXECUTE);
					if (!player.isCreative()) {
						ItemStack emptyBucket = new ItemStack(Items.BUCKET);
						heldItem.shrink(1);
						if (heldItem.isEmpty()) player.setItemInHand(hand, emptyBucket);
						else ItemHandlerHelper.giveItemToPlayer(player, emptyBucket);
					}
				}
			});
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.SUCCESS;
	}
	//Implementace Vkládání
	private void handleXpInsertion(Level world, BlockPos targetPos, BlockState blockState, Player player, boolean insertAll) {
		BlockEntity blockEntity = world.getBlockEntity(targetPos);
		if (blockEntity == null) return;
		blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, blockState.getValue(FACING)).ifPresent(capability -> {
			int currentTotal = getActualPlayerXP(player);
			int toInsert;
			if (insertAll) {
				toInsert = currentTotal;
			} else {
				int xpAtCurrentLevelBase = totalXpForLevel(player.experienceLevel);
				int partialProgressXP = currentTotal - xpAtCurrentLevelBase;
				if (partialProgressXP > 0) {
					toInsert = partialProgressXP;
				} else if (player.experienceLevel > 0) {
					toInsert = xpAtCurrentLevelBase - totalXpForLevel(player.experienceLevel - 1);
				} else {
					toInsert = 0;
				}
			}
			if (toInsert <= 0) return;
			FluidStack simFluid = new FluidStack(DifModFluids.XP.get(), toInsert);
			int accepted = capability.fill(simFluid, IFluidHandler.FluidAction.SIMULATE);
			if (accepted > 0 && getActualPlayerXP(player) >= accepted) {
				int filled = capability.fill(new FluidStack(DifModFluids.XP.get(), accepted), IFluidHandler.FluidAction.EXECUTE);
				if (filled > 0) player.giveExperiencePoints(-filled);
			}
		});
	}
	//Implementace Vytahování
	private void handleXpExtraction(Level world, BlockPos hatchPos, BlockState blockState, Player player, int levelsRequested) {
		BlockPos targetPos = hatchPos.relative(blockState.getValue(FACING));
		BlockEntity blockEntity = world.getBlockEntity(targetPos);
		if (blockEntity == null) return;
		blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, blockState.getValue(FACING)).ifPresent(capability -> {
			int targetLevel = player.experienceLevel + levelsRequested;
			int neededXP = totalXpForLevel(targetLevel) - getActualPlayerXP(player);
			if (neededXP <= 0) return;
			FluidStack drainedSim = capability.drain(new FluidStack(DifModFluids.XP.get(), neededXP), IFluidHandler.FluidAction.SIMULATE);
			if (drainedSim.getAmount() <= 0) return; // FIX: Pokud je tank prázdný, nic nedáváme
			FluidStack drained = capability.drain(drainedSim, IFluidHandler.FluidAction.EXECUTE);
			if (drained.getAmount() > 0) player.giveExperiencePoints(drained.getAmount());
		});
	}
	//XP Matematika
	private static int totalXpForLevel(int level) {
		if (level <= 0) return 0;
		if (level <= 16) return level * level + 6 * level;
		if (level <= 31) return (int) (2.5 * level * level - 40.5 * level + 360);
		return (int) (4.5 * level * level - 162.5 * level + 2220);
	}
	private static int xpBarCap(int level) {
		if (level >= 30) return 112 + (level - 30) * 9;
		if (level >= 15) return 37 + (level - 15) * 5;
		return 7 + level * 2;
	}
	private static int getActualPlayerXP(Player player) {
		return totalXpForLevel(player.experienceLevel) + (int) (player.experienceProgress * xpBarCap(player.experienceLevel));
	}
	@Override public int getLightBlock(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos) { return 0; }
	@Override public @NotNull VoxelShape getVisualShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) { return Shapes.empty(); }
	@Override public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case NORTH -> box(1, 0, 0, 15, 16, 6);
			case EAST -> box(10, 0, 1, 16, 16, 15);
			case WEST -> box(0, 0, 1, 6, 16, 15);
			default -> box(1, 0, 10, 15, 16, 16);
		};
	}
	@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, WATERLOGGED, XP); }
	@Override public BlockState getStateForPlacement(BlockPlaceContext context) {
		if (context.getClickedFace().getAxis().equals(Direction.Axis.Y)) return null;
		return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite()).setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
	}
	public @NotNull BlockState rotate(BlockState state, Rotation rot) { return state.setValue(FACING, rot.rotate(state.getValue(FACING))); }
	public @NotNull BlockState mirror(BlockState state, Mirror mirrorIn) { return state.rotate(mirrorIn.getRotation(state.getValue(FACING))); }
	@Override public @NotNull FluidState getFluidState(BlockState state) { return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state); }
	@Override public @NotNull BlockState updateShape(BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor world, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
		if (state.getValue(WATERLOGGED)) world.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
	}
}