package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
public class SpaceScaffoldingBlockEntity extends BlockEntity{
	public int lifeTime;
	public SpaceScaffoldingBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.SPACE_SCAFFOLDING.get(),pos,blockState);
	}
	@Override
	public void loadAdditional(@NotNull CompoundTag compound,HolderLookup.@NotNull Provider registries){
		super.loadAdditional(compound,registries);
		this.lifeTime=compound.getInt("liveTime");
	}
	@Override
	public void saveAdditional(@NotNull CompoundTag compound,HolderLookup.@NotNull Provider registries){
		super.saveAdditional(compound,registries);
		compound.putInt("liveTime",this.lifeTime);
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries){
		return this.saveWithFullMetadata(registries);
	}
}