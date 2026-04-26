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
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
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
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class QuarryBlockEntity extends BlockEntity implements MenuProvider {

	public enum State { NO_ENERGY, CLEARING, BUILDING_FRAME, MINING, DONE }

	private static final int ENERGY_CAPACITY      = QuarryStats.QUARRY_ENERGY_CAPACITY;
	private static final int ENERGY_INPUT         = QuarryStats.QUARRY_ENERGY_INPUT;
	private static final int FRAME_CHECK_INTERVAL = 40;
	private static final int ADJ_CACHE_INTERVAL   = 60;
	private static final int FE_TICK_INTERVAL     = 5;
	private static final int FRAME_HEIGHT         = 3;
	private static final int PREPARE_SPEED_DIV    = 4;

	public static final int DEFAULT_RANGE = 5;
	public static final int MAX_AREA_SIDE = 128;

	private State    quarryState     = State.NO_ENERGY;
	private State    activeState     = State.CLEARING;
	private BlockPos miningPos;
	private int      frameCheckTimer = 0;
	private int      adjCacheTimer   = 0;
	private boolean  hasFEThisCycle  = false;
	private float    miningProgressAcc = 0f;

	private final ArrayList<BlockPos> workQueue = new ArrayList<>();
	private int workIndex = 0;

	private int       customHalfX = -1;
	private int       customHalfZ = -1;
	@Nullable private BlockPos customCenter = null;

	@Nullable private List<BlockPos> cachedFramePos = null;
	@Nullable private BlockPos       cachedCenter   = null;
	@Nullable private Direction      cachedFacing   = null;
	private int cachedHalfX = Integer.MIN_VALUE;
	private int cachedHalfZ = Integer.MIN_VALUE;

	@Nullable private IItemHandler[] adjHandlers = null;

	// slot 0=vrták, 1=motor A, 2=motor B nebo upgrade
	private boolean hasSilkTouch     = false;
	private boolean hasLiquidRemover = false;

	private final ItemStackHandler inventory = new ItemStackHandler(3) {
		@Override protected void onContentsChanged(int slot) {
			rebuildUpgradeCache();
			setChanged();
		}
	};

	private void rebuildUpgradeCache() {
		hasSilkTouch     = false;
		hasLiquidRemover = false;
		ItemStack upgradeStack = inventory.getStackInSlot(2);
		if (upgradeStack.isEmpty()) return;
		if (upgradeStack.getItem() == DifModItems.LIQUID_REMOVER.get()) {
			hasLiquidRemover = true;
		} else if (upgradeStack.is(Items.ENCHANTED_BOOK)) {
			ListTag storedEnchants = EnchantedBookItem.getEnchantments(upgradeStack);
			ResourceLocation silkKey = ForgeRegistries.ENCHANTMENTS.getKey(Enchantments.SILK_TOUCH);
			for (int i = 0; i < storedEnchants.size(); i++) {
				ResourceLocation enchId = ResourceLocation.tryParse(storedEnchants.getCompound(i).getString("id"));
				if (silkKey != null && silkKey.equals(enchId)) { hasSilkTouch = true; break; }
			}
		}
	}

	private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> inventory);

	private int FEInAcc   = 0;
	private int FEOutAcc  = 0;
	private int feTickCounter = 0;

	private final EnergyStorage energy = new EnergyStorage(ENERGY_CAPACITY, ENERGY_INPUT, ENERGY_CAPACITY) {
		@Override public int receiveEnergy(int max, boolean sim) {
			int rcv = super.receiveEnergy(max, sim);
			if (!sim) FEInAcc += rcv;
			return rcv;
		}
		@Override public int extractEnergy(int max, boolean sim) {
			int ext = super.extractEnergy(max, sim);
			if (!sim) FEOutAcc += ext;
			return ext;
		}
	};
	private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

	// GUI data:
	// 0=stav, 1=rychlost%, 2=FE storage, 3=FE cost motorů, 4=areaX, 5=areaZ, 6=statusMode
	public final ContainerData dataAccess = new ContainerData() {
		@Override public int get(int index) {
			return switch (index) {
				case 0 -> quarryState.ordinal();
				case 1 -> (int)(getProgressPerTick() * 100);
				case 2 -> energy.getEnergyStored();
				case 3 -> getTotalFECost();
				case 4 -> getFrameHalfX() * 2 + 1;
				case 5 -> getFrameHalfZ() * 2 + 1;
				case 6 -> {
					if (getTotalDPGen() == 0)             yield 1;
					if (getHeadDPReq()  == 0)             yield 2;
					if (getTotalDPGen() < getHeadDPReq()) yield 3;
					yield 0;
				}
				default -> 0;
			};
		}
		@Override public void set(int index, int value) {}
		@Override public int getCount() { return 7; }
	};

	public QuarryBlockEntity(BlockPos pos, BlockState state) {
		super(DifModBlockEntities.QUARRY.get(), pos, state);
	}

	// Enginy / Vrták

	public int getTotalDPGen() {
		int dp = 0;
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
		int gen = getTotalDPGen(), req = getHeadDPReq();
		if (req == 0 || gen < req) return 0f;
		float t = Math.min(1f, (float)(gen - req) / QuarryStats.MAX_ACTIVE_DP);
		return QuarryStats.MIN_PROGRESS_PER_TICK + t * (QuarryStats.MAX_PROGRESS_PER_TICK - QuarryStats.MIN_PROGRESS_PER_TICK);
	}

	public float getActiveProgressPerTick() { return getProgressPerTick(); }

	public int getTotalFECost() {
		Item eng1Item = inventory.getStackInSlot(1).getItem();
		Item eng2Item = inventory.getStackInSlot(2).getItem();
		int feCost = 0;
		if (eng1Item instanceof EngineItem eng) feCost += eng.feCost;
		if (!hasSilkTouch && !hasLiquidRemover && eng2Item instanceof EngineItem eng) feCost += eng.feCost;
		// 2 různé = +25%, 2 stejné = +50%
		if (eng1Item instanceof EngineItem && eng2Item instanceof EngineItem)
			feCost = (int)(feCost * (eng1Item == eng2Item ? 1.5f : 1.25f));
		return feCost;
	}

	private ItemStack buildSimulatedTool() {
		ItemStack headStack = inventory.getStackInSlot(0);
		ItemStack tool;
		if      (headStack.is(DifModItems.STONE_DRILL_HEAD.get()))   tool = new ItemStack(Items.STONE_PICKAXE);
		else if (headStack.is(DifModItems.IRON_DRILL_HEAD.get()))    tool = new ItemStack(Items.IRON_PICKAXE);
		else if (headStack.is(DifModItems.DIAMOND_DRILL_HEAD.get())) tool = new ItemStack(Items.DIAMOND_PICKAXE);
		else                                                          tool = new ItemStack(Items.IRON_PICKAXE);
		if (hasSilkTouch) tool.enchant(Enchantments.SILK_TOUCH, 1);
		return tool;
	}

	// Tick

	public static void tick(Level level, BlockPos pos, BlockState state, QuarryBlockEntity be) {
		if (level.isClientSide) return;

		if (++be.feTickCounter >= FE_TICK_INTERVAL) {
			be.feTickCounter = 0;
			be.FEInAcc  = 0;
			be.FEOutAcc = 0;

			int FENeeded = be.getTotalFECost() * FE_TICK_INTERVAL;

			if (be.quarryState != State.DONE && be.quarryState != State.NO_ENERGY) {
				if (be.energy.getEnergyStored() >= FENeeded) {
					be.energy.extractEnergy(FENeeded, false);
					be.hasFEThisCycle = true;
				} else {
					be.hasFEThisCycle = false;
					be.activeState = be.quarryState;
					be.quarryState = State.NO_ENERGY;
					be.sync(level, pos, state);
				}
			} else if (be.quarryState == State.NO_ENERGY) {
				if (be.energy.getEnergyStored() >= FENeeded) {
					be.energy.extractEnergy(FENeeded, false);
					be.hasFEThisCycle = true;
					be.quarryState = be.activeState;
					be.sync(level, pos, state);
				}
			} else {
				be.hasFEThisCycle = true;
			}
		}

		if (++be.adjCacheTimer >= ADJ_CACHE_INTERVAL) { be.adjCacheTimer = 0; be.adjHandlers = null; }
		if (!be.hasFEThisCycle && be.quarryState != State.DONE) return;

		switch (be.quarryState) {
			case CLEARING       -> be.tickClearing(level, pos, state);
			case BUILDING_FRAME -> be.tickBuildFrame(level, pos, state);
			case MINING         -> be.tickMine(level, pos, state);
			default -> {}
		}
	}

	// Geometrie & Cache

	private int halfX() { return customHalfX > 0 ? customHalfX : DEFAULT_RANGE; }
	private int halfZ() { return customHalfZ > 0 ? customHalfZ : DEFAULT_RANGE; }

	public void setLandmarkArea(int halfX, int halfZ, BlockPos center) {
		this.customHalfX  = Math.max(2, Math.min(halfX, MAX_AREA_SIDE / 2));
		this.customHalfZ  = Math.max(2, Math.min(halfZ, MAX_AREA_SIDE / 2));
		this.customCenter = center;
		invalidateCache();
		setChanged();
		if (level != null && !level.isClientSide)
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	private void invalidateCache() {
		cachedFramePos = null;
		cachedCenter   = null;
		cachedFacing   = null;
		cachedHalfX    = Integer.MIN_VALUE;
		cachedHalfZ    = Integer.MIN_VALUE;
	}

	public List<BlockPos> computeFramePositions(BlockState state) {
		Direction facing = state.getValue(Quarry.FACING);
		int hx = halfX(), hz = halfZ();
		if (cachedFramePos != null && facing == cachedFacing && hx == cachedHalfX && hz == cachedHalfZ)
			return cachedFramePos;

		cachedFacing = facing;
		cachedHalfX  = hx;
		cachedHalfZ  = hz;
		cachedCenter = (customCenter != null) ? customCenter
				: worldPosition.relative(facing.getOpposite(), hx + 1);

		int yBase = worldPosition.getY();
		List<BlockPos> result = new ArrayList<>();
		for (int x = cachedCenter.getX() - hx; x <= cachedCenter.getX() + hx; x++) {
			for (int z = cachedCenter.getZ() - hz; z <= cachedCenter.getZ() + hz; z++) {
				boolean edgeX = (x == cachedCenter.getX() - hx || x == cachedCenter.getX() + hx);
				boolean edgeZ = (z == cachedCenter.getZ() - hz || z == cachedCenter.getZ() + hz);
				if (!edgeX && !edgeZ) continue;
				result.add(new BlockPos(x, yBase, z));
				result.add(new BlockPos(x, yBase + FRAME_HEIGHT, z));
				if (edgeX && edgeZ) {
					result.add(new BlockPos(x, yBase + 1, z));
					result.add(new BlockPos(x, yBase + 2, z));
				}
			}
		}
		cachedFramePos = result;
		return cachedFramePos;
	}

	private BlockPos getAreaCenter(BlockState state) {
		computeFramePositions(state);
		return cachedCenter;
	}

	// CLEARING

	private void startClearing(Level level, BlockState state, BlockPos pos) {
		BlockPos center = getAreaCenter(state);
		int hx = halfX(), hz = halfZ();
		int yBase = worldPosition.getY();

		workQueue.clear(); workIndex = 0;
		for (int y = yBase + FRAME_HEIGHT; y >= yBase; y--)
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
		int speed = Math.max(1, (int)(progress * 10) / PREPARE_SPEED_DIV);
		int done = 0;
		while (workIndex < workQueue.size() && done < speed) {
			BlockPos bp = workQueue.get(workIndex++);
			if (!level.isEmptyBlock(bp) && !isOwnedFrame(level, bp)) level.removeBlock(bp, false);
			done++;
		}
		if (workIndex >= workQueue.size()) { workQueue.clear(); workIndex = 0; startBuildingFrame(level, state, pos); }
	}

	// BUILDING_FRAME

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
		int speed = Math.max(1, (int)(progress * 10) / PREPARE_SPEED_DIV);
		int done = 0;
		while (workIndex < workQueue.size() && done < speed) {
			BlockPos framePos = workQueue.get(workIndex++);
			if (!isFrameBlock(level, framePos)) {
				level.setBlock(framePos, DifModBlocks.QUARRY_FRAME.get().defaultBlockState(), 3);
				if (level.getBlockEntity(framePos) instanceof QuarryFrameBlockEntity frame)
					frame.setOwner(worldPosition);
			}
			done++;
		}
		if (workIndex >= workQueue.size()) {
			workQueue.clear(); workIndex = 0;
			activeState = State.MINING;
			quarryState = State.MINING;
			resetMiningPos(state);
			sync(level, pos, state);
		}
	}

	// MINING

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

		ItemStack tool = buildSimulatedTool();
		miningProgressAcc += progressStep;

		while (true) {
			// Přeskoč bloky které jsou skutečně prázdné (vzduch, void air) a NEJSOU tekutina
			// isEmptyBlock() vrací true i pro vodu, proto musíme kontrolovat fluid zvlášť
			while (level.isEmptyBlock(miningPos) && level.getBlockState(miningPos).getFluidState().isEmpty()) {
				miningProgressAcc = 0f;
				if (!advanceMiningPos(state)) { finishMining(level, pos, state); return; }
			}

			BlockState target = level.getBlockState(miningPos);

			// Tekutiny:
			// - zdroj + liquid remover → smaž a pokračuj
			// - cokoliv jiného (flow nebo bez removeru) → přeskoč
			if (!target.getFluidState().isEmpty()) {
				if (target.getFluidState().isSource() && hasLiquidRemover) {
					if (miningProgressAcc >= 0.1f) {
						miningProgressAcc -= 0.1f;
						level.removeBlock(miningPos, false);
						if (!advanceMiningPos(state)) { finishMining(level, pos, state); return; }
						continue;
					} else {
						break;
					}
				}
				// Flow blok nebo zdroj bez removeru → přeskoč
				miningProgressAcc = 0f;
				if (!advanceMiningPos(state)) { finishMining(level, pos, state); return; }
				continue;
			}

			// Bedrock / neničitelné
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
					boolean canMine = !target.requiresCorrectToolForDrops() || tool.isCorrectToolForDrops(target);
					if (canMine) {
						LootParams.Builder loot = new LootParams.Builder(sl)
								.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(miningPos))
								.withParameter(LootContextParams.TOOL, tool)
								.withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(miningPos));
						distributeDrops(level, target.getDrops(loot));
					}
					level.removeBlock(miningPos, false);
				}
				if (!advanceMiningPos(state)) { finishMining(level, pos, state); return; }
			} else {
				break;
			}
		}

		level.sendBlockUpdated(pos, state, state, 3);
	}

	private void distributeDrops(Level level, List<ItemStack> drops) {
		if (drops.isEmpty()) return;
		if (adjHandlers == null) {
			adjHandlers = new IItemHandler[Direction.values().length];
			for (Direction dir : Direction.values()) {
				BlockEntity adjBE = level.getBlockEntity(worldPosition.relative(dir));
				if (adjBE != null)
					adjHandlers[dir.ordinal()] = adjBE.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).orElse(null);
			}
		}
		for (ItemStack dropStack : drops) {
			if (dropStack.isEmpty()) continue;
			ItemStack remaining = dropStack;
			for (IItemHandler handler : adjHandlers) {
				if (handler == null || remaining.isEmpty()) continue;
				remaining = ItemHandlerHelper.insertItemStacked(handler, remaining, false);
			}
			if (!remaining.isEmpty()) Block.popResource(level, worldPosition, remaining);
		}
	}

	private void finishMining(Level level, BlockPos pos, BlockState state) {
		quarryState = State.DONE;
		miningPos   = null;
		sync(level, pos, state);
	}

	// Callbacks

	public void onFrameDestroyed(Level level) {
		if (level == null || level.isClientSide) return;
		quarryState = State.CLEARING; workQueue.clear(); workIndex = 0; miningPos = null;
		setChanged();
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	public void onQuarryRemoved() {
		if (level == null || level.isClientSide) return;
		for (int i = 0; i < inventory.getSlots(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty()) Block.popResource(level, worldPosition, stack);
		}
		for (BlockPos fp : computeFramePositions(getBlockState()))
			if (level.getBlockEntity(fp) instanceof QuarryFrameBlockEntity frame
					&& worldPosition.equals(frame.getOwnerPos()))
				frame.scheduleRemoval();
	}

	// Frame helpers

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
				&& level.getBlockEntity(pos) instanceof QuarryFrameBlockEntity frame
				&& worldPosition.equals(frame.getOwnerPos());
	}

	// Mining position (X→Z→Y↓)

	private void resetMiningPos(BlockState state) {
		BlockPos center = getAreaCenter(state);
		if (center == null) return;
		int innerHX = halfX() - 1, innerHZ = halfZ() - 1;
		miningPos = new BlockPos(center.getX() - innerHX, worldPosition.getY() - 1, center.getZ() - innerHZ);
		setChanged();
	}

	private boolean advanceMiningPos(BlockState state) {
		if (miningPos == null || level == null) return false;
		BlockPos center = getAreaCenter(state);
		if (center == null) return false;
		int innerHX = halfX() - 1, innerHZ = halfZ() - 1;
		int minX = center.getX() - innerHX, maxX = center.getX() + innerHX;
		int minZ = center.getZ() - innerHZ, maxZ = center.getZ() + innerHZ;

		int nx = miningPos.getX() + 1, nz = miningPos.getZ(), ny = miningPos.getY();
		if (nx > maxX) { nx = minX; nz++; }
		if (nz > maxZ) { nz = minZ; ny--; }
		miningPos = new BlockPos(nx, ny, nz);
		setChanged();
		return ny > level.getMinBuildHeight();
	}

	// NBT / Network

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
		int storedFE = tag.getInt("Energy");
		energy.receiveEnergy(storedFE - energy.getEnergyStored(), false);
		int ord = tag.getInt("QS");
		quarryState = (ord >= 0 && ord < State.values().length) ? State.values()[ord] : State.NO_ENERGY;
		if (tag.contains("MineX"))
			miningPos = new BlockPos(tag.getInt("MineX"), tag.getInt("MineY"), tag.getInt("MineZ"));
		workIndex = tag.getInt("WI");
		if (tag.contains("LmHX")) { customHalfX = tag.getInt("LmHX"); customHalfZ = tag.getInt("LmHZ"); }
		customCenter = tag.contains("LmCX")
				? new BlockPos(tag.getInt("LmCX"), tag.getInt("LmCY"), tag.getInt("LmCZ")) : null;
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

	// Capabilities

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

	// Gettery

	public BlockPos getMiningPos()   { return miningPos; }
	public State    getQuarryState() { return quarryState; }
	public BlockPos getAreaCenter()  { return getAreaCenter(getBlockState()); }
	public int      getFrameHalfX() { return halfX(); }
	public int      getFrameHalfZ() { return halfZ(); }
	public int      getEnergyStored() { return energy.getEnergyStored(); }
	public int      getEnergyCapacity() { return ENERGY_CAPACITY; }

	@Override public @NotNull Component getDisplayName() {
		return Component.translatable("block.dif.quarry");
	}

	@Nullable @Override
	public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
		return new QuarryMenu(id, playerInv, this);
	}
}