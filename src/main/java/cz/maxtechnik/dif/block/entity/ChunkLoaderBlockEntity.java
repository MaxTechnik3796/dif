package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.events.ChunkLoaderData;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
public class ChunkLoaderBlockEntity extends BlockEntity{
	private UUID ownerUUID;
	private String ownerName="Unknown";
	public ChunkLoaderBlockEntity(BlockPos pos,BlockState state){
		super(DifModBlockEntities.CHUNK_LOADER_BE.get(),pos,state);
	}
	public void setOwner(UUID uuid,String name){
		this.ownerUUID=uuid;
		this.ownerName=name;
		this.setChanged();
	}
	public void updateStatus(boolean active){
		if(level instanceof ServerLevel serverLevel){
			// Důležité: Kontrola proti tvým registrovaným blokům
			boolean is3x3=getBlockState().is(DifModBlocks.CHUNK_LOADER_3X3.get());
			ChunkPos center=new ChunkPos(worldPosition);
			int radius=is3x3?1:0;
			for(int x=-radius;x<=radius;x++){
				for(int z=-radius;z<=radius;z++){
					serverLevel.setChunkForced(center.x+x,center.z+z,active);
				}
			}
			ChunkLoaderData.get(serverLevel).updateRecord(worldPosition,ownerUUID,ownerName,active,is3x3);
		}
	}
	public void handleRemoval(){
		if(level instanceof ServerLevel serverLevel){
			updateStatus(false);
			ChunkLoaderData data=ChunkLoaderData.get(serverLevel);
			data.loaders.removeIf(r->r.pos().equals(this.worldPosition));
			data.setDirty();
		}
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag tag, @NotNull net.minecraft.core.HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		if (tag.hasUUID("ownerUUID")) {
			this.ownerUUID = tag.getUUID("ownerUUID");
		}
		if (tag.contains("ownerName")) {
			this.ownerName = tag.getString("ownerName");
		}
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag, @NotNull net.minecraft.core.HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (this.ownerUUID != null) {
			tag.putUUID("ownerUUID", this.ownerUUID);
		}
		if (this.ownerName != null) {
			tag.putString("ownerName", this.ownerName);
		}
	}
}