package cz.maxtechnik.dif.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import cz.maxtechnik.dif.block.entity.CokeOvenControllerBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class CokeOvenController extends Block implements EntityBlock, IWrenchable {
	public static BooleanProperty ACTIVE = BooleanProperty.create("active");
	public static BooleanProperty FORMED = BooleanProperty.create("formed");
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public CokeOvenController(Properties properties) {
		super(properties.lightLevel((bs) -> bs.getValue(ACTIVE) ? 12 : 0));
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(ACTIVE, false)
				.setValue(FORMED, false));
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState blockState) {
		return DifModBlockEntities.COKE_OVEN_CONTROLLER.get().create(pos, blockState);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
		return level.isClientSide ? null : createServerTicker(type, DifModBlockEntities.COKE_OVEN_CONTROLLER.get());
	}

	@Nullable
	protected static <T extends BlockEntity> BlockEntityTicker<T> createServerTicker(
			BlockEntityType<T> type,
			BlockEntityType<? extends CokeOvenControllerBlockEntity> expectedType) {
		return type == expectedType
				? (lvl, pos, state, blockEntity) -> CokeOvenControllerBlockEntity.serverTick(lvl, pos, state, (CokeOvenControllerBlockEntity) blockEntity)
				: null;
	}

	/** Drop inventory when controller is broken */
	@Override
	public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos,
						 BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (level.getBlockEntity(pos) instanceof CokeOvenControllerBlockEntity be) {
				// Drop all inventory items
				for (int i = 0; i < be.getInventory().getSlots(); i++) {
					Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
							be.getInventory().getStackInSlot(i));
				}
				// Fluid is intentionally lost (no item form)
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, ACTIVE, FORMED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		// FACING stores the direction the FRONT FACE looks OUT (away from structure).
		// The structure is BEHIND the controller relative to the player.
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	public @NotNull BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	public @NotNull BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public InteractionResult onWrenched(BlockState blockState, UseOnContext ctx) {
		Level level = ctx.getLevel();
		if (level.isClientSide || blockState.getValue(FORMED)) return InteractionResult.PASS;
		return InteractionResult.CONSUME;
	}

	@Override
	protected @NotNull net.minecraft.world.ItemInteractionResult useItemOn(@NotNull net.minecraft.world.item.ItemStack heldItem, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull net.minecraft.world.InteractionHand hand, @NotNull net.minecraft.world.phys.BlockHitResult hit) {
		if (state.getValue(FORMED) && level.getBlockEntity(pos) instanceof CokeOvenControllerBlockEntity be) {
			if (be.handleInteraction(player, hand)) {
				return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
			}
		}
		return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull net.minecraft.world.phys.BlockHitResult hit) {
		if (state.getValue(FORMED) && level.getBlockEntity(pos) instanceof CokeOvenControllerBlockEntity be) {
			if (be.handleInteraction(player, net.minecraft.world.InteractionHand.MAIN_HAND)) {
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		}
		return InteractionResult.PASS;
	}
}