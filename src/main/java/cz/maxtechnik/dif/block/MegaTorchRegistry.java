package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MegaTorchRegistry {
	public static final TagKey<EntityType<?>> BLOCKED_MOBS = TagKey.create(
			Registries.ENTITY_TYPE,
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "mega_torch_blocked")
	);

	// Paměťová cache - extrémně rychlé řešení bez lagů
	private static final Map<ResourceKey<Level>, Set<BlockPos>> TORCHES = new ConcurrentHashMap<>();

	public static void addTorch(Level level, BlockPos pos) {
		TORCHES.computeIfAbsent(level.dimension(), k -> ConcurrentHashMap.newKeySet()).add(pos);
	}

	public static void removeTorch(Level level, BlockPos pos) {
		Set<BlockPos> torches = TORCHES.get(level.dimension());
		if (torches != null) {
			torches.remove(pos);
		}
	}

	public static Set<BlockPos> getTorches(Level level) {
		return TORCHES.getOrDefault(level.dimension(), Collections.emptySet());
	}
}