package cz.maxtechnik.dif.block.entity.dev;

import cz.maxtechnik.dif.block.entity.DifModBlockEntities;
import net.minecraft.core.BlockPos;
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
	public void load(@NotNull CompoundTag compound){
		super.load(compound);
		this.xp=compound.getInt("xp");
	}
	@Override
	public void saveAdditional(@NotNull CompoundTag compound){
		super.saveAdditional(compound);
		compound.putInt("xp",this.xp);
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public @NotNull CompoundTag getUpdateTag() {
		return this.saveWithFullMetadata();
	}

}
