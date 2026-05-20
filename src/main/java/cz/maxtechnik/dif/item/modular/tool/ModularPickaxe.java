package cz.maxtechnik.dif.item.modular.tool;

import cz.maxtechnik.dif.item.modular.ModularBase;
import cz.maxtechnik.dif.item.modular.ToolMaterial;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModularPickaxe extends ModularBase {
	@Override
	protected TagKey<Block> getMineableTag() {
		return BlockTags.MINEABLE_WITH_PICKAXE;
	}

	@Override
	protected float baseAttackDamage(ToolMaterial material) {
		return switch (material) {
			case STONE, COPPER -> 2F;
			case IRON -> 3F;
			case DIAMOND -> 4F;
			case OBSIDIAN, NETHERITE -> 5F;
			default -> 1F;
		};
	}

	@Override
	protected float baseAttackSpeed(ToolMaterial material) {
		return -2.8F;
	}
}