package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.Quarry;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class QuarryBlockEntity extends BlockEntity {

	public enum State { NO_ENERGY, CLEARING, BUILDING_FRAME, MINING, DONE }

	private static final int ENERGY_CAPACITY  = 100_000;
	private static final int ENERGY_INPUT     = 2_000;
	private static final int ENERGY_PER_CLEAR = 25;
	private static final int ENERGY_PER_FRAME = 50;
	private static final int ENERGY_PER_MINE  = 500;
	private static final int MINE_INTERVAL    = 1;
	private static final int FRAME_HEIGHT     = 3;
	public  static final int RANGE            = 5;

	private State    quarryState = State.NO_ENERGY;
	private BlockPos miningPos;
	private int      mineTimer   = 0;
	private List<BlockPos> workQueue  = null;
	private int            workIndex  = 0;

	// Cache – přepočítá se jednou za placement/load, ne každý tik
	private List<BlockPos> cachedFramePositions = null;
	private BlockPos       cachedAreaCenter     = null;
	private Direction      cachedFacing         = null;

	private final EnergyStorage energy = new EnergyStorage(ENERGY_CAPACITY, ENERGY_INPUT, 0);
	private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energy);

	public QuarryBlockEntity(BlockPos pos, BlockState state) {
		super(DifModBlockEntities.QUARRY.get(), pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, QuarryBlockEntity be) {
		if (level.isClientSide) return;
		if (be.energy.getEnergyStored() <= 0) {
			if (be.quarryState != State.NO_ENERGY) { be.quarryState = State.NO_ENERGY; be.sync(level, pos, state); }
			return;
		}
		switch (be.quarryState) {
			case NO_ENERGY      -> be.startClearing(level, state, pos);
			case CLEARING       -> be.tickClearing(level, pos, state);
			case BUILDING_FRAME -> be.tickBuildFrame(level, pos, state);
			case MINING         -> be.tickMine(level, pos, state);
			case DONE           -> {}
		}
	}

	// --- Cache ---

	/** Vrátí cached frame pozice; invaliduje cache pokud se změnil facing. */
	public List<BlockPos> computeFramePositions(BlockState state) {
		Direction facing = state.getValue(Quarry.FACING);
		if (cachedFramePositions == null || facing != cachedFacing) {
			cachedFacing = facing;
			cachedAreaCenter = worldPosition.relative(facing.getOpposite(), RANGE + 1);
			cachedFramePositions = buildFramePositions(cachedAreaCenter);
		}
		return cachedFramePositions;
	}

	private List<BlockPos> buildFramePositions(BlockPos center) {
		int yBase = worldPosition.getY();
		List<BlockPos> result = new ArrayList<>();
		for (int x = center.getX() - RANGE; x <= center.getX() + RANGE; x++) {
			for (int z = center.getZ() - RANGE; z <= center.getZ() + RANGE; z++) {
				boolean eX = (x == center.getX() - RANGE || x == center.getX() + RANGE);
				boolean eZ = (z == center.getZ() - RANGE || z == center.getZ() + RANGE);
				if (!eX && !eZ) continue;
				result.add(new BlockPos(x, yBase, z));
				result.add(new BlockPos(x, yBase + FRAME_HEIGHT, z));
				if (eX && eZ) {
					result.add(new BlockPos(x, yBase + 1, z));
					result.add(new BlockPos(x, yBase + 2, z));
				}
			}
		}
		return result;
	}

	private BlockPos getAreaCenter(BlockState state) {
		computeFramePositions(state); // zajistí cache
		return cachedAreaCenter;
	}

	// --- CLEARING ---

	private void startClearing(Level level, BlockState state, BlockPos pos) {
		BlockPos center = getAreaCenter(state);
		int yTop = worldPosition.getY() + FRAME_HEIGHT - 1;
		int yBot = worldPosition.getY();
		List<BlockPos> toVoid = new ArrayList<>();
		for (int y = yTop; y >= yBot; y--)
			for (int x = center.getX() - RANGE; x <= center.getX() + RANGE; x++)
				for (int z = center.getZ() - RANGE; z <= center.getZ() + RANGE; z++) {
					BlockPos p = new BlockPos(x, y, z);
					if (!level.isEmptyBlock(p) && !isOwnedFrame(level, p)) toVoid.add(p);
				}
		if (toVoid.isEmpty()) { startBuildingFrame(level, state, pos); return; }
		quarryState = State.CLEARING;
		workQueue = toVoid; workIndex = 0;
		sync(level, pos, state);
	}

	private void tickClearing(Level level, BlockPos pos, BlockState bs) {
		if (workQueue == null) { startClearing(level, bs, pos); return; }
		while (workIndex < workQueue.size()) {
			BlockPos t = workQueue.get(workIndex++);
			if (level.isEmptyBlock(t) || isOwnedFrame(level, t)) continue;
			if (energy.getEnergyStored() < ENERGY_PER_CLEAR) return;
			energy.extractEnergy(ENERGY_PER_CLEAR, false);
			level.removeBlock(t, false);
			return;
		}
		workQueue = null;
		startBuildingFrame(level, bs, pos);
	}

	// --- BUILDING_FRAME ---

	private void startBuildingFrame(Level level, BlockState state, BlockPos pos) {
		quarryState = State.BUILDING_FRAME;
		workQueue = computeFramePositions(state); workIndex = 0;
		sync(level, pos, state);
	}

	private void tickBuildFrame(Level level, BlockPos pos, BlockState bs) {
		if (workQueue == null) { startBuildingFrame(level, bs, pos); return; }
		while (workIndex < workQueue.size()) {
			BlockPos fPos = workQueue.get(workIndex++);
			if (isFrameBlock(level, fPos)) continue;
			if (energy.getEnergyStored() < ENERGY_PER_FRAME) return;
			energy.extractEnergy(ENERGY_PER_FRAME, false);
			level.setBlock(fPos, DifModBlocks.QUARRY_FRAME.get().defaultBlockState(), 3);
			if (level.getBlockEntity(fPos) instanceof QuarryFrameBlockEntity f) f.setOwner(worldPosition);
			return;
		}
		workQueue = null; quarryState = State.MINING;
		resetMiningArea(bs);
		sync(level, pos, bs);
	}

	// --- MINING ---

	private void tickMine(Level level, BlockPos pos, BlockState bs) {
		if (++mineTimer < MINE_INTERVAL) return;
		mineTimer = 0;
		if (!isFrameIntact(level, bs)) {
			// Frame zničen – reset na vrch a znovu clearing (bod 2)
			quarryState = State.CLEARING; workQueue = null;
			miningPos = null; // reset pozice – začne od vrchu jako nová
			sync(level, pos, bs); return;
		}
		if (energy.getEnergyStored() < ENERGY_PER_MINE) return;
		if (miningPos == null) resetMiningArea(bs);
		if (miningPos == null) return;
		while (level.isEmptyBlock(miningPos))
			if (!advanceMiningPos(bs)) { finishMining(level, pos, bs); return; }
		BlockState target = level.getBlockState(miningPos);
		if (!target.isAir() && target.getDestroySpeed(level, miningPos) >= 0
				&& level instanceof ServerLevel sl) {
			distributeDrops(level, Block.getDrops(target, sl, miningPos, level.getBlockEntity(miningPos)));
			level.removeBlock(miningPos, false);
			energy.extractEnergy(ENERGY_PER_MINE, false);
		}
		advanceMiningPos(bs);
		level.sendBlockUpdated(pos, bs, bs, 3);
	}

	private void distributeDrops(Level level, List<ItemStack> drops) {
		for (ItemStack stack : drops) {
			ItemStack rem = stack;
			for (Direction dir : Direction.values()) {
				if (rem.isEmpty()) break;
				BlockEntity adj = level.getBlockEntity(worldPosition.relative(dir));
				if (adj == null) continue;
				IItemHandler h = adj.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).orElse(null);
                rem = ItemHandlerHelper.insertItemStacked(h, rem, false);
			}
			if (!rem.isEmpty()) Block.popResource(level, worldPosition, rem);
		}
	}

	/** Těžba dokončena – přejde do DONE ale NERUŠÍ frame (bod 3: frame zůstane, zničení ho resetuje). */
	private void finishMining(Level level, BlockPos pos, BlockState bs) {
		quarryState = State.DONE;
		miningPos = null;
		sync(level, pos, bs);
	}

	// --- Callbacks ---

	public void onFrameDestroyed(Level level) {
		if (level == null || level.isClientSide) return;
		// Reset miningPos = null → resetMiningArea při příštím startu těžby (začne od vrchu) (bod 2+3)
		quarryState = State.CLEARING; workQueue = null; workIndex = 0;
		miningPos = null;
		setChanged();
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	public void onQuarryRemoved() {
		if (level == null || level.isClientSide) return;
		for (BlockPos fp : computeFramePositions(getBlockState()))
			if (level.getBlockEntity(fp) instanceof QuarryFrameBlockEntity f
					&& worldPosition.equals(f.getOwnerPos())) f.scheduleRemoval();
	}

	// --- Frame integrity (optimalizováno – cached list) ---

	public boolean isFrameIntact(Level level, BlockState state) {
		for (BlockPos fp : computeFramePositions(state))
			if (!isFrameBlock(level, fp)) return false;
		return true;
	}

	private boolean isFrameBlock(Level level, BlockPos pos) {
		return level.getBlockState(pos).is(DifModBlocks.QUARRY_FRAME.get());
	}

	private boolean isOwnedFrame(Level level, BlockPos pos) {
		return isFrameBlock(level, pos)
				&& level.getBlockEntity(pos) instanceof QuarryFrameBlockEntity f
				&& worldPosition.equals(f.getOwnerPos());
	}

	// --- Mining position ---

	private void resetMiningArea(BlockState state) {
		BlockPos center = getAreaCenter(state);
		miningPos = new BlockPos(center.getX() - (RANGE - 1), worldPosition.getY() - 1, center.getZ() - (RANGE - 1));
		setChanged();
	}

	private boolean advanceMiningPos(BlockState state) {
		if (miningPos == null || level == null) return false;
		BlockPos center = getAreaCenter(state);
		int minX = center.getX() - (RANGE - 1), maxX = center.getX() + (RANGE - 1);
		int minZ = center.getZ() - (RANGE - 1), maxZ = center.getZ() + (RANGE - 1);
		int x = miningPos.getX() + 1, z = miningPos.getZ(), y = miningPos.getY();
		if (x > maxX) { x = minX; z++; }
		if (z > maxZ) { z = minZ; y--; }
		miningPos = new BlockPos(x, y, z);
		setChanged();
		return y > level.getMinBuildHeight();
	}

	// --- NBT / Network ---

	private void sync(Level level, BlockPos pos, BlockState state) {
		level.sendBlockUpdated(pos, state, state, 3); setChanged();
	}

	@Override public @NotNull CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		if (miningPos != null) { tag.putInt("MineX", miningPos.getX()); tag.putInt("MineY", miningPos.getY()); tag.putInt("MineZ", miningPos.getZ()); }
		tag.putInt("QS", quarryState.ordinal());
		return tag;
	}

	@Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

	@Override public void load(@NotNull CompoundTag tag) {
		super.load(tag);
		energy.receiveEnergy(tag.getInt("Energy"), false);
		if (tag.contains("MineX")) miningPos = new BlockPos(tag.getInt("MineX"), tag.getInt("MineY"), tag.getInt("MineZ"));
		int ord = tag.getInt("QS");
		quarryState = ord < State.values().length ? State.values()[ord] : State.NO_ENERGY;
		workIndex = tag.getInt("WI");
	}

	@Override protected void saveAdditional(@NotNull CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt("Energy", energy.getEnergyStored());
		tag.putInt("QS", quarryState.ordinal());
		tag.putInt("WI", workIndex);
		if (miningPos != null) { tag.putInt("MineX", miningPos.getX()); tag.putInt("MineY", miningPos.getY()); tag.putInt("MineZ", miningPos.getZ()); }
	}

	// --- Capabilities ---

	@Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		return cap == ForgeCapabilities.ENERGY ? energyHandler.cast() : super.getCapability(cap, side);
	}

	@Override public void invalidateCaps() { super.invalidateCaps(); energyHandler.invalidate(); }

	// --- Getters ---

	public BlockPos getMiningPos()   { return miningPos; }
	public int      getRange()       { return RANGE; }
	public State    getQuarryState() { return quarryState; }
	public BlockPos getAreaCenter()  { return getAreaCenter(getBlockState()); }
}