package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.DistillationRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Destilační tank — multiblok věž stavěná z N pater (1×1, 2×2 nebo 3×3).
 *
 * STRUKTURA:
 *   - Spodní patro = Tower Master (dělá veškerou recipe logiku)
 *   - Patra nad masterem = výstupní vrstvy (max 15)
 *   - Pod masterem = Blaze Burnery (zdroj tepla)
 *
 * RYCHLOST:
 *   - KINDLED burner = 1 bod
 *   - SEETHING burner = 2 body
 *   - 1 bod → 1.0x, 10+ bodů → 4.0x (lineární škálování)
 *
 * RECIPE FLOW:
 *   1. Master má vstupní fluid v tanku
 *   2. Najde recept podle fluidu
 *   3. Počítá progress podle rychlosti (cachedSpeed)
 *   4. Při dokončení: odebere vstup, naplní výstupní patra nad sebou
 */
public class DistillationTankBlockEntity extends FluidTankBlockEntity {

	// ── Limity ─────────────────────────────────────────────────────────────────
	public static final int MAX_FOOTPRINT = 3;   // max 3×3 půdorys
	public static final int MAX_OUTPUTS   = 15;  // max výstupních vrstev nad masterem
	public static final int BASE_TICKS    = 100; // 5 sekund pro 1.0x rychlost

	private static final int CACHE_REFRESH_RATE = 20; // přepočet teplo + struktura každou sekundu

	// ── Cache (jen master ji používá) ─────────────────────────────────────────
	private int       towerOutputCount = 0;
	private HeatLevel cachedHeat       = HeatLevel.NONE;
	private int       cachedHeatPoints = 0;
	private float     cachedSpeed      = 0.0f;
	private int       cacheTick        = 0;

	// ── Recipe processing (jen master) ────────────────────────────────────────
	private int progress = 0;
	@Nullable private DistillationRecipe cachedRecipe = null;
	private FluidStack lastInput = FluidStack.EMPTY;

	public DistillationTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	// ── Multiblock konfigurace ───────────────────────────────────────────────
	@Override public int getMaxWidth()                                { return MAX_FOOTPRINT; }
	@Override public int getMaxLength(Direction.Axis axis, int width) { return 1; }
	@Override public void addBehaviours(List<BlockEntityBehaviour> b) { /* žádné Create advancementy */ }

	public IFluidHandler getFluidCapability() { return fluidCapability; }

	// ══════════════════════════════════════════════════════════════════════════
	// Tower Master detekce
	// ══════════════════════════════════════════════════════════════════════════

	/**
	 * Master = controller spodního patra věže.
	 * Pokud blok pod ním není stejně široký controller, pak jsme master.
	 */
	public boolean isTowerMaster() {
		if (!isController() || level == null) return false;
		var below = level.getBlockEntity(worldPosition.below());
		if (!(below instanceof DistillationTankBlockEntity b)) return true;
		if (!b.isController()) return true;
		return b.getWidth() != this.getWidth();
	}

	/** Najde mastera ze kteréhokoli tanku ve věži — i z non-controller shard bloku. */
	@Nullable
	public DistillationTankBlockEntity getTowerMaster() {
		if (level == null) return null;

		// 1. Najdi controller naší vrstvy (pro non-controller shard jdi přes getController())
		DistillationTankBlockEntity layerCtrl;
		if (isController()) {
			layerCtrl = this;
		} else {
			BlockPos ctrlPos = getController();
			if (ctrlPos == null) return null;
			if (!(level.getBlockEntity(ctrlPos) instanceof DistillationTankBlockEntity c)) return null;
			layerCtrl = c;
		}

		// 2. Pokud je controller naší vrstvy master, vrátíme ho
		if (layerCtrl.isTowerMaster()) return layerCtrl;

		// 3. Jinak jdeme dolů od controlleru naší vrstvy
		BlockPos check = layerCtrl.worldPosition.below();
		for (int i = 0; i < MAX_OUTPUTS + 2; i++) {
			if (!(level.getBlockEntity(check) instanceof DistillationTankBlockEntity be)) return null;
			if (be.isTowerMaster()) return be;
			check = check.below();
		}
		return null;
	}

	// ══════════════════════════════════════════════════════════════════════════
	// Cache — počítání tepla, vrstev a rychlosti
	// ══════════════════════════════════════════════════════════════════════════

	/** Přepočítá vše — vrstvy nahoru, teplo dolů, rychlost. */
	private void refreshCache() {
		int w = getWidth();

		// Spočítej kolik výstupních pater máme nad sebou
		towerOutputCount = 0;
		for (int i = 1; i <= MAX_OUTPUTS; i++) {
			if (!(level.getBlockEntity(worldPosition.above(i)) instanceof DistillationTankBlockEntity a)) break;
			if (!a.isController() || a.getWidth() != w) break;
			towerOutputCount++;
		}

		// Spočítej teplo pod celou základnou věže (controller je v rohu)
		int points = 0;
		HeatLevel best = HeatLevel.NONE;
		for (int x = 0; x < w; x++) {
			for (int z = 0; z < w; z++) {
				BlockState burner = level.getBlockState(worldPosition.offset(x, -1, z));
				HeatLevel h = BlazeBurnerBlock.getHeatLevelOf(burner);
				if (h == HeatLevel.KINDLED)  points += 1;
				else if (h == HeatLevel.SEETHING) points += 2;
				if (h.ordinal() > best.ordinal()) best = h;
			}
		}

		cachedHeat       = best;
		cachedHeatPoints = points;

		// Rychlost: 1 bod = 1.0x, 10+ bodů = 4.0x (lineárně)
		if (points == 0) {
			cachedSpeed = 0.0f;
		} else {
			float raw = 1.0f + 3.0f * ((Math.min(10, points) - 1.0f) / 9.0f);
			cachedSpeed = Math.round(raw * 10.0f) / 10.0f;
		}
		// Notify client of new values
		sendData();
	}

