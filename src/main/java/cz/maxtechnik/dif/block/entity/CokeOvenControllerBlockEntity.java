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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class CokeOvenControllerBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, IHaveGoggleInformation {
	public CokeOvenControllerBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.COKE_OVEN_CONTROLLER.get(),pos,blockState);
	}
	private final ItemStackHandler inventory=new ItemStackHandler(2){
		@Override
		protected void onContentsChanged(int slot){
			setChanged();
		}
	};
	public ItemStackHandler getInventory(){
		return inventory;
	}
	public final FluidTank fluidTank=new FluidTank(8000){
		@Override
		protected void onContentsChanged(){
			super.onContentsChanged();
			setChanged();
			if(level!=null)
				level.sendBlockUpdated(worldPosition,level.getBlockState(worldPosition),level.getBlockState(worldPosition),2);
		}
	};

	private static final java.util.function.Predicate<BlockState>[][][] PATTERN =
			MultiblockHelper.buildSolidShellPattern(
					MultiblockHelper.of(DifModBlocks.COKE_OVEN.get()),
					MultiblockHelper.AIR
			);

	private int progress = 0;
	private int totalTime = 0;

	public int getProgress() {
		return progress;
	}

	public int getTotalTime() {
		return totalTime;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, CokeOvenControllerBlockEntity be) {
		boolean isCurrentlyFormed = MultiblockHelper.isValid(level, pos, state.getValue(CokeOvenController.FACING).getOpposite(), PATTERN);
		boolean wasFormed = state.getValue(CokeOvenController.FORMED);

		if (isCurrentlyFormed != wasFormed) {
			state = state.setValue(CokeOvenController.FORMED, isCurrentlyFormed);
			level.setBlock(pos, state, 3);
			if (!isCurrentlyFormed) {
				be.progress = 0;
				be.totalTime = 0;
				be.setChanged();
				level.sendBlockUpdated(pos, state, state, 2);
			}
		}

		if (!isCurrentlyFormed) {
			if (state.getValue(CokeOvenController.ACTIVE)) {
				level.setBlock(pos, state.setValue(CokeOvenController.ACTIVE, false), 3);
			}
			return;
		}

		ItemStack input = be.inventory.getStackInSlot(0);
		ItemStack output = be.inventory.getStackInSlot(1);

		if (input.isEmpty()) {
			if (be.progress > 0) {
				be.progress = 0;
				be.totalTime = 0;
				be.setChanged();
				level.sendBlockUpdated(pos, state, state, 2);
			}
			if (state.getValue(CokeOvenController.ACTIVE)) {
				level.setBlock(pos, state.setValue(CokeOvenController.ACTIVE, false), 3);
			}
			return;
		}

		Optional<CokeOvenRecipe> recipeOpt = findRecipe(level, input);
		if (recipeOpt.isPresent()) {
			CokeOvenRecipe recipe = recipeOpt.get();
			be.totalTime = recipe.processingTime();

			boolean canOutputItem = output.isEmpty() || 
					(ItemStack.isSameItemSameComponents(output, recipe.result()) && 
					 output.getCount() + recipe.result().getCount() <= output.getMaxStackSize());
			
			boolean canOutputFluid = !recipe.hasFluidOutput() || 
					be.fluidTank.fill(recipe.fluidOutput(), IFluidHandler.FluidAction.SIMULATE) >= recipe.fluidOutput().getAmount();

			if (canOutputItem && canOutputFluid) {
				if (!state.getValue(CokeOvenController.ACTIVE)) {
					state = state.setValue(CokeOvenController.ACTIVE, true);
					level.setBlock(pos, state, 3);
				}

				be.progress++;
				be.setChanged();

				if (be.progress % 10 == 0 || be.progress >= be.totalTime) {
					level.sendBlockUpdated(pos, state, state, 2);
				}

				if (be.progress >= be.totalTime) {
					be.progress = 0;
					
					input.shrink(recipe.ingredientCount());
					be.inventory.setStackInSlot(0, input);

					if (output.isEmpty()) {
						be.inventory.setStackInSlot(1, recipe.result().copy());
					} else {
						output.grow(recipe.result().getCount());
						be.inventory.setStackInSlot(1, output);
					}

					if (recipe.hasFluidOutput()) {
						be.fluidTank.fill(recipe.fluidOutput(), IFluidHandler.FluidAction.EXECUTE);
					}

					be.setChanged();
					level.sendBlockUpdated(pos, state, state, 2);
				}
			} else {
				if (state.getValue(CokeOvenController.ACTIVE)) {
					level.setBlock(pos, state.setValue(CokeOvenController.ACTIVE, false), 3);
				}
			}
		} else {
			if (be.progress > 0) {
				be.progress = 0;
				be.totalTime = 0;
				be.setChanged();
				level.sendBlockUpdated(pos, state, state, 2);
			}
			if (state.getValue(CokeOvenController.ACTIVE)) {
				level.setBlock(pos, state.setValue(CokeOvenController.ACTIVE, false), 3);
			}
		}
	}

	private static Optional<CokeOvenRecipe> findRecipe(Level level, ItemStack input) {
		if (input.isEmpty()) return Optional.empty();
		for (RecipeHolder<CokeOvenRecipe> holder :
				level.getRecipeManager().getAllRecipesFor(DifModRecipes.COKE_OVEN_TYPE.get())) {
			if (holder.value().matches(input)) return Optional.of(holder.value());
		}
		return Optional.empty();
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		tooltip.add(Component.literal("◆ Coke Oven").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

		BlockState state = getBlockState();
		boolean isFormed = state.hasProperty(CokeOvenController.FORMED) && state.getValue(CokeOvenController.FORMED);

		if (!isFormed) {
			tooltip.add(Component.literal(" Structure is NOT formed!").withStyle(ChatFormatting.RED));
			return true;
		}

		ItemStack input = inventory.getStackInSlot(0);
		if (!input.isEmpty()) {
			tooltip.add(Component.literal(" Input: ")
					.withStyle(ChatFormatting.GRAY)
					.append(Component.literal(input.getCount() + "x " + input.getHoverName().getString())
							.withStyle(ChatFormatting.WHITE)));
		} else {
			tooltip.add(Component.literal(" Input: ")
					.withStyle(ChatFormatting.GRAY)
					.append(Component.literal("Empty")
							.withStyle(ChatFormatting.DARK_GRAY)));
		}

		ItemStack output = inventory.getStackInSlot(1);
		if (!output.isEmpty()) {
			tooltip.add(Component.literal(" Output: ")
					.withStyle(ChatFormatting.GRAY)
					.append(Component.literal(output.getCount() + "x " + output.getHoverName().getString())
							.withStyle(ChatFormatting.WHITE)));
		} else {
			tooltip.add(Component.literal(" Output: ")
					.withStyle(ChatFormatting.GRAY)
					.append(Component.literal("Empty")
							.withStyle(ChatFormatting.DARK_GRAY)));
		}

		if (!fluidTank.getFluid().isEmpty()) {
			tooltip.add(Component.literal(" Fluid: ")
					.withStyle(ChatFormatting.GRAY)
					.append(Component.literal(fluidTank.getFluid().getAmount() + " / " + fluidTank.getCapacity() + " mB " + fluidTank.getFluid().getHoverName().getString())
							.withStyle(ChatFormatting.WHITE)));
		} else {
			tooltip.add(Component.literal(" Fluid: ")
					.withStyle(ChatFormatting.GRAY)
					.append(Component.literal("Empty")
							.withStyle(ChatFormatting.DARK_GRAY)));
		}

		if (state.getValue(CokeOvenController.ACTIVE) && totalTime > 0) {
			int pct = (int) (((double) progress / totalTime) * 100.0);
			tooltip.add(Component.literal(" Progress: ")
					.withStyle(ChatFormatting.GRAY)
					.append(Component.literal(pct + "% (" + (totalTime - progress) / 20 + "s left)")
							.withStyle(ChatFormatting.GREEN)));
		} else {
			tooltip.add(Component.literal(" Status: ")
					.withStyle(ChatFormatting.GRAY)
					.append(Component.literal("Idle")
							.withStyle(ChatFormatting.YELLOW)));
		}

		return true;
	}

	@Override
	public int @NotNull [] getSlotsForFace(@NotNull Direction side){
		return IntStream.range(0,inventory.getSlots()).toArray();
	}
	@Override
	public boolean canPlaceItemThroughFace(int index,@NotNull ItemStack itemStack,@Nullable Direction direction){
		return index==0;
	}
	@Override
	public boolean canTakeItemThroughFace(int index,@NotNull ItemStack itemStack,@NotNull Direction direction){
		return index==1;
	}
	@Override
	public boolean canPlaceItem(int index,@NotNull ItemStack itemStack){
		return index==0;
	}
	@Override
	public int getContainerSize(){
		return inventory.getSlots();
	}
	@Override
	protected @NotNull Component getDefaultName(){
		return Component.translatable("container.dif.coke_oven");
	}
	@Override
	public @NotNull Component getDisplayName(){
		return Component.translatable("container.dif.coke_oven");
	}
	@Override
	protected @NotNull NonNullList<ItemStack> getItems(){
		NonNullList<ItemStack> list=NonNullList.withSize(inventory.getSlots(),ItemStack.EMPTY);
		for(int i=0;i<inventory.getSlots();i++) list.set(i,inventory.getStackInSlot(i));
		return list;
	}
	@Override
	protected void setItems(@NotNull NonNullList<ItemStack> stacks){
		for(int i=0;i<stacks.size()&&i<inventory.getSlots();i++)
			inventory.setStackInSlot(i,stacks.get(i));
	}
	@Override
	public @NotNull ItemStack getItem(int i){
		return inventory.getStackInSlot(i);
	}
	@Override
	public void setItem(int i,@NotNull ItemStack itemStack){
		inventory.setStackInSlot(i,itemStack);
	}
	@Override
	public @NotNull AbstractContainerMenu createMenu(int id,@NotNull Inventory inventory){
		return ChestMenu.threeRows(id,inventory);
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(tag,provider);
		if(tag.contains("inventory"))
			inventory.deserializeNBT(provider,tag.getCompound("inventory"));
		if(tag.get("fluidTank") instanceof CompoundTag fluidTag)
			fluidTank.readFromNBT(provider,fluidTag);
		this.progress = tag.getInt("progress");
		this.totalTime = tag.getInt("totalTime");
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.saveAdditional(tag,provider);
		tag.put("inventory",inventory.serializeNBT(provider));
		tag.put("fluidTank",fluidTank.writeToNBT(provider,new CompoundTag()));
		tag.putInt("progress", this.progress);
		tag.putInt("totalTime", this.totalTime);
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider){
		return this.saveWithFullMetadata(provider);
	}
	@Override
	public boolean isEmpty(){
		for(int i=0;i<inventory.getSlots();i++)
			if(!inventory.getStackInSlot(i).isEmpty()) return false;
		return true;
	}
}
