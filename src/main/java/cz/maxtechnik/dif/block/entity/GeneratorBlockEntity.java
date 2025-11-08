package cz.maxtechnik.dif.block.entity;


import cz.maxtechnik.dif.gui.menu.GeneratorMenu;
import cz.maxtechnik.dif.init.misc.DifModBlockEntities;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import javax.annotation.Nullable;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;


public class GeneratorBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer{
	public static final int SLOTS=1;
	public static final int INPUT_SLOT=0;

	public static final int ENERGY_PER_TICK=20;
	public static final int MAX_ENERGY=10000;
	public static final int MAX_RECEIVE=200;
	public static final int MAX_EXTRACT=200;

	private NonNullList<ItemStack>stacks=NonNullList.withSize(SLOTS,ItemStack.EMPTY);
	private final LazyOptional<?extends IItemHandler>[]handlers=SidedInvWrapper.create(this,Direction.values());
	private int burnTime;
	private int maxBurnTime;
	public final ContainerData dataAccess=new SimpleContainerData(3){
		@Override
		public int get(int index){
			return switch (index){
				case 0->GeneratorBlockEntity.this.burnTime;
				case 1->GeneratorBlockEntity.this.maxBurnTime;
				case 2->GeneratorBlockEntity.this.energyStorage.getEnergyStored();
				case 3->GeneratorBlockEntity.this.energyStorage.getMaxEnergyStored();
				default->0;
			};
		}
		@Override
		public void set(int index,int value){
			switch (index){
				case 0->GeneratorBlockEntity.this.burnTime=value;
				case 1->GeneratorBlockEntity.this.maxBurnTime=value;
			}
		}
		@Override
		public int getCount(){
			return 4;
		}
	};
	private final EnergyStorage energyStorage=new EnergyStorage(MAX_ENERGY,MAX_RECEIVE,MAX_EXTRACT,0){
		@Override
		public int receiveEnergy(int maxReceive,boolean simulate){
			int retval=super.receiveEnergy(maxReceive,simulate);
			if(!simulate){
				setChanged();
				assert level!=null;
				level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition),level.getBlockState(worldPosition),3);
			}
			return retval;
		}
		@Override
		public int extractEnergy(int maxExtract,boolean simulate){
			int retval=super.extractEnergy(maxExtract, simulate);
			if(!simulate){
				setChanged();
				assert level!=null;
				level.sendBlockUpdated(worldPosition,level.getBlockState(worldPosition),level.getBlockState(worldPosition),3);
			}
			return retval;
		}
	};
	public GeneratorBlockEntity(BlockPos position,BlockState state){
		super(DifModBlockEntities.GENERATOR_BE.get(),position,state);
	}
	@Override
	public void load(@NotNull CompoundTag compound){
		super.load(compound);
		if(!this.tryLoadLootTable(compound))
			this.stacks=NonNullList.withSize(this.getContainerSize(),ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound,this.stacks);
		if(compound.contains("energyStorage")&&compound.get("energyStorage")instanceof IntTag intTag)
			energyStorage.deserializeNBT(intTag);
		this.burnTime=compound.getInt("burnTime");
		this.maxBurnTime=compound.getInt("maxBurnTime");
	}
	@Override
	public void saveAdditional(@NotNull CompoundTag compound){
		super.saveAdditional(compound);
		if(!this.trySaveLootTable(compound))
			ContainerHelper.saveAllItems(compound,this.stacks);
		compound.put("energyStorage",energyStorage.serializeNBT());
		compound.putInt("burtTime",this.burnTime);
		compound.putInt("maxBurnTime",this.maxBurnTime);
	}
	public static void tick(Level level,BlockPos pos,BlockState state,GeneratorBlockEntity entity){
		boolean changed=false;
		if (level.isClientSide){
			return;
		}
		if(entity.energyStorage.getEnergyStored()+ENERGY_PER_TICK<=entity.energyStorage.getMaxEnergyStored()){
			if(entity.burnTime>0){
				changed=true;
				entity.burnTime--;
				entity.energyStorage.receiveEnergy(ENERGY_PER_TICK,false);
			}else{
				ItemStack fuelStack=entity.getItem(INPUT_SLOT);
				if(!fuelStack.isEmpty()){
					int burnDuration=ForgeHooks.getBurnTime(fuelStack,null);
					if(burnDuration>0){
						entity.burnTime=burnDuration;
						entity.maxBurnTime=burnDuration;
						fuelStack.shrink(1);
						entity.setItem(INPUT_SLOT,fuelStack);
						changed=true;
					}
				}
			}
		}
		if(entity.energyStorage.getEnergyStored()>0){
			for(Direction direction:Direction.values()){
				BlockEntity neighbor=level.getBlockEntity(pos.relative(direction));
				if(neighbor!=null){
					LazyOptional<IEnergyStorage>neighborEnergy=neighbor.getCapability(ForgeCapabilities.ENERGY,direction.getOpposite());
					neighborEnergy.ifPresent(storage->{
						int energyToTransfer=Math.min(entity.energyStorage.getEnergyStored(),MAX_EXTRACT);
						if(energyToTransfer>0&&storage.canReceive()){
							int received=storage.receiveEnergy(energyToTransfer,false);
							if(received>0){
								entity.energyStorage.extractEnergy(received,false);
							}
						}
					});
				}
			}
		}
		if(changed)
			setChanged(level,pos,state);
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(){
		return this.saveWithFullMetadata();
	}
	@Override
	public int getContainerSize(){
		return stacks.size();
	}
	@Override
	public boolean isEmpty(){
		for(ItemStack itemstack:this.stacks)
			if(!itemstack.isEmpty())
				return false;
		return true;
	}
	@Override
	public @NotNull Component getDefaultName(){
		return Component.literal("generator");
	}
	@Override
	public @NotNull AbstractContainerMenu createMenu(int id,@NotNull Inventory inventory){
		return new GeneratorMenu(id,inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
	}
	@Override
	public @NotNull Component getDisplayName(){
		return Component.literal("Generator");
	}
	@Override
	protected @NotNull NonNullList<ItemStack>getItems(){
		return this.stacks;
	}
	@Override
	protected void setItems(@NotNull NonNullList<ItemStack> stacks){
		this.stacks=stacks;
	}
	@Override
	public boolean canPlaceItem(int index,@NotNull ItemStack stack){
		return true;
	}
	@Override
	public int @NotNull[]getSlotsForFace(@NotNull Direction side){
		return new int[]{INPUT_SLOT,this.getContainerSize()};
	}
	@Override
	public boolean canPlaceItemThroughFace(int index,@NotNull ItemStack stack,@Nullable Direction direction){
		return this.canPlaceItem(index,stack);
	}
	@Override
	public boolean canTakeItemThroughFace(int index,@NotNull ItemStack stack,@NotNull Direction direction){
		return true;
	}
	@Override
	public <T> @NotNull LazyOptional<T>getCapability(@NotNull Capability<T>capability,@Nullable Direction facing){
		if (!this.remove && facing!=null && capability==ForgeCapabilities.ITEM_HANDLER)
			return handlers[facing.ordinal()].cast();
		if(!this.remove&&capability==ForgeCapabilities.ENERGY)
			return LazyOptional.of(()->new IEnergyStorage(){
				@Override
				public int receiveEnergy(int maxReceive,boolean simulate){
					return 0;
				}
				@Override
				public int extractEnergy(int maxExtract,boolean simulate){
					return energyStorage.extractEnergy(maxExtract,simulate);
				}
				@Override
				public int getEnergyStored(){
					return energyStorage.getEnergyStored();
				}
				@Override
				public int getMaxEnergyStored(){
					return energyStorage.getMaxEnergyStored();
				}
				@Override
				public boolean canExtract(){
					return true;
				}
				@Override
				public boolean canReceive(){
					return false;
				}
			}).cast();
		return super.getCapability(capability,facing);
	}
	@Override
	public void setRemoved(){
		super.setRemoved();
		for (LazyOptional<?extends IItemHandler>handler:handlers)
			handler.invalidate();
	}
}