package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.BurningGenerator;
import cz.maxtechnik.dif.config.DifModServerConfig;
import cz.maxtechnik.dif.gui.menu.BurningGeneratorMenu;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.IntStream;
public class BurningGeneratorBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer{
	public static final int SLOTS=1;
	public static final int INPUT_SLOT=0;
	public static int getEnergyPerTick(){
		return DifModServerConfig.BURNING_GENERATOR_ENERGY_PER_TICK.get();
	}
	public static int getMaxEnergy(){
		return DifModServerConfig.BURNING_GENERATOR_MAX_ENERGY.get();
	}
	public static final int MAX_RECEIVE=Integer.MAX_VALUE;
	public static int getMaxExtract(){
		return DifModServerConfig.BURNING_GENERATOR_MAX_EXTRACT.get();
	}
	private final ItemStackHandler itemHandler=new ItemStackHandler(SLOTS){
		@Override
		protected void onContentsChanged(int slot){
			setChanged();
		}
	};
	public static class GeneratorEnergyStorage extends EnergyStorage{
		public GeneratorEnergyStorage(int capacity,int maxReceive,int maxExtract,int energy){
			super(capacity,maxReceive,maxExtract,energy);
		}
		public void generateEnergy(int amount){
			this.energy=Math.min(this.energy+amount,this.capacity);
		}
		public void setEnergy(int energy){
			this.energy=Math.clamp(energy,0,this.capacity);
		}
		@Override
		public int receiveEnergy(int maxReceive,boolean simulate){
			return 0;
		}
		@Override
		public boolean canReceive(){
			return false;
		}
	}
	private final GeneratorEnergyStorage energyStorage=new GeneratorEnergyStorage(getMaxEnergy(),MAX_RECEIVE,getMaxExtract(),0);
	public ItemStackHandler getItemHandler(){
		return itemHandler;
	}
	public ItemStackHandler getInventory(){
		return getItemHandler();
	}
	public GeneratorEnergyStorage getEnergyStorage(){
		return energyStorage;
	}
	private int burnTime;
	private int maxBurnTime;
	private int clientLit;
	private int clientEnergyLower;
	private int clientEnergyUpper;
	private int clientMaxEnergyLower;
	private int clientMaxEnergyUpper;
	private int clientFuel;
	private int clientEmpty;
	public final ContainerData dataAccess=new SimpleContainerData(9){
		@Override
		public int get(int index){
			if(level!=null&&level.isClientSide){
				return switch(index){
					case 0 -> BurningGeneratorBlockEntity.this.burnTime;
					case 1 -> BurningGeneratorBlockEntity.this.maxBurnTime;
					case 2 -> BurningGeneratorBlockEntity.this.clientLit;
					case 3 -> BurningGeneratorBlockEntity.this.clientEnergyLower;
					case 4 -> BurningGeneratorBlockEntity.this.clientEnergyUpper;
					case 5 -> BurningGeneratorBlockEntity.this.clientMaxEnergyLower;
					case 6 -> BurningGeneratorBlockEntity.this.clientMaxEnergyUpper;
					case 7 -> BurningGeneratorBlockEntity.this.clientFuel;
					case 8 -> BurningGeneratorBlockEntity.this.clientEmpty;
					default -> 0;
				};
			}
			int lit=getBlockState().getValue(BurningGenerator.LIT)?1:0;
			int empty=isEmpty()?1:0;
			int energy=BurningGeneratorBlockEntity.this.energyStorage.getEnergyStored();
			int maxEnergy=BurningGeneratorBlockEntity.this.energyStorage.getMaxEnergyStored();
			return switch(index){
				case 0 -> BurningGeneratorBlockEntity.this.burnTime;
				case 1 -> BurningGeneratorBlockEntity.this.maxBurnTime;
				case 2 -> lit;
				case 3 -> energy&0xFFFF;
				case 4 -> (energy >>> 16)&0xFFFF;
				case 5 -> maxEnergy&0xFFFF;
				case 6 -> (maxEnergy >>> 16)&0xFFFF;
				case 7 -> itemHandler.getStackInSlot(INPUT_SLOT).getBurnTime(null);
				case 8 -> empty;
				default -> 0;
			};
		}
		@Override
		public void set(int index,int value){
			switch(index){
				case 0 -> BurningGeneratorBlockEntity.this.burnTime=value;
				case 1 -> BurningGeneratorBlockEntity.this.maxBurnTime=value;
				case 2 -> BurningGeneratorBlockEntity.this.clientLit=value;
				case 3 -> {
					BurningGeneratorBlockEntity.this.clientEnergyLower=value&0xFFFF;
					int combined=(BurningGeneratorBlockEntity.this.clientEnergyUpper<<16)|BurningGeneratorBlockEntity.this.clientEnergyLower;
					BurningGeneratorBlockEntity.this.energyStorage.setEnergy(combined);
				}
				case 4 -> {
					BurningGeneratorBlockEntity.this.clientEnergyUpper=value&0xFFFF;
					int combined=(BurningGeneratorBlockEntity.this.clientEnergyUpper<<16)|BurningGeneratorBlockEntity.this.clientEnergyLower;
					BurningGeneratorBlockEntity.this.energyStorage.setEnergy(combined);
				}
				case 5 -> BurningGeneratorBlockEntity.this.clientMaxEnergyLower=value&0xFFFF;
				case 6 -> BurningGeneratorBlockEntity.this.clientMaxEnergyUpper=value&0xFFFF;
				case 7 -> BurningGeneratorBlockEntity.this.clientFuel=value;
				case 8 -> BurningGeneratorBlockEntity.this.clientEmpty=value;
			}
		}
		@Override
		public int getCount(){
			return 9;
		}
	};
	public BurningGeneratorBlockEntity(BlockPos position,BlockState blockState){
		super(DifModBlockEntities.BURNING_GENERATOR.get(),position,blockState);
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag compound,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(compound,provider);
		if(compound.contains("inventory"))
			itemHandler.deserializeNBT(provider,compound.getCompound("inventory"));
		if(compound.contains("energyStorage"))
			energyStorage.deserializeNBT(provider,Objects.requireNonNull(compound.get("energyStorage")));
		this.burnTime=compound.getInt("burnTime");
		this.maxBurnTime=compound.getInt("maxBurnTime");
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag compound,@NotNull HolderLookup.Provider provider){
		super.saveAdditional(compound,provider);
		compound.put("inventory",itemHandler.serializeNBT(provider));
		compound.put("energyStorage",energyStorage.serializeNBT(provider));
		compound.putInt("burnTime",this.burnTime);
		compound.putInt("maxBurnTime",this.maxBurnTime);
	}
	public static void clientTick(Level level,BlockPos pos,BlockState blockState){
		if(blockState.getValue(BurningGenerator.LIT)){
			switch(blockState.getValue(BurningGenerator.FACING)){
				case NORTH -> level.addParticle(ParticleTypes.SMOKE,pos.getX()+0.1,pos.getY()+0.999,pos.getZ()+0.5,0,0.007,0);
				case SOUTH -> level.addParticle(ParticleTypes.SMOKE,pos.getX()+0.9,pos.getY()+0.999,pos.getZ()+0.5,0,0.007,0);
				case EAST -> level.addParticle(ParticleTypes.SMOKE,pos.getX()+0.5,pos.getY()+0.999,pos.getZ()+0.1,0,0.007,0);
				case WEST -> level.addParticle(ParticleTypes.SMOKE,pos.getX()+0.5,pos.getY()+0.999,pos.getZ()+0.9,0,0.007,0);
				default -> {
				}
			}
		}
	}
	public static void serverTick(Level level,BlockPos pos,BlockState blockState,BurningGeneratorBlockEntity entity){
		boolean shouldBeLit=false;
		int energyPerTick=getEnergyPerTick();
		if(entity.energyStorage.getEnergyStored()+energyPerTick<=entity.energyStorage.getMaxEnergyStored()){
			if(entity.burnTime>0){
				shouldBeLit=true;
				entity.burnTime--;
				entity.energyStorage.generateEnergy(energyPerTick);
				entity.setChanged();
			}else{
				ItemStack fuelStack=entity.itemHandler.getStackInSlot(INPUT_SLOT);
				if(!fuelStack.isEmpty()){
					int burnDuration=fuelStack.getBurnTime(null);
					if(burnDuration>0){
						entity.burnTime=burnDuration;
						entity.maxBurnTime=burnDuration;
						if(fuelStack.getItem().equals(Items.LAVA_BUCKET))
							entity.itemHandler.setStackInSlot(INPUT_SLOT,new ItemStack(Items.BUCKET));
						else{
							ItemStack copy=fuelStack.copy();
							copy.shrink(1);
							entity.itemHandler.setStackInSlot(INPUT_SLOT,copy);
						}
						shouldBeLit=true;
						entity.setChanged();
					}
				}
			}
		}
		// Distribuce energie do sousedů
		if(entity.energyStorage.getEnergyStored()>0){
			for(Direction direction: Direction.values()){
				BlockPos neighborPos=pos.relative(direction);
				IEnergyStorage storage=level.getCapability(Capabilities.EnergyStorage.BLOCK,neighborPos,direction.getOpposite());
				if(storage!=null&&storage.canReceive()){
					int energyToTransfer=Math.min(entity.energyStorage.getEnergyStored(),getMaxExtract());
					if(energyToTransfer>0){
						int received=storage.receiveEnergy(energyToTransfer,false);
						if(received>0){
							entity.energyStorage.extractEnergy(received,false);
							entity.setChanged();
							if(entity.energyStorage.getEnergyStored()<=0) break;
						}
					}
				}
			}
		}
		if(blockState.getValue(BurningGenerator.LIT)!=shouldBeLit)
			level.setBlock(pos,blockState.setValue(BurningGenerator.LIT,shouldBeLit),3);
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
	public int getContainerSize(){
		return SLOTS;
	}
	@Override
	public boolean isEmpty(){
		return itemHandler.getStackInSlot(INPUT_SLOT).isEmpty();
	}
	@Override
	public @NotNull Component getDefaultName(){
		return Component.literal("generator");
	}
	@Override
	public @NotNull AbstractContainerMenu createMenu(int id,@NotNull Inventory inventory){
		FriendlyByteBuf buffer=new FriendlyByteBuf(Unpooled.buffer());
		buffer.writeBlockPos(this.worldPosition);
		return new BurningGeneratorMenu(id,inventory,buffer);
	}
	@Override
	public @NotNull Component getDisplayName(){
		return Component.literal("Generator");
	}
	@Override
	protected @NotNull NonNullList<ItemStack> getItems(){
		NonNullList<ItemStack> list=NonNullList.withSize(SLOTS,ItemStack.EMPTY);
		list.set(0,itemHandler.getStackInSlot(0));
		return list;
	}
	@Override
	protected void setItems(@NotNull NonNullList<ItemStack> itemStacks){
		for(int i=0;i<itemStacks.size()&&i<itemHandler.getSlots();i++)
			itemHandler.setStackInSlot(i,itemStacks.get(i));
	}
	@Override
	public boolean canPlaceItem(int index,@NotNull ItemStack itemStack){
		return true;
	}
	@Override
	public int @NotNull [] getSlotsForFace(@NotNull Direction side){
		return IntStream.range(0,SLOTS).toArray();
	}
	@Override
	public boolean canPlaceItemThroughFace(int index,@NotNull ItemStack itemStack,@Nullable Direction direction){
		return true;
	}
	@Override
	public boolean canTakeItemThroughFace(int index,@NotNull ItemStack itemStack,@NotNull Direction direction){
		return true;
	}
}