package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import cz.maxtechnik.dif.MultiblockHelper;
import cz.maxtechnik.dif.block.CokeOvenController;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.CokeOvenRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class CokeOvenControllerBlockEntity extends RandomizableContainerBlockEntity
		implements WorldlyContainer, IHaveGoggleInformation {

	// ── Pattern: ALL 26 surrounding blocks must be COKE_OVEN bricks ─────
	//   z=0 is the front face (where controller sits), z increases INTO structure.
	//   Inner position [y=1][x=1][z=1] is the solid center block – also COKE_OVEN.
	//   Controller position [y=1][x=1][z=0] is automatically skipped by MultiblockHelper.
	private static final Predicate<BlockState>[][][] PATTERN =
			MultiblockHelper.buildSolidShellPattern(
					MultiblockHelper.of(DifModBlocks.COKE_OVEN.get()),
					MultiblockHelper.of(DifModBlocks.COKE_OVEN.get())  // center is also a brick
			);

	// ── Inventory & Fluid ────────────────────────────────────────────────
	private final ItemStackHandler inventory = new ItemStackHandler(2) {
		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
		}
	};

	public ItemStackHandler getInventory() {
		return inventory;
	}

	public final FluidTank fluidTank = new FluidTank(8000) {
		@Override
		protected void onContentsChanged() {
			super.onContentsChanged();
			setChanged();
		}
	};

	// ── Progress tracking ────────────────────────────────────────────────
	private int progress = 0;
	private int totalTime = 0;

	public int getProgress()  { return progress; }
	public int getTotalTime() { return totalTime; }

	// ── Constructor ──────────────────────────────────────────────────────
	public CokeOvenControllerBlockEntity(BlockPos pos, BlockState blockState) {
		super(DifModBlockEntities.COKE_OVEN_CONTROLLER.get(), pos, blockState);
	}

	// ── Sync: every setChanged also pushes data to clients ──────────────
	@Override
	public void setChanged() {
		super.setChanged();
		if (level != null && !level.isClientSide) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	// ── Server tick ──────────────────────────────────────────────────────
	public static void serverTick(Level level, BlockPos pos, BlockState state, CokeOvenControllerBlockEntity be) {
		// FACING = direction controller's front-face looks (away from structure).
		// Direction INTO structure = FACING.getOpposite().
		Direction facing = state.getValue(CokeOvenController.FACING);
		Direction intoStructure = facing.getOpposite();

		boolean isCurrentlyFormed = MultiblockHelper.isValid(level, pos, intoStructure, PATTERN);
		boolean wasFormed = state.getValue(CokeOvenController.FORMED);

		if (isCurrentlyFormed != wasFormed) {
			if (isCurrentlyFormed) {
				// Check no brick is already claimed by a DIFFERENT controller
				if (!canClaimAllBricks(level, pos, intoStructure)) {
					isCurrentlyFormed = false;
				}
			}

			if (isCurrentlyFormed) {
				// Claim all bricks
				claimBricks(level, pos, intoStructure, pos);
			} else {
				// Release all bricks
				claimBricks(level, pos, intoStructure, null);
				be.progress = 0;
				be.totalTime = 0;
			}

			state = state.setValue(CokeOvenController.FORMED, isCurrentlyFormed)
					      .setValue(CokeOvenController.ACTIVE, false);
			level.setBlock(pos, state, 3);
			be.setChanged();
		}

		if (!isCurrentlyFormed) {
			return;
		}

		// ── Recipe processing ─────────────────────────────────────────────
		ItemStack input  = be.inventory.getStackInSlot(0);
		ItemStack output = be.inventory.getStackInSlot(1);

		if (input.isEmpty()) {
			if (be.progress > 0) {
				be.progress = 0;
				be.totalTime = 0;
				be.setChanged();
			}
			if (state.getValue(CokeOvenController.ACTIVE)) {
				level.setBlock(pos, state.setValue(CokeOvenController.ACTIVE, false), 3);
			}
			return;
		}

		Optional<CokeOvenRecipe> recipeOpt = findRecipe(level, input);
		if (recipeOpt.isEmpty()) {
			if (be.progress > 0) {
				be.progress = 0;
				be.totalTime = 0;
				be.setChanged();
			}
			if (state.getValue(CokeOvenController.ACTIVE)) {
				level.setBlock(pos, state.setValue(CokeOvenController.ACTIVE, false), 3);
			}
			return;
		}

		CokeOvenRecipe recipe = recipeOpt.get();
		be.totalTime = recipe.processingTime();

		boolean canOutputItem = output.isEmpty() ||
				(ItemStack.isSameItemSameComponents(output, recipe.result()) &&
				 output.getCount() + recipe.result().getCount() <= output.getMaxStackSize());

		boolean canOutputFluid = !recipe.hasFluidOutput() ||
				be.fluidTank.fill(recipe.fluidOutput(), IFluidHandler.FluidAction.SIMULATE)
						>= recipe.fluidOutput().getAmount();

		if (!canOutputItem || !canOutputFluid) {
			if (state.getValue(CokeOvenController.ACTIVE)) {
				level.setBlock(pos, state.setValue(CokeOvenController.ACTIVE, false), 3);
			}
			return;
		}

		if (!state.getValue(CokeOvenController.ACTIVE)) {
			state = state.setValue(CokeOvenController.ACTIVE, true);
			level.setBlock(pos, state, 3);
		}

		be.progress++;
		// Sync every 10 ticks so goggles feel responsive
		if (be.progress % 10 == 0) {
			be.setChanged();
		}

		if (be.progress >= be.totalTime) {
			be.progress = 0;

			// Consume input
			input.shrink(recipe.ingredientCount());
			be.inventory.setStackInSlot(0, input);

			// Produce item output
			if (output.isEmpty()) {
				be.inventory.setStackInSlot(1, recipe.result().copy());
			} else {
				output.grow(recipe.result().getCount());
				be.inventory.setStackInSlot(1, output);
			}

			// Produce fluid output
			if (recipe.hasFluidOutput()) {
				be.fluidTank.fill(recipe.fluidOutput(), IFluidHandler.FluidAction.EXECUTE);
			}

			be.setChanged();
		}
	}

	// ── Shared-wall helpers ──────────────────────────────────────────────

	/** Returns all 26 brick positions (controller pos is skipped). */
	private static List<BlockPos> getBrickPositions(BlockPos controllerPos, Direction intoStructure) {
		Direction right = intoStructure.getClockWise();
		List<BlockPos> list = new ArrayList<>(26);
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				for (int z = 0; z < 3; z++) {
					if (y == 1 && x == 1 && z == 0) continue; // skip controller
					BlockPos p = controllerPos
							.relative(intoStructure, z)
							.relative(right, x - 1)
							.above(y - 1);
					list.add(p);
				}
			}
		}
		return list;
	}

	/** Returns true only if every brick is unclaimed or already owned by this controller. */
	private static boolean canClaimAllBricks(Level level, BlockPos controllerPos, Direction intoStructure) {
		for (BlockPos brickPos : getBrickPositions(controllerPos, intoStructure)) {
			if (level.getBlockEntity(brickPos) instanceof CokeOvenBlockEntity brick) {
				if (!brick.canBeClaimedBy(controllerPos)) return false;
			}
		}
		return true;
	}

	/** Claims (owner = controllerPos) or releases (owner = null) all bricks. */
	private static void claimBricks(Level level, BlockPos controllerPos,
									Direction intoStructure, @Nullable BlockPos owner) {
		for (BlockPos brickPos : getBrickPositions(controllerPos, intoStructure)) {
			if (level.getBlockEntity(brickPos) instanceof CokeOvenBlockEntity brick) {
				brick.setControllerPos(owner);
			}
		}
	}

	// ── Recipe lookup ────────────────────────────────────────────────────

	private static Optional<CokeOvenRecipe> findRecipe(Level level, ItemStack input) {
		if (input.isEmpty()) return Optional.empty();
		for (RecipeHolder<CokeOvenRecipe> holder :
				level.getRecipeManager().getAllRecipesFor(DifModRecipes.COKE_OVEN_TYPE.get())) {
			if (holder.value().matches(input)) return Optional.of(holder.value());
		}
		return Optional.empty();
	}

	// ── Goggle tooltip ───────────────────────────────────────────────────

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		tooltip.add(Component.literal("◆ Coke Oven").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

		BlockState state = getBlockState();
		boolean isFormed = state.hasProperty(CokeOvenController.FORMED)
				&& state.getValue(CokeOvenController.FORMED);

		if (!isFormed) {
			tooltip.add(Component.literal(" Structure is NOT formed!").withStyle(ChatFormatting.RED));
			return true;
		}

		// Input slot
		ItemStack input = inventory.getStackInSlot(0);
		tooltip.add(Component.literal(" ▶ Input: ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal(
						input.isEmpty() ? "Empty" : input.getCount() + "x " + input.getHoverName().getString()
				).withStyle(input.isEmpty() ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE)));

		// Output slot
		ItemStack output = inventory.getStackInSlot(1);
		tooltip.add(Component.literal(" ▶ Output: ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal(
						output.isEmpty() ? "Empty" : output.getCount() + "x " + output.getHoverName().getString()
				).withStyle(output.isEmpty() ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE)));

		// Fluid
		if (!fluidTank.getFluid().isEmpty()) {
			tooltip.add(Component.literal(" ▶ Fluid: ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal(
							fluidTank.getFluid().getAmount() + "/" + fluidTank.getCapacity() + " mB "
							+ fluidTank.getFluid().getHoverName().getString()
					).withStyle(ChatFormatting.AQUA)));
		} else {
			tooltip.add(Component.literal(" ▶ Fluid: ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal("Empty").withStyle(ChatFormatting.DARK_GRAY)));
		}

		// Progress
		if (state.getValue(CokeOvenController.ACTIVE) && totalTime > 0) {
			int pct = (int) (((double) progress / totalTime) * 100.0);
			int secsLeft = Math.max(0, (totalTime - progress) / 20);
			tooltip.add(Component.literal(" ▶ Progress: ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal(pct + "% (" + secsLeft + "s left)")
							.withStyle(ChatFormatting.GREEN)));
		} else {
			tooltip.add(Component.literal(" ▶ Status: ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal("Idle").withStyle(ChatFormatting.YELLOW)));
		}

		return true;
	}

	// ── WorldlyContainer (sided automation) ──────────────────────────────

	@Override
	public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
		return IntStream.range(0, inventory.getSlots()).toArray();
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack itemStack, @Nullable Direction direction) {
		return index == 0;
	}

	@Override
	public boolean canTakeItemThroughFace(int index, @NotNull ItemStack itemStack, @NotNull Direction direction) {
		return index == 1;
	}

	@Override
	public boolean canPlaceItem(int index, @NotNull ItemStack itemStack) {
		return index == 0;
	}

	@Override
	public int getContainerSize() { return inventory.getSlots(); }

	@Override
	protected @NotNull Component getDefaultName() {
		return Component.translatable("container.dif.coke_oven");
	}

	@Override
	public @NotNull Component getDisplayName() {
		return Component.translatable("container.dif.coke_oven");
	}

	@Override
	protected @NotNull NonNullList<ItemStack> getItems() {
		NonNullList<ItemStack> list = NonNullList.withSize(inventory.getSlots(), ItemStack.EMPTY);
		for (int i = 0; i < inventory.getSlots(); i++) list.set(i, inventory.getStackInSlot(i));
		return list;
	}

	@Override
	protected void setItems(@NotNull NonNullList<ItemStack> stacks) {
		for (int i = 0; i < stacks.size() && i < inventory.getSlots(); i++)
			inventory.setStackInSlot(i, stacks.get(i));
	}

	@Override
	public @NotNull ItemStack getItem(int i) { return inventory.getStackInSlot(i); }

	@Override
	public void setItem(int i, @NotNull ItemStack itemStack) { inventory.setStackInSlot(i, itemStack); }

	@Override
	public @NotNull AbstractContainerMenu createMenu(int id, @NotNull Inventory inv) {
		return ChestMenu.threeRows(id, inv);
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < inventory.getSlots(); i++)
			if (!inventory.getStackInSlot(i).isEmpty()) return false;
		return true;
	}

	// ── NBT ─────────────────────────────────────────────────────────────

	@Override
	protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		if (tag.contains("inventory"))
			inventory.deserializeNBT(provider, tag.getCompound("inventory"));
		if (tag.get("fluidTank") instanceof CompoundTag fluidTag)
			fluidTank.readFromNBT(provider, fluidTag);
		this.progress  = tag.getInt("progress");
		this.totalTime = tag.getInt("totalTime");
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		tag.put("inventory", inventory.serializeNBT(provider));
		tag.put("fluidTank", fluidTank.writeToNBT(provider, new CompoundTag()));
		tag.putInt("progress",  this.progress);
		tag.putInt("totalTime", this.totalTime);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider) {
		return this.saveWithFullMetadata(provider);
	}
}
