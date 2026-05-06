package cz.maxtechnik.dif.block.entity.dev;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
public class XpStorageBlockEntity extends BlockEntity{
	public int xp;
	public XpStorageBlockEntity(BlockPos pos,BlockState state){
		super(DifModBlockEntities.XP_STORAGE.get(),pos,state);
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag compound, @NotNull HolderLookup.Provider provider){
		super.loadAdditional(compound, provider);
		this.xp=compound.getInt("xp");
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag compound, @NotNull HolderLookup.Provider provider){
		super.saveAdditional(compound, provider);
		compound.putInt("xp",this.xp);
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider){
		return this.saveWithFullMetadata(provider);
	}
}
}
