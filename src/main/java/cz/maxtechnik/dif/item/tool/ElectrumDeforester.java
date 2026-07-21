package cz.maxtechnik.dif.item.tool;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ElectrumDeforester extends AxeItem {
	public ElectrumDeforester() {
		super(Tiers.NETHERITE, new Item.Properties().fireResistant().attributes(AxeItem.createAttributes(Tiers.NETHERITE, 5.0F, -3.0F)));
	}

	@Override
	public boolean isCorrectToolForDrops(@NotNull ItemStack stack, BlockState state) {
		return state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LOGS) || super.isCorrectToolForDrops(stack, state);
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack stack, BlockState state) {
		if (state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LOGS)) {
			return this.getTier().getSpeed();
		}
		return super.getDestroySpeed(stack, state);
	}

	@Override
	public boolean isEnchantable(@NotNull ItemStack stack) {
		return true;
	}
}
