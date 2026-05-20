package cz.maxtechnik.dif.item.modular.tool;

import cz.maxtechnik.dif.item.modular.ModularBase;
import cz.maxtechnik.dif.item.modular.ToolMaterial;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModularAxe extends ModularBase {
	@Override
	protected TagKey<Block> getMineableTag() {
		return BlockTags.MINEABLE_WITH_AXE;
	}

	@Override
	protected float baseAttackDamage(ToolMaterial material) {
		return switch (material) {
			case STONE, COPPER, IRON, DIAMOND -> 8F;
			case OBSIDIAN, NETHERITE -> 9F;
			default -> 6F;
		};
	}

	@Override
	protected float baseAttackSpeed(ToolMaterial material) {
		return switch (material) {
			case WOOD, STONE, COPPER -> -3.2F;
			case IRON -> -3.1F;
			default -> -3.0F;
		};
	}
}