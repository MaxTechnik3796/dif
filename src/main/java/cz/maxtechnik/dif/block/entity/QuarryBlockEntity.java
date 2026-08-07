package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import cz.maxtechnik.dif.block.Quarry;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.quarry.QuarryArea;
import cz.maxtechnik.dif.util.quarry.QuarryAreaManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * BlockEntity pro těžební zařízení (Quarry).
 * Připojeno na Create kinetic síť (shaft zdola).
 * 1 RPM = 128 SU zátěže. Těžební rychlost odvislá přímo od RPM.
 * Podporuje vyžadované Goggles info a Redstone zastavení.
 */
public class QuarryBlockEntity extends KineticBlockEntity {
	public enum State { NO_ENERGY, CLEARING, BUILDING_FRAME, MINING, DONE }

	private static final int FRAME_CHECK_INTERVAL = 40;

	// ── Stav Quarry ─────────────────────────────────────────────────────
	private State quarryState = State.NO_ENERGY;
	private State activeState = State.CLEARING;
	private int frameCheckTimer = 0;
	private float miningProgressAcc = 0f;
	private boolean chunksNeedReload = false;
	private boolean lastRedstoneState = false;

	// ── QuarryAreaManager ────────────────────────────────────────────────
	private final QuarryAreaManager areaManager = new QuarryAreaManager();

	// ── Pracovní fronta (pro čištění a stavbu rámu) ─────────────────────
	private final ArrayList<BlockPos> workQueue = new ArrayList<>();
	private int workIndex = 0;

	public QuarryBlockEntity(BlockPos pos, BlockState blockState) {
		super(DifModBlockEntities.QUARRY.get(), pos, blockState);
	}

	public boolean isRedstonePowered() {
		return level != null && level.hasNeighborSignal(worldPosition);
	}

	// ── Create Kinetic Stress (1 RPM = 128 SU, 0 při redstonu) ──────────
	@Override
	public float calculateStressApplied() {
		if (isRedstonePowered()) {
			this.lastStressApplied = 0f;
			return 0f;
		}
		float impact = 128.0f;
		this.lastStressApplied = impact;
		return impact;
	}

	// ── Goggles Tooltip (Engineer's Goggles Info) ────────────────────────
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        // 1. Status
		Component statusComponent;
		if (isRedstonePowered()) {
			statusComponent = Component.literal("Stopped").withStyle(ChatFormatting.GOLD);
		} else if (quarryState == State.DONE) {
			statusComponent = Component.literal("Finished").withStyle(ChatFormatting.AQUA);
		} else if (isOverStressed()) {
			statusComponent = Component.literal("Overstressed").withStyle(ChatFormatting.RED);
		} else if (Math.abs(getSpeed()) == 0) {
			statusComponent = Component.literal("No Power").withStyle(ChatFormatting.RED);
		} else {
			String actionName = switch (quarryState) {
				case CLEARING -> "Clearing Area";
				case BUILDING_FRAME -> "Building Frame";
				case MINING -> "Mining";
				default -> "Active";
			};
			statusComponent = Component.literal(actionName).withStyle(ChatFormatting.GREEN);
		}
		tooltip.add(Component.literal("     Status: ").withStyle(ChatFormatting.GRAY).append(statusComponent));

		// 2. Area Size
		if (areaManager.hasArea()) {
			int sizeX = areaManager.getArea().sizeX();
			int sizeZ = areaManager.getArea().sizeZ();
			tooltip.add(Component.literal("     Area: ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal(sizeX + " x " + sizeZ + " blocks").withStyle(ChatFormatting.WHITE)));
		}
		return true;
	}

	// ── Inicializace oblasti ───────────────────────────────────────────
	public void setArea(int minX, int maxX, int minZ, int maxZ) {
		areaManager.setArea(new QuarryArea(minX, maxX, minZ, maxZ));
		sendData();
	}

	public void ensureAreaInitialized() {
		if (!areaManager.hasArea()) {
			Direction facing = getBlockState().getValue(Quarry.FACING);
			BlockPos center = worldPosition.relative(facing.getOpposite(), QuarryAreaManager.DEFAULT_RANGE + 1);
			int cx = center.getX(), cz = center.getZ(), dr = QuarryAreaManager.DEFAULT_RANGE;
			areaManager.setArea(new QuarryArea(cx - dr, cx + dr, cz - dr, cz + dr));
		}
	}

	// Rychlost těžby podle RPM sítě
	public float getProgressPerTick() {
		float speed = Math.abs(getSpeed());
		if (speed <= 0f || isOverStressed() || isRedstonePowered()) return 0f;
		return Math.clamp(speed / 12.8f, 0.1f, 20.0f);
	}

	// ══════════════════════════════════════════════════════════════════════
	// ── HLAVNÍ TICK ───────────────────────────────────────────────────────
	// ══════════════════════════════════════════════════════════════════════
	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide) return;
		ensureAreaInitialized();

