package cz.maxtechnik.dif.item.modular.tool;

import cz.maxtechnik.dif.item.modular.ModularBase;
import cz.maxtechnik.dif.item.modular.ToolMaterial;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModularSword extends ModularBase {
	@Override
	protected TagKey<Block> getMineableTag() {
		return BlockTags.SWORD_EFFICIENT;
	}

	@Override
	protected float baseAttackDamage(ToolMaterial material) {
		return switch (material) {
			case STONE, COPPER -> 4F;
			case IRON -> 5F;
			case DIAMOND -> 6F;
			case OBSIDIAN, NETHERITE -> 7F;
			default -> 3F;
		};
	}

	@Override
	protected float baseAttackSpeed(ToolMaterial material) {
		return -2.4F;
	}
}