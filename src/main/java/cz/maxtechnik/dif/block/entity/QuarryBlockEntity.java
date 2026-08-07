package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.Quarry;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.events.QuarryStats;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.quarry.QuarryArea;
import cz.maxtechnik.dif.util.quarry.QuarryAreaManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * BlockEntity pro těžební zařízení (Quarry).
 * Přijímá FE energii a běží plnou rychlostí (QP = 256).
 * Veškeré GUI a upgrady byly odstraněny.
 */
public class QuarryBlockEntity extends BlockEntity {
	public enum State { NO_ENERGY, CLEARING, BUILDING_FRAME, MINING, DONE }

	private static final int ENERGY_CAPACITY = 100000;
	private static final int ENERGY_INPUT = 5000;
	private static final int ENERGY_USAGE_PER_TICK = 100;
	private static final int FRAME_CHECK_INTERVAL = 40;

	// ── Stav Quarry ─────────────────────────────────────────────────────
	private State quarryState = State.NO_ENERGY;
	private State activeState = State.CLEARING;
	private int frameCheckTimer = 0;
	private float miningProgressAcc = 0f;
	private boolean chunksNeedReload = false;

	// ── QuarryAreaManager ────────────────────────────────────────────────
	private final QuarryAreaManager areaManager = new QuarryAreaManager();

	// ── Pracovní fronta (pro čištění a stavbu rámu) ─────────────────────
	private final ArrayList<BlockPos> workQueue = new ArrayList<>();
	private int workIndex = 0;

	// ── FE Energie ──────────────────────────────────────────────────────
	private final EnergyStorage energy = new EnergyStorage(ENERGY_CAPACITY, ENERGY_INPUT, ENERGY_CAPACITY);

	public QuarryBlockEntity(BlockPos pos, BlockState blockState) {
		super(DifModBlockEntities.QUARRY.get(), pos, blockState);
	}

	// ── Inicializace oblasti ───────────────────────────────────────────
	public void setArea(int minX, int maxX, int minZ, int maxZ) {
		areaManager.setArea(new QuarryArea(minX, maxX, minZ, maxZ));
		sync();
	}

	private void ensureAreaInitialized() {
		if (!areaManager.hasArea()) {
			Direction facing = getBlockState().getValue(Quarry.FACING);
			BlockPos center = worldPosition.relative(facing.getOpposite(), QuarryAreaManager.DEFAULT_RANGE + 1);
			int cx = center.getX(), cz = center.getZ(), dr = QuarryAreaManager.DEFAULT_RANGE;
			areaManager.setArea(new QuarryArea(cx - dr, cx + dr, cz - dr, cz + dr));
		}
	}

	public float getProgressPerTick() {
		return QuarryStats.MAX_PROGRESS_PER_TICK;
	}

	// ══════════════════════════════════════════════════════════════════════
	// ── HLAVNÍ TICK ───────────────────────────────────────────────────────
	// ══════════════════════════════════════════════════════════════════════
	public static void tick(Level level, QuarryBlockEntity be) {
		if (level.isClientSide) return;
		be.ensureAreaInitialized();

		// Odběr energie
		boolean hasEnergy = be.energy.getEnergyStored() >= ENERGY_USAGE_PER_TICK;

		if (be.quarryState != State.DONE) {
			if (hasEnergy) {
				be.energy.extractEnergy(ENERGY_USAGE_PER_TICK, false);
				if (be.quarryState == State.NO_ENERGY) {
					be.quarryState = be.activeState;
					be.sync();
				}
			} else {
				if (be.quarryState != State.NO_ENERGY) {
					be.activeState = be.quarryState;
					be.quarryState = State.NO_ENERGY;
					be.sync();
				}
				return;
			}
		}

		switch (be.quarryState) {
			case CLEARING -> be.tickClearing(level);
			case BUILDING_FRAME -> be.tickBuildFrame(level);
			case MINING -> be.tickMine(level);
			default -> {}
		}

		if (be.chunksNeedReload && be.quarryState == State.MINING && level instanceof ServerLevel sl) {
			be.areaManager.loadMiningChunks(sl);
			be.chunksNeedReload = false;
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
		sync();
	}

	private void tickClearing(Level level) {
		if (workQueue.isEmpty()) {
			startClearing(level);
			return;
		}
		float progress = getProgressPerTick();
		if (progress <= 0f) return;
		miningProgressAcc += progress;
		float cost = 10f;
		while (workIndex < workQueue.size() && miningProgressAcc >= cost) {
			BlockPos bp = workQueue.get(workIndex++);
			if (!level.isEmptyBlock(bp) && isOwnedFrame(level, bp)) {
				level.removeBlock(bp, false);
				miningProgressAcc -= cost;
			}
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
		sync();
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
			sync();
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
				sync();
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
		sync();
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
		sync();
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

	// ── Sync a Ukládání/Načítání (NBT) ────────────────────────────────────
	private void sync() {
		setChanged();
		if (level != null && !level.isClientSide) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider p) {
		CompoundTag tag = new CompoundTag();
		tag.putInt("QS", quarryState.ordinal());
		BlockPos miningPos = areaManager.getMiningPos();
		if (miningPos != null) {
			tag.putInt("MineX", miningPos.getX());
			tag.putInt("MineY", miningPos.getY());
			tag.putInt("MineZ", miningPos.getZ());
		}
		if (areaManager.hasArea()) {
			areaManager.getArea().save(tag);
		}
		return tag;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider p) {
		super.loadAdditional(tag, p);
		energy.receiveEnergy(tag.getInt("Energy") - energy.getEnergyStored(), false);
		int ord = tag.getInt("QS");
		quarryState = (ord >= 0 && ord < State.values().length) ? State.values()[ord] : State.NO_ENERGY;
		workIndex = tag.getInt("WI");
		if (tag.contains("MineX")) {
			areaManager.setMiningPos(new BlockPos(tag.getInt("MineX"), tag.getInt("MineY"), tag.getInt("MineZ")));
		}
		QuarryArea loadedArea = QuarryArea.load(tag);
		if (loadedArea == null) {
			loadedArea = QuarryArea.loadLegacyHalf(tag, "LmHX", "LmHZ", "LmCX", "LmCZ");
		}
		if (loadedArea != null) {
			areaManager.setArea(loadedArea);
		}
		if (quarryState == State.MINING) {
			chunksNeedReload = true;
		}
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider p) {
		super.saveAdditional(tag, p);
		tag.putInt("Energy", energy.getEnergyStored());
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

	public IEnergyStorage getEnergyStorage() {
		return energy;
	}

	public int getAreaMinX() {
		return areaManager.hasArea() ? areaManager.getArea().minX() : getBlockPos().getX() - QuarryAreaManager.DEFAULT_RANGE;
	}

	public int getAreaMinZ() {
		return areaManager.hasArea() ? areaManager.getArea().minZ() : getBlockPos().getZ() - QuarryAreaManager.DEFAULT_RANGE;
	}

	public int getAreaMaxX() {
		return areaManager.hasArea() ? areaManager.getArea().maxX() : getBlockPos().getX() + QuarryAreaManager.DEFAULT_RANGE;
	}

	public int getAreaMaxZ() {
		return areaManager.hasArea() ? areaManager.getArea().maxZ() : getBlockPos().getZ() + QuarryAreaManager.DEFAULT_RANGE;
	}
}