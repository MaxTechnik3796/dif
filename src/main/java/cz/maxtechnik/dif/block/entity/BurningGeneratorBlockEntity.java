package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.DifModCommonConfig;
import cz.maxtechnik.dif.gui.menu.BurningGeneratorMenu;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;

import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.IntStream;
public class BurningGeneratorBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer{
	public static final int SLOTS=1;
	public static final int INPUT_SLOT=0;
	public static final int ENERGY_PER_TICK=DifModCommonConfig.BURNING_GENERATOR_ENERGY_PER_TICK.get();
	public static final int MAX_ENERGY=DifModCommonConfig.BURNING_GENERATOR_MAX_ENERGY.get();
	public static final int MAX_RECEIVE=Integer.MAX_VALUE;
	public static final int MAX_EXTRACT=DifModCommonConfig.BURNING_GENERATOR_MAX_EXTRACT.get();
	private final ItemStackHandler itemHandler=new ItemStackHandler(SLOTS){
		@Override
		protected void onContentsChanged(int slot){
			setChanged();
		}
	};
	// EnergyStorage přijímá i vydává – interně nabíjíme přes receiveEnergy
	private final EnergyStorage energyStorage=new EnergyStorage(MAX_ENERGY,MAX_RECEIVE,MAX_EXTRACT,0){
		@Override
		public int receiveEnergy(int maxReceive,boolean simulate){
			return 0;
		}
		@Override
		public boolean canReceive(){
			return false;
		}
		@Override
		public int extractEnergy(int maxExtract,boolean simulate){
			int retval=super.extractEnergy(maxExtract,simulate);
			if(!simulate){
				setChanged();
				assert level!=null;
				level.sendBlockUpdated(worldPosition,level.getBlockState(worldPosition),level.getBlockState(worldPosition),3);
			}
			return retval;
		}
	};
	public ItemStackHandler getItemHandler(){
		return itemHandler;
	}
	public EnergyStorage getEnergyStorage(){
		return energyStorage;
	}
	private int burnTime;
	private int maxBurnTime;
	public final ContainerData dataAccess=new SimpleContainerData(7){
		@Override
		public int get(int index){
			int lit=0;
			if(getBlockState().getValue(cz.maxtechnik.dif.block.BurningGenerator.LIT)) lit=1;
			int empty=isEmpty()?1:0;
			return switch(index){
				case 0 -> BurningGeneratorBlockEntity.this.burnTime;
				case 1 -> BurningGeneratorBlockEntity.this.maxBurnTime;
				case 2 -> lit;
				case 3 -> BurningGeneratorBlockEntity.this.energyStorage.getEnergyStored();
				case 4 -> BurningGeneratorBlockEntity.this.energyStorage.getMaxEnergyStored();
				case 5 -> itemHandler.getStackInSlot(INPUT_SLOT).getBurnTime(null);
				case 6 -> empty;
				default -> 0;
			};
		}
		@Override
		public void set(int index,int value){
			switch(index){
				case 0 -> BurningGeneratorBlockEntity.this.burnTime=value;
				case 1 -> BurningGeneratorBlockEntity.this.maxBurnTime=value;
			}
		}
		@Override
		public int getCount(){
			return 7;
		}
	};
	public BurningGeneratorBlockEntity(BlockPos position,BlockState state){
		super(DifModBlockEntities.BURNING_GENERATOR.get(),position,state);
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
	public static void clientTick(Level level,BlockPos pos,BlockState state){
		if(state.getValue(cz.maxtechnik.dif.block.BurningGenerator.LIT)){
			final double Y_OFFSET=0.28;
			final double Y_VELOCITY=0.007;
			double[][] offsets;
			Direction.Axis axis=state.getValue(cz.maxtechnik.dif.block.BurningGenerator.FACING).getAxis();
			if(axis.equals(Direction.Axis.X)){
				offsets=new double[][]{{0.42,0.15},{0.58,0.15},{0.42,0.85},{0.58,0.85}};
			}else if(axis.equals(Direction.Axis.Z)){
				offsets=new double[][]{{0.15,0.42},{0.15,0.58},{0.85,0.42},{0.85,0.58}};
			}else{
				return;
			}
			for(double[] offset: offsets){
				if(DifMod.rouletteBoolean(8)){
					level.addParticle(ParticleTypes.SMOKE,pos.getX()+offset[0],pos.getY()+Y_OFFSET,pos.getZ()+offset[1],0,Y_VELOCITY,0);
				}
			}
		}
	}
	public static void serverTick(Level level,BlockPos pos,BlockState state,BurningGeneratorBlockEntity entity){
		boolean shouldBeLit=false;
		if(entity.energyStorage.getEnergyStored()+ENERGY_PER_TICK<=entity.energyStorage.getMaxEnergyStored()){
			if(entity.burnTime>0){
				shouldBeLit=true;
				entity.burnTime--;
				// Přidej energii přímo přes receiveEnergy – funguje interně i když canReceive() vrací false navenek
				try{
					java.lang.reflect.Field f=EnergyStorage.class.getDeclaredField("energy");
					f.setAccessible(true);
					int current=(int)f.get(entity.energyStorage);
					int max=entity.energyStorage.getMaxEnergyStored();
					f.set(entity.energyStorage,Math.min(current+ENERGY_PER_TICK,max));
				}catch(Exception ignored){
				}
				entity.setChanged();
				level.sendBlockUpdated(pos,state,state,3);
				entity.setChanged();
			}else{
				ItemStack fuelStack=entity.itemHandler.getStackInSlot(INPUT_SLOT);
				if(!fuelStack.isEmpty()){
					int burnDuration=fuelStack.getBurnTime(null);
					if(burnDuration>0){
						entity.burnTime=burnDuration;
						entity.maxBurnTime=burnDuration;
						if(fuelStack.getItem().equals(Items.LAVA_BUCKET)){
							entity.itemHandler.setStackInSlot(INPUT_SLOT,new ItemStack(Items.BUCKET));
						}else{
							ItemStack copy=fuelStack.copy();
							copy.shrink(1);
							entity.itemHandler.setStackInSlot(INPUT_SLOT,copy);
						}
						shouldBeLit=true;
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
					int energyToTransfer=Math.min(entity.energyStorage.getEnergyStored(),MAX_EXTRACT);
					if(energyToTransfer>0){
						int received=storage.receiveEnergy(energyToTransfer,false);
						if(received>0){
							entity.energyStorage.extractEnergy(received,false);
						}
					}
				}
			}
		}
		if(state.getValue(cz.maxtechnik.dif.block.BurningGenerator.LIT)!=shouldBeLit)
			level.setBlock(pos,state.setValue(cz.maxtechnik.dif.block.BurningGenerator.LIT,shouldBeLit),3);
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
	protected void setItems(@NotNull NonNullList<ItemStack> stacks){
		for(int i=0;i<stacks.size()&&i<itemHandler.getSlots();i++)
			itemHandler.setStackInSlot(i,stacks.get(i));
	}
	@Override
	public boolean canPlaceItem(int index,@NotNull ItemStack stack){
		return true;
	}
	@Override
	public int @NotNull [] getSlotsForFace(@NotNull Direction side){
		return IntStream.range(0,SLOTS).toArray();
	}
	@Override
	public boolean canPlaceItemThroughFace(int index,@NotNull ItemStack stack,@Nullable Direction direction){
		return true;
	}
	@Override
	public boolean canTakeItemThroughFace(int index,@NotNull ItemStack stack,@NotNull Direction direction){
		return true;
	}
}