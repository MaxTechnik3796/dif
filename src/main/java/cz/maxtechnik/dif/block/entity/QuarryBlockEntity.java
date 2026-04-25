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
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import cz.maxtechnik.dif.gui.menu.QuarryMenu;
import cz.maxtechnik.dif.init.basic.DifModItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * QuarryBlockEntity – optimalizovaná verze.
 *
 * TPS optimalizations:
 *  - Frame check jen jednou za FRAME_CHECK_INTERVAL ticků (ne každý tick).
 *  - Clearing/building zpracovává BATCH_SIZE bloků za tick → méně ticků na přípravu.
 *  - Adjacent-inventory cache (obnoví se jednou za ADJ_CACHE_INTERVAL ticků).
 *  - workQueue je reusable ArrayList, nekonstruuje se každý tick.
 *  - Žádné zbytečné sync() při bežném těžení – jen při změně stavu nebo dokončení.
 *  - MINE_INTERVAL = 1 tick (quarry tice 20× za sekundu), ale energetická bariéra
 *    přirozeně omezuje rychlost; přidán konfigurovatelný BLOCKS_PER_TICK.
 */
public class QuarryBlockEntity extends BlockEntity implements MenuProvider {

	public enum State { NO_ENERGY, CLEARING, BUILDING_FRAME, MINING, DONE }


	// ── Energie ──────────────────────────────────────────────────────────────
	private static final int ENERGY_CAPACITY  = 1_000_000;
	private static final int ENERGY_INPUT     = 10_000;   // RF/t přijatých
	private static final int ENERGY_PER_CLEAR =     25;
	private static final int ENERGY_PER_FRAME =     50;
	private static final int ENERGY_PER_MINE  =    500;

	// ── Výkon ─────────────────────────────────────────────────────────────────
	/** Počet bloků vytěžených (nebo přeskočených) za jeden game-tick při těžení. */
	private static final int BLOCKS_PER_TICK      =  4;
	/** Počet bloků zpracovaných při clearing/building za jeden tick. */
	private static final int BATCH_SIZE           =  8;
	/** Každých N ticků se kontroluje celistvost framu. */
	private static final int FRAME_CHECK_INTERVAL = 40;  // 2 sekundy
	/** Každých N ticků se obnovuje cache sousedních inventářů. */
	private static final int ADJ_CACHE_INTERVAL   = 60;

	// ── Geometrie ─────────────────────────────────────────────────────────────
	private static final int FRAME_HEIGHT = 3;
	/** Výchozí dosah (poloměr) – přepsatelný landmarky. */
	public  static final int DEFAULT_RANGE = 5;
	/** Maximální povolená šířka/délka těžící oblasti (exkluzivní). 64×64 = 4096 bloků. */
	public  static final int MAX_AREA_SIDE = 64;

	// ── Stav ──────────────────────────────────────────────────────────────────
	private State    quarryState = State.NO_ENERGY;
	private BlockPos miningPos;
	private int      frameCheckTimer = 0;
	private int      adjCacheTimer   = 0;

	private final ArrayList<BlockPos> workQueue = new ArrayList<>();
	private int workIndex = 0;

	// Landmark-definovaná oblast (null = výchozí čtverec dle DEFAULT_RANGE)
	// Uloženo jako offset od worldPosition (pro snadnou serializaci).
	private int customHalfX = -1; // -1 = neaktivní (použije DEFAULT_RANGE)
	private int customHalfZ = -1;
	private BlockPos customCenter = null;

	// ── Cache ──────────────────────────────────────────────────────────────────
	private List<BlockPos>  cachedFramePos  = null;
	private BlockPos        cachedCenter    = null;
	private Direction       cachedFacing    = null;
	// Halfx/z při posledním výpočtu cache (aby se cache invalidovala po změně landmarku)
	private int             cachedHalfX     = Integer.MIN_VALUE;
	private int             cachedHalfZ     = Integer.MIN_VALUE;

	// Cache sousedních inventářů pro distribuci dropů
	@Nullable private IItemHandler[] adjHandlers = null;

