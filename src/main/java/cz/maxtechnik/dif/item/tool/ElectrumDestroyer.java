package cz.maxtechnik.dif.item.tool;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ElectrumDestroyer extends PickaxeItem {
	public ElectrumDestroyer() {
		super(Tiers.NETHERITE, new Properties().fireResistant().attributes(PickaxeItem.createAttributes(Tiers.NETHERITE, 1.0F, -2.8F)));
	}

	@Override
	public boolean isCorrectToolForDrops(@NotNull ItemStack stack, BlockState state) {
		if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
			return true;
		}
		if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
			return !state.is(Tiers.NETHERITE.getIncorrectBlocksForDrops());
		}
		return super.isCorrectToolForDrops(stack, state);
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack stack, BlockState state) {
		if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
			return this.getTier().getSpeed();
		}
		return super.getDestroySpeed(stack, state);
	}

	@Override
	public boolean isEnchantable(@NotNull ItemStack stack) {
		return true;
	}
}