		boolean redstoneStopped = isRedstonePowered();
		if (lastRedstoneState != redstoneStopped) {
			lastRedstoneState = redstoneStopped;
			if (hasNetwork()) {
				getOrCreateNetwork().updateStress();
			}
			sendData();
		}

		float speed = Math.abs(getSpeed());
		boolean hasPower = speed > 0f && !isOverStressed() && !redstoneStopped;

		if (!hasPower && quarryState != State.DONE) {
			if (quarryState != State.NO_ENERGY) {
				activeState = quarryState;
				quarryState = State.NO_ENERGY;
				sendData();
			}
			return;
		}

		if (hasPower && quarryState == State.NO_ENERGY) {
			quarryState = activeState;
			sendData();
		}

		switch (quarryState) {
			case CLEARING -> tickClearing(level);
			case BUILDING_FRAME -> tickBuildFrame(level);
			case MINING -> tickMine(level);
			default -> {}
		}

		if (chunksNeedReload && quarryState == State.MINING && level instanceof ServerLevel sl) {
			areaManager.loadMiningChunks(sl);
			chunksNeedReload = false;
		}
	}

	// ── ČIŠTĚNÍ OBLASTI PRO RÁM ───────────────────────────────────────────
	private void startClearing(Level level) {
		QuarryArea area = areaManager.getArea();
		int yBase = worldPosition.getY();
		workQueue.clear();
		workIndex = 0;
		for (int y = yBase + 3; y >= yBase; y--) {
			for (int x = area.minX(); x <= area.maxX(); x++) {
				for (int z = area.minZ(); z <= area.maxZ(); z++) {
					BlockPos bp = new BlockPos(x, y, z);
					if (!level.isEmptyBlock(bp) && isOwnedFrame(level, bp)) {
						workQueue.add(bp);
					}
				}
			}
		}
		if (workQueue.isEmpty()) {
			startBuildingFrame();
			return;
		}
		quarryState = State.CLEARING;
		miningProgressAcc = 0f;
		sendData();
	}

	private void tickClearing(Level level) {
		if (workQueue.isEmpty()) {
			startClearing(level);
			return;
		}
		float progress = getProgressPerTick();
		if (progress <= 0f) return;
		miningProgressAcc += progress;

		int safety = 0;
		while (workIndex < workQueue.size() && safety++ < 1000) {
			BlockPos bp = workQueue.get(workIndex);
			BlockState state = level.getBlockState(bp);

			if (state.isAir() || !isOwnedFrame(level, bp)) {
				workIndex++;
				continue;
			}

			float hardness = state.getDestroySpeed(level, bp);
			if (hardness < 0) {
				workIndex++;
				continue;
			}

			float required = Math.max(1f, hardness * 10f);
			if (miningProgressAcc < required) {
				break;
			}

			miningProgressAcc -= required;
			level.removeBlock(bp, false);
			workIndex++;
		}

		if (workIndex >= workQueue.size()) {
			workQueue.clear();
			workIndex = 0;
			miningProgressAcc = 0f;
			startBuildingFrame();
		}
	}

	// ── STAVBA RÁMU ───────────────────────────────────────────────────────
	private void startBuildingFrame() {
		quarryState = State.BUILDING_FRAME;
		workQueue.clear();
		workQueue.addAll(areaManager.computeFramePositions(worldPosition.getY()));
		workIndex = 0;
		sendData();
	}

	private void tickBuildFrame(Level level) {
		if (workQueue.isEmpty()) {
			startBuildingFrame();
			return;
		}
		float progress = getProgressPerTick();
		if (progress <= 0f) return;
		miningProgressAcc += progress;
		float cost = 15f;
		while (workIndex < workQueue.size() && miningProgressAcc >= cost) {
			BlockPos fp = workQueue.get(workIndex++);
			if (isFrameBlock(level, fp)) {
				level.setBlock(fp, DifModBlocks.QUARRY_FRAME.get().defaultBlockState(), 3);
				if (level.getBlockEntity(fp) instanceof QuarryFrameBlockEntity frame) {
					frame.setOwner(worldPosition);
				}
				miningProgressAcc -= cost;
			}
		}
		if (workIndex >= workQueue.size()) {
			workQueue.clear();
			workIndex = 0;
			miningProgressAcc = 0f;
			activeState = State.MINING;
			quarryState = State.MINING;
			areaManager.resetMiningPos(worldPosition.getY());
			if (level instanceof ServerLevel sl) {
				areaManager.loadMiningChunks(sl);
			}
			sendData();
		}
	}

	// ── TĚŽBA ─────────────────────────────────────────────────────────────
	private void tickMine(Level level) {
		if (++frameCheckTimer >= FRAME_CHECK_INTERVAL) {
			frameCheckTimer = 0;
			if (!isFrameIntact(level)) {
				if (level instanceof ServerLevel sl) areaManager.unloadForcedChunks(sl);
				quarryState = State.CLEARING;
				activeState = State.CLEARING;
				workQueue.clear();
				workIndex = 0;
				areaManager.setMiningPos(null);
				sendData();
				return;
			}
		}
		float step = getProgressPerTick();
		if (step <= 0f) return;
		miningProgressAcc = QuarryMiningLogic.doMiningTick(this, level, miningProgressAcc, step);
	}

	public void finishMining() {
		quarryState = State.DONE;
		areaManager.setMiningPos(null);
		if (level instanceof ServerLevel sl) areaManager.unloadForcedChunks(sl);
		sendData();
	}

	// ── Frame Utility ─────────────────────────────────────────────────────
	public boolean isFrameIntact(Level level) {
		for (BlockPos fp : areaManager.computeFramePositions(worldPosition.getY())) {
			if (!level.isLoaded(fp)) continue;
			if (isFrameBlock(level, fp)) return false;
		}
		return true;
	}

	private boolean isFrameBlock(Level level, BlockPos pos) {
		return !level.getBlockState(pos).is(DifModBlocks.QUARRY_FRAME.get());
	}

	private boolean isOwnedFrame(Level level, BlockPos pos) {
		return isFrameBlock(level, pos) || !(level.getBlockEntity(pos) instanceof QuarryFrameBlockEntity frame) || !worldPosition.equals(frame.getOwnerPos());
	}

	public void onFrameDestroyed(Level level) {
		if (level == null || level.isClientSide) return;
		if (level instanceof ServerLevel sl) areaManager.unloadForcedChunks(sl);
		quarryState = State.CLEARING;
		workQueue.clear();
		workIndex = 0;
		areaManager.setMiningPos(null);
		sendData();
	}

	public void onQuarryRemoved() {
		if (level == null || level.isClientSide) return;
		if (level instanceof ServerLevel sl) areaManager.unloadForcedChunks(sl);
		for (BlockPos fp : areaManager.computeFramePositions(worldPosition.getY())) {
			if (level.getBlockEntity(fp) instanceof QuarryFrameBlockEntity frame && worldPosition.equals(frame.getOwnerPos())) {
				frame.scheduleRemoval();
			}
		}
	}

	// ── Create Kinetic NBT (read / write) ──────────────────────────────────
	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		int ord = tag.getInt("QS");
		quarryState = (ord >= 0 && ord < State.values().length) ? State.values()[ord] : State.NO_ENERGY;
		workIndex = tag.getInt("WI");
		if (tag.contains("MineX")) {
			areaManager.setMiningPos(new BlockPos(tag.getInt("MineX"), tag.getInt("MineY"), tag.getInt("MineZ")));
		}
		QuarryArea loadedArea = QuarryArea.load(tag);
		if (loadedArea != null) {
			areaManager.setArea(loadedArea);
		}
		if (quarryState == State.MINING) {
			chunksNeedReload = true;
		}
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.putInt("QS", quarryState.ordinal());
		tag.putInt("WI", workIndex);
		BlockPos miningPos = areaManager.getMiningPos();
		if (miningPos != null) {
			tag.putInt("MineX", miningPos.getX());
			tag.putInt("MineY", miningPos.getY());
			tag.putInt("MineZ", miningPos.getZ());
		}
		if (areaManager.hasArea()) {
			areaManager.getArea().save(tag);
		}
	}

	// ── Gettery ───────────────────────────────────────────────────────────
	public QuarryAreaManager getAreaManager() {
		return areaManager;
	}

	public BlockPos getMiningPos() {
		return areaManager.getMiningPos();
	}

	public State getQuarryState() {
		return quarryState;
	}

	public int getAreaMinX() {
		ensureAreaInitialized();
		return areaManager.hasArea() ? areaManager.getArea().minX() : getBlockPos().getX() - QuarryAreaManager.DEFAULT_RANGE;
	}

	public int getAreaMinZ() {
		ensureAreaInitialized();
		return areaManager.hasArea() ? areaManager.getArea().minZ() : getBlockPos().getZ() - QuarryAreaManager.DEFAULT_RANGE;
	}

	public int getAreaMaxX() {
		ensureAreaInitialized();
		return areaManager.hasArea() ? areaManager.getArea().maxX() : getBlockPos().getX() + QuarryAreaManager.DEFAULT_RANGE;
	}

	public int getAreaMaxZ() {
		ensureAreaInitialized();
		return areaManager.hasArea() ? areaManager.getArea().maxZ() : getBlockPos().getZ() + QuarryAreaManager.DEFAULT_RANGE;
	}
}