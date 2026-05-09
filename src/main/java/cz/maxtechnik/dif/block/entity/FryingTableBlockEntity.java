package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.FryingTable;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.recipes.FryingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
public class FryingTableBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer{
	public static final int SLOTS=2;
	public static final int INPUT_SLOT=0;
	public static final int OUTPUT_SLOT=1;
	public int progress=0;
	public NonNullList<ItemStack> stacks=NonNullList.withSize(SLOTS,ItemStack.EMPTY);
	// -------------------------------------------------------------------------
	// Fluid tank
	// -------------------------------------------------------------------------
	public final FluidTank fluidTank=new FluidTank(1000,fs->fs.getFluid()==DifModFluids.SUNFLOWER_OIL.get()){
		@Override
		protected void onContentsChanged(){
			super.onContentsChanged();
			setChanged();
			if(level!=null){
				level.sendBlockUpdated(worldPosition,
						level.getBlockState(worldPosition),
						level.getBlockState(worldPosition),2);
			}
		}
	};
	// -------------------------------------------------------------------------
	// Item handler (SidedInvWrapper) – v NeoForge 1.21.1 se registruje přes
	// BlockEntityType.registerCapability() v hlavní mod třídě nebo event handleru,
	// NE přes getCapability override.
	// Viz: DifModBlockEntities nebo @Mod konstruktor:
	//   event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
	//       DifModBlockEntities.FRYING_TABLE.get(),
	//       (be, side) -> be.getItemHandler(side));
	// -------------------------------------------------------------------------
	private final IItemHandler[] itemHandlers=new IItemHandler[Direction.values().length];
	public FryingTableBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.FRYING_TABLE.get(),pos,blockState);
		for(Direction dir: Direction.values()){
			itemHandlers[dir.ordinal()]=new SidedInvWrapper(this,dir);
		}
	}
	/**
	 * Použij toto v capability registration callbacku.
	 * Příklad registrace v hlavní mod třídě (@Mod konstruktor nebo RegisterCapabilitiesEvent):
	 * <pre>
	 * event.registerBlockEntity(
	 *     Capabilities.ItemHandler.BLOCK,
	 *     DifModBlockEntities.FRYING_TABLE.get(),
	 *     (be, side) -> be.getItemHandler(side)
	 * );
	 * event.registerBlockEntity(
	 *     Capabilities.FluidHandler.BLOCK,
	 *     DifModBlockEntities.FRYING_TABLE.get(),
	 *     (be, side) -> be.fluidTank
	 * );
	 * </pre>
	 */
	@Nullable
	public IItemHandler getItemHandler(@Nullable Direction side){
		if(side==null) return itemHandlers[0];
		return itemHandlers[side.ordinal()];
	}
	// -------------------------------------------------------------------------
	// WorldlyContainer
	// -------------------------------------------------------------------------
	@Override
	public int @NotNull [] getSlotsForFace(@NotNull Direction side){
		return new int[]{INPUT_SLOT,OUTPUT_SLOT};
	}
	@Override
	public boolean canPlaceItemThroughFace(int index,@NotNull ItemStack itemStack,@Nullable Direction direction){
		return index==INPUT_SLOT;
	}
	@Override
	public boolean canTakeItemThroughFace(int index,@NotNull ItemStack itemStack,@NotNull Direction direction){
		return index==OUTPUT_SLOT;
	}
	// -------------------------------------------------------------------------
	// RandomizableContainerBlockEntity
	// -------------------------------------------------------------------------
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
	@Override
	public boolean isEmpty(){
		for(ItemStack stack: this.stacks) if(!stack.isEmpty()) return false;
		return true;
	}
	// -------------------------------------------------------------------------
	// NBT – v 1.21.1 loadAdditional přijímá HolderLookup.Provider
	// -------------------------------------------------------------------------
	@Override
	public void loadAdditional(@NotNull CompoundTag tag,HolderLookup.@NotNull Provider provider){
		super.loadAdditional(tag,provider);
		if(!this.tryLoadLootTable(tag))
			this.stacks=NonNullList.withSize(this.getContainerSize(),ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag,this.stacks,provider);
		this.progress=tag.getInt("progress");
		if(tag.get("fluidTank") instanceof CompoundTag fluidTag)
			fluidTank.readFromNBT(provider,fluidTag);
	}
	@Override
	public void saveAdditional(@NotNull CompoundTag tag,HolderLookup.@NotNull Provider provider){
		super.saveAdditional(tag,provider);
		if(!this.trySaveLootTable(tag))
			ContainerHelper.saveAllItems(tag,this.stacks,provider);
		tag.putInt("progress",this.progress);
		tag.put("fluidTank",fluidTank.writeToNBT(provider,new CompoundTag()));
	}
	// -------------------------------------------------------------------------
	// Sync packet
	// -------------------------------------------------------------------------
	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider){
		CompoundTag tag=new CompoundTag();
		ContainerHelper.saveAllItems(tag,this.stacks,provider);
		return tag;
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	// -------------------------------------------------------------------------
	// setItem – vyvolá sync na klientovi
	// -------------------------------------------------------------------------
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
	// -------------------------------------------------------------------------
	// Tick – server
	// -------------------------------------------------------------------------
	public static void serverTick(Level world,BlockPos pos,BlockState blockState,FryingTableBlockEntity be){
		// Aktualizuj stav bloku (má/nemá olej)
		if(be.fluidTank.isEmpty())
			world.setBlock(pos,blockState.setValue(FryingTable.OIL,false),3);
		else
			world.setBlock(pos,blockState.setValue(FryingTable.OIL,true),3);
		// Hledej recept – v 1.21.1 getRecipeFor vrací Optional<RecipeHolder<T>>
		SingleRecipeInput recipeInput=new SingleRecipeInput(be.getItem(INPUT_SLOT));
		Optional<RecipeHolder<FryingRecipe>> recipeOpt=
				world.getRecipeManager().getRecipeFor(FryingRecipe.Type.INSTANCE,recipeInput,world);
		if(recipeOpt.isPresent()){
			FryingRecipe recipe=recipeOpt.get().value();
			ItemStack result=recipe.getResultItem(world.registryAccess());
			if(be.fluidTank.getFluidAmount()>=recipe.getOilAmount()&&canInsertResult(be,result)){
				be.progress++;
				if(be.progress>=recipe.getProcessingTime()){
					be.getItem(INPUT_SLOT).shrink(1);
					be.fluidTank.drain(recipe.getOilAmount(),IFluidHandler.FluidAction.EXECUTE);
					insertItem(be,result.copy());
					be.progress=0;
				}
				be.setChanged();
			}else{
				be.progress=0;
			}
		}else{
			be.progress=0;
		}
	}
	// -------------------------------------------------------------------------
	// Tick – klient (částice + zvuky)
	// -------------------------------------------------------------------------
	public static void clientTick(Level world,BlockPos pos){
		RandomSource rng=world.getRandom();
		if(rng.nextInt(12)==0)
			world.addParticle(ParticleTypes.LAVA,
					pos.getX()+0.5,pos.getY()+0.3,pos.getZ()+0.5,
					(rng.nextDouble()-0.5)*0.05,rng.nextDouble()*0.1,(rng.nextDouble()-0.5)*0.05);
		if(rng.nextInt(20)==0)
			world.playLocalSound(pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,
					SoundEvents.CAMPFIRE_CRACKLE,SoundSource.BLOCKS,
					0.5F+rng.nextFloat(),rng.nextFloat()*0.7F+0.6F,false);
	}
	// -------------------------------------------------------------------------
	// Pomocné metody
	// -------------------------------------------------------------------------
	private static boolean canInsertResult(FryingTableBlockEntity be,ItemStack result){
		ItemStack current=be.getItem(OUTPUT_SLOT);
		return current.isEmpty()||
				(current.is(result.getItem())&&current.getCount()+result.getCount()<=result.getMaxStackSize());
	}
	private static void insertItem(FryingTableBlockEntity be,ItemStack result){
		ItemStack current=be.getItem(OUTPUT_SLOT);
		if(current.isEmpty()) be.setItem(OUTPUT_SLOT,result);
		else current.grow(result.getCount());
	}
}