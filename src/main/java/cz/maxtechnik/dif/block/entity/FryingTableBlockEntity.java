package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.FryingTable;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.recipes.FryingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class FryingTableBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer{
	public static final int SLOTS=2;
	public static final int INPUT_SLOT=0;
	public static final int OUTPUT_SLOT=1;
	public int progress=0;
	public NonNullList<ItemStack> stacks=NonNullList.withSize(SLOTS,ItemStack.EMPTY);
	private final LazyOptional<? extends IItemHandler>[] handlers=SidedInvWrapper.create(this,Direction.values());
	public FryingTableBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.FRYING_TABLE.get(),pos,blockState);
	}
	@Override
	public int @NotNull [] getSlotsForFace(@NotNull Direction side){
		return new int[]{INPUT_SLOT,OUTPUT_SLOT};
	}
	@Override
	public boolean canPlaceItemThroughFace(int index,@NotNull ItemStack itemStack,@Nullable Direction pDirection){
		return index==INPUT_SLOT;
	}
	@Override
	public boolean canTakeItemThroughFace(int index,@NotNull ItemStack itemStack,@NotNull Direction direction){
		return index==OUTPUT_SLOT;
	}
	@Override
	protected @NotNull NonNullList<ItemStack> getItems(){
		return this.stacks;
	}
	@Override
	protected void setItems(@NotNull NonNullList<ItemStack> itemStacks){
		this.stacks=itemStacks;
	}
	@Override
	protected @NotNull Component getDefaultName(){
		return Component.literal("Frying Table");
	}
	@Override
	public @NotNull AbstractContainerMenu createMenu(int id,@NotNull Inventory inventory){
		return ChestMenu.threeRows(id,inventory);
	}
	@Override
	public int getContainerSize(){
		return SLOTS;
	}
	public final FluidTank fluidTank=new FluidTank(1000,fs->fs.getFluid()==DifModFluids.SUNFLOWER_OIL.get()){
		@Override
		protected void onContentsChanged(){
			super.onContentsChanged();
			setChanged();
			assert level!=null;
			level.sendBlockUpdated(worldPosition,level.getBlockState(worldPosition),level.getBlockState(worldPosition),2);
		}
	};
	@Override
	public void load(@NotNull CompoundTag tag){
		super.load(tag);
		if(!this.tryLoadLootTable(tag)) this.stacks=NonNullList.withSize(this.getContainerSize(),ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag,this.stacks);
		this.progress=tag.getInt("progress");
		if(tag.get("fluidTank") instanceof CompoundTag) fluidTank.readFromNBT(tag);
	}
	@Override
	public void saveAdditional(@NotNull CompoundTag tag){
		super.saveAdditional(tag);
		if(!this.trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag,this.stacks);
		tag.putInt("progress",this.progress);
		tag.put("fluidTank",fluidTank.writeToNBT(new CompoundTag()));
	}
	@Override
	public boolean isEmpty(){
		for(ItemStack itemStack: this.stacks) if(!itemStack.isEmpty()) return false;
		return true;
	}
	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability,@Nullable Direction facing){
		if(!this.remove&&facing!=null&&capability==ForgeCapabilities.ITEM_HANDLER)
			return handlers[facing.ordinal()].cast();
		if(!this.remove&&capability==ForgeCapabilities.FLUID_HANDLER)
			return LazyOptional.of(()->fluidTank).cast();
		return super.getCapability(capability,facing);
	}
	@Override
	public void setRemoved(){
		super.setRemoved();
		for(LazyOptional<? extends IItemHandler> handler: handlers) handler.invalidate();
	}
	public static void serverTick(Level world,BlockPos pos,BlockState blockState,FryingTableBlockEntity blockEntity){
		if(blockEntity.fluidTank.isEmpty()) world.setBlock(pos,blockState.setValue(FryingTable.OIL,false),3);
		else world.setBlock(pos,blockState.setValue(FryingTable.OIL,true),3);
		// 2. Logika receptu
		// Najdeme recept pro item v prvním slotu
		var recipeOpt=world.getRecipeManager().getRecipeFor(FryingRecipe.Type.INSTANCE,blockEntity,world);
		if(recipeOpt.isPresent()){
			FryingRecipe recipe=recipeOpt.get();
			// Máme dost oleje a místo na výstup?
			if(blockEntity.fluidTank.getFluidAmount()>=recipe.getOilAmount()&&
					canInsertResult(blockEntity,recipe.getResultItem(world.registryAccess()))){
				blockEntity.progress++;
				if(blockEntity.progress>=recipe.getProcessingTime()){
					// DOKONČENO
					blockEntity.getItem(0).shrink(1); // Seber vstup
					blockEntity.fluidTank.drain(recipe.getOilAmount(),IFluidHandler.FluidAction.EXECUTE); // Seber olej
					ItemStack result=recipe.getResultItem(world.registryAccess()).copy();
					insertItem(blockEntity,result); // Přidej výstup do slotu 1
					blockEntity.progress=0;
				}
				blockEntity.setChanged();
			}else{
				blockEntity.progress=0;
			}
		}else{
			blockEntity.progress=0;
		}
	}
	public static void clientTick(Level world,BlockPos pos){
		RandomSource random00=world.getRandom();
		if(random00.nextInt(12)==0)
			world.addParticle(ParticleTypes.LAVA,pos.getX()+0.5D,pos.getY()+0.3D,pos.getZ()+0.5D,(random00.nextDouble()-0.5D)*0.05D,random00.nextDouble()*0.1D,(random00.nextDouble()-0.5D)*0.05D);
		RandomSource random01=world.getRandom();
		if(random01.nextInt(20)==0)
			world.playLocalSound((double)pos.getX()+0.5D,(double)pos.getY()+0.5D,(double)pos.getZ()+0.5D,SoundEvents.CAMPFIRE_CRACKLE,SoundSource.BLOCKS,0.5F+random01.nextFloat(),random01.nextFloat()*0.7F+0.6F,false);
	}
	private static boolean canInsertResult(FryingTableBlockEntity entity,ItemStack result){
		ItemStack currentOutput=entity.getItem(1);
		return currentOutput.isEmpty()||(currentOutput.is(result.getItem())&&currentOutput.getCount()+result.getCount()<=result.getMaxStackSize());
	}
	private static void insertItem(FryingTableBlockEntity entity,ItemStack result){
		ItemStack current=entity.getItem(1);
		if(current.isEmpty()) entity.setItem(1,result);
		else current.grow(result.getCount());
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(){
		CompoundTag tag=new CompoundTag();
		ContainerHelper.saveAllItems(tag,this.stacks);
		return tag;
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public void setItem(int slot,@NotNull ItemStack stack){
		this.unpackLootTable(null);
		this.stacks.set(slot,stack);
		if(stack.getCount()>this.getMaxStackSize()) stack.setCount(this.getMaxStackSize());
		if(this.level!=null){
			this.setChanged();
			if(!this.level.isClientSide)
				this.level.sendBlockUpdated(this.worldPosition,this.getBlockState(),this.getBlockState(),3);
		}
	}
}