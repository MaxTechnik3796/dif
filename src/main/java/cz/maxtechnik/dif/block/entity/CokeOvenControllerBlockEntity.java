package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.CokeOvenController;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.init.other.DifModRecipes;
import cz.maxtechnik.dif.recipe.CokeOvenRecipe;
import cz.maxtechnik.dif.util.MultiblockHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static cz.maxtechnik.dif.DifMod.goggleTooltipFix;
public class CokeOvenControllerBlockEntity extends AbstractMultiblockControllerBlockEntity<CokeOvenRecipe>{
	// ── Vzor struktury ────────────────────────────────────────────────────────
	private static final Predicate<BlockState>[][][] PATTERN=MultiblockHelper.buildSolidShellPattern(
			MultiblockHelper.of(DifModBlocks.COKE_OVEN.get()),
			MultiblockHelper.of(DifModBlocks.COKE_OVEN.get())
	);
	// ── Fluid tank (pouze výstupní, CokeOven nepotřebuje vstupní fluid) ───────
	public final FluidTank fluidTank=new FluidTank(8000){
		@Override
		protected void onContentsChanged(){
			super.onContentsChanged();
			setChanged();
		}
	};
	// ── Konstruktor ───────────────────────────────────────────────────────────
	public CokeOvenControllerBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.COKE_OVEN_CONTROLLER.get(),pos,blockState);
	}
	// ── Ticker factory (voláno z CokeOvenController bloku) ───────────────────
	public static <T extends net.minecraft.world.level.block.entity.BlockEntity>
	BlockEntityTicker<T> ticker(BlockEntityType<T> type){
		BlockEntityType<CokeOvenControllerBlockEntity> expected=DifModBlockEntities.COKE_OVEN_CONTROLLER.get();
		return type.equals(expected)
				?(lvl,pos,state,be)->((CokeOvenControllerBlockEntity)be).tick(lvl,pos,state)
				:null;
	}
	// ── AbstractMultiblockControllerBlockEntity – implementace ────────────────
	@Override
	protected Predicate<BlockState>[][][] getPattern(){
		return PATTERN;
	}
	@Override
	protected DirectionProperty getFacingProperty(){
		return CokeOvenController.FACING;
	}
	@Override
	protected BooleanProperty getFormedProperty(){
		return CokeOvenController.FORMED;
	}
	@Override
	protected BooleanProperty getActiveProperty(){
		return CokeOvenController.ACTIVE;
	}
	@Override
	protected Component getGoggleName(){
		return Component.literal("◆ Coke Oven");
	}
	@Override
	protected ChatFormatting getGoggleNameColor(){
		return ChatFormatting.GOLD;
	}
	// ── Validace vstupu ───────────────────────────────────────────────────────
	@Override
	protected boolean hasValidInput(){
		return !inventory.getStackInSlot(SLOT_INPUT).isEmpty();
	}
	// ── Recipe ────────────────────────────────────────────────────────────────
	@Override
	protected @Nullable CokeOvenRecipe findRecipe(Level level){
		final ItemStack input=inventory.getStackInSlot(SLOT_INPUT);
		if(cachedRecipe!=null&&cachedRecipe.matches(input)) return cachedRecipe;
		for(var holder: level.getRecipeManager().getAllRecipesFor(DifModRecipes.COKE_OVEN_TYPE.get())){
			if(holder.value().matches(input)){
				cachedRecipe=holder.value();
				return cachedRecipe;
			}
		}
		cachedRecipe=null;
		return null;
	}
	@Override
	protected int getProcessingTime(CokeOvenRecipe recipe){
		return recipe.processingTime();
	}
	@Override
	protected boolean canOutput(CokeOvenRecipe recipe){
		final ItemStack output=inventory.getStackInSlot(SLOT_OUTPUT);
		final ItemStack result=recipe.result();
		boolean canItem=output.isEmpty()
				||(ItemStack.isSameItemSameComponents(output,result)
				&&output.getCount()+result.getCount()<=output.getMaxStackSize());
		boolean canFluid=!recipe.hasFluidOutput()
				||fluidTank.fill(recipe.fluidOutput(),IFluidHandler.FluidAction.SIMULATE)>=recipe.fluidOutput().getAmount();
		return canItem&&canFluid;
	}
	@Override
	protected void finishRecipe(CokeOvenRecipe recipe){
		// Spotřebuj vstup
		ItemStack input=inventory.getStackInSlot(SLOT_INPUT);
		input.shrink(recipe.ingredientCount());
		inventory.setStackInSlot(SLOT_INPUT,input);
		// Vlož výstupní item
		ItemStack output=inventory.getStackInSlot(SLOT_OUTPUT);
		ItemStack result=recipe.result().copy();
		if(output.isEmpty()){
			inventory.setStackInSlot(SLOT_OUTPUT,result);
		}else{
			output.grow(result.getCount());
			inventory.setStackInSlot(SLOT_OUTPUT,output);
		}
		// Vlož výstupní fluid
		if(recipe.hasFluidOutput()){
			fluidTank.fill(recipe.fluidOutput(),IFluidHandler.FluidAction.EXECUTE);
		}
	}
	// ── Claiming ──────────────────────────────────────────────────────────────
	@Override
	protected boolean brickCanBeClaimedBy(Level level,BlockPos brickPos,BlockPos controllerPos){
		return !(level.getBlockEntity(brickPos) instanceof CokeOvenBlockEntity brick)
				||brick.canBeClaimedBy(controllerPos);
	}
	@Override
	protected void setBrickController(Level level,BlockPos brickPos,@Nullable BlockPos owner){
		if(level.getBlockEntity(brickPos) instanceof CokeOvenBlockEntity brick){
			brick.setControllerPos(owner);
		}
	}
	// ── Kbelík ────────────────────────────────────────────────────────────────
	@Override
	protected void tryFillBucket(Player player,InteractionHand hand,ItemStack heldBucket){
		if(fluidTank.getFluidAmount()<1000) return;
		final var drained=fluidTank.drain(1000,IFluidHandler.FluidAction.EXECUTE);
		if(drained.isEmpty()) return;
		heldBucket.shrink(1);
		final ItemStack filled=new ItemStack(drained.getFluid().getBucket());
		if(heldBucket.isEmpty()) player.setItemInHand(hand,filled);
		else if(!player.getInventory().add(filled)) player.drop(filled,false);
		setChanged();
	}
	// ── Goggle tooltip ────────────────────────────────────────────────────────
	@Override
	protected void appendFormedTooltip(List<Component> tooltip){
		appendItemSlot(tooltip,goggleTooltipFix+" ▶ Input: ",inventory.getStackInSlot(SLOT_INPUT));
		appendItemSlot(tooltip,goggleTooltipFix+" ▶ Output: ",inventory.getStackInSlot(SLOT_OUTPUT));
		appendFluidSlot(tooltip,goggleTooltipFix+" ▶ Fluid: ",fluidTank);
	}
	@Override
	public @Nullable IFluidHandler getFluidCapability(@Nullable net.minecraft.core.Direction side){
		return fluidTank;
	}
	// ── Display name ──────────────────────────────────────────────────────────
	@Override
	protected @NotNull Component getDefaultName(){
		return Component.translatable("container.dif.coke_oven");
	}
	@Override
	public @NotNull Component getDisplayName(){
		return Component.translatable("container.dif.coke_oven");
	}
	// ── NBT – fluid tank ──────────────────────────────────────────────────────
	@Override
	protected void saveExtraData(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		tag.put("fluidTank",fluidTank.writeToNBT(provider,new CompoundTag()));
	}
	@Override
	protected void loadExtraData(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		if(tag.get("fluidTank") instanceof CompoundTag t) fluidTank.readFromNBT(provider,t);
	}
}