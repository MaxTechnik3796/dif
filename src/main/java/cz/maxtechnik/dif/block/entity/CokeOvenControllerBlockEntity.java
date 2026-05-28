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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
public class CokeOvenControllerBlockEntity extends RandomizableContainerBlockEntity
		implements WorldlyContainer, IHaveGoggleInformation{
	private static final Predicate<BlockState>[][][] PATTERN=MultiblockHelper.buildSolidShellPattern(MultiblockHelper.of(DifModBlocks.COKE_OVEN.get()),MultiblockHelper.of(DifModBlocks.COKE_OVEN.get()));
	private static final int FORMED_REVALIDATE_PERIOD=40;
	private static final int UNFORMED_REVALIDATE_PERIOD=20;
	private static final int SLOT_INPUT=0;
	private static final int SLOT_OUTPUT=1;
	private static final int[] SLOTS_ALL={SLOT_INPUT,SLOT_OUTPUT};
	private final ItemStackHandler inventory=new ItemStackHandler(2){
		@Override
		protected void onContentsChanged(int slot){
			if(slot==SLOT_INPUT) cachedRecipe=null;
			setChanged();
		}
	};
	public final FluidTank fluidTank=new FluidTank(8000){
		@Override
		protected void onContentsChanged(){
			super.onContentsChanged();
			setChanged();
		}
	};
	public ItemStackHandler getInventory(){
		return inventory;
	}
	private int progress=0;
	private int totalTime=0;
	public boolean forceValidation=true;
	private final int tickOffset=(int)(Math.random()*UNFORMED_REVALIDATE_PERIOD);
	private boolean isConflicted=false;
	@Nullable
	private transient CokeOvenRecipe cachedRecipe;
	public CokeOvenControllerBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.COKE_OVEN_CONTROLLER.get(),pos,blockState);
	}
	@Override
	public void setChanged(){
		super.setChanged();
		if(level!=null&&!level.isClientSide) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
	}
	public static void serverTick(Level level,BlockPos pos,BlockState blockState,CokeOvenControllerBlockEntity blockEntity){
		final Direction intoStructure=blockState.getValue(CokeOvenController.FACING).getOpposite();
		final boolean wasFormed=blockState.getValue(CokeOvenController.FORMED);
		final long gameTime=level.getGameTime()+blockEntity.tickOffset;
		final int period=wasFormed?FORMED_REVALIDATE_PERIOD:UNFORMED_REVALIDATE_PERIOD;
		final boolean shouldValidate=blockEntity.forceValidation||gameTime%period==0;
		boolean isFormed=wasFormed;
		if(shouldValidate){
			blockEntity.forceValidation=false;
			isFormed=MultiblockHelper.isValid(level,pos,intoStructure,PATTERN);
			if(isFormed&&!wasFormed){
				if(!canClaimAllBricks(level,pos,intoStructure)){
					isFormed=false;
					if(!blockEntity.isConflicted){
						blockEntity.isConflicted=true;
						blockEntity.setChanged();
					}
				}
			}
		}
		if(isFormed!=wasFormed){
			if(isFormed){
				claimBricks(level,pos,intoStructure,pos);
				blockEntity.isConflicted=false;
			}else{
				claimBricks(level,pos,intoStructure,null);
				blockEntity.progress=0;
				blockEntity.totalTime=0;
				blockEntity.cachedRecipe=null;
			}
			blockState=blockState.setValue(CokeOvenController.FORMED,isFormed).setValue(CokeOvenController.ACTIVE,false);
			level.setBlock(pos,blockState,3);
			blockEntity.setChanged();
		}
		if(!isFormed) return;
		final ItemStack input=blockEntity.inventory.getStackInSlot(SLOT_INPUT);
		if(input.isEmpty()){
			blockEntity.resetProgressAndDeactivate(level,pos,blockState);
			return;
		}
		final CokeOvenRecipe recipe=blockEntity.getRecipeFor(level,input);
		if(recipe==null){
			blockEntity.resetProgressAndDeactivate(level,pos,blockState);
			return;
		}
		blockEntity.totalTime=recipe.processingTime();
		final ItemStack output=blockEntity.inventory.getStackInSlot(SLOT_OUTPUT);
		final ItemStack result=recipe.result();
		final boolean canOutputItem=output.isEmpty()||(ItemStack.isSameItemSameComponents(output,result)&&output.getCount()+result.getCount()<=output.getMaxStackSize());
		final boolean canOutputFluid=!recipe.hasFluidOutput()||blockEntity.fluidTank.fill(recipe.fluidOutput(),IFluidHandler.FluidAction.SIMULATE)>=recipe.fluidOutput().getAmount();
		if(!canOutputItem||!canOutputFluid){
			blockEntity.setActive(level,pos,blockState,false);
			return;
		}
		blockEntity.setActive(level,pos,blockState,true);
		blockEntity.progress++;
		if(blockEntity.progress>=blockEntity.totalTime){
			finishRecipe(blockEntity,recipe,input,output,result);
		}else if(blockEntity.progress%10==0) blockEntity.setChanged();
	}
	private static void finishRecipe(CokeOvenControllerBlockEntity blockEntity,CokeOvenRecipe recipe,ItemStack input,ItemStack output,ItemStack result){
		blockEntity.progress=0;
		input.shrink(recipe.ingredientCount());
		blockEntity.inventory.setStackInSlot(SLOT_INPUT,input);
		if(output.isEmpty()) blockEntity.inventory.setStackInSlot(SLOT_OUTPUT,result.copy());
		else{
			output.grow(result.getCount());
			blockEntity.inventory.setStackInSlot(SLOT_OUTPUT,output);
		}
		if(recipe.hasFluidOutput()) blockEntity.fluidTank.fill(recipe.fluidOutput(),IFluidHandler.FluidAction.EXECUTE);
		blockEntity.setChanged();
	}
	private void resetProgressAndDeactivate(Level level,BlockPos pos,BlockState blockState){
		if(progress!=0||totalTime!=0){
			progress=0;
			totalTime=0;
			setChanged();
		}
		setActive(level,pos,blockState,false);
	}
	private void setActive(Level level,BlockPos pos,BlockState state,boolean active){
		if(state.getValue(CokeOvenController.ACTIVE)!=active) level.setBlock(pos,state.setValue(CokeOvenController.ACTIVE,active),3);
	}
	@FunctionalInterface
	private interface BrickVisitor{
		boolean visit(BlockPos.MutableBlockPos pos);
	}
	private static void forEachBrick(BlockPos controllerPos,Direction intoStructure,BrickVisitor visitor){
		final Direction right=intoStructure.getClockWise();
		final BlockPos.MutableBlockPos mp=new BlockPos.MutableBlockPos();
		for(int y=0;y<3;y++){
			for(int x=0;x<3;x++){
				for(int z=0;z<3;z++){
					if(y==1&&x==1&&z==0) continue;
					mp.set(controllerPos).move(intoStructure,z).move(right,x-1).move(Direction.UP,y-1);
					if(!visitor.visit(mp)) return;
				}
			}
		}
	}
	private static boolean canClaimAllBricks(Level level,BlockPos controllerPos,Direction intoStructure){
		final boolean[] ok={true};
		forEachBrick(controllerPos,intoStructure,mutableBlockPos->{
			if(level.getBlockEntity(mutableBlockPos) instanceof CokeOvenBlockEntity brick&&!brick.canBeClaimedBy(controllerPos)){
				ok[0]=false;
				return false;
			}
			return true;
		});
		return ok[0];
	}
	private static void claimBricks(Level level,BlockPos controllerPos,Direction intoStructure,@Nullable BlockPos owner){
		forEachBrick(controllerPos,intoStructure,mutableBlockPos->{
			if(level.getBlockEntity(mutableBlockPos) instanceof CokeOvenBlockEntity brick) brick.setControllerPos(owner);
			return true;
		});
	}
	@Nullable
	private CokeOvenRecipe getRecipeFor(Level level,ItemStack input){
		final CokeOvenRecipe cached=cachedRecipe;
		if(cached!=null&&cached.matches(input)) return cached;
		final List<RecipeHolder<CokeOvenRecipe>> all=level.getRecipeManager().getAllRecipesFor(DifModRecipes.COKE_OVEN_TYPE.get());
		for(RecipeHolder<CokeOvenRecipe> holder: all){
			if(holder.value().matches(input)){
				cachedRecipe=holder.value();
				return cachedRecipe;
			}
		}
		cachedRecipe=null;
		return null;
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip,boolean isPlayerSneaking){
		tooltip.add(Component.literal("◆ Coke Oven").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD));
		final BlockState state=getBlockState();
		final boolean formed=state.hasProperty(CokeOvenController.FORMED)
				&&state.getValue(CokeOvenController.FORMED);
		if(!formed){
			tooltip.add(isConflicted?Component.literal(" ⚠ Structure already use some blocks").withStyle(ChatFormatting.DARK_RED):Component.literal(" Structure is NOT formed!").withStyle(ChatFormatting.RED));
			return true;
		}
		appendSlot(tooltip," ▶ Input: ",inventory.getStackInSlot(SLOT_INPUT));
		appendSlot(tooltip," ▶ Output: ",inventory.getStackInSlot(SLOT_OUTPUT));
		final FluidStack fluid=fluidTank.getFluid();
		if(!fluid.isEmpty()) tooltip.add(Component.literal(" ▶ Fluid: ").withStyle(ChatFormatting.GRAY).append(Component.literal(fluid.getAmount()+"/"+fluidTank.getCapacity()+" mB "+fluid.getHoverName().getString()).withStyle(ChatFormatting.AQUA)));
		else tooltip.add(Component.literal(" ▶ Fluid: ").withStyle(ChatFormatting.GRAY).append(Component.literal("Empty").withStyle(ChatFormatting.DARK_GRAY)));
		if(state.getValue(CokeOvenController.ACTIVE)&&totalTime>0){
			int pct=(int)(((double)progress/totalTime)*100.0);
			int secsLeft=Math.max(0,(totalTime-progress)/20);
			tooltip.add(Component.literal(" ▶ Progress: ").withStyle(ChatFormatting.GRAY).append(Component.literal(pct+"% ("+secsLeft+"s left)").withStyle(ChatFormatting.GREEN)));
		}else tooltip.add(Component.literal(" ▶ Status: ").withStyle(ChatFormatting.GRAY).append(Component.literal("Idle").withStyle(ChatFormatting.YELLOW)));
		return true;
	}
	private static void appendSlot(List<Component> tooltip,String label,ItemStack stack){
		tooltip.add(Component.literal(label).withStyle(ChatFormatting.GRAY).append(Component.literal(stack.isEmpty()?"Empty":stack.getCount()+"x "+stack.getHoverName().getString()).withStyle(stack.isEmpty()?ChatFormatting.DARK_GRAY:ChatFormatting.WHITE)));
	}
	@Override
	public int @NotNull [] getSlotsForFace(@NotNull Direction side){
		return SLOTS_ALL;
	}
	@Override
	public boolean canPlaceItemThroughFace(int index,@NotNull ItemStack itemStack,@Nullable Direction side){
		return index==SLOT_INPUT;
	}
	@Override
	public boolean canTakeItemThroughFace(int index,@NotNull ItemStack itemStack,@NotNull Direction side){
		return index==SLOT_OUTPUT;
	}
	@Override
	public boolean canPlaceItem(int index,@NotNull ItemStack itemStack){
		return index==SLOT_INPUT;
	}
	@Override
	public int getContainerSize(){
		return inventory.getSlots();
	}
	@Override
	public @NotNull ItemStack getItem(int index){
		return inventory.getStackInSlot(index);
	}
	@Override
	public void setItem(int index,@NotNull ItemStack itemStack){
		inventory.setStackInSlot(index,itemStack);
	}
	@Override
	public @NotNull ItemStack removeItem(int slot,int amount){
		return inventory.extractItem(slot,amount,false);
	}
	@Override
	public @NotNull ItemStack removeItemNoUpdate(int index){
		ItemStack stack=inventory.getStackInSlot(index);
		inventory.setStackInSlot(index,ItemStack.EMPTY);
		return stack;
	}
	@Override
	public boolean isEmpty(){
		for(int i=0;i<inventory.getSlots();i++) if(!inventory.getStackInSlot(i).isEmpty()) return false;
		return true;
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
	protected @NotNull Component getDefaultName(){
		return Component.translatable("container.dif.coke_oven");
	}
	@Override
	public @NotNull Component getDisplayName(){
		return Component.translatable("container.dif.coke_oven");
	}
	@Override
	public @NotNull AbstractContainerMenu createMenu(int id,@NotNull Inventory inventory){
		return ChestMenu.threeRows(id,inventory);
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(tag,provider);
		if(tag.contains("inventory")) inventory.deserializeNBT(provider,tag.getCompound("inventory"));
		if(tag.get("fluidTank") instanceof CompoundTag fluidTag) fluidTank.readFromNBT(provider,fluidTag);
		progress=tag.getInt("progress");
		totalTime=tag.getInt("totalTime");
		isConflicted=tag.getBoolean("isConflicted");
		cachedRecipe=null;
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.saveAdditional(tag,provider);
		tag.put("inventory",inventory.serializeNBT(provider));
		tag.put("fluidTank",fluidTank.writeToNBT(provider,new CompoundTag()));
		tag.putInt("progress",progress);
		tag.putInt("totalTime",totalTime);
		tag.putBoolean("isConflicted",isConflicted);
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider){
		return saveWithFullMetadata(provider);
	}
	public boolean handleInteraction(Player player,InteractionHand hand){
		if(level==null||level.isClientSide) return true;
		final ItemStack held=player.getItemInHand(hand);
		if(held.getItem()==Items.BUCKET){
			tryFillBucket(player,hand,held);
			return true;
		}
		if(!held.isEmpty()){
			final ItemStack currentInput=inventory.getStackInSlot(SLOT_INPUT);
			if(currentInput.isEmpty()||ItemStack.isSameItemSameComponents(currentInput,held)){
				final ItemStack remaining=inventory.insertItem(SLOT_INPUT,held.copy(),false);
				player.setItemInHand(hand,remaining);
			}else{
				player.setItemInHand(hand,currentInput);
				inventory.setStackInSlot(SLOT_INPUT,held.copy());
			}
		}else{
			ItemStack out=inventory.getStackInSlot(SLOT_OUTPUT);
			if(!out.isEmpty()){
				player.setItemInHand(hand,out.copy());
				inventory.setStackInSlot(SLOT_OUTPUT,ItemStack.EMPTY);
			}else{
				ItemStack in=inventory.getStackInSlot(SLOT_INPUT);
				if(!in.isEmpty()){
					player.setItemInHand(hand,in.copy());
					inventory.setStackInSlot(SLOT_INPUT,ItemStack.EMPTY);
				}
			}
		}
		setChanged();
		return true;
	}
	private void tryFillBucket(Player player,InteractionHand hand,ItemStack heldBucket){
		if(fluidTank.getFluidAmount()<1000) return;
		final FluidStack drained=fluidTank.drain(1000,IFluidHandler.FluidAction.EXECUTE);
		if(drained.isEmpty()) return;
		heldBucket.shrink(1);
		final ItemStack filled=new ItemStack(drained.getFluid().getBucket());
		if(heldBucket.isEmpty()) player.setItemInHand(hand,filled);
		else if(!player.getInventory().add(filled)) player.drop(filled,false);
		setChanged();
	}
}