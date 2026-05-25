package cz.maxtechnik.dif.item;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryWandItem;
import cz.maxtechnik.dif.block.DistillationTank;
import cz.maxtechnik.dif.block.entity.DistillationTankBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class DistillationTankItem extends BlockItem {

	public DistillationTankItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public @NotNull InteractionResult place(@NotNull BlockPlaceContext ctx) {
		InteractionResult initialResult = super.place(ctx);
		if (!initialResult.consumesAction())
			return initialResult;
		tryMultiPlace(ctx);
		return initialResult;
	}

	private void tryMultiPlace(BlockPlaceContext ctx) {
		Player player = ctx.getPlayer();
		if (player == null || player.isShiftKeyDown()) return;

		Direction face = ctx.getClickedFace();
		if (!face.getAxis().isVertical()) return;

		ItemStack stack = ctx.getItemInHand();
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockPos placedOnPos = pos.relative(face.getOpposite());
		BlockState placedOnState = world.getBlockState(placedOnPos);

		if (!(placedOnState.getBlock() instanceof DistillationTank)) return;
		if (SymmetryWandItem.presentInHotbar(player)) return;

		DistillationTankBlockEntity tankAt = ConnectivityHandler.partAt(
				DifModBlockEntities.DISTILLATION_TANK.get(), world, placedOnPos
		);
		if (tankAt == null) return;

		DistillationTankBlockEntity controllerBE = (DistillationTankBlockEntity) tankAt.getControllerBE();
		if (controllerBE == null) return;

		int width = controllerBE.getWidth();
		if (width == 1) return;

		int tanksToPlace = 0;
		BlockPos startPos = face == Direction.DOWN ?
				controllerBE.getBlockPos().below() :
				controllerBE.getBlockPos().above(controllerBE.getHeight());

		if (startPos.getY() != pos.getY()) return;

		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = startPos.offset(xOffset, 0, zOffset);
				BlockState blockState = world.getBlockState(offsetPos);
				if (blockState.getBlock() instanceof DistillationTank) continue;
				if (!blockState.canBeReplaced()) return;
				tanksToPlace++;
			}
		}

		if (!player.isCreative() && stack.getCount() < tanksToPlace) return;

		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = startPos.offset(xOffset, 0, zOffset);
				BlockState blockState = world.getBlockState(offsetPos);
				if (blockState.getBlock() instanceof DistillationTank) continue;
				
				BlockPlaceContext context = BlockPlaceContext.at(ctx, offsetPos, face);
				player.getPersistentData().putBoolean("SilenceTankSound", true);
				super.place(context);
				player.getPersistentData().remove("SilenceTankSound");
			}
		}
	}
}
