package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.Quarry;
import cz.maxtechnik.dif.gui.menu.QuarryMenu;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.events.QuarryStats;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.item.quarry.DrillHeadItem;
import cz.maxtechnik.dif.item.quarry.EngineItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * QuarryBlockEntity
 *
 * Slots:
 *   0 = DrillHead
 *   1 = Engine (slot A)
 *   2 = Engine (slot B) OR upgrade (SilkTouch book / LiquidRemover)
 *
 * Landmark oblasti: asymetrické, střed uložen absolutně.
 * Performance: frame check přes HashSet, batch mining, adj-inv cache.
 */
public class QuarryBlockEntity extends BlockEntity implements MenuProvider {

	public enum State { NO_ENERGY, CLEARING, BUILDING_FRAME, MINING, DONE }

	// ── Energie ──────────────────────────────────────────────────────────────
	private static final int ENERGY_CAPACITY  = QuarryStats.QUARRY_ENERGY_CAPACITY;
	private static final int ENERGY_INPUT     = QuarryStats.QUARRY_ENERGY_INPUT;

	// ── Výkon ─────────────────────────────────────────────────────────────────
	private static final int FRAME_CHECK_INTERVAL = 40;   // 2 s
	private static final int ADJ_CACHE_INTERVAL   = 60;   // 3 s

	// ── Geometrie ─────────────────────────────────────────────────────────────
	private static final int FRAME_HEIGHT = 3;
	public  static final int DEFAULT_RANGE  = 5;
	/** Maximální povolená polovina strany těžební oblasti. 128/2 = 64 → 128×128. */
	public  static final int MAX_AREA_SIDE  = 128;

	// ── Stav ──────────────────────────────────────────────────────────────────
	private State    quarryState = State.NO_ENERGY;
	private BlockPos miningPos;
	private int      frameCheckTimer = 0;
	private int      adjCacheTimer   = 0;

	private final ArrayList<BlockPos> workQueue = new ArrayList<>();
	private int workIndex = 0;

	// Landmark oblast (absolutní souřadnice středu, -1 = neaktivní)
	private int      customHalfX = -1;
	private int      customHalfZ = -1;
	@Nullable private BlockPos customCenter = null;

	// ── Cache ──────────────────────────────────────────────────────────────────
	/** Pozice frame bloků jako HashSet pro O(1) lookup při isFrameIntact. */
	@Nullable private Set<BlockPos>  cachedFrameSet  = null;
	/** Seřazený seznam (pro postupné stavění). */
	@Nullable private List<BlockPos> cachedFrameList = null;
	@Nullable private BlockPos       cachedCenter    = null;
	@Nullable private Direction      cachedFacing    = null;
	private int cachedHalfX = Integer.MIN_VALUE;
	private int cachedHalfZ = Integer.MIN_VALUE;

	@Nullable private IItemHandler[] adjHandlers = null;

	// ── Inventář (3 sloty: vrták | motor A | motor B nebo upgrade) ─────────────
	private boolean hasSilkTouch    = false;
	private boolean hasLiquidRemover = false;

	private final ItemStackHandler inventory = new ItemStackHandler(3) {
		@Override protected void onContentsChanged(int slot) {
			rebuildUpgradeCache();
			setChanged();
		}
	};

