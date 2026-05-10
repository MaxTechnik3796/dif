package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
public class DistillationTankBlockEntity extends BlockEntity{
	public static final int CAPACITY=8000;
	public DistillationTankBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.DISTILLATION_TANK.get(),pos,blockState);
	}
	public final FluidTank tank=new FluidTank(CAPACITY){
		@Override
		protected void onContentsChanged(){
			setChanged();
			if(level!=null&&!level.isClientSide)
				level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
		}
	};
	public IFluidHandler getFluidHandler(){
		return tank;
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.saveAdditional(tag,provider);
		tag.put("tank",tank.writeToNBT(provider,new CompoundTag()));
	}
	@Override
	public void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(tag,provider);
		if(tag.contains("tank")) tank.readFromNBT(provider,tag.getCompound("tank"));
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider){
		return saveWithFullMetadata(provider);
	}
}