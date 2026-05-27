package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
public class CokeOvenBlockEntity extends BlockEntity {
	public CokeOvenBlockEntity(BlockPos pos,BlockState blockState) {
		super(DifModBlockEntities.COKE_OVEN.get(),pos,blockState);
	}

}