	// ══════════════════════════════════════════════════════════════════════════
	// Server tick — jen master
	// ══════════════════════════════════════════════════════════════════════════

	public static void serverTick(Level level, BlockPos pos, DistillationTankBlockEntity be) {
		if (!be.isTowerMaster()) return;

		// Pravidelný refresh cache
		if (be.cacheTick-- <= 0) {
			be.refreshCache();
			be.cacheTick = CACHE_REFRESH_RATE;
		}

		// Bez tepla nic nedělej
		if (be.cachedHeatPoints == 0) {
			be.resetProgress();
			return;
		}

		// Bez vstupního fluidu nic nedělej
		FluidStack input = be.tankInventory.getFluid();
		if (input.isEmpty()) {
			be.resetProgress();
			return;
		}

		// Najdi recept (cached podle vstupního fluidu)
		if (be.cachedRecipe == null || !FluidStack.isSameFluidSameComponents(be.lastInput, input)) {
			be.cachedRecipe = findRecipe(level, input).orElse(null);
			be.lastInput    = input.copy();
			be.progress     = 0;
		}
		if (be.cachedRecipe == null) {
			be.resetProgress();
			return;
		}

		List<FluidStack> outputs = be.cachedRecipe.outputs();

		// Věž musí mít dost výstupních pater
		if (outputs.size() > be.towerOutputCount) return;

		// Všechny výstupní tanky musí mít místo
		if (!canFitOutputs(level, be.worldPosition, outputs)) return;

		// Posun progressu (×10 pro decimální přesnost — 3.4x = +34/tick)
		be.progress += (int) (be.cachedSpeed * 10f);
		be.setChanged();

		// Hotovo → odeber vstup, naplň výstupy
		if (be.progress >= BASE_TICKS * 10) {
			be.progress = 0;
			be.tankInventory.drain(be.cachedRecipe.input().amount(), IFluidHandler.FluidAction.EXECUTE);
			for (int i = 0; i < outputs.size(); i++) {
				IFluidHandler out = level.getCapability(
						Capabilities.FluidHandler.BLOCK, be.worldPosition.above(i + 1), null);
				if (out != null) out.fill(outputs.get(i).copy(), IFluidHandler.FluidAction.EXECUTE);
			}
		}
	}

	private void resetProgress() {
		if (progress != 0) { progress = 0; setChanged(); }
	}

	/** Simulace plnění — false pokud aspoň jeden výstup nemá místo. */
	private static boolean canFitOutputs(Level level, BlockPos masterPos, List<FluidStack> outputs) {
		for (int i = 0; i < outputs.size(); i++) {
			IFluidHandler h = level.getCapability(
					Capabilities.FluidHandler.BLOCK, masterPos.above(i + 1), null);
			if (h == null) return false;
			if (h.fill(outputs.get(i).copy(), IFluidHandler.FluidAction.SIMULATE) < outputs.get(i).getAmount())
				return false;
		}
		return true;
	}

	private static Optional<DistillationRecipe> findRecipe(Level level, FluidStack input) {
		for (RecipeHolder<DistillationRecipe> holder :
				level.getRecipeManager().getAllRecipesFor(DifModRecipes.DISTILLATION_TYPE.get())) {
			if (holder.value().matches(input)) return Optional.of(holder.value());
		}
		return Optional.empty();
	}

	// ══════════════════════════════════════════════════════════════════════════
	// Strukturální změny (Create ConnectivityHandler)
	// ══════════════════════════════════════════════════════════════════════════

	@Override
	public void notifyMultiUpdated() {
		super.notifyMultiUpdated();
		// Resetuj cache i progress — věž se mohla změnit
		cacheTick = 0;
		progress  = 0;
	}

	// ══════════════════════════════════════════════════════════════════════════
	// Goggles tooltip — funguje z kteréhokoli tanku ve věži
	// ══════════════════════════════════════════════════════════════════════════

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);

		DistillationTankBlockEntity master = getTowerMaster();
		if (master == null) return added;

		// Heat body
		tooltip.add(Component.literal(" "));
		tooltip.add(Component.literal(" Heat: ")
				.withStyle(ChatFormatting.GRAY)
				.append(Component.literal(master.cachedHeatPoints + " / 10")
						.withStyle(ChatFormatting.WHITE)));

		// Speed nebo "No heat"
		if (master.cachedSpeed > 0) {
			tooltip.add(Component.literal(" Speed: ")
					.withStyle(ChatFormatting.GRAY)
					.append(Component.literal(master.cachedSpeed + "x")
							.withStyle(ChatFormatting.AQUA)));
		} else {
			tooltip.add(Component.literal(" No heat source!")
					.withStyle(ChatFormatting.RED));
		}

		return true;
	}

	// ══════════════════════════════════════════════════════════════════════════
	// NBT
	// ══════════════════════════════════════════════════════════════════════════

	@Override
	public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.putInt("dif_progress", progress);
		// Sync heat info to client so Goggles tooltip works
		tag.putInt("dif_heatPoints", cachedHeatPoints);
		tag.putFloat("dif_speed",    cachedSpeed);
	}

	@Override
	public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		progress         = tag.getInt("dif_progress");
		cachedHeatPoints = tag.getInt("dif_heatPoints");
		cachedSpeed      = tag.getFloat("dif_speed");
		cacheTick        = 0;
	}
}