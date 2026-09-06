package cz.maxtechnik.dif.block.entity.barrel;

import cz.maxtechnik.dif.block.barrel.CopperBarrel;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CopperBarrelBlockEntity extends BaseBarrelBlockEntity {
	public static final int CONTAINER_SIZE = 45;

	public CopperBarrelBlockEntity(BlockPos position, BlockState blockState) {
		super(DifModBlockEntities.COPPER_BARREL.get(), position, blockState, CONTAINER_SIZE, CopperBarrel.OPEN);
	}

	@Override
	public @NotNull Component getDefaultName() {
		return Component.translatable("container.dif.copper_barrel");
	}

	@Override
	public @NotNull AbstractContainerMenu createMenu(int id, @NotNull Inventory inv) {
		return new ChestMenu(MenuType.GENERIC_9x5, id, inv, this, 5);
	}
}