package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import cz.maxtechnik.dif.block.CokeOvenController;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CokeOvenBlockEntity extends BlockEntity implements IHaveGoggleInformation {
	public CokeOvenBlockEntity(BlockPos pos, BlockState blockState) {
		super(DifModBlockEntities.COKE_OVEN.get(), pos, blockState);
	}

	public CokeOvenControllerBlockEntity getFormedController() {
		if (level == null) return null;
		BlockPos myPos = worldPosition;
		// Scan 5x5x5 box centered on us to find any CokeOvenControllerBlockEntity
		for (int x = -2; x <= 2; x++) {
			for (int y = -2; y <= 2; y++) {
				for (int z = -2; z <= 2; z++) {
					BlockPos checkPos = myPos.offset(x, y, z);
					if (level.getBlockEntity(checkPos) instanceof CokeOvenControllerBlockEntity controllerBe) {
						BlockState state = level.getBlockState(checkPos);
						if (state.hasProperty(CokeOvenController.FORMED) && state.getValue(CokeOvenController.FORMED)) {
							Direction facing = state.getValue(CokeOvenController.FACING).getOpposite();
							Direction right = facing.getClockWise();
							for (int dy = -1; dy <= 1; dy++) {
								for (int dx = -1; dx <= 1; dx++) {
									for (int dz = 0; dz <= 2; dz++) {
										BlockPos partPos = checkPos.relative(facing, dz).relative(right, dx).above(dy);
										if (partPos.equals(myPos)) {
											return controllerBe;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		CokeOvenControllerBlockEntity controller = getFormedController();
		if (controller != null) {
			return controller.addToGoggleTooltip(tooltip, isPlayerSneaking);
		}
		tooltip.add(Component.literal("◆ Coke Oven").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
		tooltip.add(Component.literal(" Structure is NOT formed!").withStyle(ChatFormatting.RED));
		return true;
	}
}
