package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import cz.maxtechnik.dif.block.DistillationTank;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static cz.maxtechnik.dif.DifMod.goggleTooltipFix;
public class DistillationTankBlockEntity extends FluidTankBlockEntity {
	public static final int MAX_FOOTPRINT = 3;
	public static final int MAX_OUTPUTS = 15;
	public static final int BASE_TICKS = 100;
	private static final int CACHE_REFRESH_RATE = 20;

	private int towerOutputCount = 0;
	private int cachedHeatPoints = 0;
	private float cachedSpeed = 0F;
	private int cacheTick = 0;
	private int progress = 0;
	@Nullable
	private DistillationRecipe cachedRecipe = null;
	private FluidStack lastInput = FluidStack.EMPTY;
	public DistillationTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		this.window = false;
	}
	@Override
	public int getMaxWidth() {
		return MAX_FOOTPRINT;
	}
	@Override
	public int getMaxLength(Direction.Axis axis, int width) {
		return axis == Direction.Axis.Y ? 1 : getMaxWidth();
	}
	@Override
	public void addBehaviours(List<BlockEntityBehaviour> list) {
		// BoilerHeater behavior is intentionally omitted to avoid steam engine logic
	}
	public IFluidHandler getFluidCapability() {
		return fluidCapability;
	}
	public IFluidHandler fluidTank() {
		return getFluidCapability();
	}
	public boolean isTowerMaster() {
		if (!isController() || level == null) return false;
		return !(level.getBlockState(worldPosition.below()).getBlock() instanceof DistillationTank);
	}
	@Nullable
	public DistillationTankBlockEntity getTowerMaster() {
		if (level == null) return null;
		DistillationTankBlockEntity ctrl = isController() ? this : (DistillationTankBlockEntity) getControllerBE();
		if (ctrl == null) return null;
		if (ctrl.isTowerMaster()) return ctrl;
		BlockPos.MutableBlockPos p = ctrl.getBlockPos().mutable();
		while (level.getBlockEntity(p.move(Direction.DOWN)) instanceof DistillationTankBlockEntity be) {
			DistillationTankBlockEntity beCtrl = be.isController() ? be : (DistillationTankBlockEntity) be.getControllerBE();
			if (beCtrl != null && beCtrl.isTowerMaster()) return beCtrl;
		}
		return null;
	}
	@Override
	public void notifyMultiUpdated() {
		super.notifyMultiUpdated();
		cacheTick = 0;
		progress = 0;
		DistillationTankBlockEntity master = getTowerMaster();
		if (master != null && master != this) {
			master.cacheTick = 0;
		}
	}
	private void refreshCache() {
		if (level == null) return;
		int w = getWidth();
		int newOutputCount = 0;
		for (int i = 1; i <= MAX_OUTPUTS; i++) {
			BlockPos checkPos = worldPosition.above(i);
			if (!(level.getBlockState(checkPos).getBlock() instanceof DistillationTank)) break;
			if (level.getBlockEntity(checkPos) instanceof DistillationTankBlockEntity a) {
				DistillationTankBlockEntity aCtrl = a.isController() ? a : (DistillationTankBlockEntity) a.getControllerBE();
				if (aCtrl == null || aCtrl.getWidth() != w || !aCtrl.getBlockPos().equals(checkPos)) break;
				newOutputCount++;
			} else {
				break;
			}
		}
		int points = 0;
		BlockPos.MutableBlockPos burnerPos = new BlockPos.MutableBlockPos();
		for (int x = 0; x < w; x++) {
			for (int z = 0; z < w; z++) {
				burnerPos.set(worldPosition.getX() + x, worldPosition.getY() - 1, worldPosition.getZ() + z);
				BlockState burner = level.getBlockState(burnerPos);
				HeatLevel heatLevel = BlazeBurnerBlock.getHeatLevelOf(burner);
				if (heatLevel == HeatLevel.KINDLED) points += 1;
				else if (heatLevel == HeatLevel.SEETHING) points += 2;
			}
		}
		float newSpeed = Math.min(10, points) * 0.5F;
		if (this.cachedHeatPoints != points || this.towerOutputCount != newOutputCount || this.cachedSpeed != newSpeed) {
			this.cachedHeatPoints = points;
			this.towerOutputCount = newOutputCount;
			this.cachedSpeed = newSpeed;
			setChanged();
			sendData();
		}
	}
	public static void serverTick(Level level, DistillationTankBlockEntity be) {
		if (!be.isTowerMaster()) return;

		if (be.cacheTick-- <= 0) {
			be.refreshCache();
			be.cacheTick = CACHE_REFRESH_RATE;
		}
		if (be.cachedHeatPoints == 0 || be.cachedSpeed <= 0) {
			be.resetProgress();
			return;
		}
		FluidStack input = be.tankInventory.getFluid();
		if (input.isEmpty()) {
			be.resetProgress();
			return;
		}
		if (be.cachedRecipe == null || !be.cachedRecipe.input().test(input)) {
			if (!FluidStack.isSameFluidSameComponents(be.lastInput, input)) {
				be.cachedRecipe = findRecipe(level, input).orElse(null);
				be.lastInput = input.copy();
				be.progress = 0;
			}
		}
		if (be.cachedRecipe == null) {
			be.resetProgress();
			return;
		}

		int requiredInput = be.cachedRecipe.input().amount();
		if (input.getAmount() < requiredInput) {
			be.resetProgress();
			return;
		}
		List<FluidStack> outputs = be.cachedRecipe.outputs();
		if (outputs.size() > be.towerOutputCount) {
			be.resetProgress();
			return;
		}
		List<IFluidHandler> outputHandlers = getOutputHandlersIfFit(level, be.worldPosition, outputs);
		if (outputHandlers == null) {
			return;
		}
		be.progress++;
		int targetTicks = Math.max(1, Math.round(BASE_TICKS / be.cachedSpeed));
		if (be.progress >= targetTicks) {
			be.progress = 0;
			be.tankInventory.drain(requiredInput, IFluidHandler.FluidAction.EXECUTE);
			for (int i = 0; i < outputs.size(); i++) {
				outputHandlers.get(i).fill(outputs.get(i).copy(), IFluidHandler.FluidAction.EXECUTE);
			}
			be.setChanged();
		}
	}
	private void resetProgress() {
		if (progress != 0) {
			progress = 0;
			setChanged();
		}
	}
	@Nullable
	private static List<IFluidHandler> getOutputHandlersIfFit(Level level, BlockPos masterPos, List<FluidStack> outputs) {
		List<IFluidHandler> handlers = new ArrayList<>(outputs.size());
		for (int i = 0; i < outputs.size(); i++) {
			IFluidHandler h = level.getCapability(Capabilities.FluidHandler.BLOCK, masterPos.above(i + 1), null);
			if (h == null) return null;
			FluidStack out = outputs.get(i);
			if (h.fill(out, IFluidHandler.FluidAction.SIMULATE) < out.getAmount())
				return null;
			handlers.add(h);
		}
		return handlers;
	}
	private static Optional<DistillationRecipe> findRecipe(Level level, FluidStack input) {
		for (RecipeHolder<DistillationRecipe> holder : level.getRecipeManager().getAllRecipesFor(DifModRecipes.DISTILLATION_TYPE.get())) {
			if (holder.value().input().test(input)) {
				return Optional.of(holder.value());
			}
		}
		return Optional.empty();
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		DistillationTankBlockEntity master = isTowerMaster() ? this : getTowerMaster();
		if (master != null && (master.cachedHeatPoints > 0 || master.towerOutputCount > 0)) {
			ChatFormatting heatColor = master.cachedHeatPoints == 0 ? ChatFormatting.GRAY : ChatFormatting.AQUA;
			tooltip.add(Component.literal(goggleTooltipFix + "Heat: ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal(master.cachedHeatPoints + " / 10").withStyle(heatColor)));
			ChatFormatting speedColor = master.cachedSpeed == 0 ? ChatFormatting.GRAY : ChatFormatting.GOLD;
			tooltip.add(Component.literal(goggleTooltipFix + "Speed: ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal(master.cachedSpeed + "×").withStyle(speedColor)));
			if (isTowerMaster()) {
				tooltip.add(Component.literal(goggleTooltipFix + "Master").withStyle(ChatFormatting.AQUA));
			}
			return true;
		}
		return added;
	}
	@Override
	public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.putInt("dif_progress", progress);
		tag.putInt("dif_heatPoints", cachedHeatPoints);
		tag.putFloat("dif_speed", cachedSpeed);
		tag.putInt("dif_outputCount", towerOutputCount);
	}
	@Override
	public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		this.window = false;
		progress = tag.getInt("dif_progress");
		cachedHeatPoints = tag.getInt("dif_heatPoints");
		cachedSpeed = tag.getFloat("dif_speed");
		towerOutputCount = tag.getInt("dif_outputCount");
		if (clientPacket) {
			requestModelDataUpdate();
		}
	}
	@Override
	public void setWindows(boolean window) {
		super.setWindows(false);
	}
}