	/** Přepočítá hasSilkTouch / hasLiquidRemover ze slotu 2. */
	private void rebuildUpgradeCache() {
		hasSilkTouch     = false;
		hasLiquidRemover = false;
		ItemStack upgrade = inventory.getStackInSlot(2);
		if (upgrade.isEmpty()) return;
		if (upgrade.getItem() == DifModItems.LIQUID_REMOVER.get()) {
			hasLiquidRemover = true;
		} else if (upgrade.is(Items.ENCHANTED_BOOK)
				&& EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, upgrade) > 0) {
			hasSilkTouch = true;
		}
	}

	private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> inventory);

	// ── Energie ────────────────────────────────────────────────────────────────
	private int FEInputLast  = 0;
	private int FEInputAcc   = 0;
	private int FEOutputLast = 0;
	private int FEOutputAcc  = 0;
	private int tickCounter  = 0;

	private final EnergyStorage energy = new EnergyStorage(ENERGY_CAPACITY, ENERGY_INPUT, ENERGY_CAPACITY) {
		@Override public int receiveEnergy(int maxReceive, boolean simulate) {
			int rcv = super.receiveEnergy(maxReceive, simulate);
			if (!simulate) FEInputAcc += rcv;
			return rcv;
		}
		@Override public int extractEnergy(int maxExtract, boolean simulate) {
			int ext = super.extractEnergy(maxExtract, simulate);
			if (!simulate) FEOutputAcc += ext;
			return ext;
		}
	};
	private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

	private float miningProgressAcc = 0f;
	private State activeState       = State.CLEARING;
	private boolean has10TickEnergy = false;

	// ── Container Data ─────────────────────────────────────────────────────────
	public final ContainerData dataAccess = new ContainerData() {
		@Override public int get(int index) {
			return switch (index) {
				case 0 -> quarryState.ordinal();
				case 1 -> (int)(getProgressPerTick() * 100);
				case 2 -> FEOutputLast;
				case 3 -> FEInputLast;
				case 4 -> getFrameHalfX() * 2 + 1;
				case 5 -> getFrameHalfZ() * 2 + 1;
				case 6 -> {
					if (getTotalDPGen() == 0) yield 1;
					if (getHeadDPReq() == 0)  yield 2;
					if (getTotalDPGen() < getHeadDPReq()) yield 3;
					yield 0;
				}
				default -> 0;
			};
		}
		@Override public void set(int index, int value) {}
		@Override public int getCount() { return 7; }
	};

	// ── Konstruktor ───────────────────────────────────────────────────────────
	public QuarryBlockEntity(BlockPos pos, BlockState state) {
		super(DifModBlockEntities.QUARRY.get(), pos, state);
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  Enginy / Vrták – pomocné metody
	// ══════════════════════════════════════════════════════════════════════════

	public int getTotalDPGen() {
		int dp = 0;
		// Slot 1 vždy engine; slot 2 jen pokud není upgrade
		if (inventory.getStackInSlot(1).getItem() instanceof EngineItem eng) dp += eng.dpGen;
		if (!hasSilkTouch && !hasLiquidRemover
				&& inventory.getStackInSlot(2).getItem() instanceof EngineItem eng) dp += eng.dpGen;
		return Math.max(0, dp);
	}

	public int getHeadDPReq() {
		if (inventory.getStackInSlot(0).getItem() instanceof DrillHeadItem head) return head.dpReq;
		return 0;
	}

	public float getProgressPerTick() {
		int gen = getTotalDPGen();
		int req = getHeadDPReq();
		if (req == 0 || gen < req) return 0f;
		int excess = gen - req;
		float t = Math.min(1f, (float) excess / QuarryStats.MAX_ACTIVE_DP);
		return QuarryStats.MIN_PROGRESS_PER_TICK + t * (QuarryStats.MAX_PROGRESS_PER_TICK - QuarryStats.MIN_PROGRESS_PER_TICK);
	}

	private int getTotalFECost() {
		Item eng1 = inventory.getStackInSlot(1).getItem();
		Item eng2 = inventory.getStackInSlot(2).getItem();
		int feCost = 0;
		if (eng1 instanceof EngineItem e) feCost += e.feCost;
		// Slot 2 jako engine jen pokud není upgrade
		if (!hasSilkTouch && !hasLiquidRemover && eng2 instanceof EngineItem e) feCost += e.feCost;
		// Penalizace za duplicitní enginy
		if (eng1 == eng2 && eng1 != Items.AIR && eng1 instanceof EngineItem) {
			feCost = (int)(feCost * QuarryStats.DUP_ENGINE_PENALTY);
		}
		return feCost;
	}

	/** Sestaví simulovaný nástroj (správný typ vrták + silk touch pokud aktivní). */
	private ItemStack buildSimulatedTool() {
		ItemStack head = inventory.getStackInSlot(0);
		ItemStack tool;
		if      (head.is(DifModItems.STONE_DRILL_HEAD.get()))   tool = new ItemStack(Items.STONE_PICKAXE);
		else if (head.is(DifModItems.IRON_DRILL_HEAD.get()))    tool = new ItemStack(Items.IRON_PICKAXE);
		else if (head.is(DifModItems.DIAMOND_DRILL_HEAD.get())) tool = new ItemStack(Items.DIAMOND_PICKAXE);
		else                                                     tool = new ItemStack(Items.WOODEN_PICKAXE);
		if (hasSilkTouch) tool.enchant(Enchantments.SILK_TOUCH, 1);
		return tool;
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  Tick
	// ══════════════════════════════════════════════════════════════════════════

	public static void tick(Level level, BlockPos pos, BlockState state, QuarryBlockEntity be) {
		if (level.isClientSide) return;

		be.tickCounter++;
		if (be.tickCounter >= 10) {
			be.tickCounter = 0;
			be.FEInputLast  = be.FEInputAcc  / 10; be.FEInputAcc  = 0;
			be.FEOutputLast = be.FEOutputAcc / 10; be.FEOutputAcc = 0;

			int FENeeded = be.getTotalFECost() * 10;

			if (be.quarryState != State.DONE && be.quarryState != State.NO_ENERGY) {
				if (be.energy.getEnergyStored() >= FENeeded) {
					be.energy.extractEnergy(FENeeded, false);
					be.has10TickEnergy = true;
				} else {
					be.has10TickEnergy = false;
					be.activeState = be.quarryState;
					be.quarryState = State.NO_ENERGY;
					be.sync(level, pos, state);
				}
			} else if (be.quarryState == State.NO_ENERGY) {
				if (be.energy.getEnergyStored() >= FENeeded) {
					be.energy.extractEnergy(FENeeded, false);
					be.has10TickEnergy = true;
					be.quarryState = be.activeState;
					be.sync(level, pos, state);
				}
			} else {
				be.has10TickEnergy = true; // DONE nepotřebuje energii
			}
		}

		if (++be.adjCacheTimer >= ADJ_CACHE_INTERVAL) { be.adjCacheTimer = 0; be.adjHandlers = null; }

		if (!be.has10TickEnergy && be.quarryState != State.DONE) return;

		switch (be.quarryState) {
			case CLEARING       -> be.tickClearing(level, pos, state);
			case BUILDING_FRAME -> be.tickBuildFrame(level, pos, state);
			case MINING         -> be.tickMine(level, pos, state);
			default -> {}
		}
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  Geometrie & Cache
	// ══════════════════════════════════════════════════════════════════════════

	private int halfX() { return customHalfX > 0 ? customHalfX : DEFAULT_RANGE; }
	private int halfZ() { return customHalfZ > 0 ? customHalfZ : DEFAULT_RANGE; }

	/** Nastaví oblast z landmarků (absolutní střed). Invaliduje cache. */
	public void setLandmarkArea(int halfX, int halfZ, BlockPos center) {
		this.customHalfX = Math.max(2, Math.min(halfX, MAX_AREA_SIDE / 2));
		this.customHalfZ = Math.max(2, Math.min(halfZ, MAX_AREA_SIDE / 2));
		this.customCenter = center;
		invalidateCache();
		setChanged();
		if (level != null && !level.isClientSide)
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	private void invalidateCache() {
		cachedFrameSet  = null;
		cachedFrameList = null;
		cachedCenter    = null;
		cachedFacing    = null;
		cachedHalfX     = Integer.MIN_VALUE;
		cachedHalfZ     = Integer.MIN_VALUE;
	}

	private void ensureFrameCache(BlockState state) {
		Direction facing = state.getValue(Quarry.FACING);
		int hx = halfX(), hz = halfZ();
		if (cachedFrameList != null && facing == cachedFacing && hx == cachedHalfX && hz == cachedHalfZ) return;

		cachedFacing = facing;
		cachedHalfX  = hx;
		cachedHalfZ  = hz;
		cachedCenter = (customCenter != null) ? customCenter
				: worldPosition.relative(facing.getOpposite(), hx + 1);

		cachedFrameList = buildFramePositionList(cachedCenter, hx, hz);
		cachedFrameSet  = new HashSet<>(cachedFrameList);
	}

	/** Vrátí seřazený seznam pozic framu (pro stavění). */
	public List<BlockPos> computeFramePositions(BlockState state) {
		ensureFrameCache(state);
		return cachedFrameList;
	}

	private BlockPos getAreaCenter(BlockState state) {
		ensureFrameCache(state);
		return cachedCenter;
	}

	private List<BlockPos> buildFramePositionList(BlockPos center, int hx, int hz) {
		int yBase = worldPosition.getY();
		int yTop  = yBase + FRAME_HEIGHT;
		List<BlockPos> result = new ArrayList<>();
		for (int x = center.getX() - hx; x <= center.getX() + hx; x++) {
			for (int z = center.getZ() - hz; z <= center.getZ() + hz; z++) {
				boolean edgeX = (x == center.getX() - hx || x == center.getX() + hx);
				boolean edgeZ = (z == center.getZ() - hz || z == center.getZ() + hz);
				if (!edgeX && !edgeZ) continue;
				result.add(new BlockPos(x, yBase, z));
				result.add(new BlockPos(x, yTop,  z));
				if (edgeX && edgeZ) {
					result.add(new BlockPos(x, yBase + 1, z));
					result.add(new BlockPos(x, yBase + 2, z));
				}
			}
		}
		return result;
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  CLEARING
	// ══════════════════════════════════════════════════════════════════════════

	private void startClearing(Level level, BlockState state, BlockPos pos) {
		ensureFrameCache(state);
		BlockPos center = cachedCenter;
		int hx = halfX(), hz = halfZ();
		int yBot = worldPosition.getY();
		int yTop = yBot + FRAME_HEIGHT;

		workQueue.clear();
		workIndex = 0;
		for (int y = yTop; y >= yBot; y--)
			for (int x = center.getX() - hx; x <= center.getX() + hx; x++)
				for (int z = center.getZ() - hz; z <= center.getZ() + hz; z++) {
					BlockPos bp = new BlockPos(x, y, z);
					if (!level.isEmptyBlock(bp) && !isOwnedFrame(level, bp)) workQueue.add(bp);
				}

		if (workQueue.isEmpty()) { startBuildingFrame(level, state, pos); return; }
		quarryState = State.CLEARING;
		sync(level, pos, state);
	}

	private void tickClearing(Level level, BlockPos pos, BlockState state) {
		if (workQueue.isEmpty()) { startClearing(level, state, pos); return; }
		float progress = getProgressPerTick();
		if (progress <= 0f) return;
		int speed = Math.max(1, (int)(progress * 10));
		int processed = 0;
		while (workIndex < workQueue.size() && processed < speed) {
			BlockPos bp = workQueue.get(workIndex++);
			if (!level.isEmptyBlock(bp) && !isOwnedFrame(level, bp)) level.removeBlock(bp, false);
			processed++;
		}
		if (workIndex >= workQueue.size()) {
			workQueue.clear(); workIndex = 0;
			startBuildingFrame(level, state, pos);
		}
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  BUILDING_FRAME
	// ══════════════════════════════════════════════════════════════════════════

	private void startBuildingFrame(Level level, BlockState state, BlockPos pos) {
		quarryState = State.BUILDING_FRAME;
		workQueue.clear();
		workQueue.addAll(computeFramePositions(state));
		workIndex = 0;
		sync(level, pos, state);
	}

	private void tickBuildFrame(Level level, BlockPos pos, BlockState state) {
		if (workQueue.isEmpty()) { startBuildingFrame(level, state, pos); return; }
		float progress = getProgressPerTick();
		if (progress <= 0f) return;
		int speed = Math.max(1, (int)(progress * 10));
		int processed = 0;
		while (workIndex < workQueue.size() && processed < speed) {
			BlockPos framePos = workQueue.get(workIndex++);
			if (!isFrameBlock(level, framePos)) {
				level.setBlock(framePos, DifModBlocks.QUARRY_FRAME.get().defaultBlockState(), 3);
				if (level.getBlockEntity(framePos) instanceof QuarryFrameBlockEntity frame)
					frame.setOwner(worldPosition);
			}
			processed++;
		}
		if (workIndex >= workQueue.size()) {
			workQueue.clear(); workIndex = 0;
			activeState = State.MINING;
			quarryState = State.MINING;
			resetMiningPos(state);
			sync(level, pos, state);
		}
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  MINING
	// ══════════════════════════════════════════════════════════════════════════

	private void tickMine(Level level, BlockPos pos, BlockState state) {
		if (++frameCheckTimer >= FRAME_CHECK_INTERVAL) {
			frameCheckTimer = 0;
			if (!isFrameIntact(level, state)) {
				quarryState = State.CLEARING; activeState = State.CLEARING;
				workQueue.clear(); workIndex = 0; miningPos = null;
				sync(level, pos, state); return;
			}
		}

		float progressStep = getProgressPerTick();
		if (progressStep <= 0f) return;
		if (miningPos == null) resetMiningPos(state);
		if (miningPos == null) return;

		ItemStack simulatedTool = buildSimulatedTool();

		miningProgressAcc += progressStep;

		outer:
		while (true) {
			// Přeskakuj prázdné bloky
			while (level.isEmptyBlock(miningPos)) {
				miningProgressAcc = 0f;
				if (!advanceMiningPos(state)) { finishMining(level, pos, state); return; }
			}

			BlockState target = level.getBlockState(miningPos);

			// Kapaliny
			if (!target.getFluidState().isEmpty()) {
				if (target.getFluidState().isSource() && hasLiquidRemover) {
					if (miningProgressAcc >= 0.1f) {
						miningProgressAcc -= 0.1f;
						level.removeBlock(miningPos, false);
						if (!advanceMiningPos(state)) { finishMining(level, pos, state); return; }
						continue;
					} else {
						break; // Čekáme na více energie
					}
				}
				// Tekoucí tekutina nebo bez liquid removeru – přeskoč
				miningProgressAcc = 0f;
				if (!advanceMiningPos(state)) { finishMining(level, pos, state); return; }
				continue;
			}

			// Bedrock a neničitelné bloky
			float hardness = target.getDestroySpeed(level, miningPos);
			if (hardness < 0) {
				miningProgressAcc = 0f;
				if (!advanceMiningPos(state)) { finishMining(level, pos, state); return; }
				continue;
			}

			float requiredProgress = Math.max(1f, hardness * 10f);
			if (miningProgressAcc >= requiredProgress) {
				miningProgressAcc -= requiredProgress;
				if (level instanceof ServerLevel sl) {
					boolean canMine = !target.requiresCorrectToolForDrops() || simulatedTool.isCorrectToolForDrops(target);
					if (canMine) {
						LootParams.Builder lootBuilder = new LootParams.Builder(sl)
								.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(miningPos))
								.withParameter(LootContextParams.TOOL, simulatedTool)
								.withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(miningPos));
						distributeDrops(level, target.getDrops(lootBuilder));
					}
					level.removeBlock(miningPos, false);
				}
				if (!advanceMiningPos(state)) { finishMining(level, pos, state); return; }
			} else {
				break; // Nestačí energie na tento tick
			}
		}

		level.sendBlockUpdated(pos, state, state, 3);
	}

	private void distributeDrops(Level level, List<ItemStack> drops) {
		if (drops.isEmpty()) return;
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
			for (IItemHandler handler : adjHandlers) {
				if (handler == null || rem.isEmpty()) continue;
				rem = ItemHandlerHelper.insertItemStacked(handler, rem, false);
			}
			if (!rem.isEmpty()) Block.popResource(level, worldPosition, rem);
		}
	}

	private void finishMining(Level level, BlockPos pos, BlockState state) {
		quarryState = State.DONE;
		miningPos = null;
		sync(level, pos, state);
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  Callbacks
	// ══════════════════════════════════════════════════════════════════════════

	public void onFrameDestroyed(Level level) {
		if (level == null || level.isClientSide) return;
		quarryState = State.CLEARING; workQueue.clear(); workIndex = 0; miningPos = null;
		invalidateCache();
		setChanged();
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	public void onQuarryRemoved() {
		if (level == null || level.isClientSide) return;
		for (int i = 0; i < inventory.getSlots(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty()) Block.popResource(level, worldPosition, stack);
		}
		for (BlockPos fp : computeFramePositions(getBlockState())) {
			if (level.getBlockEntity(fp) instanceof QuarryFrameBlockEntity frame
					&& worldPosition.equals(frame.getOwnerPos()))
				frame.scheduleRemoval();
		}
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  Frame helpers
	// ══════════════════════════════════════════════════════════════════════════

	/** O(1) díky HashSet cache. */
	public boolean isFrameIntact(Level level, BlockState state) {
		ensureFrameCache(state);
		if (cachedFrameSet == null) return false;
		for (BlockPos fp : cachedFrameSet)
			if (!isFrameBlock(level, fp)) return false;
		return true;
	}

	private boolean isFrameBlock(Level level, BlockPos pos) {
		return level.getBlockState(pos).is(DifModBlocks.QUARRY_FRAME.get());
	}

	private boolean isOwnedFrame(Level level, BlockPos pos) {
		return isFrameBlock(level, pos)
				&& level.getBlockEntity(pos) instanceof QuarryFrameBlockEntity frame
				&& worldPosition.equals(frame.getOwnerPos());
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  Mining position – iterace po vrstvách (Y→X→Z)
	// ══════════════════════════════════════════════════════════════════════════

	/** Resetuje miningPos na první blok těžební oblasti (1 blok pod quarry, NW roh). */
	private void resetMiningPos(BlockState state) {
		ensureFrameCache(state);
		if (cachedCenter == null) return;
		// Těžební oblast je uvnitř framu: od (center-hx+1) do (center+hx-1)
		int innerHX = halfX() - 1;
		int innerHZ = halfZ() - 1;
		miningPos = new BlockPos(
				cachedCenter.getX() - innerHX,
				worldPosition.getY() - 1,
				cachedCenter.getZ() - innerHZ);
		setChanged();
	}

	/**
	 * Posune miningPos na další pozici v pořadí X→Z→Y↓.
	 * @return false pokud jsme dosáhli dna světa.
	 */
	private boolean advanceMiningPos(BlockState state) {
		if (miningPos == null || level == null) return false;
		ensureFrameCache(state);
		if (cachedCenter == null) return false;

		int innerHX = halfX() - 1;
		int innerHZ = halfZ() - 1;
		int minX = cachedCenter.getX() - innerHX;
		int maxX = cachedCenter.getX() + innerHX;
		int minZ = cachedCenter.getZ() - innerHZ;
		int maxZ = cachedCenter.getZ() + innerHZ;

		int nx = miningPos.getX() + 1;
		int nz = miningPos.getZ();
		int ny = miningPos.getY();

		if (nx > maxX) { nx = minX; nz++; }
		if (nz > maxZ) { nz = minZ; ny--; }

		miningPos = new BlockPos(nx, ny, nz);
		setChanged();
		return ny > level.getMinBuildHeight();
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
		if (customHalfX > 0) { tag.putInt("LmHX", customHalfX); tag.putInt("LmHZ", customHalfZ); }
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
		if (tag.contains("Inventory")) inventory.deserializeNBT(tag.getCompound("Inventory"));
		rebuildUpgradeCache();
		int stored = tag.getInt("Energy");
		energy.receiveEnergy(stored - energy.getEnergyStored(), false);

		int ord = tag.getInt("QS");
		quarryState = (ord >= 0 && ord < State.values().length) ? State.values()[ord] : State.NO_ENERGY;

		if (tag.contains("MineX"))
			miningPos = new BlockPos(tag.getInt("MineX"), tag.getInt("MineY"), tag.getInt("MineZ"));

		workIndex = tag.getInt("WI");
		if (tag.contains("LmHX")) { customHalfX = tag.getInt("LmHX"); customHalfZ = tag.getInt("LmHZ"); }
		customCenter = tag.contains("LmCX")
				? new BlockPos(tag.getInt("LmCX"), tag.getInt("LmCY"), tag.getInt("LmCZ"))
				: null;
	}

	@Override protected void saveAdditional(@NotNull CompoundTag tag) {
		super.saveAdditional(tag);
		tag.put("Inventory", inventory.serializeNBT());
		tag.putInt("Energy", energy.getEnergyStored());
		tag.putInt("QS", quarryState.ordinal());
		tag.putInt("WI", workIndex);
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
		if (cap == ForgeCapabilities.ENERGY)       return energyCap.cast();
		if (cap == ForgeCapabilities.ITEM_HANDLER) return inventoryCap.cast();
		return super.getCapability(cap, side);
	}

	@Override public void invalidateCaps() {
		super.invalidateCaps();
		energyCap.invalidate();
		inventoryCap.invalidate();
	}

	// ══════════════════════════════════════════════════════════════════════════
	//  Gettery
	// ══════════════════════════════════════════════════════════════════════════

	public BlockPos getMiningPos()   { return miningPos; }
	public State    getQuarryState() { return quarryState; }
	public BlockPos getAreaCenter()  { ensureFrameCache(getBlockState()); return cachedCenter; }
	public int      getFrameHalfX() { return halfX(); }
	public int      getFrameHalfZ() { return halfZ(); }

	// ── MenuProvider ───────────────────────────────────────────────────────────
	@Override public @NotNull Component getDisplayName() {
		return Component.translatable("block.dif.quarry");
	}

	@Nullable @Override
	public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
		return new QuarryMenu(id, playerInv, this);
	}
}