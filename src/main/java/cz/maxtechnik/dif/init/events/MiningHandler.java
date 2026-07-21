package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.config.DifModCommonConfig;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.*;

@EventBusSubscriber(modid = DifMod.MODID)
public final class MiningHandler {
	private static final ThreadLocal<Boolean> BREAKING = new ThreadLocal<>();

	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		if (Boolean.TRUE.equals(BREAKING.get())) return;
		Level level = (Level) event.getLevel();
		if (level.isClientSide) return;
		if (!(event.getPlayer() instanceof ServerPlayer player)) return;

		ItemStack tool = player.getMainHandItem();
		if (tool.isEmpty()) return;

		boolean isDestroyer = tool.is(DifModItems.ELECTRUM_DESTROYER.get());
		boolean isDeforester = tool.is(DifModItems.ELECTRUM_DEFORESTER.get()) || tool.is(DifModItems.ELECTRUM_DEFORESTRATOR.get());

		if (!isDestroyer && !isDeforester) return;

		BlockPos centre = event.getPos();
		BlockState centreState = event.getState();

		BREAKING.set(Boolean.TRUE);
		try {
			if (isDestroyer) {
				handleDestroyer(level, player, tool, centre, centreState);
			} else {
				handleDeforester(level, player, tool, centre, centreState);
			}
		} finally {
			BREAKING.remove();
		}
	}

	private static void handleDestroyer(Level level, ServerPlayer player, ItemStack tool, BlockPos centre, BlockState centreState) {
		Direction face = getPlayerFacingFace(player);
		List<BlockPos> neighbours = get3x3Plane(centre, face);

		for (BlockPos pos : neighbours) {
			if (tool.isEmpty()) break;
			BlockState state = level.getBlockState(pos);
			if (state.isAir()) continue;
			if (state.getDestroySpeed(level, pos) < 0) continue;

			if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL) || tool.isCorrectToolForDrops(state)) {
				level.destroyBlock(pos, true, player);
				if (!player.isCreative()) {
					tool.hurtAndBreak(1, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
				}
			}
		}
	}

	private static void handleDeforester(Level level, ServerPlayer player, ItemStack tool, BlockPos centre, BlockState centreState) {
		int maxLogs = DifModCommonConfig.ELECTRUM_DEFORESTER_MAX_LOGS.get();

		if (centreState.is(BlockTags.LOGS)) {
			List<BlockPos> logs = collectNaturalLogs(level, centre, maxLogs);
			if (logs != null && !logs.isEmpty()) {
				for (int i = 1; i < logs.size(); i++) {
					if (tool.isEmpty()) break;
					BlockPos pos = logs.get(i);
					BlockState state = level.getBlockState(pos);
					if (state.isAir()) continue;

					level.destroyBlock(pos, true, player);
					if (!player.isCreative()) {
						tool.hurtAndBreak(1, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
					}
				}
				return;
			}
		}

		Direction face = getPlayerFacingFace(player);
		List<BlockPos> neighbours = get3x3Plane(centre, face);

		for (BlockPos pos : neighbours) {
			if (tool.isEmpty()) break;
			BlockState state = level.getBlockState(pos);
			if (state.isAir()) continue;
			if (state.getDestroySpeed(level, pos) < 0) continue;

			if (state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LOGS) || tool.isCorrectToolForDrops(state)) {
				level.destroyBlock(pos, true, player);
				if (!player.isCreative()) {
					tool.hurtAndBreak(1, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
				}
			}
		}
	}

	public static List<BlockPos> get3x3Plane(BlockPos centre, Direction face) {
		List<BlockPos> all = getAoePlane(centre, face, 1);
		all.remove(centre);
		return all;
	}

	public static List<BlockPos> getAoePlane(BlockPos centre, Direction face, int radius) {
		Direction[] axes = perpendicularAxes(face);
		Direction a = axes[0], b = axes[1];
		int side = radius * 2 + 1;
		List<BlockPos> result = new ArrayList<>(side * side);
		for (int da = -radius; da <= radius; da++) {
			for (int db = -radius; db <= radius; db++) {
				result.add(centre.relative(a, da).relative(b, db));
			}
		}
		return result;
	}

	private static Direction[] perpendicularAxes(Direction face) {
		return switch (face) {
			case UP, DOWN -> new Direction[]{Direction.EAST, Direction.SOUTH};
			case NORTH, SOUTH -> new Direction[]{Direction.EAST, Direction.UP};
			case EAST, WEST -> new Direction[]{Direction.SOUTH, Direction.UP};
		};
	}

	private static Direction getPlayerFacingFace(Player player) {
		HitResult hit = player.pick(5.0D, 0.0F, false);
		if (hit instanceof BlockHitResult blockHit) return blockHit.getDirection();
		float pitch = player.getXRot();
		if (pitch > 45f) return Direction.DOWN;
		if (pitch < -45f) return Direction.UP;
		return player.getDirection();
	}

	private static List<BlockPos> collectNaturalLogs(Level level, BlockPos start, int maxLogs) {
		List<BlockPos> result = new ArrayList<>();
		it.unimi.dsi.fastutil.longs.LongSet visited = new it.unimi.dsi.fastutil.longs.LongOpenHashSet();
		Deque<BlockPos> queue = new ArrayDeque<>();
		queue.add(start);
		visited.add(start.asLong());
		boolean foundLeaves = false;
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		while (!queue.isEmpty() && result.size() < maxLogs) {
			BlockPos cur = queue.poll();
			result.add(cur);
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = -1; dy <= 1; dy++) {
					for (int dz = -1; dz <= 1; dz++) {
						if (dx == 0 && dy == 0 && dz == 0) continue;
						mutable.setWithOffset(cur, dx, dy, dz);
						long posLong = mutable.asLong();
						if (visited.add(posLong)) {
							BlockState state = level.getBlockState(mutable);
							if (state.is(BlockTags.LOGS)) {
								queue.add(mutable.immutable());
							} else if (!foundLeaves && state.is(BlockTags.LEAVES) && state.hasProperty(LeavesBlock.PERSISTENT) && !state.getValue(LeavesBlock.PERSISTENT)) {
								foundLeaves = true;
							}
						}
					}
				}
			}
		}
		if (!foundLeaves) return null;
		result.sort(Comparator.comparingInt(BlockPos::getY));
		return result;
	}
}