	// ── Inventář ───────────────────────────────────────────────────────────────
	private final ItemStackHandler inventory = new ItemStackHandler(3) {
		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
		}
	};
	private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> inventory);

	// ── Energie (Forge) ────────────────────────────────────────────────────────
	private int lastEnergyInput = 0;
	private int energyInputAccumulator = 0;
	private int lastEnergyOutput = 0;
	private int energyOutputAccumulator = 0;
	private int tickCounter = 0;

	private final EnergyStorage energy = new EnergyStorage(ENERGY_CAPACITY, ENERGY_INPUT, ENERGY_CAPACITY) {
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			int received = super.receiveEnergy(maxReceive, simulate);
			if (!simulate) energyInputAccumulator += received;
			return received;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			int extracted = super.extractEnergy(maxExtract, simulate);
			if (!simulate) energyOutputAccumulator += extracted;
			return extracted;
		}
	};
	private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

	private float miningProgressAccumulator = 0;
	private State activeState = State.CLEARING;
	private boolean has10TickEnergy = false;

	// ── Container Data (GUI Sync) ─────────────────────────────────────────────
	public final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			switch (index) {
				case 0: return quarryState.ordinal();
				case 1: return getActiveDPMod(); // Speed = DP
				case 2: return lastEnergyOutput;
				case 3: return lastEnergyInput;
				case 4: return getFrameHalfX() * 2;
				case 5: return getFrameHalfZ() * 2;
				case 6: return getTotalDPGen() == 0 ? 1 : 0; // indikátor chybějícího enginu
				default: return 0;
			}
		}

		@Override
		public void set(int index, int value) {}

		@Override
		public int getCount() {
			return 7;
		}
	};

	// ── Pomocné metody pro enginy a drilly ─────────────────────────────────────

	public int getTotalDPGen() {
		int bpt = 0;
		if (inventory.getStackInSlot(1).getItem() instanceof cz.maxtechnik.dif.item.quarry.EngineItem e) bpt += e.dpGen;
		if (inventory.getStackInSlot(2).getItem() instanceof cz.maxtechnik.dif.item.quarry.EngineItem e) bpt += e.dpGen;
		return Math.max(0, bpt); 
	}

	public int getHeadDPReq() {
		if (inventory.getStackInSlot(0).getItem() instanceof cz.maxtechnik.dif.item.quarry.DrillHeadItem d) return d.dpReq;
		return 0; // No head
	}

	public int getActiveDPMod() {
		int gen = getTotalDPGen();
		int req = getHeadDPReq();
		if (req == 0 || gen < req) return 0;
		return 1 + (gen - req); // 1 + excess DP. Pokud mám přesně DP co to žere, bude rychlost 1. Čím víc, tím víc.
	}

	private int getTotalFECost() {
		int e = 0;
		if (inventory.getStackInSlot(1).getItem() instanceof cz.maxtechnik.dif.item.quarry.EngineItem eng) e += eng.feCost;
		if (inventory.getStackInSlot(2).getItem() instanceof cz.maxtechnik.dif.item.quarry.EngineItem eng) e += eng.feCost;
		return e; // FE per tick
	}

	private net.minecraft.world.item.ItemStack getSimulatedTool() {
		net.minecraft.world.item.ItemStack tool = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WOODEN_PICKAXE);
		net.minecraft.world.item.ItemStack head = inventory.getStackInSlot(0);
		
		if (head.is(cz.maxtechnik.dif.init.basic.DifModItems.STONE_DRILL_HEAD.get())) tool = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE_PICKAXE);
		else if (head.is(cz.maxtechnik.dif.init.basic.DifModItems.IRON_DRILL_HEAD.get())) tool = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE);
		else if (head.is(cz.maxtechnik.dif.init.basic.DifModItems.DIAMOND_DRILL_HEAD.get())) tool = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE);
		
		net.minecraft.world.item.ItemStack upgrade = inventory.getStackInSlot(2);
		if (upgrade.is(net.minecraft.world.item.Items.ENCHANTED_BOOK) &&
			net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH, upgrade) > 0) {
			tool.enchant(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH, 1);
		}
		return tool;
	}

	private boolean hasLiquidRemover() {
		return inventory.getStackInSlot(2).is(cz.maxtechnik.dif.init.basic.DifModItems.LIQUID_REMOVER.get());
	}

	public QuarryBlockEntity(BlockPos pos, BlockState state) {
		super(DifModBlockEntities.QUARRY.get(), pos, state);
	}


	// ══════════════════════════════════════════════════════════════════════════
	//  Tick
	// ══════════════════════════════════════════════════════════════════════════

	public static void tick(Level level, BlockPos pos, BlockState state, QuarryBlockEntity be) {
		if (level.isClientSide) return;

		be.tickCounter++;
		if (be.tickCounter >= 20) {
			be.lastEnergyInput = be.energyInputAccumulator / 20;
			be.energyInputAccumulator = 0;
			be.lastEnergyOutput = be.energyOutputAccumulator / 20;
			be.energyOutputAccumulator = 0;
			be.tickCounter = 0;
		}

		// Obnovení adj cache
		if (++be.adjCacheTimer >= ADJ_CACHE_INTERVAL) { be.adjCacheTimer = 0; be.adjHandlers = null; }

		boolean hasEnergy = be.energy.getEnergyStored() > 0;

		if (!hasEnergy) {
			if (be.quarryState != State.NO_ENERGY) { be.quarryState = State.NO_ENERGY; be.sync(level, pos, state); }
			return;
		}

		switch (be.quarryState) {
			case NO_ENERGY      -> be.startClearing(level, state, pos);
			case CLEARING       -> be.tickClearing(level, pos, state);
			case BUILDING_FRAME -> be.tickBuildFrame(level, pos, state);
			case MINING         -> be.tickMine(level, pos, state);
			case DONE           -> { /* nic */ }
		}
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  Geometrie & Cache
	// ══════════════════════════════════════════════════════════════════════════

	/** Vrátí aktuální "půl-rozměr" v ose X (od středu k okraji, včetně). */
	private int halfX() { return customHalfX > 0 ? customHalfX : DEFAULT_RANGE; }
	/** Vrátí aktuální "půl-rozměr" v ose Z (od středu k okraji, včetně). */
	private int halfZ() { return customHalfZ > 0 ? customHalfZ : DEFAULT_RANGE; }

	/** Nastaví oblast z landmarků a invaliduje cache. */
	public void setLandmarkArea(int halfX, int halfZ, BlockPos center) {
		// Minimum 1×1 těžící oblast → frame musí být o 2 bloky větší (stěny)
		this.customHalfX = Math.max(2, Math.min(halfX, MAX_AREA_SIDE / 2));
		this.customHalfZ = Math.max(2, Math.min(halfZ, MAX_AREA_SIDE / 2));
		this.customCenter = center;
		invalidateCache();
		setChanged();
		if (level != null && !level.isClientSide) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	private void invalidateCache() {
		cachedFramePos = null;
		cachedCenter   = null;
		cachedFacing   = null;
		cachedHalfX    = Integer.MIN_VALUE;
		cachedHalfZ    = Integer.MIN_VALUE;
	}

	/** Vrátí (a zakešuje) seznam pozic frame bloků. */
	public List<BlockPos> computeFramePositions(BlockState state) {
		Direction facing = state.getValue(Quarry.FACING);
		int hx = halfX(), hz = halfZ();
		if (cachedFramePos == null || facing != cachedFacing || hx != cachedHalfX || hz != cachedHalfZ) {
			cachedFacing = facing;
			cachedHalfX  = hx;
			cachedHalfZ  = hz;
			
			if (customCenter != null) {
				cachedCenter = customCenter;
			} else {
				// Střed těžící oblasti leží (hx+1) bloků před quarry (ve směru facing.getOpposite())
				cachedCenter = worldPosition.relative(facing.getOpposite(), hx + 1);
			}
			
			cachedFramePos = buildFramePositions(cachedCenter, hx, hz);
		}
		return cachedFramePos;
	}

	private List<BlockPos> buildFramePositions(BlockPos c, int hx, int hz) {
		int yBase = worldPosition.getY();
		List<BlockPos> result = new ArrayList<>();
		for (int x = c.getX() - hx; x <= c.getX() + hx; x++) {
			for (int z = c.getZ() - hz; z <= c.getZ() + hz; z++) {
				boolean eX = (x == c.getX() - hx || x == c.getX() + hx);
				boolean eZ = (z == c.getZ() - hz || z == c.getZ() + hz);
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
		computeFramePositions(state);
		return cachedCenter;
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  CLEARING
	// ══════════════════════════════════════════════════════════════════════════

	private void startClearing(Level level, BlockState state, BlockPos pos) {
		BlockPos center = getAreaCenter(state);
		int hx = halfX(), hz = halfZ();
		// +1 blok nahoru oproti původnímu: yTop = worldPosition.getY() + FRAME_HEIGHT
		int yTop = worldPosition.getY() + FRAME_HEIGHT;
		int yBot = worldPosition.getY();

		workQueue.clear();
		workIndex = 0;
		for (int y = yTop; y >= yBot; y--)
			for (int x = center.getX() - hx; x <= center.getX() + hx; x++)
				for (int z = center.getZ() - hz; z <= center.getZ() + hz; z++) {
					BlockPos p = new BlockPos(x, y, z);
					if (!level.isEmptyBlock(p) && !isOwnedFrame(level, p)) workQueue.add(p);
				}

		if (workQueue.isEmpty()) { startBuildingFrame(level, state, pos); return; }
		quarryState = State.CLEARING;
		sync(level, pos, state);
	}

	private void tickClearing(Level level, BlockPos pos, BlockState bs) {
		if (workQueue.isEmpty()) { startClearing(level, bs, pos); return; }
		int processed = 0;

		// Clearing rychlost odvozujeme od getTotalDPGen() bez vlivu heady (stavi frame/cisti vzduch)
		int speed = Math.max(1, getTotalDPGen() / 15);

		while (workIndex < workQueue.size() && processed < speed) {
			BlockPos t = workQueue.get(workIndex++);
			if (level.isEmptyBlock(t) || isOwnedFrame(level, t)) { processed++; continue; }
			level.removeBlock(t, false);
			processed++;
		}
		if (workIndex >= workQueue.size()) {
			workQueue.clear(); workIndex = 0;
			startBuildingFrame(level, bs, pos);
		}
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  BUILDING_FRAME
	// ══════════════════════════════════════════════════════════════════════════

	private void startBuildingFrame(Level level, BlockState state, BlockPos pos) {
		quarryState = State.BUILDING_FRAME;
		List<BlockPos> fp = computeFramePositions(state);
		workQueue.clear();
		workQueue.addAll(fp);
		workIndex = 0;
		sync(level, pos, state);
	}

	private void tickBuildFrame(Level level, BlockPos pos, BlockState bs) {
		if (workQueue.isEmpty()) { startBuildingFrame(level, bs, pos); return; }
		int processed = 0;
		int speed = Math.max(1, getTotalDPGen() / 15);

		while (workIndex < workQueue.size() && processed < speed) {
			BlockPos fPos = workQueue.get(workIndex++);
			if (isFrameBlock(level, fPos)) { processed++; continue; }
			level.setBlock(fPos, DifModBlocks.QUARRY_FRAME.get().defaultBlockState(), 3);
			if (level.getBlockEntity(fPos) instanceof QuarryFrameBlockEntity f) f.setOwner(worldPosition);
			processed++;
		}
		if (workIndex >= workQueue.size()) {
			workQueue.clear(); workIndex = 0;
			activeState = State.MINING;
			quarryState = State.MINING;
			resetMiningArea(bs);
			sync(level, pos, bs);
		}
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  MINING
	// ══════════════════════════════════════════════════════════════════════════

	private void tickMine(Level level, BlockPos pos, BlockState bs) {
		if (++frameCheckTimer >= FRAME_CHECK_INTERVAL) {
			frameCheckTimer = 0;
			if (!isFrameIntact(level, bs)) {
				quarryState = State.CLEARING; activeState = State.CLEARING; workQueue.clear(); workIndex = 0; miningPos = null;
				sync(level, pos, bs); return;
			}
		}

		int activeDP = getActiveDPMod();
		if (activeDP <= 0) return; // Nemá správnou kombinaci hlavy a enginu
		
		if (miningPos == null) resetMiningArea(bs);
		if (miningPos == null) return;

		net.minecraft.world.item.ItemStack simulatedTool = getSimulatedTool();
		boolean liquidRemover = hasLiquidRemover();

		miningProgressAccumulator += activeDP;

		while (true) {
			while (level.isEmptyBlock(miningPos)) {
				miningProgressAccumulator = 0;
				if (!advanceMiningPos(bs)) { finishMining(level, pos, bs); return; }
			}

			BlockState target = level.getBlockState(miningPos);

			// Kapaliny
			if (!target.getFluidState().isEmpty()) {
				if (target.getFluidState().isSource() && liquidRemover) {
					if (miningProgressAccumulator >= 5) { // Trochu DP stojí smáznutí
						miningProgressAccumulator -= 5;
						level.removeBlock(miningPos, false);
					} else {
						return; // Zkusí dodělat příště tick
					}
				}
				miningProgressAccumulator = 0;
				if (!advanceMiningPos(bs)) { finishMining(level, pos, bs); return; }
				continue;
			}

			float hardness = target.getDestroySpeed(level, miningPos);
			if (hardness < 0 && !target.isAir()) {
				miningProgressAccumulator = 0;
				if (!advanceMiningPos(bs)) { finishMining(level, pos, bs); return; }
				continue; // Bedrock
			}

			// Required progress to break: hardness * 10
			float requiredProgress = Math.max(1, hardness * 10);

			if (miningProgressAccumulator >= requiredProgress) {
				miningProgressAccumulator -= requiredProgress;
				
				if (level instanceof ServerLevel sl) {
					boolean canMine = !target.requiresCorrectToolForDrops() || simulatedTool.isCorrectToolForDrops(target);
					if (canMine) {
						List<net.minecraft.world.item.ItemStack> drops = Block.getDrops(target, sl, miningPos, level.getBlockEntity(miningPos), null, simulatedTool);
						distributeDrops(level, drops);
					}
					level.removeBlock(miningPos, false);
				} 
				
				if (!advanceMiningPos(bs)) { finishMining(level, pos, bs); return; }
			} else {
				break; // Done this tick
			}
		}

		level.sendBlockUpdated(pos, bs, bs, 3);
	}

	private void distributeDrops(Level level, List<ItemStack> drops) {
		if (drops.isEmpty()) return;

		// Lazy-build adjacent handler cache
		if (adjHandlers == null) {
			adjHandlers = new IItemHandler[Direction.values().length];
			for (Direction dir : Direction.values()) {
				BlockEntity adj = level.getBlockEntity(worldPosition.relative(dir));
				if (adj != null)
					adjHandlers[dir.ordinal()] = adj.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).orElse(null);
			}
		}

		for (ItemStack stack : drops) {
			if (stack.isEmpty()) continue;
			ItemStack rem = stack;
			for (IItemHandler h : adjHandlers) {
				if (h == null || rem.isEmpty()) continue;
				rem = ItemHandlerHelper.insertItemStacked(h, rem, false);
			}
			if (!rem.isEmpty()) Block.popResource(level, worldPosition, rem);
		}
	}

	private void finishMining(Level level, BlockPos pos, BlockState bs) {
		quarryState = State.DONE;
		miningPos = null;
		sync(level, pos, bs);
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  Callbacks
	// ══════════════════════════════════════════════════════════════════════════

	public void onFrameDestroyed(Level level) {
		if (level == null || level.isClientSide) return;
		quarryState = State.CLEARING; workQueue.clear(); workIndex = 0; miningPos = null;
		setChanged();
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	public void onQuarryRemoved() {
		if (level == null || level.isClientSide) return;
		for (BlockPos fp : computeFramePositions(getBlockState()))
			if (level.getBlockEntity(fp) instanceof QuarryFrameBlockEntity f
					&& worldPosition.equals(f.getOwnerPos())) f.scheduleRemoval();
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  Frame helpers
	// ══════════════════════════════════════════════════════════════════════════

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

	// ══════════════════════════════════════════════════════════════════════════
	//  Mining position
	// ══════════════════════════════════════════════════════════════════════════

	private void resetMiningArea(BlockState state) {
		BlockPos center = getAreaCenter(state);
		int hx = halfX() - 1, hz = halfZ() - 1;
		miningPos = new BlockPos(center.getX() - hx, worldPosition.getY() - 1, center.getZ() - hz);
		setChanged();
	}

	private boolean advanceMiningPos(BlockState state) {
		if (miningPos == null || level == null) return false;
		BlockPos center = getAreaCenter(state);
		int hx = halfX() - 1, hz = halfZ() - 1;
		int minX = center.getX() - hx, maxX = center.getX() + hx;
		int minZ = center.getZ() - hz, maxZ = center.getZ() + hz;

		int x = miningPos.getX() + 1, z = miningPos.getZ(), y = miningPos.getY();
		if (x > maxX) { x = minX; z++; }
		if (z > maxZ) { z = minZ; y--; }
		miningPos = new BlockPos(x, y, z);
		setChanged();
		return y > level.getMinBuildHeight();
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  NBT / Network
	// ══════════════════════════════════════════════════════════════════════════

	private void sync(Level level, BlockPos pos, BlockState state) {
		level.sendBlockUpdated(pos, state, state, 3);
		setChanged();
	}

	@Override public @NotNull CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		tag.putInt("QS", quarryState.ordinal());
		if (miningPos != null) {
			tag.putInt("MineX", miningPos.getX());
			tag.putInt("MineY", miningPos.getY());
			tag.putInt("MineZ", miningPos.getZ());
		}
		if (customHalfX > 0) {
			tag.putInt("LmHX", customHalfX);
			tag.putInt("LmHZ", customHalfZ);
		}
		if (customCenter != null) {
			tag.putInt("LmCX", customCenter.getX());
			tag.putInt("LmCY", customCenter.getY());
			tag.putInt("LmCZ", customCenter.getZ());
		}
		return tag;
	}

	@Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override public void load(@NotNull CompoundTag tag) {
		super.load(tag);
		if (tag.contains("Inventory")) {
			inventory.deserializeNBT(tag.getCompound("Inventory"));
		}
		// Energie – workaround: EnergyStorage nemá setter, použijeme receiveEnergy
		int stored = tag.getInt("Energy");
		energy.receiveEnergy(stored - energy.getEnergyStored(), false);

		int ord = tag.getInt("QS");
		quarryState = (ord >= 0 && ord < State.values().length) ? State.values()[ord] : State.NO_ENERGY;

		if (tag.contains("MineX"))
			miningPos = new BlockPos(tag.getInt("MineX"), tag.getInt("MineY"), tag.getInt("MineZ"));

		workIndex = tag.getInt("WI");
		if (tag.contains("LmHX")) { customHalfX = tag.getInt("LmHX"); customHalfZ = tag.getInt("LmHZ"); }
		if (tag.contains("LmCX")) {
			customCenter = new BlockPos(tag.getInt("LmCX"), tag.getInt("LmCY"), tag.getInt("LmCZ"));
		} else {
			customCenter = null;
		}
	}

	@Override protected void saveAdditional(@NotNull CompoundTag tag) {
		super.saveAdditional(tag);
		tag.put("Inventory", inventory.serializeNBT());
		tag.putInt("Energy", energy.getEnergyStored());
		tag.putInt("QS",     quarryState.ordinal());
		tag.putInt("WI",     workIndex);
		if (miningPos != null) {
			tag.putInt("MineX", miningPos.getX());
			tag.putInt("MineY", miningPos.getY());
			tag.putInt("MineZ", miningPos.getZ());
		}
		if (customHalfX > 0) { tag.putInt("LmHX", customHalfX); tag.putInt("LmHZ", customHalfZ); }
		if (customCenter != null) {
			tag.putInt("LmCX", customCenter.getX());
			tag.putInt("LmCY", customCenter.getY());
			tag.putInt("LmCZ", customCenter.getZ());
		}
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  Capabilities
	// ══════════════════════════════════════════════════════════════════════════

	@Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (cap == ForgeCapabilities.ENERGY) return energyCap.cast();
		if (cap == ForgeCapabilities.ITEM_HANDLER) return inventoryCap.cast();
		return super.getCapability(cap, side);
	}

	@Override public void invalidateCaps() { super.invalidateCaps(); energyCap.invalidate(); inventoryCap.invalidate(); }

	// ══════════════════════════════════════════════════════════════════════════
	//  Getters (pro renderer + veřejné API)
	// ══════════════════════════════════════════════════════════════════════════

	public BlockPos getMiningPos()   { return miningPos; }
	public State    getQuarryState() { return quarryState; }
	public BlockPos getAreaCenter()  { return getAreaCenter(getBlockState()); }
	/** Vrátí half-rozměr těžící oblasti v ose X (bez frame stěny). */
	public int      getHalfX()       { return halfX() - 1; }
	/** Vrátí half-rozměr těžící oblasti v ose Z (bez frame stěny). */
	public int      getHalfZ()       { return halfZ() - 1; }
	/** Pro renderer – half-rozměr včetně frame stěny. */
	public int      getFrameHalfX()  { return halfX(); }
	public int      getFrameHalfZ()  { return halfZ(); }
	
	// ── MenuProvider ───────────────────────────────────────────────────────────
	@Override
	public @NotNull Component getDisplayName() {
		return Component.translatable("block.dif.quarry");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
		return new QuarryMenu(id, playerInv, this);
	}
}