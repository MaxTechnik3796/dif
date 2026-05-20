package cz.maxtechnik.dif.item.modular.tool;

import cz.maxtechnik.dif.item.modular.ModularBase;
import cz.maxtechnik.dif.item.modular.ToolMaterial;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModularShovel extends ModularBase {
	@Override
	protected TagKey<Block> getMineableTag() {
		return BlockTags.MINEABLE_WITH_SHOVEL;
	}

	@Override
	protected float baseAttackDamage(ToolMaterial material) {
		return switch (material) {
			case STONE, COPPER -> 2.5F;
			case IRON -> 3.5F;
			case DIAMOND -> 4.5F;
			case OBSIDIAN, NETHERITE -> 5.5F;
			default -> 1.5F;
		};
	}

	@Override
	protected float baseAttackSpeed(ToolMaterial material) {
		return -3.0F;
	}
}