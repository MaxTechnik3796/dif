package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import cz.maxtechnik.dif.block.Engine;
import cz.maxtechnik.dif.block.EngineExtender;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.FuelType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class EngineBlockEntity extends GeneratingKineticBlockEntity {

	// ── Konstanty pro každé palivo (speed, su per extender) ──────────────
	private record FuelStats(float speedPerExtender, float suPerExtender) {}

	private static final Map<FuelType, FuelStats> FUEL_STATS = Map.of(
			FuelType.DIESEL,          new FuelStats(12F, 2.0F),
			FuelType.HEAVY_FUEL_OIL,  new FuelStats(10F, 2.3F),
			FuelType.GASOLINE,        new FuelStats( 8F, 3.0F),
			FuelType.LPG,             new FuelStats( 9F, 2.2F)
	);

	/** Lazy lookup `Block -> FuelType` (nelze static-final, blocky registrují později). */
	private static volatile Map<Block, FuelType> BLOCK_TO_FUEL;

	private static Map<Block, FuelType> blockToFuel() {
		Map<Block, FuelType> m = BLOCK_TO_FUEL;
		if (m == null) {
			m = Map.of(
					DifModBlocks.ENGINE_EXTENDER_DIESEL.get(),         FuelType.DIESEL,
					DifModBlocks.ENGINE_EXTENDER_GASOLINE.get(),       FuelType.GASOLINE,
					DifModBlocks.ENGINE_EXTENDER_LPG.get(),            FuelType.LPG,
					DifModBlocks.ENGINE_EXTENDER_HEAVY_FUEL_OIL.get(), FuelType.HEAVY_FUEL_OIL
			);
			BLOCK_TO_FUEL = m;
		}
		return m;
	}

	// ── Konfigurace ─────────────────────────────────────────────────────
	private static final int SCAN_PERIOD = 10;          // ticků mezi scany
	private static final int FUEL_DRAIN_PER_TICK = 1;   // mB/tick (nezávisle na počtu extenderů)
	private static final int FLUID_CAPACITY = 1000;

	// ── Stav (perzistován v NBT) ────────────────────────────────────────
	private boolean generating = false;
	private float speed = 0F;
	private float su = 0F;

	// ── Runtime cache (neperzistuje, počítá se ze scanu) ────────────────
	/** True když chceme přeskanovat hned, nezávisle na periodě. */
	private boolean dirty = true;
	/** Tick offset aby všechny enginy nescanovaly současně. */
	private final int tickOffset = (int) (Math.random() * SCAN_PERIOD);

	// ── Fluid tank ──────────────────────────────────────────────────────
	public final FluidTank fluidTank = new FluidTank(FLUID_CAPACITY) {
		@Override
		protected void onContentsChanged() {
			super.onContentsChanged();
			setChanged();
			sendData(); // Create's smart sync
		}
	};

	public EngineBlockEntity(BlockPos pos, BlockState blockState) {
		super(DifModBlockEntities.ENGINE.get(), pos, blockState);
	}

	// ── Public API pro neighbor-update hooks ────────────────────────────

	/** Vyžádá přeskanování extenderů v dalším ticku (i mimo periodu). */
	public void markDirty() {
		this.dirty = true;
	}

	// ── Create kinetic API ──────────────────────────────────────────────

	@Override
	public float getGeneratedSpeed() {
		return generating ? speed : 0F;
	}

	@Override
	public float calculateAddedStressCapacity() {
		lastCapacityProvided = generating ? su : 0F;
		return lastCapacityProvided;
	}

	@Override
	public void initialize() {
		super.initialize();
		if (level != null && !level.isClientSide) {
			dirty = true;
		}
	}

	// ── Tick ────────────────────────────────────────────────────────────

	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide) return;

		// Periodický scan + force scan při dirty flagu nebo Create's reActivateSource
		boolean shouldScan = dirty
				|| reActivateSource
				|| (level.getGameTime() + tickOffset) % SCAN_PERIOD == 0;

		if (shouldScan) {
			dirty = false;
			reActivateSource = false;
			recalcFromExtenders();
		}

		// Spotřeba paliva — jen když opravdu generujeme
		if (generating) {
			if (fluidTank.getFluidAmount() < FUEL_DRAIN_PER_TICK) {
				// Došlo palivo → zastav okamžitě
				if (generating) {
					generating = false;
					updateGeneratedRotation();
				}
			} else {
				fluidTank.drain(FUEL_DRAIN_PER_TICK, IFluidHandler.FluidAction.EXECUTE);
			}
		}
	}

	/** Přepočítá speed/su/generating na základě sousedů. Volá updateGeneratedRotation() jen pokud něco změnilo. */
	private void recalcFromExtenders() {
		final BlockState ownState = getBlockState();
		if (!(ownState.getBlock() instanceof Engine)) return;

		final Direction.Axis axis = ownState.getValue(Engine.FACING).getAxis();
		final FuelType fuel = scanExtenders(axis);

		final boolean newGenerating;
		final float newSpeed;
		final float newSu;

		if (fuel == FuelType.INVALID) {
			newGenerating = false;
			newSpeed = 0F;
			newSu = 0F;
		} else {
			final FuelStats stats = FUEL_STATS.get(fuel);
			final int count = countExtenders(axis);
			newSpeed = count * stats.speedPerExtender();
			newSu    = count * stats.suPerExtender();
			newGenerating = fluidTank.getFluidAmount() >= FUEL_DRAIN_PER_TICK;
		}

		if (newGenerating != generating || newSpeed != speed || newSu != su) {
			generating = newGenerating;
			speed = newSpeed;
			su = newSu;
			updateGeneratedRotation();
			setChanged();
		}
	}

	// ── Scan extenderů ──────────────────────────────────────────────────

	/**
	 * Vrátí společný FuelType, pokud jsou všechny ne-INVALID extendery stejné.
	 * Jinak INVALID (mix nebo žádné).
	 */
	private FuelType scanExtenders(Direction.Axis axis) {
		FuelType common = FuelType.INVALID;

		FuelType f;
		f = getExtenderFuel(worldPosition.above());
		if (f != FuelType.INVALID) common = f;

		if (axis == Direction.Axis.Z) {
			f = getExtenderFuel(worldPosition.east());
			if (f != FuelType.INVALID) {
				if (common != FuelType.INVALID && common != f) return FuelType.INVALID;
				common = f;
			}
			f = getExtenderFuel(worldPosition.west());
			if (f != FuelType.INVALID) {
				if (common != FuelType.INVALID && common != f) return FuelType.INVALID;
				common = f;
			}
		} else if (axis == Direction.Axis.X) {
			f = getExtenderFuel(worldPosition.north());
			if (f != FuelType.INVALID) {
				if (common != FuelType.INVALID && common != f) return FuelType.INVALID;
				common = f;
			}
			f = getExtenderFuel(worldPosition.south());
			if (f != FuelType.INVALID) {
				if (common != FuelType.INVALID && common != f) return FuelType.INVALID;
				common = f;
			}
		}
		return common;
	}

	private FuelType getExtenderFuel(BlockPos pos) {
		if (level == null) return FuelType.INVALID;
		Block block = level.getBlockState(pos).getBlock();
		if (!(block instanceof EngineExtender)) return FuelType.INVALID;
		return blockToFuel().getOrDefault(block, FuelType.INVALID);
	}

	private int countExtenders(Direction.Axis axis) {
		if (level == null) return 0;
		int count = isExtender(worldPosition.above()) ? 1 : 0;
		if (axis == Direction.Axis.Z) {
			if (isExtender(worldPosition.east())) count++;
			if (isExtender(worldPosition.west())) count++;
		} else if (axis == Direction.Axis.X) {
			if (isExtender(worldPosition.north())) count++;
			if (isExtender(worldPosition.south())) count++;
		}
		return count;
	}

	private boolean isExtender(BlockPos pos) {
		return level != null && level.getBlockState(pos).getBlock() instanceof EngineExtender;
	}

	// ── NBT ─────────────────────────────────────────────────────────────

	@Override
	public void read(CompoundTag tag, HolderLookup.@NotNull Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		if (tag.get("fluidTank") instanceof CompoundTag fluidTag) {
			fluidTank.readFromNBT(registries, fluidTag);
		}
		generating = tag.getBoolean("generating");
		speed      = tag.getFloat("speed");
		su         = tag.getFloat("su");
		// Po loadu vždy přeskanovat — sousedi se mohli změnit (chunk reload)
		dirty = true;
	}

	@Override
	public void write(CompoundTag tag, HolderLookup.@NotNull Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.put("fluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
		tag.putBoolean("generating", generating);
		tag.putFloat("speed", speed);
		tag.putFloat("su", su);
	}
}