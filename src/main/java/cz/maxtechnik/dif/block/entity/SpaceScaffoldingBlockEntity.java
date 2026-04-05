package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
public class SpaceScaffoldingBlockEntity extends BlockEntity{
	public int lifeTime;
	public SpaceScaffoldingBlockEntity(BlockPos pos,BlockState state){
		super(DifModBlockEntities.SPACE_SCAFFOLDING.get(),pos,state);
	}
	@Override
	public void load(@NotNull CompoundTag compound){
		super.load(compound);
		this.lifeTime=compound.getInt("liveTime");
	}
	@Override
	public void saveAdditional(@NotNull CompoundTag compound){
		super.saveAdditional(compound);
		compound.putInt("liveTime",this.lifeTime);
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(){
		return this.saveWithFullMetadata();
	}
}
