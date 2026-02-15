package cz.maxtechnik.dif.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class SpaceCrate extends BaseEntityBlock{
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

	public SpaceCrate(BlockBehaviour.Properties p_49046_) {
		super(p_49046_);
		this.registerDefaultState(this.stateDefinition.any().setValue(OPEN,Boolean.FALSE));
	}

	public @NotNull InteractionResult use(@NotNull BlockState blockState,Level world,@NotNull BlockPos pos,@NotNull Player player,@NotNull InteractionHand p_49073_,@NotNull BlockHitResult hit) {
		if (world.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			BlockEntity blockentity = world.getBlockEntity(pos);
			if (blockentity instanceof BarrelBlockEntity) {
				player.openMenu((BarrelBlockEntity)blockentity);
				player.awardStat(Stats.OPEN_BARREL);
				PiglinAi.angerNearbyPiglins(player,true);
			}

			return InteractionResult.CONSUME;
		}
	}

	public void onRemove(BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,BlockState state,boolean var0) {
		if (!blockState.is(state.getBlock())) {
			BlockEntity blockentity = world.getBlockEntity(pos);
			if (blockentity instanceof Container) {
				Containers.dropContents(world, pos, (Container)blockentity);
				world.updateNeighbourForOutputSignal(pos, this);
			}
			super.onRemove(blockState, world, pos, state, var0);
		}
	}

	public void tick(@NotNull BlockState blockState,ServerLevel world,@NotNull BlockPos pos,@NotNull RandomSource source) {
		BlockEntity blockentity = world.getBlockEntity(pos);
		if (blockentity instanceof BarrelBlockEntity) {
			((BarrelBlockEntity)blockentity).recheckOpen();
		}

	}

	@Nullable
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState) {
		return new BarrelBlockEntity(pos, blockState);
	}

	public @NotNull RenderShape getRenderShape(@NotNull BlockState blockState) {
		return RenderShape.MODEL;
	}

	public void setPlacedBy(@NotNull Level world,@NotNull BlockPos pos,@NotNull BlockState state,@Nullable LivingEntity entity,ItemStack itemStack) {
		if (itemStack.hasCustomHoverName()) {
			BlockEntity blockentity = world.getBlockEntity(pos);
			if (blockentity instanceof BarrelBlockEntity) {
				((BarrelBlockEntity)blockentity).setCustomName(itemStack.getHoverName());
			}
		}

	}

	public boolean hasAnalogOutputSignal(@NotNull BlockState blockState) {
		return true;
	}

	public int getAnalogOutputSignal(@NotNull BlockState blockState,Level world,@NotNull BlockPos pos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
	}

	public @NotNull BlockState rotate(@NotNull BlockState blockState,@NotNull Rotation rotation) {
		return blockState;
	}

	public @NotNull BlockState mirror(@NotNull BlockState blockState,@NotNull Mirror mirror) {
		return blockState;
	}

	protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> builder) {
		builder.add(OPEN);
	}

	public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
		return this.defaultBlockState();
	}
}