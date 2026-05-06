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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;

import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;
public class BurningGeneratorBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer{
	public static final int SLOTS=1;
	public static final int INPUT_SLOT=0;
	public static final int ENERGY_PER_TICK=DifModCommonConfig.burningGeneratorEnergyPerTick;
	public static final int MAX_ENERGY=DifModCommonConfig.burningGeneratorMaxEnergy;
	public static final int MAX_RECEIVE=Integer.MAX_VALUE;
	public static final int MAX_EXTRACT=DifModCommonConfig.burningGeneratorMaxExtract;
	private NonNullList<ItemStack> stacks=NonNullList.withSize(SLOTS,ItemStack.EMPTY);
	private int burnTime;
	private int maxBurnTime;
	public final ContainerData dataAccess=new SimpleContainerData(7){
		@Override
		public int get(int index){
			int lit=0;
			if(getBlockState().getValue(cz.maxtechnik.dif.block.BurningGenerator.LIT))
				lit=1;
			int empty=0;
			if(isEmpty()){
				empty=1;
			}
			return switch(index){
				case 0 -> BurningGeneratorBlockEntity.this.burnTime;
				case 1 -> BurningGeneratorBlockEntity.this.maxBurnTime;
				case 2 -> lit;
				case 3 -> BurningGeneratorBlockEntity.this.energyStorage.getEnergyStored();
				case 4 -> BurningGeneratorBlockEntity.this.energyStorage.getMaxEnergyStored();
				case 5 -> getItem(INPUT_SLOT).getBurnTime(null);
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
				level.sendBlockUpdated(worldPosition,level.getBlockState(worldPosition),level.getBlockState(worldPosition),3);
			}
			return retval;
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
	public BurningGeneratorBlockEntity(BlockPos position,BlockState state){
		super(DifModBlockEntities.BURNING_GENERATOR.get(),position,state);
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag compound, @NotNull HolderLookup.Provider provider){
		super.loadAdditional(compound, provider);
		if(!this.tryLoadLootTable(compound))
			this.stacks=NonNullList.withSize(this.getContainerSize(),ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound,this.stacks, provider);
		if(compound.contains("energyStorage"))
			energyStorage.deserializeNBT(provider, compound.get("energyStorage"));
		this.burnTime=compound.getInt("burnTime");
		this.maxBurnTime=compound.getInt("maxBurnTime");
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag compound, @NotNull HolderLookup.Provider provider){
		super.saveAdditional(compound, provider);
		ContainerHelper.saveAllItems(compound,this.stacks, provider);
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
				offsets=new double[][]{
						{0.42,0.15},
						{0.58,0.15},
						{0.42,0.85},
						{0.58,0.85}};
			}else if(axis.equals(Direction.Axis.Z)){
				offsets=new double[][]{
						{0.15,0.42},
						{0.15,0.58},
						{0.85,0.42},
						{0.85,0.58}};
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
				entity.energyStorage.receiveEnergy(ENERGY_PER_TICK,false);
			}else{
				ItemStack fuelStack=entity.getItem(INPUT_SLOT);
				if(!fuelStack.isEmpty()){
					int burnDuration=fuelStack.getBurnTime(null);
					if(burnDuration>0){
						entity.burnTime=burnDuration;
						entity.maxBurnTime=burnDuration;
						if(fuelStack.getItem().equals(Items.LAVA_BUCKET)){
							fuelStack=new ItemStack(Items.BUCKET);
						}else{
							fuelStack.shrink(1);
						}
						entity.setItem(INPUT_SLOT,fuelStack);
						shouldBeLit=true;
					}
				}
			}
		}
		if(entity.energyStorage.getEnergyStored()>0){
			for(Direction direction: Direction.values()){
				BlockPos neighborPos = pos.relative(direction);
				IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, direction.getOpposite());
				if(storage != null){
					int energyToTransfer=Math.min(entity.energyStorage.getEnergyStored(),MAX_EXTRACT);
					if(energyToTransfer>0&&storage.canReceive()){
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
		return stacks.size();
	}
	@Override
	public boolean isEmpty(){
		for(ItemStack itemstack: this.stacks)
			if(!itemstack.isEmpty())
				return false;
		return true;
	}
	@Override
	public @NotNull Component getDefaultName(){
		return Component.literal("generator");
	}
	@Override
	public @NotNull AbstractContainerMenu createMenu(int id,@NotNull Inventory inventory,@NotNull Player player){
		return new BurningGeneratorMenu(id,inventory,new RegistryFriendlyByteBuf(Unpooled.buffer(), this.level.registryAccess()).writeBlockPos(this.worldPosition));
	}
	@Override
	public @NotNull Component getDisplayName(){
		return Component.literal("Generator");
	}
	@Override
	protected @NotNull NonNullList<ItemStack> getItems(){
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
	public int @NotNull [] getSlotsForFace(@NotNull Direction side){
		return IntStream.range(0,this.getContainerSize()).toArray();
	}
	@Override
	public boolean canPlaceItemThroughFace(int index,@NotNull ItemStack stack,@Nullable Direction direction){
		return this.canPlaceItem(index,stack);
	}
	@Override
	public boolean canTakeItemThroughFace(int index,@NotNull ItemStack stack,@NotNull Direction direction){
		return true;
	}
}